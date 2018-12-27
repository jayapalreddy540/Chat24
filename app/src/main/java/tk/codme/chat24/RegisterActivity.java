package tk.codme.chat24;


import android.app.ProgressDialog;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;


    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseDatabase mDatabase;

    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();


        mDisplayName=(TextInputLayout)findViewById(R.id.reg_disp_name);
        mEmail=(TextInputLayout)findViewById(R.id.reg_email);
        mPassword=(TextInputLayout)findViewById(R.id.reg_pass);
        mCreateBtn=(Button)findViewById(R.id.reg_create_btn);

        mRegProgress=new ProgressDialog(this);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                if(!TextUtils.isEmpty(display_name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait..  we are creating your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(display_name,email,password);
                }

            }
        });
    }

              private void  register_user(final String display_name, String email, String password){
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {


                                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (task.isSuccessful()) {

                                                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                                                    final String uid=current_user.getUid();
                                                    String deviceToken = task.getResult().getToken();
                                                    mRef=FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                                                    HashMap<String,String> userMap=new HashMap<>();
                                                    userMap.put("name",display_name);
                                                    userMap.put("status","Hey There! Iam using Chat24");
                                                    userMap.put("image","default");
                                                    userMap.put("thumb_image","default");
                                                    userMap.put("device_token",deviceToken);
                                                    mRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            mRegProgress.dismiss();
                                                            Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(mainIntent);
                                                            finish();
                                                        }
                                                    });
                                                }

                                            }
                                        });


                                    }
                                    else {
                                            mRegProgress.hide();
                                            String error="";
                                            try{
                                                throw task.getException();
                                            }
                                            catch(FirebaseAuthWeakPasswordException e){
                                                error="Weak Password";
                                            }catch(FirebaseAuthInvalidCredentialsException e){
                                                error="invalid Email";
                                            }catch(FirebaseAuthUserCollisionException e){
                                                error="Account Already Exists";
                                            }catch(Exception e){
                                                error="Unknown error";
                                                e.printStackTrace();
                                            }
                                        Toast.makeText(getApplicationContext(),error, Toast.LENGTH_SHORT).show();
                                    }

                                    // ...
                                }
                            });
                }

}
