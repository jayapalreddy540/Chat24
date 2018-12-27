package tk.codme.chat24;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendscount;
    private Button mProfileSendReqBtn;
    private Button mDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mFriendReqDatabase;
    private FirebaseUser mCurrent_user;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user=FirebaseAuth.getInstance().getCurrentUser();
        mRootRef=FirebaseDatabase.getInstance().getReference();

        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_displayName);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);
        mProfileFriendscount=(TextView)findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn=(Button)findViewById(R.id.profile_send_req_btn);
        mDeclineBtn=(Button)findViewById(R.id.profile_fndreqdecline_btn);

        mCurrent_state="not_friends";
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_img).into(mProfileImage);

                // friend list/request feature
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mProgressDialog.dismiss();
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("recieved")){

                                mCurrent_state="req_recieved";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                            }
                            else if(req_type.equals("sent")){
                                mCurrent_state="req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }
                        else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state="friends";
                                        mProfileSendReqBtn.setText("Unfriend");
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);

                // not friends state
                if(mCurrent_state.equals("not_friends"))
                {
                    DatabaseReference newNotificationref=mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId=newNotificationref.getKey();
                    HashMap<String,String> notificationData=new HashMap<>();
                    notificationData.put("from",mCurrent_user.getUid());
                    notificationData.put("type","request");
                    Map requestMap=new HashMap();
                    requestMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+user_id+"/request_type","sent");
                    requestMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid()+"/request_type","recieved");
                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);


                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError!=null){
                            Toast.makeText(ProfileActivity.this,"Error in Sending Request",Toast.LENGTH_SHORT).show();
                        }
                           mProfileSendReqBtn.setEnabled(true);

                        mCurrent_state="req_sent";
                        mProfileSendReqBtn.setText("Cancel Friend Request");
                        }
                    });
                }

                // cancel request state
                if(mCurrent_state.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                //  req recieved state
                if(mCurrent_state.equals("req_recieved")){
                    final String currentDate=DateFormat.getDateInstance().format(new Date());

                    Map friendsMap=new HashMap();
                    friendsMap.put("Friends/"+mCurrent_user.getUid()+"/"+user_id+"/date",currentDate);
                    friendsMap.put("Friends/"+user_id+"/"+mCurrent_user.getUid()+"/date",currentDate);

                    friendsMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+user_id,null);
                    friendsMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state="not_friends";
                                mProfileSendReqBtn.setText("Unfriend");
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else{
                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                // Unfriend

                if(mCurrent_state.equals("friends")){
                    Map unFriendMap=new HashMap();
                    unFriendMap.put("Friends/"+mCurrent_user.getUid()+"/"+user_id,null);
                    unFriendMap.put("Friends/"+user_id+"/"+mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){

                                mCurrent_state="not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else{
                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}
