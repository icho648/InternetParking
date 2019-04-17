package com.example.icho.internetparking.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.icho.internetparking.cla.mapListItem;
import com.example.icho.internetparking.adapter.mapListItemAdapter;

public class MapActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener, GeocodeSearch.OnGeocodeSearchListener {

    LatLonPoint myPoint = null;
    LatLonPoint searchPoint=null;
    GeocodeSearch geocodeSearch;
    String cityCode = "";
    List<mapListItem> listItems = new ArrayList<>();
    int page = 1;
    int pageCount = 0;
    AMap aMap = null;
    boolean isExit = false;
    boolean isFirstOpen=true;
    mapListItemAdapter adapter = null;

    MapView mapView = null;
    FloatingActionButton fab;
    BottomSheetBehavior sheetBehavior;
    RecyclerView mapRecycleView = null;
    View bottomSheet=null;
    DrawerLayout drawerLayout = null;
    Button homeButton = null;
    EditText searchEdit = null;
    NavigationView navigationView = null;
    Button searchButton = null;

    public void initViews(){
        mapView = findViewById(R.id.map);
        bottomSheet = findViewById(R.id.recycle_view);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        fab = findViewById(R.id.fab);
        mapRecycleView = findViewById(R.id.recycle_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        homeButton = findViewById(R.id.button_home);
        searchEdit = findViewById(R.id.edit_search);
        navigationView = findViewById(R.id.nav_view);
        searchButton = findViewById(R.id.button_search);
    }

    public void initEvents(){
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (searchEdit.getText().toString().isEmpty())
                        Toast.makeText(MapActivity.this, "请输入搜索关键字", Toast.LENGTH_SHORT).show();
                    else {
                        if (isNetworkOnline()) {
                            if (!(listItems.isEmpty()))
                                listItems.clear();//清空poi列表
                            aMap.clear();//清空标记
                            page = 1;
                            RegeocodeQuery query = new RegeocodeQuery(myPoint, 200, GeocodeSearch.AMAP);//先查找本机所在城市
                            geocodeSearch.getFromLocationAsyn(query);//在回调中进行处理
                        } else
                            Toast.makeText(MapActivity.this, "网络不可用，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkOnline()) {
                    if (!(listItems.isEmpty()))
                        listItems.clear();//清空poi列表
                    aMap.clear();//清空标记
                    searchPoint = myPoint;
                    page = 1;
                    searchPoiAround();
                } else
                    Toast.makeText(MapActivity.this, "网络不可用，请检查您的网络连接", Toast.LENGTH_SHORT).show();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        fab.setVisibility(View.VISIBLE);
                        //aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(myPoint.getLatitude(), myPoint.getLongitude()), 16, 0, 0)));
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        fab.setVisibility(View.GONE);
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                                hideSoftInputFromWindow(searchEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        break;
                    default:
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //这里是拖拽中的回调，根据slideOffset可以做一些动画
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.nav_order:
                        intent = new Intent(MapActivity.this, myOrderActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_wallet:
                        intent = new Intent(MapActivity.this, walletActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_settings:
                        intent = new Intent(MapActivity.this, settingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_share:
                        intent = new Intent(MapActivity.this, shareActivity.class);
                        startActivity(intent);
                        break;
                    default:
                }
                return true;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchEdit.getText().toString().isEmpty())
                    Toast.makeText(MapActivity.this, "请输入搜索关键字", Toast.LENGTH_SHORT).show();
                else {
                    if (isNetworkOnline()) {
                        if (!(listItems.isEmpty()))
                            listItems.clear();//清空poi列表
                        aMap.clear();//清空标记
                        page = 1;
                        RegeocodeQuery query = new RegeocodeQuery(myPoint, 200, GeocodeSearch.AMAP);//先查找本机所在城市
                        geocodeSearch.getFromLocationAsyn(query);//在回调中进行处理
                    } else
                        Toast.makeText(MapActivity.this, "网络不可用，请检查您的网络连接", Toast.LENGTH_SHORT).show();


                }

            }
        });
    }

    public void initAdapter(){
        mapRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        final Intent intent = new Intent(MapActivity.this, reserveActivity.class);
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
                bundle.putString("imageUrl", item.getImageUrl());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {//下拉加载更多
            @Override
            public void onLoadMoreRequested() {
                mapRecycleView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (page>=pageCount){
                            adapter.loadMoreEnd();
                        }
                        else {
                            page++;
                            searchPoiAround();
                            adapter.loadMoreComplete();
                        }
                    }
                },1000);
            }
        },mapRecycleView);
        mapRecycleView.setAdapter(adapter);

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        initEvents();
        initAdapter();

        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                myPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
                if (isFirstOpen){
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(myPoint.getLatitude(), myPoint.getLongitude()), 16, 0, 0)));
                    //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
                    isFirstOpen=false;
                }
            }
        });//监听位置改变
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);//取消放大缩小按钮
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);


    }



    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            fab.setVisibility(View.VISIBLE);
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(myPoint.getLatitude(), myPoint.getLongitude()), 16, 0, 0)));
        }
        else {
            if (!isExit) {
                isExit = true;
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
            } else {
                finish();
            }
        }


    }

    public void searchPoiAround() {
        PoiSearch.Query query = new PoiSearch.Query("", "150900");
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(page);//设置查询页码
        PoiSearch poiSearch = new PoiSearch(MapActivity.this, query);
        poiSearch.setOnPoiSearchListener(MapActivity.this);
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(searchPoint.getLatitude(), searchPoint.getLongitude()), 3000));
        poiSearch.searchPOIAsyn();
    }


    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }


    @Override
    public void onPoiSearched(PoiResult poiResult, final int i) {
        if (i == 1000) {
            pageCount = poiResult.getPageCount();
            ArrayList<PoiItem> poiItems = poiResult.getPois();

            for (PoiItem item : poiItems) {
                LatLng latLng = new LatLng(item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
                if (page == 1)
                    aMap.addMarker(new MarkerOptions().position(latLng));
                mapListItem listItem = new mapListItem(item.getTitle(), +item.getDistance() + "米  " + item.getSnippet(), "￥10/h", "有车位", "");
                if (!item.getPhotos().isEmpty())
                    listItem.setImageUrl(item.getPhotos().get(0).getUrl());
                listItems.add(listItem);
            }
            adapter.notifyDataSetChanged();
            //if (page==1)
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(searchPoint.getLatitude() - 0.0055, searchPoint.getLongitude()), 16, 0, 0)));
            //else
            //aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(searchPoint.getLatitude() - 0.009, searchPoint.getLongitude()), 15, 0, 0)));
            //缩小地图并将POI移至上方
            if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
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

    /*public boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }*/

    public boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 1 www.baidu.com");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 1000) {
            cityCode = regeocodeResult.getRegeocodeAddress().getCityCode();
            GeocodeQuery query = new GeocodeQuery(searchEdit.getText().toString(), cityCode);
            geocodeSearch.getFromLocationNameAsyn(query);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        if (i == 1000) {
            searchPoint = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
            LatLng latLng = new LatLng(searchPoint.getLatitude(), searchPoint.getLongitude());
            aMap.addMarker(new MarkerOptions().position(latLng));
            searchPoiAround();
        }
    }
}
