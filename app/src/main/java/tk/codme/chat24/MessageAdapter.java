package tk.codme.chat24;

import android.app.Notification;
import android.graphics.Color;
import android.util.Log;
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
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessagesList.get(position);
        String from_user = c.getFrom();
        String message_type = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                holder.displayName.setText(name);
                Picasso.get().load(image)
                        .placeholder(R.drawable.default_img).into(holder.profileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log(Log.DEBUG, "tag", databaseError.toString());
            }
        });
        if (message_type.equals("text")) {
            holder.messageText.setText(c.getMessage());
            holder.messageView.setVisibility(View.INVISIBLE);
        } else {
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage())
                    .placeholder(R.drawable.default_img).into(holder.messageView);
        }


        if(from_user.equals(current_user_id)){
            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);
        }
        else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
        }
        holder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText,timeText;
        public CircleImageView profileImage;
        public ImageView messageView;
        public TextView displayName;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=(TextView)itemView.findViewById(R.id.message_text_layout);
            timeText=(TextView)itemView.findViewById(R.id.time_text_layout);
            profileImage=(CircleImageView)itemView.findViewById(R.id.message_profile_layout);
            messageView=(ImageView)itemView.findViewById(R.id.message_image_layout);
            displayName = (TextView)itemView.findViewById(R.id.name_text_layout);
        }

    }
}
