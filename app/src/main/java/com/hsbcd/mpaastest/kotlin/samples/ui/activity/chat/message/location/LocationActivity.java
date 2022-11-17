package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.location;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.ActivityLocationBinding;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.util.ImageUtils;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.io.File;

/**
 * 选择位置页(基于高德定位sdk)
 * <p>
 * 参考：
 * 1. 开发指南：https://lbs.amap.com/api/android-location-sdk/guide/android-location/getlocation
 * 2. 示例代码：https://lbs.amap.com/api/android-location-sdk/download
 *
 * @author liyalong
 * @version LocationActivity.java, v 0.1 2022年08月19日 22:48 liyalong
 */
public class LocationActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener {

    private ActivityLocationBinding binding;

    public AMapLocationClient locationClient = null;

    private OnLocationChangedListener locationChangedListener;

    private AMapLocation location;

    private GeocodeSearch geocoderSearch;

    private Marker locationMarker;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> super.onBackPressed());
        binding.sendLocation.setOnClickListener(v -> sendLocation());

        binding.mapView.onCreate(savedInstanceState);
        initMapView();
        initGeocodeSearch();
    }

    private void initMapView() {
        AMap aMap = binding.mapView.getMap();

        // 设置定位蓝点样式
        MyLocationStyle style = new MyLocationStyle();
        style.interval(2000);
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        style.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_mark));
        style.strokeColor(Color.argb(0, 0, 0, 0));
        style.radiusFillColor(Color.argb(0, 0, 0, 0));
        aMap.setMyLocationStyle(style);

        // 设置地图默认缩放等级
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));

        // 设置是否显示定位按钮
        aMap.getUiSettings().setMyLocationButtonEnabled(true);

        // 设置定位监听，用于把定位结果更新到地图
        aMap.setLocationSource(this);

        // 立即触发定位(调用LocationSource.activate)
        // 注：这一步必须放在设置定位监听的后面，否则打开页面时无法自动定位
        aMap.setMyLocationEnabled(true);

        // 设置地图拖动监听，用于获取拖动后的地图中心点定位地址
        aMap.setOnCameraChangeListener(this);
    }

    private void initGeocodeSearch() {
        try {
            geocoderSearch = new GeocodeSearch(this);
            geocoderSearch.setOnGeocodeSearchListener(this);
        } catch (AMapException e) {
            e.printStackTrace();
            ToastUtil.makeToast(this, "地图搜索组件初始化失败", 3000);
            super.onBackPressed();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;

        // 隐私政策合规检查
        AMapLocationClient.updatePrivacyShow(LocationActivity.this, true, true);
        AMapLocationClient.updatePrivacyAgree(LocationActivity.this, true);

        // 初始化定位
        try {
            locationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.makeToast(this, "定位sdk初始化失败", 3000);
            return;
        }

        // 设置定位参数
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setInterval(2000);
        option.setNeedAddress(true);
        option.setOnceLocation(true);
        option.setHttpTimeOut(10000);
        locationClient.setLocationOption(option);

        // 设置定位回调监听
        locationClient.setLocationListener(this);

        // 启动定位
        locationClient.startLocation();
    }

    @Override
    public void deactivate() {
        locationChangedListener = null;

        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            // 定位成功
            if (aMapLocation.getErrorCode() == 0) {
                // 通知地图视图移动到定位的位置
                locationChangedListener.onLocationChanged(aMapLocation);

                binding.mapView.getMap().moveCamera(CameraUpdateFactory.zoomTo(17));

                // 暂存并渲染定位到的详细地址
                updateCurrentLocation(aMapLocation);

                // 渲染定位图标
                setLocationMarker();

            } else {
                // 定位失败，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        // 地图拖动完成，根据当前经纬度查询地址
        LatLng latLng = cameraPosition.target;
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude, latLng.longitude), 200,
                GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    /**
     * 处理逆地理编码(经纬度转地址)的搜索结果
     *
     * @param regeocodeResult
     * @param i
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        // 搜索成功
        if (i == 1000) {
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null &&
                    regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                // 更新当前地址
                updateCurrentLocation(convertLocation(regeocodeResult));
            }
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 渲染定位图标
     * 不管地图如何移动，该图标始终位于地图正中
     */
    private void setLocationMarker() {
        if (locationMarker != null) {
            locationMarker.remove();
        }

        MarkerOptions markOptions = new MarkerOptions();
        markOptions.draggable(true);
        markOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_mark2));

        // 定位图标添加到地图上
        locationMarker = binding.mapView.getMap().addMarker(markOptions);

        // 显示在屏幕居中位置
        WindowManager wm = this.getWindowManager();
        int width = (wm.getDefaultDisplay().getWidth()) / 2;
        int height = ((wm.getDefaultDisplay().getHeight()) / 2) - 100;
        locationMarker.setPositionByPixels(width, height);

        // 设置图标的位置经纬度(暂不需要)
//        locationMarker.setPosition(new LatLng(latitude, longitude));

        // 设置图标上方的悬浮信息窗(暂不需要)
//        locationMarker.setTitle("title");
//        locationMarker.setSnippet("content");
//        if (!TextUtils.isEmpty("content")) {
//            locationMarker.showInfoWindow();
//        }

        // 刷新地图
        binding.mapView.invalidate();
    }

    private void updateCurrentLocation(AMapLocation location) {
        this.location = location;
        binding.currentLocation.setText(location.getAddress());
    }

    /**
     * 经纬度转地址的结果转换为定位对象
     *
     * @param regeocodeResult
     * @return
     */
    private AMapLocation convertLocation(RegeocodeResult regeocodeResult) {
        AMapLocation location = new AMapLocation("aaa");

        LatLonPoint point = regeocodeResult.getRegeocodeQuery().getPoint();
        location.setLatitude(point.getLatitude());
        location.setLongitude(point.getLongitude());

        location.setAddress(regeocodeResult.getRegeocodeAddress().getFormatAddress());

        return location;
    }

    private void sendLocation() {
        if (this.location == null) {
            return;
        }

        binding.mapView.getMap().getMapScreenShot(bitmap -> {
            // 位置截图写入文件
            String filePath = String.format("%slocation-%d.jpg",
                    AlipayCcmIMClient.getInstance().getInitConfig().getStorageDir(), System.nanoTime());
            File file = new File(filePath);
            ImageUtils.writeBitMapToFileWithCompressQuantity(bitmap, file, 20);

            // 定位结果返回给消息页
            handler.post(() -> {
                Intent intent = new Intent();
                intent.putExtra("snapshotFilePath", filePath);
                intent.putExtra("location", location);

                setResult(RESULT_OK, intent);
                finish();
            });
        });
    }

}