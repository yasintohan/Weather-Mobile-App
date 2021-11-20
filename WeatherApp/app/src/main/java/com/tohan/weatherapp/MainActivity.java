package com.tohan.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

    private ConstraintLayout mainLayout;

    private String tempUrl = "";
    private String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
    private String apiKey = "c252c4bf78f6f01306be4fcf8401292c";
    DecimalFormat df = new DecimalFormat("#.#");
    private ResultObject resultObject = null;

    private String currentLocation = "";

    private TextInputEditText searchEditTxt;

    private MaterialTextView cityText;
    private MaterialTextView tempText;
    private MaterialTextView weatherText;
    private MaterialTextView dateText;
    private MaterialTextView timeText;

    private ImageView weatherIcon;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private SwitchMaterial darkModeSwitch;
    private boolean isDarkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        isDarkMode = sharedPref.getBoolean("isDarkMode", false);
        if (isDarkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_main);

        initState();

        locationService();
        getData(currentLocation);

        searchEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                getData(editable.toString());
            }
        });

    }

    void initState() {

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        mainLayout = findViewById(R.id.mainLayout);

        searchEditTxt = findViewById(R.id.search_edittext);
        cityText = findViewById(R.id.cityText);
        tempText = findViewById(R.id.celsiusText);
        weatherText = findViewById(R.id.weathertypeText);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);

        weatherIcon = findViewById(R.id.weatherIcon);

        darkModeSwitch = findViewById(R.id.dark_mode_switch);

        if (isDarkMode)
            darkModeSwitch.setChecked(true);

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    isDarkMode = true;
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    isDarkMode = false;
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }


                editor.putBoolean("isDarkMode", isDarkMode);
                editor.commit();

                getData(searchEditTxt.getText().toString().trim());
            }
        });
    }

    void getData(String city) {

        tempUrl = baseUrl + "?q=" + city + "&appid=" + apiKey;

        Log.i("deneme", tempUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("deneme", response);

                String jsonString = response; //http request
                ResultObject data = new ResultObject();
                Gson gson = new Gson();
                data = gson.fromJson(jsonString, ResultObject.class);
                resultObject = data;

                cityText.setText(data.name);
                Double temp = data.main.temp - 272.15;
                tempText.setText(df.format(temp) + " \u2103");

                weatherText.setText(data.weather.get(0).main);


                weatherIcon.setImageResource(weatherIconId(data.weather.get(0).main));

                dateText.setText(getTimePattern(data.timezone, "MMMM"));
                timeText.setText(getTimePattern(data.timezone, "hh:mm a"));


                dayNightTest(data.sys.sunset);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("deneme", error.toString().trim());
                resultObject = null;
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public String getTimePattern(int timezone, String pattern) {
        TimeZone tz = TimeZone.getDefault();
        tz.setRawOffset(timezone * 1000);

        Calendar c = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        format.setTimeZone(tz);
        String time = format.format(c.getTime());


        return time;
    }

    public void dayNightTest(long sunset) {
        Calendar c = Calendar.getInstance();
        Window window = getWindow();

        if (c.getTimeInMillis() > sunset * 1000) {
            if (isDarkMode) {
                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusbardark));
                mainLayout.setBackgroundResource(R.drawable.bg3_night);
            } else {
                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusbar_night));
                mainLayout.setBackgroundResource(R.drawable.bg3);
            }

        } else {
            if (isDarkMode) {
                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.statusbardark));
                mainLayout.setBackgroundResource(R.drawable.bg2_night);
            } else {
                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                mainLayout.setBackgroundResource(R.drawable.bg2);
            }}


    }

    private int weatherIconId(String type) {
        switch (type) {
            case "Rain":
                return R.drawable.rainy;

            case "Snow":
                return R.drawable.snowy;

            case "Clear":
                return R.drawable.sunny;

            case "Clouds":
                return R.drawable.cloud;

            case "Thunderstorm":
                return R.drawable.thunderstorm;

            case "Drizzle":
                return R.drawable.rainy;

            default:
                return R.drawable.atmosphere;

        }
    }


    private void locationService() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 22);
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 23);

        }
        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();
        if (null != locations && null != providerList && providerList.size() > 0) {
            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    //String _Location = listAddresses.get(0).getAddressLine(0);
                    currentLocation = listAddresses.get(0).getCountryName();

                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Error", e.toString());
            }

        }

    }

}