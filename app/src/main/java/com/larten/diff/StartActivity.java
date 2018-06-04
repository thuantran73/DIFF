package com.larten.diff;

import android.content.Intent;
import android.print.PrinterId;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button mStartBtn;
    private Button mLoginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mStartBtn = (Button)findViewById(R.id.reg_start_btn);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(mainIntent);
            }
        });
        mLoginBtn = (Button)findViewById(R.id.start_login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(mainIntent);
            }
        });
    }
}
