package ru.nsu.ccfit.korneshchuk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.korneshchuk.socks.SOCKSProxy;

import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        startProxy(8080);
    }

    private static void startProxy(int port) {
        try (SOCKSProxy proxy = new SOCKSProxy(port)) {
            logger.info("Proxy starts!");
            proxy.start();
        } catch (IOException e) {
            logger.error("Proxy close exception", e);
        }
    }
}