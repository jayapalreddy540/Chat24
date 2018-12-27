package tk.codme.chat24;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mStatusBtn;

    private DatabaseReference mRef;
    private FirebaseUser currentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar=(Toolbar)findViewById(R.id.status_appbar);
       // setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("Status");
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String statusValue=getIntent().getStringExtra("status_value");

        mProgress=new ProgressDialog(StatusActivity.this);

        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=currentUser.getUid();
        mRef=FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);

        mStatus=(TextInputLayout)findViewById(R.id.status_input);
        mStatusBtn=(Button)findViewById(R.id.status_btn);

        mStatus.getEditText().setText(statusValue);
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the chenges");
                mProgress.show();

                String status=mStatus.getEditText().getText().toString();

                mRef.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                            Intent settingsIntent=new Intent(StatusActivity.this,SettingsActivity.class);
                            startActivity(settingsIntent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

    }
}
