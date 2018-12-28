package tk.codme.chat24;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String mCurrentUser,mCurrentUserName;
    private DatabaseReference mRootRef;

    private TextView mTitleView,mLastSeenView;
    private CircleImageView mProfileImage;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();

        mCurrentUser=getIntent().getStringExtra("user_id");
        mCurrentUserName=getIntent().getStringExtra("user_name");

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
        mMessagesList=(RecyclerView)findViewById(R.id.users_list);

        messageAdapter=new MessageAdapter(messagesList);
        mTitleView.setText(mCurrentUser);

        mLinearLayout=new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(messageAdapter);

        loadMessages();


        mRootRef.child("users").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").toString();
                String image=dataSnapshot.child("image").toString();

                if(online.equals("true"))
                {
                    mLastSeenView.setText("online");
                }
                else{
                    GetTimeAgo getTimeAgo=new GetTimeAgo();
                    long lastTime=Long.parseLong(online);
                    String lastSeenTime=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mCurrentUser)){
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
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
    }

    public void loadMessages(){
mRootRef.child("messages").child(mCurrentUser).addChildEventListener(new ChildEventListener() {
    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        Messages message=dataSnapshot.getValue(Messages.class);
        messagesList.add(message);
        messageAdapter.notifyDataSetChanged();
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

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

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
