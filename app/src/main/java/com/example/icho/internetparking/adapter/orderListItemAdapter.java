package com.example.icho.internetparking.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.icho.internetparking.R;
import com.example.icho.internetparking.database.orderListItem;

import java.util.List;

public class orderListItemAdapter extends BaseQuickAdapter<orderListItem, BaseViewHolder> {
    public orderListItemAdapter(int layoutResId, List<orderListItem> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, orderListItem item) {
        helper.setText(R.id.order_title, item.getTitle());
        helper.setText(R.id.order_state, item.getState());
        helper.setText(R.id.order_time_create, item.getTimeCreate());
    }
}
