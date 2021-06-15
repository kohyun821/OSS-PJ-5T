package com.example.exam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
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

        ProgressTask task = new ProgressTask();
        task.execute("Start");

        LatLng SEOUL = new LatLng(37.56, 126.97);
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            }
        });
        mMap.setOnMarkerClickListener(this);
    }

    //보고 있는 위치
    public void onViewLocationButtonClicked(View view){
        int scale=0;
        Location location = new Location("");
        LatLng latLng = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        //= mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
        mMap.addMarker(new MarkerOptions().position(latLng).title("구글맵 중심"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        //맵 초기화
        mMap.clear();

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        MarkerOptions marker = new MarkerOptions();
        for(int i=0 ;i<chargings.size();i++){
            Location targetLocation = new Location("");
            targetLocation.setLatitude(Double.parseDouble(chargings.get(i).getLat()));
            targetLocation.setLongitude(Double.parseDouble(chargings.get(i).getLongi()));
            float distance = location.distanceTo(targetLocation) /1000;
            if(distance<=10){
                LatLng latLng2 = new LatLng(Double.parseDouble(chargings.get(i).lat), Double.parseDouble(chargings.get(i).longi));
                marker.position(latLng2);
                marker.title("충전소의 주소 : "+chargings.get(i).addr);
                marker.snippet("충전기 타입 : "+chargings.get(i).cpNm+"\n"
                        +"충전 방식 : " + chargings.get(i).getCpTp() + "\n"
                        +"충전 가능 여부 : "+chargings.get(i).getCpStat()+"\n"
                        +"마지막 갱신 시각 : "+chargings.get(i).getStatUpdateDatetime()+"\n");
                mMap.addMarker(marker);
                scale++;

            }
        }
        if(scale<=0){
            Toast.makeText(this,"근처에 충전소가 위치해 있지 않습니다.",Toast.LENGTH_SHORT).show();
        }
        if(scale>0){
            Toast.makeText(this,scale+"개의 충전소 발견!",Toast.LENGTH_SHORT).show();
        }

    }

    //내 위치 확인!
    public void onLastLocationButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSONS);
            return;
        }
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                ArrayList<Charging> tradArr = new ArrayList<Charging>();
                if(location != null){
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("현재 위치"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                    //맵 초기화
                    mMap.clear();

                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    MarkerOptions marker = new MarkerOptions();

                    for(int i=0 ;i<chargings.size();i++){
                        Location targetLocation = new Location("");
                        targetLocation.setLatitude(Double.parseDouble(chargings.get(i).getLat()));
                        targetLocation.setLongitude(Double.parseDouble(chargings.get(i).getLongi()));
                        float distance = location.distanceTo(targetLocation) /1000;
                        if(distance<=10){
                            LatLng latLng = new LatLng(Double.parseDouble(chargings.get(i).lat), Double.parseDouble(chargings.get(i).longi));
                            marker.position(latLng);
                            marker.title("충전소의 주소 : "+chargings.get(i).addr);
                            marker.snippet("충전기 타입 : "+chargings.get(i).cpNm+"\n"
                                    +"충전 방식 : " + chargings.get(i).getCpTp() + "\n"
                                    +"충전 가능 여부 : "+chargings.get(i).getCpStat()+"\n"
                                    +"마지막 갱신 시각 : "+chargings.get(i).getStatUpdateDatetime()+"\n");
                            mMap.addMarker(marker);
                        }
                    }
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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("충전소 상세 설명").setMessage(marker.getTitle()+"\n"+marker.getSnippet());


        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Toast.makeText(getApplicationContext(), "확인 하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //Toast.makeText(this, marker.getTitle() +"\n"+marker.getSnippet(),Toast.LENGTH_SHORT).show();
        return true;
    }


    class ProgressTask extends AsyncTask<String, Integer, ArrayList<Charging>>{
        String key="us2ROEBp96Lg%2F%2FFoXYqqzHC3S1TgYQQFrruf%2FbjcENdpvC3PsZnoWsV1jb8VJLuNXorx%2BL75uwFTfFLSj2bI8Q%3D%3D";
        GoogleMap googleMap;
        String TAG = "AsyncTask";
        @Override
        protected ArrayList<Charging> doInBackground(String... strings) {
            Log.d(TAG,"AsyncTask시작");
            ArrayList<Charging> chargingArrayList = new ArrayList<Charging>();
            StringBuffer buffer=new StringBuffer();
//        String str= edit.getText().toString();//EditText에 작성된 Text얻어오기
//        String location = URLEncoder.encode(str);

            String queryUrl="http://openapi.kepco.co.kr/service/EvInfoServiceV2/getEvSearchList?pageNo=1&numOfRows=3000&ServiceKey="+key;
            try{
                Log.d(TAG,"파싱 시작 ");

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
            return chargingArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Charging> integer) {

            super.onPostExecute(integer);

            MarkerOptions markerOptions = new MarkerOptions();

            Log.d(TAG,"Tast size : "+chargings.size());
            for(int i = 0; i<chargings.size();i++){


                if(chargings.get(i).cpTp.equals("1")){
                    chargings.get(i).setCpTp("B타입(5핀)");
                }else if(chargings.get(i).cpTp.equals("2")){
                   chargings.get(i).setCpTp("C타입(5핀)");
                }
                else if(chargings.get(i).cpTp.equals("3")){
                    chargings.get(i).setCpTp("BC타입(5핀)");
                }
                else if(chargings.get(i).cpTp.equals("4")){
                    chargings.get(i).setCpTp("BC타입(7핀)");
                }
                else if(chargings.get(i).cpTp.equals("5")){
                    chargings.get(i).setCpTp("DC차데모");
                }
                else if(chargings.get(i).cpTp.equals("6")){
                    chargings.get(i).setCpTp("AC3상");
                }
                else if(chargings.get(i).cpTp.equals("7")){
                    chargings.get(i).setCpTp("DC콤보");
                }
                else if(chargings.get(i).cpTp.equals("8")){
                    chargings.get(i).setCpTp("DC차데모+DC콤보");
                }
                else if(chargings.get(i).cpTp.equals("9")){
                    chargings.get(i).setCpTp("DC차데모+AC3상");
                }
                else if(chargings.get(i).cpTp.equals("10")){
                    chargings.get(i).setCpTp("DC차데모+DC콤보+AC3상");
                }else{
                    Log.d(TAG,"일어날 리 없음.");
                }

                if(chargings.get(i).cpStat.equals("1")){
                    chargings.get(i).setCpStat("충전가능");
                }else if(chargings.get(i).cpStat.equals("2")){
                    chargings.get(i).setCpStat("충전중");
                }
                else if(chargings.get(i).cpStat.equals("3")){
                    chargings.get(i).setCpStat("고장/점검");
                }
                else if(chargings.get(i).cpStat.equals("4")){
                    chargings.get(i).setCpStat("통신장애");
                }else{
                    chargings.get(i).setCpStat("통신미연결");
                }
                Log.d(TAG,"테스트 : cptp : "+chargings.get(i).cpTp);
                Log.d(TAG,"테스트 : cpStat : "+chargings.get(i).cpStat);

                LatLng latLng = new LatLng(Double.parseDouble(chargings.get(i).lat), Double.parseDouble(chargings.get(i).longi));

                markerOptions.position(latLng);
                markerOptions.title("충전소의 주소 : "+chargings.get(i).addr);
                markerOptions.snippet("충전기 타입 : "+chargings.get(i).cpNm+"\n"
                        +"충전 방식 : " + chargings.get(i).getCpTp() + "\n"
                +"충전 가능 여부 : "+chargings.get(i).getCpStat()+"\n"
                +"마지막 갱신 시각 : "+chargings.get(i).getStatUpdateDatetime()+"\n");

                mMap.addMarker(markerOptions);


            }

            Toast.makeText(getApplicationContext(), "파싱 완료", Toast.LENGTH_LONG).show();
        }
    }
}