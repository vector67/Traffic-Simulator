package main;

/**
 * The narrow contract that logic classes (Vehicle, Factory) need from the
 * simulation.  Contains no rendering or LWJGL references.
 */
public interface SimulationContext {
    int getDelta();
    int getScreenWidth();
    int getScreenHeight();
    Bucketset getBuckets();
    void removeVehicle(Vehicle v);
}
