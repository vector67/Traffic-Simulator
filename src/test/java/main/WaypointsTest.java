package main;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WaypointsTest {

    // Resolve test map via classpath (Gradle copies src/test/resources to build/resources/test)
    private static final String TEST_MAP =
        WaypointsTest.class.getClassLoader().getResource("test_map.txt").getFile();

    // --- Empty Waypoints ---

    @Test
    public void testEmptyWaypoints_isEmpty() {
        Waypoints wp = new Waypoints();
        assertTrue(wp.isEmpty());
    }

    @Test
    public void testEmptyWaypoints_sizeIsZero() {
        Waypoints wp = new Waypoints();
        assertEquals(0, wp.size());
    }

    // --- add and get ---

    @Test
    public void testAdd_byIdAndGet() {
        Waypoints wp = new Waypoints();
        Node n = new Node(5.0, 10.0, 42);
        wp.add(42, n);
        assertSame(n, wp.get(42));
    }

    @Test
    public void testAdd_sizeIncreases() {
        Waypoints wp = new Waypoints();
        wp.add(1, new Node(0.0, 0.0, 1));
        wp.add(2, new Node(1.0, 0.0, 2));
        assertEquals(2, wp.size());
    }

    @Test
    public void testGet_missingId_returnsNull() {
        Waypoints wp = new Waypoints();
        assertNull(wp.get(999));
    }

    // --- clear ---

    @Test
    public void testClear_makesEmpty() {
        Waypoints wp = new Waypoints();
        wp.add(1, new Node(0.0, 0.0, 1));
        wp.clear();
        assertTrue(wp.isEmpty());
    }

    // --- File loading ---

    @Test
    public void testLoadFromFile_correctNodeCount() {
        Waypoints wp = new Waypoints(TEST_MAP);
        // test_map.txt has 3 nodes with IDs 1, 2, 3
        assertEquals(3, wp.size());
    }

    @Test
    public void testLoadFromFile_nodeCoordinates() {
        Waypoints wp = new Waypoints(TEST_MAP);
        Node n1 = wp.get(1);
        assertNotNull(n1);
        assertEquals(0.0,   n1.x, 1e-9);
        assertEquals(0.0,   n1.y, 1e-9);

        Node n2 = wp.get(2);
        assertNotNull(n2);
        assertEquals(100.0, n2.x, 1e-9);
        assertEquals(0.0,   n2.y, 1e-9);

        Node n3 = wp.get(3);
        assertNotNull(n3);
        assertEquals(200.0, n3.x, 1e-9);
        assertEquals(0.0,   n3.y, 1e-9);
    }

    @Test
    public void testLoadFromFile_nodeConnectivity() {
        Waypoints wp = new Waypoints(TEST_MAP);
        Node n1 = wp.get(1);
        Node n2 = wp.get(2);
        Node n3 = wp.get(3);

        // n1 → n2 → n3 (children direction)
        assertTrue(n1.children.contains(n2), "Node 1 should have Node 2 as child");
        assertTrue(n2.children.contains(n3), "Node 2 should have Node 3 as child");
    }

    @Test
    public void testLoadFromFile_missingFile_doesNotThrow() {
        // Should print "file not found" but not throw
        assertDoesNotThrow(() -> new Waypoints("nonexistent_file.txt"));
    }
}
