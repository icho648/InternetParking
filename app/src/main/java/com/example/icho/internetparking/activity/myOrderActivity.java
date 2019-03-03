package com.example.icho.internetparking.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.example.icho.internetparking.R;
import com.example.icho.internetparking.adapter.orderListItemAdapter;
import com.example.icho.internetparking.database.orderListItem;

import org.litepal.LitePal;

import java.util.List;

public class myOrderActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView toolbarTitle;
    ActionBar actionBar = null;
    RecyclerView recyclerView;
    List<orderListItem> list;

    void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        recyclerView = findViewById(R.id.order_recycle_view);
        setSupportActionBar(toolbar);
        /*setNavigationOnClickListener()必须要在setSupportActionBar()之后调用才能生效.
                因为setSupportActionBar(Toolbar),会将Toolbar转换成Acitionbar.点击监听也会重新设置.*/
    }

    void initEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void initData() {
        list = LitePal.findAll(orderListItem.class);
    }

    void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        orderListItemAdapter adapter = new orderListItemAdapter(R.layout.order_list_item, list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        initViews();
        initEvents();
        initData();
        initAdapter();
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
        toolbarTitle.setText("我的订单");


    }
}
