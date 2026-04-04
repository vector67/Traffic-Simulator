package main;

import java.awt.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {

    private FakeSimulationContext ctx;
    private Waypoints wp;
    private Node nodeA;
    private Node nodeB;
    private Car car;

    @BeforeEach
    public void setUp() {
        ctx = new FakeSimulationContext();

        // Two-node route: A(50,50) → B(250,50) — well inside the 300×300 screen
        nodeA = new Node(50.0, 50.0, 1);
        nodeB = new Node(250.0, 50.0, 2);
        nodeA.addChild(nodeB);

        wp = new Waypoints();
        wp.add(1, nodeA);
        wp.add(2, nodeB);

        // Start at nodeA, heading east (angle 0)
        car = new Car(50.0, 50.0, 10, 5, 0.0, 100.0,
                      new Color(1f, 0f, 0f), wp, ctx, nodeA, 50);
    }

    // --- SimulationContext no longer comes from Start ---

    @Test
    public void testVehicleDoesNotReferenceStart() {
        // Vehicle.sim must be a SimulationContext, not a Start
        assertNotNull(car.sim);
        assertFalse(car.sim instanceof Start,
            "Vehicle.sim should be typed as SimulationContext, not Start");
    }

    // --- Position advances each tick ---

    @Test
    public void testPositionAdvancesAfterUpdate() {
        double xBefore = car.x;
        car.update();
        // Car is heading east; x should increase
        assertTrue(car.x > xBefore || car.y != 50.0,
            "Vehicle should have moved after update()");
    }

    @Test
    public void testSpeedIncreasesFromZero() {
        // Car starts nearly stationary (speed=0.1); after a tick it should have accelerated
        double speedBefore = car.speed;
        car.update();
        assertTrue(car.speed >= speedBefore, "Speed should not decrease on open road");
    }

    // --- Screen-wrap ---
    // Note: the first call to update() initialises the spatial bucket (begin=true).
    // Screen-wrap only runs after that, so we do one priming tick first.

    @Test
    public void testScreenWrapRight() {
        car.update(); // prime: places car in a bucket
        car.x = FakeSimulationContext.WIDTH + 1;
        car.update();
        assertTrue(car.x <= FakeSimulationContext.WIDTH,
            "Vehicle beyond right edge should wrap to left");
    }

    @Test
    public void testScreenWrapBottom() {
        car.update(); // prime
        car.y = FakeSimulationContext.HEIGHT + 1;
        car.update();
        assertTrue(car.y <= FakeSimulationContext.HEIGHT,
            "Vehicle beyond bottom edge should wrap to top");
    }

    @Test
    public void testScreenWrapLeft() {
        car.update(); // prime
        car.x = -1;
        car.update();
        assertTrue(car.x >= 0,
            "Vehicle beyond left edge should wrap to right");
    }

    // --- End-of-route removal ---

    @Test
    public void testRemoveVehicleCalledAtEndOfRoute() {
        // Move car directly to nodeB (the terminal node which has no children)
        car.x = nodeB.x;
        car.y = nodeB.y;
        car.currentnode = nodeB;
        car.update();
        assertTrue(ctx.removed.contains(car),
            "Vehicle at terminal node should be queued for removal");
    }

    // --- Bucket assignment ---

    @Test
    public void testVehiclePlacedInBucketOnFirstUpdate() {
        car.update();
        assertNotNull(car.currentbucket, "Vehicle should have a currentbucket after first update");
    }

    // --- getDelta delegation ---

    @Test
    public void testDeltaUsedFromSimulationContext() {
        ctx.delta = 100; // 100 ms tick
        double xBefore = car.x;
        car.update();
        // With a larger delta the vehicle should move further than with delta=16
        ctx.delta = 16;
        Car car2 = new Car(50.0, 50.0, 10, 5, 0.0, 100.0,
                           new Color(1f, 0f, 0f), wp, ctx, nodeA, 51);
        car2.x = 50.0;
        double xBefore2 = car2.x;
        car2.update();
        // car moved more than car2 because delta was larger for car
        assertTrue(Math.abs(car.x - xBefore) >= Math.abs(car2.x - xBefore2),
            "Larger delta should produce greater displacement");
    }
}
