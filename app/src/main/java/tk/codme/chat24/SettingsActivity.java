package tk.codme.chat24;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mRef;
    private FirebaseUser currentUser;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private ImageButton mStatusBtn;
    private ImageButton mImgBtn;
    private ProgressDialog mProgressDialog;
    private static final int GALLERY_PICK=1;

    private StorageReference mImageStorage;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage=(CircleImageView)findViewById(R.id.settings_image);
        mName=(TextView)findViewById(R.id.settings_name);
        mStatus=(TextView)findViewById(R.id.settings_profile);
        mStatusBtn=(ImageButton)findViewById(R.id.settings_changeStatusBtn);
        mImgBtn=(ImageButton)findViewById(R.id.settings_changeImgBtn);

        mImageStorage=FirebaseStorage.getInstance().getReference();

        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null){
        final String currentUid=currentUser.getUid();
        mRef=FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);
        mRef.keepSynced(true);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                mName.setGravity(Gravity.CENTER);
                mStatus.setGravity(Gravity.CENTER);

                if (!image.equals("default")) {
                    //Picasso.get().load(image).placeholder(R.drawable.default_img).into(mDisplayImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_img).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_img).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String statusValue=mStatus.getText().toString();
                Intent statusIntent=new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",statusValue);
                startActivity(statusIntent);
            }
        });

        mImgBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {

                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

                // start picker to get image for cropping and then use the image in cropping activity
               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
                        */
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500,500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image....");
                mProgressDialog.setMessage("Please Wait.. Your Image has Been Uploading");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());
                final String currentUserId = currentUser.getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filepath = mImageStorage.child("profile_images").child(currentUserId + ".jpg");

                UploadTask uploadTask = filepath.putFile(resultUri);
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
                            mRef.child("image").setValue(downloadUri.toString());
                            Toast.makeText(SettingsActivity.this, "Upload Success", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Upload Error", Toast.LENGTH_LONG).show();
                        }
                        mProgressDialog.dismiss();
                    }

                });


                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(currentUserId + ".jpg");
                UploadTask uploadTask1=thumb_filepath.putBytes(thumb_byte);
                Task<Uri> urlTask1 =uploadTask1.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }

                        return thumb_filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
                    @Override
                    public void onComplete(@NonNull Task<Uri> task){
                        if(task.isSuccessful())
                        {
                            Uri downloadUri1=task.getResult();
                            mRef.child("thumb_image").setValue(downloadUri1.toString());
                            Toast.makeText(SettingsActivity.this, "Upload Success", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(SettingsActivity.this, "Upload Error", Toast.LENGTH_LONG).show();
                        }
                    }

                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Exception error = result.getError();
            }
        }
    }
}
