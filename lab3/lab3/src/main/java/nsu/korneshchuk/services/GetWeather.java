package nsu.korneshchuk.services;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import nsu.korneshchuk.common.LocationContext;
import nsu.korneshchuk.common.LocationInfo;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

public class GetWeather implements Runnable {
    private final LocationContext locationContext;

    private static final String APIKey = "";

    public GetWeather(LocationContext locationContext) {
        this.locationContext = locationContext;
    }

    public void makeRequest() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/weather?" +
                        "lat=" + this.locationContext.getSelectedLocation().point().lat() +
                        "&lon=" + this.locationContext.getSelectedLocation().point().lng() +
                        "&units=metric" +
                        "&appid=" + APIKey)
                .get()
                .build();

        Response response = client.newCall(request).execute();

        JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("main");

        double temp = jsonObject.getDouble("temp");
        double feelsLike = jsonObject.getDouble("feels_like");
        System.out.println("");
        System.out.println("Temperature in " + this.locationContext.getSelectedLocation().name() + ": " + temp + " °C, feels like " + feelsLike + " °C");
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nPlease choose in which one you interested: ");
        int idx = scanner.nextInt();
        LocationInfo location = this.locationContext.getMap().get(idx);
        this.locationContext.setSelectedLocation(location);
        try {
            makeRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
