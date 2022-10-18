package nsu.korneshchuk;

import nsu.korneshchuk.common.LocationContext;
import nsu.korneshchuk.services.GetLocation;
import nsu.korneshchuk.services.GetPlacesAndDescr;
import nsu.korneshchuk.services.GetWeather;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        LocationContext locationContext = new LocationContext(new HashMap<>());

        CompletableFuture<Void> completableFuture = CompletableFuture
                .runAsync(new GetLocation(locationContext))
                .thenRunAsync(new GetWeather(locationContext))
                .thenRunAsync(new GetPlacesAndDescr(locationContext));

        completableFuture.get();
    }
}