package com.skyview.wtw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {

    final int REQUEST_CODE = 123;

    final String weather_URL = "http://api.openweathermap.org/data/2.5/weather";
    final String App_ID = "3d6fcd521e23fcd550a8b19824186c96";
    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTempratureLabel;






    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        mCityLabel = findViewById(R.id.locationTV);
        mWeatherImage = findViewById(R.id.weatherSymbolTv);
        mTempratureLabel = findViewById(R.id.tempTv);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this , ChangeCityController.class);
                startActivity(myIntent);

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent myNewIntent = getIntent();
        String newCity = myNewIntent.getStringExtra("city");
        if(newCity!=null){
            getWeatherForNewCity(newCity);
        } else {
            getWeatherForCUrrentLocation();
        }
    }
    private void getWeatherForNewCity(String city){
        RequestParams param = new RequestParams();
        param.put("q" , city);
        param.put("appid" , App_ID);
        letsDoSomeNetworning(param);

    }

    private void getWeatherForCUrrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //instance of location through location service
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());
            RequestParams params = new RequestParams();
            params.put("lat", latitude);
            params.put("long", longitude);
            params.put("AppID", App_ID);
            letsDoSomeNetworning(params);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                getWeatherForCUrrentLocation();
            }
            else {
                Log.d("WTW", "Permision Denied");
            }
        }
    }


    private  void letsDoSomeNetworning(RequestParams params){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(weather_URL,params, new  JsonHttpResponseHandler () {
            @Override
            public  void onSuccess(int statusCode , Header[] headers, JSONObject response){
                Log.d("WTW", "Success Json" + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);
            }
            @Override
            public void onFailure(int statusCode ,Header[] header ,  Throwable e , JSONObject response){
                Log.e("WTW" , "Fail" + e.toString());
                Log.d("WTW" , "StstusCode" + statusCode);
                Toast.makeText(WeatherController.this , "RequestFailed" , Toast.LENGTH_SHORT).show();
            }
        });
    }
    private  void updateUI(WeatherDataModel weather){
        mTempratureLabel.setText(weather.getTempreature());
        mCityLabel.setText(weather.getCity());
        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable" , getPackageCodePath());
        mWeatherImage.setImageResource(resourceID);
    };

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null) mLocationManager.removeUpdates(mLocationListener);
    }
}
