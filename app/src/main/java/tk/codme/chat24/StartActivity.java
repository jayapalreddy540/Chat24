package tk.codme.chat24;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class StartActivity extends AppCompatActivity {

    private Button mRegBtn,mLogBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn=(Button)findViewById(R.id.start_reg_btn);
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_intent);
            }
        });
        mLogBtn=(Button)findViewById(R.id.start_login_btn);
        mLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log_intent=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(log_intent);
            }
        });
    }
}
