package tk.codme.chat24;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG ="" ;
    private String mCurrentUser,mCurrentUserName;
    private DatabaseReference mRootRef;

    private TextView mTitleView,mLastSeenView;
    private CircleImageView mProfileImage;

    private FirebaseAuth mAuth;
    private StorageReference mImageStorage;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter messageAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD=10;
    private int mCurrentPage=1,itemPos=0;

    private  String mLastKey="";
    private String mPrevKey="",messageKey="";

    private static final int GALLERY_PICK=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef=FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mAuth=FirebaseAuth.getInstance();

        mCurrentUser=getIntent().getStringExtra("user_id");
        mCurrentUserName=getIntent().getStringExtra("user_name");
        mCurrentUserId=mAuth.getCurrentUser().getUid();

        getSupportActionBar().setTitle(mCurrentUserName);

        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);


        mTitleView=(TextView)findViewById(R.id.custom_bar_title);
        mLastSeenView=(TextView)findViewById(R.id.custom_bar_seen);
        mProfileImage=(CircleImageView)findViewById(R.id.custom_bar_image);

        mChatAddBtn=(ImageButton)findViewById(R.id.chat_add_btn);
        mChatSendBtn=(ImageButton)findViewById(R.id.chat_send_btn);
        mChatMessageView=(EditText)findViewById(R.id.chat_message_view);
        mMessagesList=(RecyclerView)findViewById(R.id.messages_list);
        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);

        messageAdapter=new MessageAdapter(messagesList);
        mTitleView.setText(mCurrentUserName);

        mLinearLayout=new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(messageAdapter);



        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef.child("chat").child(mCurrentUserId).child(mCurrentUser).child("seen").setValue(true);


        loadMessages();


        mRootRef.child("users").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").toString();

                if(online.equals("online")){mLastSeenView.setText("online");}
                else{
                    Long online1 = (Long)(dataSnapshot.child("online").getValue());
                    // GetTimeAgo getTimeAgo=new GetTimeAgo();
                    // long lastTime=Long.parseLong(online);
                    //String lastSeenTime=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(getTimeDate(online1));
                }

                String image=dataSnapshot.child("image").toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log(Log.DEBUG, "tag", databaseError.toString());

            }
        });

        mRootRef.child("chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mCurrentUser)){
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen","false");
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("chat/"+mCurrentUserId+"/"+mCurrentUser,chatAddMap);
                    chatUserMap.put("chat/"+mCurrentUser+"/"+mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //send message

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });
    }

    public static String getTimeDate(long timedate){
        DateFormat dateFormat=DateFormat.getDateTimeInstance();
        Date netDate=new Date(timedate);
        return dateFormat.format(netDate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
           Uri imageUri=data.getData();

           final String current_user_ref="messages/"+mCurrentUserId+"/"+mCurrentUser;
           final String chat_user_ref="messages/"+mCurrentUser+"/"+mCurrentUserId;
           DatabaseReference user_message_push=mRootRef.child("messages")
                   .child(mCurrentUserId).child(mCurrentUser).push();
           final String push_id=user_message_push.getKey();

           final StorageReference filepath=mImageStorage.child("message_images").child(push_id+".jpg");
            UploadTask uploadTask = filepath.putFile(imageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        Map messageMap=new HashMap();
                        messageMap.put("message",downloadUri.toString());
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUserId);

                        Map messageUserMap=new HashMap();
                        messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                        messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

                        mChatMessageView.setText("");
                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ChatActivity.this, "Image Sending Error", Toast.LENGTH_LONG).show();
                    }
                }

            });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Exception error = result.getError();
            }
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mCurrentUser);
        messageRef.keepSynced(true);
        Query messageQuery=messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message=dataSnapshot.getValue(Messages.class);
                messageKey=dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                }
                else{
                    mPrevKey=mLastKey;
                }
                if(itemPos==1){

                    mLastKey=messageKey;
                }

                Log.d("TOTAL KEYS","Last key :"+mLastKey+" | Prev key :"+mPrevKey+" | Message Key :"+messageKey);

                messageAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadMessages(){

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mCurrentUser);
        messageRef.keepSynced(true);

        Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        Messages message=dataSnapshot.getValue(Messages.class);

        itemPos++;
        if(itemPos==1){
            String messageKey=dataSnapshot.getKey();
            mLastKey=messageKey;
            mPrevKey=messageKey;
        }
        messagesList.add(message);
        messageAdapter.notifyDataSetChanged();

        mMessagesList.scrollToPosition(messagesList.size()-1);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});
    }


    private void sendMessage(){
        String message=mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref="messages/"+mCurrentUserId+"/"+mCurrentUser;
            String chat_user_ref="messages/"+mCurrentUser+"/"+mCurrentUserId;

            DatabaseReference user_message_push=mRootRef.child("messages").child(mCurrentUserId).child(mCurrentUser).push();
            String push_id=user_message_push.getKey();

            Map messageMap=new HashMap();
            messageMap.put("message",message);
            messageMap.put("send",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

            mChatMessageView.setText("");

            mRootRef.child("chat").child(mCurrentUserId).child(mCurrentUser).child("seen").setValue(true);
            mRootRef.child("chat").child(mCurrentUserId).child(mCurrentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("chat").child(mCurrentUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("chat").child(mCurrentUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);


            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}
