package nsu.korneshchuk.services;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import nsu.korneshchuk.common.LocationInfo;
import nsu.korneshchuk.common.LocationContext;
import nsu.korneshchuk.common.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class GetLocation implements Runnable {
    private final LocationContext locationContext;
    private static final String APIKey = "07813426-c567-4088-b25f-de54caff7e39";

    public GetLocation(LocationContext locationContext) {
        this.locationContext = locationContext;
    }

    public void makeRequest(String location) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://graphhopper.com/api/1/geocode?q=" + location + "&key=" + APIKey)
                .get()
                .build();

        Response response = client.newCall(request).execute();

        JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("hits");

        int idx = 1;
        for (Object o : jsonArray) {
            JSONObject jsonsObject = (JSONObject) o;

            double lng = jsonsObject.getJSONObject("point").getDouble("lng");
            double lat = jsonsObject.getJSONObject("point").getDouble("lat");

            String name = jsonsObject.getString("name");
            String country = jsonsObject.getString("country");

            this.locationContext.getMap().put(idx, new LocationInfo(name, new Point(lng, lat)));
            System.out.println(idx + ") " + name + " " + country + " " + lng + " " + lat);
            idx++;
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter location: ");

        String location = scanner.nextLine();
        try {
            makeRequest(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
