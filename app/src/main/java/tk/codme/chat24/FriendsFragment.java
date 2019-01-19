package tk.codme.chat24;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView)mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("users");
        mFriendsDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendsDatabase,Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {
                friendsViewHolder.setDate(friends.getDate());
                final String list_users_id=getRef(i).getKey();
                mUsersDatabase.child(list_users_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName=dataSnapshot.child("name").getValue().toString();
                        final String thumbImage=dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setOnline(userOnline);
                        }
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setThumb_image(thumbImage);

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                              final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //click event for each item
                                        if (which == 0) {
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_users_id);
                                            startActivity(profileIntent);
                                        }
                                        if (which == 1) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_users_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Crashlytics.log(Log.DEBUG, "tag", databaseError.toString());
                    }
                });
            }

        };
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
        friendsRecyclerViewAdapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(View itemView){
            super(itemView);
            mView=itemView;
        }

        public void setDate(String date){
            TextView userStatusView=(TextView)mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);
        }
        public void setName(String name){
            TextView userNameView=(TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setThumb_image(final String thumb_image){
            final CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.user_single_img);
            if (!thumb_image.equals("default")) {
                //Picasso.get().load(image).placeholder(R.drawable.default_img).into(mDisplayImage);
                Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_img).into(userImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(thumb_image).placeholder(R.drawable.default_img).into(userImageView);
                    }
                });
            }
        }
        public void setOnline(String online_status){
            ImageView userOnline=(ImageView)mView.findViewById(R.id.user_single_online_icon);
            if(online_status.equals("online")){
                userOnline.setVisibility(View.VISIBLE);
            }
            else userOnline.setVisibility(View.INVISIBLE);
        }
    }
}
