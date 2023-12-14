package com.example.test2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;

import com.example.test2.WeatherData;

public class MainActivity extends AppCompatActivity implements WeatherData.WeatherCallback {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton imageButton = findViewById(R.id.imageButton);


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeatherData weatherData = new WeatherData();
                Log.d("WeatherData", "Image button clicked. Executing WeatherAsyncTask");

                weatherData.getWeather(MainActivity.this);
            }
        });
    }

    @Override
    public void onWeatherReceived(String weatherInfo) {
        TextView textView = findViewById(R.id.weatherTextView);
        textView.setText(weatherInfo);

    }

}
