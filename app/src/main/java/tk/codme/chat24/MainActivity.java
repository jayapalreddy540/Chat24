package tk.codme.chat24;

import androidx.viewpager.widget.ViewPager;
import android.app.ProgressDialog;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.widget.Toolbar;
import io.fabric.sdk.android.Fabric;

import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private AdView mAdView;

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private ProgressDialog mSignoutProgress;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        //initialising crashlytics
        Fabric.with(this,new Crashlytics());
        MobileAds.initialize(this,"ca-app-pub-4974110873156124~6823462870");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mSignoutProgress=new ProgressDialog(this);

       // mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        //setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat24");


        if(mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }
        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());
         mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            sendToStart();
        }
        else {
            mUserRef.child("online").setValue("online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToStart() {
        mSignoutProgress.dismiss();
        Intent startIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.main_logout_btn){

             mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

             mSignoutProgress.setTitle("Logout");
             mSignoutProgress.setMessage("Logging out...");
             mSignoutProgress.setCanceledOnTouchOutside(false);
             mSignoutProgress.show();
             FirebaseAuth.getInstance().signOut();
             sendToStart();
         }

         if(item.getItemId()==R.id.main_settings_btn){
             Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
             startActivity(settingsIntent);
         }
         if(item.getItemId()==R.id.main_allusers_btn){
             Intent usersIntent=new Intent(MainActivity.this,UsersActivity.class);
             startActivity(usersIntent);
         }
         return true;
    }


}
