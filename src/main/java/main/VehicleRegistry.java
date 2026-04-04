package main;

import java.util.List;

/**
 * Allows Factory to add vehicles to the simulation and read the live vehicle
 * list for spawn-collision checking, without depending on Start directly.
 */
public interface VehicleRegistry {
    void addVehicle(Vehicle v);
    List<Vehicle> getVehicles();
}
