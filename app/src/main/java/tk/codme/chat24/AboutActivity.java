package tk.codme.chat24;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity  extends AppCompatActivity {

    private TextView versiontxt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        versiontxt=(TextView)findViewById(R.id.version);
        PackageInfo pinfo=null;
        try{
            pinfo=getPackageManager().getPackageInfo(getPackageName(),0);
        }
        catch (Exception e){

        }

        int versionNumber=pinfo.versionCode;
        String versionName=pinfo.versionName;
        versiontxt.setText("Version : "+versionName+versionNumber);

    }
}
