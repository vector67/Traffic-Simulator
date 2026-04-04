package main;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal in-memory SimulationContext + VehicleRegistry for use in unit tests.
 * Provides a 3x3 grid of 100×100 buckets covering a 300×300 "screen".
 */
class FakeSimulationContext implements SimulationContext, VehicleRegistry {

    static final int WIDTH  = 300;
    static final int HEIGHT = 300;
    static final int BUCKET_SIZE = 100;

    final List<Vehicle> vehicles = new ArrayList<>();
    final List<Vehicle> removed  = new ArrayList<>();
    final Bucketset buckets;
    int delta = 16; // ~60 fps

    FakeSimulationContext() {
        int cols = WIDTH  / BUCKET_SIZE;
        int rows = HEIGHT / BUCKET_SIZE;
        buckets = new Bucketset(cols, rows, WIDTH, HEIGHT);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double cx = col * BUCKET_SIZE + BUCKET_SIZE / 2.0;
                double cy = row * BUCKET_SIZE + BUCKET_SIZE / 2.0;
                buckets.add(new Bucket(cx, cy, BUCKET_SIZE, BUCKET_SIZE));
            }
        }
    }

    // SimulationContext
    @Override public int getDelta()        { return delta; }
    @Override public int getScreenWidth()  { return WIDTH; }
    @Override public int getScreenHeight() { return HEIGHT; }
    @Override public Bucketset getBuckets(){ return buckets; }
    @Override public void removeVehicle(Vehicle v) { removed.add(v); }

    // VehicleRegistry
    @Override public void addVehicle(Vehicle v) { vehicles.add(v); }
    @Override public List<Vehicle> getVehicles() { return vehicles; }
}
