package cn.edu.gdmec.s07150724.mymap;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;


public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private BaiduMap baiduMap;
    private Marker markerA;
    private InfoWindow infoWindow;
    private PoiSearch mPoiSearch;
    private LatLng dajidian = new LatLng(23.3906, 113.4535);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                String hint = "纬度:" + latLng.latitude + "\n经度:" + latLng.longitude;
                Toast.makeText(MainActivity.this, hint, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (marker != markerA) {
                    return false;
                }
                Button button = new Button(getApplicationContext());
                button.setBackgroundResource(R.drawable.popup);
                button.setText("更改位置");
                button.setTextColor(Color.BLACK);
                final LatLng ll = marker.getPosition();
                android.graphics.Point p = baiduMap.getProjection().toScreenLocation(ll);
                p.y -= 47;
                LatLng llInfo = baiduMap.getProjection().fromScreenLocation(p);
                InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {
                        LatLng llNew = new LatLng(ll.latitude + 0.005, ll.longitude + 0.005);
                        marker.setPosition(llNew);
                        baiduMap.hideInfoWindow();
                    }
                };
                infoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), llInfo, -47, listener);
                baiduMap.showInfoWindow(infoWindow);
                return false;
            }
        });
        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener getPoiSearchResultListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    return;
                }
                if (poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    baiduMap.clear();
                    PoiOverlay overlay = new PoiOverlay(baiduMap);
                    baiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(poiResult);
                    overlay.addToMap();
                    overlay.zoomToSpan();
                    Toast.makeText(MainActivity.this, "总共查到" + poiResult.getTotalPageNum() + "个兴趣点,分为" + poiResult.getTotalPageNum() + "页", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, poiDetailResult.getName() + "\n你是吃货，鉴定完毕", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        };
        mPoiSearch.setOnGetPoiSearchResultListener(getPoiSearchResultListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "更改地图类型");
        menu.add(0, 2, 0, "飞到机电");
        menu.add(0, 3, 0, "标注覆盖物");
        menu.add(0, 4, 0, "更改地图类型");
        menu.add(0, 5, 0, "更改地图类型");
        menu.add(0, 6, 0, "更改地图类型");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case 1:
                setMapType();
                break;
            case 2:
                setCenter(dajidian);
                break;
            case 3:
                setOverlay();
                break;
            case 4:
                setMapText();
                break;
            case 5:
                POISearch();
                break;
            case 6:
                GPSLocation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMapType() {
        if (baiduMap.getMapType() == BaiduMap.MAP_TYPE_SATELLITE) {
            baiduMap.setMapType(baiduMap.MAP_TYPE_NORMAL);
        } else {
            baiduMap.setMapType(baiduMap.MAP_TYPE_SATELLITE);
        }
    }

    private void setCenter(LatLng latlng) {
        MapStatus mapStatus = new MapStatus.Builder().target(latlng).zoom(18).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }

    private void setOverlay() {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
        OverlayOptions overlayOptions = new MarkerOptions().position(dajidian).icon(bitmapDescriptor);
        markerA = (Marker) baiduMap.addOverlay(overlayOptions);
    }

    private void setMapText() {
        OverlayOptions overlayOptions = new TextOptions().text("大机电").fontColor(0).fontSize(24).bgColor(0).position(dajidian).rotate(45);
        baiduMap.addOverlay(overlayOptions);
    }

    private void POISearch() {
        mPoiSearch.searchNearby(new PoiNearbySearchOption().location(dajidian).keyword("餐厅").radius(1000).pageNum(1));
        mPoiSearch.searchNearby(new PoiNearbySearchOption().location(dajidian).keyword("餐厅").radius(1000).pageNum(1));
    }

    private class MyPoiOverlay extends com.baidu.mapapi.overlayutil.PoiOverlay {

        /**
         * 构造函数
         *
         * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
         */
        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            if (poi.hasCaterDetails) {
                mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
            }
            return true;
        }
    }

    private void GPSLocation() {
        String serviceString = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager) getSystemService(serviceString);
        String provider = LocationManager.GPS_PROVIDER;
        Location location = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider,2000,10,locationListener);
    }
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setCenter(new LatLng(location.getLatitude(),location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
