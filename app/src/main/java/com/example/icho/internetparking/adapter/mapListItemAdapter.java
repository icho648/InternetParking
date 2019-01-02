package com.example.icho.internetparking.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.icho.internetparking.R;
import com.example.icho.internetparking.cla.mapListItem;

import java.util.List;

public class mapListItemAdapter extends BaseQuickAdapter<mapListItem,BaseViewHolder> {
    public mapListItemAdapter(int layoutResId,List<mapListItem> data){
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, mapListItem item) {
        helper.setText(R.id.park_title,item.getTitle());
        helper.setText(R.id.park_info,item.getInfo());
        helper.setText(R.id.hour_price,item.getPrice());
        helper.setText(R.id.available,item.getAvailable());
    }
}
