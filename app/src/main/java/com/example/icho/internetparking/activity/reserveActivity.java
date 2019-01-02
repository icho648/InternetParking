package com.example.icho.internetparking.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.icho.internetparking.R;

public class reserveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        TextView textView=findViewById(R.id.test);
        textView.setText(bundle.getString("title"));
    }
}
