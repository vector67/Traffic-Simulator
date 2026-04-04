package main;

import java.awt.Color;

public class Car extends Vehicle {

    public Car(double x1, double y1, int w1, int h1, double a1, double s1,
               Color col, Waypoints wp, SimulationContext sim1,
               Node currentnode1, double steer) {
        super(x1, y1, w1, h1, a1, s1, col, wp, sim1, steer, currentnode1);
    }

    public Car(double x1, double y1, int w1, int h1, double a1, double s1,
               Color col, Waypoints wp, SimulationContext sim1,
               double steer, Node currentnode1, int id) {
        super(x1, y1, w1, h1, a1, s1, col, wp, sim1, steer, currentnode1, id);
    }

    public Car(Vehicle c) {
        super(c);
    }
}
