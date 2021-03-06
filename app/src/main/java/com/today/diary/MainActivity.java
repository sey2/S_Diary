package com.today.diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import com.today.diary.adapter.Note;
import com.today.diary.custom.StopWriteDialog;
import com.today.diary.data.AppConstants;
import com.today.diary.data.GridUtil;
import com.today.diary.data.WeatherItem;
import com.today.diary.data.WeatherResult;
import com.today.diary.db.NoteDatabase;
import com.today.diary.listener.OnRequestListener;
import com.today.diary.listener.OnTabSelectedListener;
import com.today.diary.ui.Fragment1;
import com.today.diary.ui.Fragment2;
import com.today.diary.ui.Fragment3;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import com.today.diary.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/* OnTabSelectedListener -> 하나의 프래그먼트에서 다른 프래그먼트로 전환하는 용도 */
public class MainActivity extends AppCompatActivity
        implements OnTabSelectedListener, OnRequestListener,MyApplication.OnResponseListener {

    private static final String SELECTED_TAB_INDEX = "selected_tab_index";

    /* Fragment */
    Fragment1 fragment1;    // 일기 목록
    Fragment2 fragment2;    // 일기 작성
    Fragment3 fragment3;    // 기분 통계

    StopWriteDialog stopWriteDialog;

    BottomNavigationView bottomNavigation;

    Location currentLocation;
    GPSListener gpsListener;

    int locationCount = 0;  // 위치 정보를 확인한 횟수
    String currentWeather;
    String currentAddress;
    String currentDateString;
    Date currentDate;
    SimpleDateFormat todayDateFormat;

    private long backPressTime = 0;

    /* 데이터베이스 인스턴스 */
    public static NoteDatabase mDatabase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override   // 하단 탭 버튼을 눌렀을 때 호출되는 메서드
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.tab1:
                        transaction.replace(R.id.container, fragment1).commit();
                        return true;

                    case R.id.tab2:
                        transaction.replace(R.id.container, fragment2).commit();
                        return true;

                    case R.id.tab3:
                        transaction.replace(R.id.container, fragment3).commit();

                        return true;
                }
                return false;
            }
        });

        if (savedInstanceState == null)
            onTabSelected(0, null);       // 일기목록 프래그먼트 호출
        else {
            int index = savedInstanceState.getInt(SELECTED_TAB_INDEX);
            onTabSelected(index, null);       // 저장된 탭 번호에    맞는 프래그먼트 화면 지정
        }

        setPicturePath();

        // FCM 설정
        FirebaseMessaging.getInstance().getToken() // 등록 id 확인을 위한 리스너 설정
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            Log.d("Main", "토큰 가져오는데 실패", task.getException());
                            return;
                        }
                        String newToken = task.getResult();
                        println("등록id : " + newToken);
                    }
                });

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("거부된 권한 갯수 : " + permissions.size());
                    }
                })
                .start();

        openDatabase();

    }

    public void setPicturePath() {
        String folderPath = getFilesDir().getAbsolutePath();
        AppConstants.FOLDER_PHOTO = folderPath + File.separator + "photo";

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);
        if (!photoFolder.exists()) {
            photoFolder.mkdirs();
        }
    }

    /* 이 메서드가 호출되면 하단 탭의 setSelected 메서드를 이용해 다른 탭 버튼이 선택 되도록 함 */
    /* 현재는 position3일 경우만 사용 되는 중*/
    @Override
    public void onTabSelected(int position, Note item) {
        if (position == 0)
            bottomNavigation.setSelectedItemId(R.id.tab1);
        else if (position == 1) {
            bottomNavigation.setSelectedItemId(R.id.tab2);
        } else if (position == 2)
            bottomNavigation.setSelectedItemId(R.id.tab3);
        else if (position == 3) {       // 일기 수정시
            fragment2.setItem(item);
            fragment2.setmMode(AppConstants.MODE_MODIFY);       // 일기 수정
            bottomNavigation.setSelectedItemId(R.id.tab2);

        }
    }

    @Override
    public void onRequest(String command) {
        if (command != null) {
            if (command.equals("getCurrentLocation")) {
                getCurrentLocation();
            }
        }
    }

    public void getCurrentLocation() {
        // set current time
        currentDate = new Date();

        //currentDateString = AppConstants.dateFormat3.format(currentDate);
        if (todayDateFormat == null) {
            todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
        }
        currentDateString = todayDateFormat.format(currentDate);
        AppConstants.println("currentDateString : " + currentDateString);

        /*  00월 00일
        if (fragment2 != null) {
            fragment2.setDateString(currentDateString);
        } */

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        try {
            currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (currentLocation != null) {
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                String message = "Last Location -> Latitude : " + latitude + "\nLongitude:" + longitude;
                println(message);

                getCurrentWeather();
                getCurrentAddress();
            }

            gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
            println("Current location requested.");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            currentLocation = location;

            locationCount++;

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String message = "Current Location -> Latitude: " + latitude + "\nLongitude:" + longitude;
            println(message);

            getCurrentWeather();
            getCurrentAddress();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    public void getCurrentAddress() {
        // 현재 위치를 주소로 반환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            currentAddress = null;

            Address address = addresses.get(0);

            if (address.getLocality() != null) {
                currentAddress = address.getLocality();
            }

            if (address.getSubLocality() != null) {
                if (currentAddress != null) {
                    currentAddress += " " + address.getSubLocality();
                } else {
                    currentAddress = address.getSubLocality();
                }
            }

            if (address.getThoroughfare() != null) {
                currentAddress += " " + address.getThoroughfare();
            }

            String adminArea = address.getAdminArea();
            String country = address.getCountryName();
            println("Address : " + country + " " + adminArea + " " + currentAddress);

            if (fragment2 != null) {
                fragment2.setAddress(currentAddress);
            }
        }
    }

    public void getCurrentWeather() {

        Map<String, Double> gridMap = GridUtil.getGrid(currentLocation.getLatitude(), currentLocation.getLongitude());
        double gridX = gridMap.get("x");
        double gridY = gridMap.get("y");
        println("x -> " + gridX + ", y -> " + gridY);

        sendLocalWeatherReq(gridX, gridY);

    }

    public void sendLocalWeatherReq(double gridX, double gridY) {
        String url = "http://www.kma.go.kr/wid/queryDFS.jsp";
        url += "?gridx=" + Math.round(gridX);
        url += "&gridy=" + Math.round(gridY);

        Map<String, String> params = new HashMap<String, String>();

        MyApplication.send(AppConstants.REQ_WEATHER_BY_GRID, Request.Method.GET, url, params, this);
    }


    @Override
    public void processResponse(int requestCode, int responseCode, String response) {
        if (responseCode == 200) {
            if (requestCode == AppConstants.REQ_WEATHER_BY_GRID) {
                // Grid 좌표를 이용한 날씨 정보 처리 응답
                //println("response -> " + response);

                XmlParserCreator parserCreator = new XmlParserCreator() {
                    @Override
                    public XmlPullParser createParser() {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                GsonXml gsonXml = new GsonXmlBuilder()
                        .setXmlParserCreator(parserCreator)
                        .setSameNameLists(true)
                        .create();

                WeatherResult weather = gsonXml.fromXml(response, WeatherResult.class);

                // 현재 기준 시간
                try {
                    Date tmDate = AppConstants.dateFormat.parse(weather.header.tm);
                    String tmDateText = AppConstants.dateFormat2.format(tmDate);
                    println("기준 시간 : " + tmDateText);

                    for (int i = 0; i < weather.body.datas.size(); i++) {
                        WeatherItem item = weather.body.datas.get(i);
                        println("#" + i + " 시간 : " + item.hour + "시, " + item.day + "일째");
                        println("  날씨 : " + item.wfKor);
                        println("  기온 : " + item.temp + " C");
                        println("  강수확률 : " + item.pop + "%");

                        println("debug 1 : " + (int) Math.round(item.ws * 10));
                        float ws = Float.valueOf(String.valueOf((int) Math.round(item.ws * 10))) / 10.0f;
                        println("  풍속 : " + ws + " m/s");
                    }

                    // set current weather
                    WeatherItem item = weather.body.datas.get(0);
                    currentWeather = item.wfKor;

                    if (fragment2 != null) {
                        fragment2.setWeather(item.wfKor);

                        // 00월 00일 어느 맑은 날
                        String DateWeather = ChangeWeatherString(currentWeather);
                        fragment2.setDateString(currentDateString + " 어느 " + DateWeather);

                    }

                    // stop request location service after 2 times
                    if (locationCount > 1) {
                        stopLocationService();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                // Unknown request code
                println("Unknown request code : " + requestCode);

            }

        } else {
            println("Failure response code : " + responseCode);

        }

    }

    public void stopLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            manager.removeUpdates(gpsListener);

            println("Current location requested.");

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void println(String data) {
        Log.d("MainActivity", data);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /*  데이터베이스 열기 (데이터베이스가 없을 때는 만들기) */
    public void openDatabase() {
        // open database
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }

        mDatabase = NoteDatabase.getInstance(this);
        boolean isOpen = mDatabase.open();
        if (isOpen) {
            Log.d("MainActivity", "Note database is open.");
        } else {
            Log.d("MainActivity>", "Note database is not open.");
        }
    }

    public void showStopWriteDialog() {
        stopWriteDialog = new StopWriteDialog(this);
        stopWriteDialog.show();

        stopWriteDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWriteDialog.dismiss();
            }
        });

        stopWriteDialog.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWriteDialog.dismiss();
                bottomNavigation.setSelectedItemId(R.id.tab1);
            }
        });

        stopWriteDialog.setContinueButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopWriteDialog.dismiss();
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (bottomNavigation.getSelectedItemId() == R.id.tab1) {
            if (System.currentTimeMillis() > backPressTime + 2000) {
                backPressTime = System.currentTimeMillis();
                Toast.makeText(this, "버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (System.currentTimeMillis() <= backPressTime + 2000)
                super.onBackPressed();

        } else if (bottomNavigation.getSelectedItemId() == R.id.tab2) {
            showStopWriteDialog();
        } else
            bottomNavigation.setSelectedItemId(R.id.tab1);
    }


    public static String ChangeWeatherString(String weather) {

        if (weather.equals("맑음"))
            return "맑은 날";
        else if (weather.equals("구름 조금"))
            return "구름 조금 있는 날";
        else if (weather.equals("구름 많음"))
            return "구름 많은 날";
        else if (weather.equals("흐림"))
            return "흐린 날";
        else if (weather.equals("비"))
            return "비 내리는 날";
        else if (weather.equals("눈/비"))
            return "눈과 비 오는 날";
        else if (weather.equals("눈"))
            return "눈 내리는 날";
        else
            return "";
    }

}