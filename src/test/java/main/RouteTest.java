package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RouteTest {

    private Node a, b, c;
    private Route routeAB;

    @BeforeEach
    public void setUp() {
        a = new Node(0.0,   0.0, 1);
        b = new Node(100.0, 0.0, 2);
        c = new Node(200.0, 0.0, 3);
        routeAB = new Route(a, b);
    }

    // --- equals ---

    @Test
    public void testEquals_sameNodeInstances() {
        Route other = new Route(a, b);
        assertEquals(routeAB, other);
    }

    @Test
    public void testEquals_differentEnd() {
        Route other = new Route(a, c);
        assertNotEquals(routeAB, other);
    }

    @Test
    public void testEquals_differentStart() {
        Route other = new Route(c, b);
        assertNotEquals(routeAB, other);
    }

    @Test
    public void testEquals_reflexive() {
        assertEquals(routeAB, routeAB);
    }

    @Test
    public void testEquals_notEqualToNonRoute() {
        assertNotEquals(routeAB, "not a route");
    }

    @Test
    public void testEquals_symmetric() {
        Route other = new Route(a, b);
        assertEquals(routeAB, other);
        assertEquals(other, routeAB);
    }
}
