package com.example.icho.internetparking.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.icho.internetparking.R;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;

import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import com.example.icho.internetparking.cla.mapListItem;
import com.example.icho.internetparking.adapter.mapListItemAdapter;

public class MapActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener, AMap.OnMyLocationChangeListener {
    MapView mapView = null;
    AMap aMap = null;
    LatLonPoint myPoint = null;
    FloatingActionButton fab;
    BottomSheetBehavior sheetBehavior;
    List<mapListItem> listItems = new ArrayList<>();
    Button beforeButton = null;
    Button nextButton = null;
    RecyclerView mapRecycleView = null;
    int page = 1;
    int pageCount = 0;
    mapListItemAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.map);


        View bottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        fab = findViewById(R.id.fab);
        beforeButton = findViewById(R.id.before);
        nextButton = findViewById(R.id.next);
        mapRecycleView = findViewById(R.id.recycle_view);
        mapRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List list = new ArrayList<mapListItem>();
        adapter = new mapListItemAdapter(R.id.recycle_view, list);
        mapRecycleView.setAdapter(adapter);

        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        aMap.setOnMyLocationChangeListener(this);


        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);//取消放大缩小按钮

        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);

        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (myPoint == null) {
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(myPoint.getLatitude(), myPoint.getLongitude()), 18, 0, 0)));
                //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
            }
        }).start();

        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                searchPoi(1);
                page = 1;
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                fab.setVisibility(View.GONE);

            }
        });

        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page == 1) {

                } else {
                    page = page - 1;
                    searchPoi(page);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page < pageCount) {
                    page = page + 1;
                    searchPoi(page);
                }
            }
        });

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        fab.setVisibility(View.VISIBLE);
                        break;
                    default:
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //这里是拖拽中的回调，根据slideOffset可以做一些动画
            }
        });

    }


    public void searchPoi(int i) {
        aMap.clear();
        PoiSearch.Query query = new PoiSearch.Query("", "150900");
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(i);//设置查询页码
        PoiSearch poiSearch = new PoiSearch(MapActivity.this, query);
        poiSearch.setOnPoiSearchListener(MapActivity.this);
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(myPoint.getLatitude(), myPoint.getLongitude()), 3000));
        poiSearch.searchPOIAsyn();
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(myPoint.getLatitude() - 0.009, myPoint.getLongitude()), 15, 0, 0)));
        //缩小地图并将POI移至上方
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }


    @Override
    public void onPoiSearched(PoiResult poiResult, final int i) {
        if (!(listItems.isEmpty()))
            listItems.clear();
        pageCount = poiResult.getPageCount();
        ArrayList<PoiItem> poiItems = poiResult.getPois();
        final Intent intent = new Intent(MapActivity.this, reserveActivity.class);

        for (PoiItem item : poiItems) {
            LatLng latLng = new LatLng(item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
            aMap.addMarker(new MarkerOptions().position(latLng));
            mapListItem listItem = new mapListItem(item.getTitle(), +item.getDistance() + "米  " + item.getSnippet(), "￥10/h", "有车位");
            listItems.add(listItem);
        }
        adapter = new mapListItemAdapter(R.layout.map_list_item, listItems);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mapListItem item = (mapListItem) adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("info", item.getInfo());
                bundle.putString("title", item.getTitle());
                bundle.putString("price", item.getPrice());
                bundle.putString("available", item.getAvailable());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        mapRecycleView.setAdapter(adapter);

    }

    @Override
    public void onMyLocationChange(Location location) {
        myPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }




}
