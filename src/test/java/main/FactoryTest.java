package main;

import java.awt.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FactoryTest {

    private FakeSimulationContext ctx;
    private Waypoints wp;
    private Node nodeA;
    private Node nodeB;
    private Factory factory;

    @BeforeEach
    public void setUp() {
        ctx = new FakeSimulationContext();

        nodeA = new Node(50.0, 50.0, 1);
        nodeB = new Node(250.0, 50.0, 2);
        nodeA.addChild(nodeB);

        wp = new Waypoints();
        wp.add(1, nodeA);
        wp.add(2, nodeB);

        factory = new Factory(ctx, ctx, wp);
        factory.x = 50.0;
        factory.y = 50.0;
        factory.direction = 0.0;
        factory.frequency = 0.5; // spawn every 0.5 s

        // Add a Car model vehicle
        factory.modelvehicles.add(
            new Car(0, 0, 10, 5, 0, 88, new Color(0.8f, 0.8f, 0.2f), wp, ctx, nodeA, 50));
    }

    // --- Factory does not reference Start ---

    @Test
    public void testFactoryDoesNotReferenceStart() {
        assertNotNull(factory.sim);
        assertFalse(factory.sim instanceof Start,
            "Factory.sim should be typed as SimulationContext, not Start");
        assertNotNull(factory.registry);
        assertFalse(factory.registry instanceof Start,
            "Factory.registry should be typed as VehicleRegistry, not Start");
    }

    // --- No vehicle spawned before enough time passes ---

    @Test
    public void testNoVehicleSpawnedBelowFrequency() {
        // delta=16ms means timesincecreate += 0.016 per tick; frequency=0.5 s
        // → needs ~32 ticks to spawn one vehicle
        factory.update();
        assertTrue(ctx.vehicles.isEmpty(),
            "No vehicle should be spawned after a single 16ms tick");
    }

    // --- Vehicle spawned after sufficient simulated time ---

    @Test
    public void testVehicleSpawnedAfterEnoughTime() {
        // Simulate 600 ms worth of ticks (frequency=0.5, so at least 1 spawn)
        ctx.delta = 600;
        factory.update();
        assertFalse(ctx.vehicles.isEmpty(), "A vehicle should have been spawned after 600ms tick");
    }

    // --- Spawn is blocked when location is occupied ---

    @Test
    public void testSpawnBlockedWhenColliding() {
        // Place an existing vehicle directly at the factory spawn point
        Car blocker = new Car(factory.x, factory.y, 10, 5, 0, 88,
                              new Color(1f, 0f, 0f), wp, ctx, nodeA, 99);
        ctx.vehicles.add(blocker);

        ctx.delta = 600; // enough time to normally spawn
        factory.update();
        // Only the blocker should be in the list; no new vehicle added
        assertEquals(1, ctx.vehicles.size(),
            "Spawn should be blocked when the spawn point is occupied");
    }

    // --- isColliding detects occupied spawn point ---

    @Test
    public void testIsColliding_falseWhenEmpty() {
        assertFalse(factory.isColliding());
    }

    @Test
    public void testIsColliding_trueWhenVehicleAtSpawnPoint() {
        Car blocker = new Car(factory.x, factory.y, 10, 5, 0, 88,
                              new Color(1f, 0f, 0f), wp, ctx, nodeA, 99);
        ctx.vehicles.add(blocker);
        assertTrue(factory.isColliding());
    }

    // --- Respects amount limit ---

    @Test
    public void testAmountLimitHonoured() {
        factory.amount = 1;
        ctx.delta = 600;
        factory.update(); // spawns 1
        ctx.delta = 600;
        factory.update(); // amount is now 0, should not spawn
        assertEquals(1, ctx.vehicles.size(),
            "Factory should not spawn more than its amount limit");
    }
}
