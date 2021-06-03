package com.example.exam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    private static final int REQUEST_CODE_PERMISSONS = 1000;
    XmlPullParser xpp;
    Context context = this;

    String key="us2ROEBp96Lg%2F%2FFoXYqqzHC3S1TgYQQFrruf%2FbjcENdpvC3PsZnoWsV1jb8VJLuNXorx%2BL75uwFTfFLSj2bI8Q%3D%3D";
    String data;
    EditText edit;
    TextView text;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocation;

    ArrayList<Charging> chargings = new ArrayList<Charging>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String TAG = "OnCreate";

        Log.d(TAG,"정상 작동 하는가");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

    }

    //맵 레디
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        String TAG = "maploading";

        ProgressTask task = new ProgressTask();
        task.execute("Start");


        Log.d(TAG,"on Map Ready잘 실행 되는가?");

        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        mMap.addMarker(markerOptions);
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            }
        });

        mMap.setOnMarkerClickListener(this);

    }

    //내 위치 확인!
    public void onLastLocationButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSONS);
            return;
        }
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("현재 위치"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                }

            }
        });

    }

    //위치 확인 수락/거절
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE_PERMISSONS:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"권한 체크 거부 됨",Toast.LENGTH_SHORT).show();
                }

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getTitle() +"\n"+marker.getSnippet(),Toast.LENGTH_SHORT).show();
        return false;
    }


    class ProgressTask extends AsyncTask<String, Integer, ArrayList<Charging>>{
        GoogleMap googleMap;
        String TAG = "AsyncTask";

        @Override
        protected ArrayList<Charging> doInBackground(String... strings) {
            ArrayList<Charging> chargingArrayList = new ArrayList<Charging>();
            StringBuffer buffer=new StringBuffer();
//        String str= edit.getText().toString();//EditText에 작성된 Text얻어오기
//        String location = URLEncoder.encode(str);

            String queryUrl="http://openapi.kepco.co.kr/service/EvInfoServiceV2/getEvSearchList?pageNo=1&numOfRows=10&ServiceKey="+key;
            try{

                Charging charging = null;

                URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
                InputStream is= url.openStream(); //url위치로 입력스트림 연결

                XmlPullParserFactory factory= XmlPullParserFactory.newInstance();//xml파싱을 위한
                XmlPullParser xpp= factory.newPullParser();
                xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

                String tag;

                xpp.next();
                int eventType= xpp.getEventType();
                while( eventType != XmlPullParser.END_DOCUMENT ){
                    switch( eventType ){
                        case XmlPullParser.START_DOCUMENT:
                            buffer.append("파싱 시작...\n\n");
                            break;

                        case XmlPullParser.START_TAG:

                            tag= xpp.getName();//테그 이름 얻어오기

                            if(tag.equals("item")){
                                charging = new Charging();
                            }
                            else if(tag.equals("addr")){
                                xpp.next();
                                charging.setAddr(xpp.getText());
                            }
                            else if(tag.equals("chargeTp")){
                                xpp.next();
                                charging.setChargeTp(xpp.getText());
                            }
                            else if(tag.equals("cpId")){
                                xpp.next();
                                charging.setCpId(xpp.getText());
                            }
                            else if(tag.equals("cpNm")){
                                xpp.next();
                                charging.setCpNm(xpp.getText());
                            }
                            else if(tag.equals("cpStat")){
                                xpp.next();
                                charging.setCpStat(xpp.getText());
                            }
                            else if(tag.equals("cpTp")){
                                xpp.next();
                                charging.setCpTp(xpp.getText());
                            }
                            else if(tag.equals("csId")){
                                xpp.next();
                                charging.setCsId(xpp.getText());
                            }
                            else if(tag.equals("csNm")){
                                xpp.next();
                                charging.setCsNm(xpp.getText());
                            }
                            else if(tag.equals("lat")){
                                xpp.next();
                                charging.setLat(xpp.getText());
                            }
                            else if(tag.equals("longi")){
                                xpp.next();
                                charging.setLongi(xpp.getText());
                            }
                            else if(tag.equals("statUpdateDatetime")){
                                xpp.next();
                                charging.setStatUpdateDatetime(xpp.getText());
                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tag= xpp.getName(); //테그 이름 얻어오기

                            if(tag.equals("item")){
                                chargings.add(charging);
                            }
                            break;
                    }

                    eventType= xpp.next();
                }

            } catch (Exception e){
                e.printStackTrace();
            }
//        return buffer.toString();//StringBuffer 문자열 객체 반환


            return chargingArrayList;

        }

        @Override
        protected void onPostExecute(ArrayList<Charging> integer) {

            super.onPostExecute(integer);

            MarkerOptions markerOptions = new MarkerOptions();

            Log.d(TAG,"Tast size : "+chargings.size());
            for(int i = 0; i<chargings.size();i++){


                String cpTP = "";


                switch (Integer.parseInt(chargings.get(i).cpTp)){
                    case 1 :
                        cpTP="B타입(5핀)";
                    case 2:
                        cpTP="C타입(5핀)";
                    case 3:
                        cpTP="BC타입(5핀)";
                    case 4:
                        cpTP="BC타입(7핀)";
                    case 5:
                        cpTP="DC차데모";
                    case 6:
                        cpTP="AC3상";
                    case 7:
                        cpTP="DC콤보";
                    case 8:
                        cpTP="DC차데모+DC콤보";
                    case 9:
                        cpTP="DC차데모+AC3상";
                    case 10:
                        cpTP="DC차데모+DC콤보+AC3상";
                    default:{
                        break;
                    }
                }

                LatLng latLng = new LatLng(Double.parseDouble(chargings.get(i).lat), Double.parseDouble(chargings.get(i).longi));

                markerOptions.position(latLng);
                markerOptions.title("충전소의 주소 : "+chargings.get(i).addr);
                markerOptions.snippet("충전기 타입 : "+chargings.get(i).cpNm+"\n"
                        +"충전 방식 : " + cpTP + "\n");

                Log.d(TAG,"충전소의 주소 : "+chargings.get(i).addr+"\n"
                        +"충전기 타입 : "+chargings.get(i).cpNm+"\n"
                        +"충전 방식 : " + cpTP + "\n");

                mMap.addMarker(markerOptions);


            }
            
            Toast.makeText(getApplicationContext(), "파싱 완료", Toast.LENGTH_LONG).show();
        }
    }
}

