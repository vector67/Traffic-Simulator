package main;

import java.util.ArrayList;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Transform;

public class Factory {
    double x, y;
    double direction;
    double frequency;
    ArrayList<Vehicle> modelvehicles;
    int width = 10, height = 10;
    SimulationContext sim;
    VehicleRegistry registry;
    int amount;
    double timesincecreate;
    Waypoints wp;
    int curid;

    public Factory(SimulationContext sim1, VehicleRegistry registry1, Waypoints wp1) {
        modelvehicles = new ArrayList<Vehicle>();
        this.sim = sim1;
        this.registry = registry1;
        wp = wp1;
        curid = 0;
        amount = 10000;
    }

    public void update() {
        int delta = sim.getDelta();
        timesincecreate += ((double) delta) / 1000.0;
        int numcars = (int) (timesincecreate / frequency);
        timesincecreate -= numcars * frequency;
        if (timesincecreate < 0) timesincecreate = 0;

        if (numcars != 0 && !(amount <= 0)) {
            Vehicle v;
            if (!isColliding()) {
                for (int i = 0; i < numcars; i++) {
                    v = modelvehicles.get((int) Math.random() * modelvehicles.size());
                    if (v.getClass() == (new Car(v)).getClass()) {
                        registry.addVehicle(new Car(x, y, v.w, v.h, direction, v.maxspeed, v.col, wp, sim, v.steer, v.currentnode, curid));
                    } else {
                        registry.addVehicle(new Bus(x, y, v.w, v.h, direction, v.maxspeed, v.col, wp, sim, v.steer, v.currentnode, curid));
                    }
                    curid++;
                    amount--;
                }
            }
        }
    }

    public boolean isColliding() {
        Rectangle us = new Rectangle((float) x, (float) y, width, height);
        for (Vehicle b : registry.getVehicles()) {
            Rectangle them = new Rectangle((float) b.x, (float) b.y, (float) b.w, (float) b.h);
            them.transform(Transform.createRotateTransform(((float) b.angle)));
            if (them.intersects(us)) return true;
        }
        return false;
    }
}
