package tk.codme.chat24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mUsersDatabase, Users.class)
                .build();


        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull Users users) {

                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getImage());
                //usersViewHolder.setUserOnline(users.getOnline());

                final String user_id=getRef(i).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setName(String name){
            TextView userNameView =(TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setStatus(String status){
            TextView userStatusView =(TextView)mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }
        public void setUserImage(String thumb_image){
            CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.user_single_img);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_img).into(userImageView);
        }
        public void setUserOnline(String online_status){
            ImageView userOnline=(ImageView)mView.findViewById(R.id.user_single_online_icon);
            if(online_status.equals("online")){
                userOnline.setVisibility(View.VISIBLE);
            }
            else userOnline.setVisibility(View.INVISIBLE);
        }
    }
}

