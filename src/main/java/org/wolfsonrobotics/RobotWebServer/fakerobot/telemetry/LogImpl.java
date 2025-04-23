package org.wolfsonrobotics.RobotWebServer.fakerobot.telemetry;

import java.util.ArrayList;
import java.util.List;

// Mimic internal Telemetry implementation for easier porting to Control Hub SDK
public class LogImpl {

    private final List<String> entries = new ArrayList<>();

    public void add(String entry) {
        entries.add(entry);
    }
    public void display() {
//        entries.forEach(System.out::println);
    }
    public void markClean() {
        entries.clear();
    }

}
