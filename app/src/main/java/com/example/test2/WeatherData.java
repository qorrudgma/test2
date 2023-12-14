package com.example.test2;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;


public class WeatherData {

    private static final String API_KEY = "RsJEn/IRe/ppOwVnkKo7GDBCSUkWKMo+iteJ8efWikPGqWMAcp9dzambo/24vgAfsTPWe3tIVSLRFKG1hInuBQ==";
    private static final String API_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0";
    private static final String BASE_DATE = "20231214";
    private static final String BASE_TIME = "0800";
    private static final String NX = "55";
    private static final String NY = "127";

    public interface WeatherCallback {
        void onWeatherReceived(String weatherInfo);
    }

    public void getWeather(WeatherCallback callback) {
        new WeatherAsyncTask(callback).execute();
    }

    private static class WeatherAsyncTask extends AsyncTask<Void, Void, Pair<String, String>> {


        private WeatherCallback callback;

        WeatherAsyncTask(WeatherCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Pair<String, String> doInBackground(Void... voids) {
            StringBuilder response = null;
            try {
                StringBuilder urlBuilder = new StringBuilder(API_URL + "/getVilageFcst");
                urlBuilder.append("?serviceKey=" + URLEncoder.encode(API_KEY, "UTF-8"));
                urlBuilder.append("&pageNo=1");
                urlBuilder.append("&numOfRows=1000");
                urlBuilder.append("&dataType=XML");
                urlBuilder.append("&base_date=" + URLEncoder.encode(BASE_DATE, "UTF-8"));
                urlBuilder.append("&base_time=" + URLEncoder.encode(BASE_TIME, "UTF-8"));
                urlBuilder.append("&nx=" + URLEncoder.encode(NX, "UTF-8"));
                urlBuilder.append("&ny=" + URLEncoder.encode(NY, "UTF-8"));


                URL url = new URL(urlBuilder.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                Log.d("url", "URL 정보:" + url);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                reader.close();
                Log.d("WeatherData", "API Response: " + response.toString());

                return new Pair<>("", response.toString());

            } catch (IOException e) {
                String errorMessage = "Error in URL connection: " + e.getMessage();
                Log.d("WeatherData", "API Response: " + response.toString());
                return new Pair<>(errorMessage, "Error: " + e.getMessage());
            }
        }
        @Override
        protected void onPostExecute(Pair<String, String> resultPair) {
            String urlError = resultPair.first;
            String result = resultPair.second;

            if (!urlError.isEmpty()) {
                Log.e("WeatherData", urlError);
                callback.onWeatherReceived(urlError);
                return;
            }

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(result));

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                        String category = null;
                        String fcstValue = null;

                        while (eventType != XmlPullParser.END_TAG || !"item".equals(parser.getName())) {
                            if (eventType == XmlPullParser.START_TAG) {
                                String tagName = parser.getName();

                                if ("category".equals(tagName)) {
                                    parser.next();
                                    category = parser.getText();
                                } else if ("fcstValue".equals(tagName)) {
                                    parser.next();
                                    fcstValue = parser.getText();
                                }
                            }
                            eventType = parser.next();
                        }

                        String weatherInfo = String.format(Locale.getDefault(), "Category: %s, Value: %s", category, fcstValue);
                        callback.onWeatherReceived(weatherInfo);
                        return; // Break from the loop once the first item is processed
                    }
                    eventType = parser.next();
                }
                // If no valid weather information is found
                Log.e("WeatherData", "No valid weather information found");
                callback.onWeatherReceived("No valid weather information found");
            } catch (XmlPullParserException | IOException e) {
                Log.e("WeatherData", "Error parsing XML: " + e.getMessage());
                callback.onWeatherReceived("Error parsing weather data");
            }
        }
    }
}
