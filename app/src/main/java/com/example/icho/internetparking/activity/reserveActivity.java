package com.example.icho.internetparking.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.icho.internetparking.R;
import com.example.icho.internetparking.database.orderListItem;

import java.util.Date;

import static java.lang.Thread.sleep;

public class reserveActivity extends AppCompatActivity {
    Date date = new Date();
    ProgressDialog waitingDialog;
    ImageView reserveImage;
    TextView reserveTitle;
    TextView reserveInfo;
    TextView reservePriceAndAvailable;
    Button reserveButton;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;

    public void initViews() {
        reserveImage = findViewById(R.id.reserve_image);
        reserveTitle = findViewById(R.id.reserve_title);
        reserveInfo = findViewById(R.id.reserve_info);
        reservePriceAndAvailable = findViewById(R.id.reserve_price_and_available);
        reserveButton = findViewById(R.id.reserve_button);
        toolbar = findViewById(R.id.reserve_bar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
    }

    public void initEvents() {
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderListItem order = new orderListItem();
                order.setTitle(reserveTitle.getText().toString());
                order.setState("已完成");
                order.setTimeCreate(date.toString());
                order.save();
                showWaitingDialog();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            waitingDialog.cancel();
                            Toast.makeText(reserveActivity.this, "预定完成", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 1500);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();

        initViews();
        initEvents();

        collapsingToolbarLayout.setTitle(bundle.getString("title"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        Glide.with(this).load(Uri.parse(bundle.getString("imageUrl"))).into(reserveImage);
        reserveTitle.setText(bundle.getString("title"));
        reserveInfo.setText(bundle.getString("info"));
        reservePriceAndAvailable.setText(bundle.getString("price") + "   " + bundle.getString("available"));


    }

    private void showWaitingDialog() {
        waitingDialog = new ProgressDialog(reserveActivity.this);
        waitingDialog.setTitle("正在创建订单");
        waitingDialog.setMessage("请求中...");
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);
        waitingDialog.show();
    }
}
