package nsu.korneshchuk.services;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import nsu.korneshchuk.common.LocationContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class GetPlacesAndDescr implements Runnable {
    private final LocationContext locationContext;

    private final static String APIKey = "";

    public GetPlacesAndDescr(LocationContext locationContext) {
        this.locationContext = locationContext;
    }


    public JSONArray getPlaces() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.opentripmap.com/0.1/en/places/radius?" +
                        "radius=500" +
                        "&lon=" + locationContext.getSelectedLocation().point().lng() +
                        "&lat=" + locationContext.getSelectedLocation().point().lat() +
                        "&apikey=" + APIKey
                )
                .get()
                .build();

        Response response = client.newCall(request).execute();

        return new JSONObject(response.body().string()).getJSONArray("features");
    }
    public void getDescriptions(JSONArray features) throws IOException {
        OkHttpClient client = new OkHttpClient();

        System.out.println("");
        System.out.println("Interested places in this area:");
        System.out.println("_______________________________");
        for (Object feature : features) {
            JSONObject jsonFeature = (JSONObject)feature;

            String placeName = jsonFeature.getJSONObject("properties").getString("name");
            if (!placeName.equals("")) {
                Request request = new Request.Builder()
                        .url("https://api.opentripmap.com/0.1/en/places/xid/" +
                                jsonFeature.getJSONObject("properties").getString("xid") +
                                "?apikey=" + APIKey
                        )
                        .get()
                        .build();

                Response response = client.newCall(request).execute();

                JSONObject jsonObject = new JSONObject(response.body().string());
                if (jsonObject.has("kinds")) {
                    String description = jsonObject.getString("kinds");
                    System.out.println(placeName + " - " + description);
                }
            }
        }
    }
    public void makeRequest() throws IOException {
        JSONArray features = getPlaces();
        getDescriptions(features);
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
