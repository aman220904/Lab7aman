package com.example.lab7;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private EditText cityTextField;
    private Button forecastButton;
    private TextView weatherDescription;
    private TextView temperature;
    private ImageView weatherIcon;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        cityTextField = findViewById(R.id.cityTextField);
        forecastButton = findViewById(R.id.forecastButton);
        weatherDescription = findViewById(R.id.weatherDescription);
        temperature = findViewById(R.id.temperature);
        weatherIcon = findViewById(R.id.weatherIcon);  // ImageView for the icon

        // Set up Volley RequestQueue
        queue = Volley.newRequestQueue(this);

        // Set up button click listener
        forecastButton.setOnClickListener(v -> {
            String cityName = cityTextField.getText().toString().trim();

            if (!cityName.isEmpty()) {
                try {
                    String encodedCityName = URLEncoder.encode(cityName, "UTF-8");
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&appid=7e943c97096a9784391a981c4d878b22&units=metric";

                    // Make the weather API request
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            response -> {
                                // Process the JSON response
                                try {
                                    // Extract weather details from the JSON response
                                    JSONArray weatherArray = response.getJSONArray("weather");
                                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                                    String description = weatherObject.getString("description");
                                    String iconCode = weatherObject.getString("icon");

                                    // Get temperature from the main object
                                    JSONObject mainObject = response.getJSONObject("main");
                                    double currentTemp = mainObject.getDouble("temp");

                                    // Update UI on the main thread
                                    runOnUiThread(() -> {
                                        weatherDescription.setText("Weather: " + description);
                                        temperature.setText("Temperature: " + currentTemp + "°C");

                                        // Load the weather icon image
                                        String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + ".png";
                                        loadWeatherIcon(iconUrl);
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> {
                                // Handle error
                                runOnUiThread(() -> {
                                    weatherDescription.setText("Error fetching data");
                                    temperature.setText("");
                                    weatherIcon.setImageResource(0);  // Reset icon on error
                                });
                            });

                    // Add the request to the queue
                    queue.add(request);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                weatherDescription.setText("Please enter a city.");
                temperature.setText("");
                weatherIcon.setImageResource(0);  // Reset icon if no city entered
            }
        });
    }

    // Helper method to load the weather icon
    private void loadWeatherIcon(String iconUrl) {
        ImageRequest imageRequest = new ImageRequest(iconUrl, new com.android.volley.Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                // Set the image to the ImageView
                weatherIcon.setImageBitmap(response);
            }
        }, 0, 0, null, null, error -> {
            // Handle error if the image loading fails
            weatherIcon.setImageResource(0);  // Reset to a default image or empty
        });

        // Add image request to the queue
        queue.add(imageRequest);
    }
}
