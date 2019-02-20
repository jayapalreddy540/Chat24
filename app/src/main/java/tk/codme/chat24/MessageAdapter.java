package tk.codme.chat24;

import android.app.Notification;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String current_user_id,from_user,message_type,name,image;

    public MessageAdapter(List<Messages> mMessagesList){
        this.mMessagesList=mMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
         current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessagesList.get(position);
         from_user = c.getFrom();
         message_type = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(from_user);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 name = dataSnapshot.child("name").getValue().toString();
                 image = dataSnapshot.child("thumb_image").getValue().toString();
               // holder.displayName.setText(name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log(Log.DEBUG, "tag", databaseError.toString());
            }
        });


        if(from_user.equals(current_user_id)){
            Picasso.get().load(image)
                    .placeholder(R.drawable.default_img).into(holder.profileImageRight);
        }
        else{
            Picasso.get().load(image)
                    .placeholder(R.drawable.default_img).into(holder.profileImage);
        }

        if (message_type.equals("text")) {

            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(c.getMessage());
            holder.messageView.setVisibility(View.GONE);

            if(from_user.equals(current_user_id)){
               // holder.messageText.setBackgroundColor(Color.WHITE);
                holder.messageText.setTextColor(Color.BLACK);
                holder.messageText.setGravity(Gravity.END);
                holder.profileImage.setVisibility(View.GONE);
            }
            else{
               // holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                holder.messageText.setTextColor(Color.BLUE);
                holder.messageText.setGravity(Gravity.START);
                holder.profileImageRight.setVisibility(View.GONE);
            }
        }
        else {
            holder.messageView.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);

            if(from_user.equals(current_user_id)) {
                Picasso.get().load(c.getMessage())
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.default_img).into(holder.messageView);
                holder.profileImage.setVisibility(View.GONE);
            }
            else{

                Picasso.get().load(c.getMessage())
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.default_img).into(holder.messageView);
                holder.profileImageRight.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText,timeText;
        public CircleImageView profileImage,profileImageRight;
        public ImageView messageView;
        public TextView displayName;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=(TextView)itemView.findViewById(R.id.message_text_layout);
           // timeText=(TextView)itemView.findViewById(R.id.time_text_layout);
            profileImage=(CircleImageView)itemView.findViewById(R.id.message_profile_layout);
            messageView=(ImageView)itemView.findViewById(R.id.message_image_layout);
            profileImageRight=(CircleImageView)itemView.findViewById(R.id.message_profile_layout_right);
           // displayName = (TextView)itemView.findViewById(R.id.name_text_layout);
        }

    }
}
