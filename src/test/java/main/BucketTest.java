package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BucketTest {

    // Bucket centered at (100, 200) with width=100, height=60
    private Bucket bucket;

    @BeforeEach
    public void setUp() {
        bucket = new Bucket(100, 200, 100, 60);
    }

    // --- Boundary accessors ---

    @Test
    public void testTopSide() {
        // y + height/2 = 200 + 30 = 230
        assertEquals(230, bucket.topSide());
    }

    @Test
    public void testBottomSide() {
        // y - height/2 = 200 - 30 = 170
        assertEquals(170, bucket.bottomSide());
    }

    @Test
    public void testRightSide() {
        // x + width/2 = 100 + 50 = 150
        assertEquals(150, bucket.rightSide());
    }

    @Test
    public void testLeftSide() {
        // x - width/2 = 100 - 50 = 50
        assertEquals(50, bucket.leftSide());
    }

    // --- isPointIn ---

    @Test
    public void testIsPointIn_center() {
        assertTrue(bucket.isPointIn(100, 200));
    }

    @Test
    public void testIsPointIn_insideNearCorner() {
        // Just inside the top-right: x=149, y=229 (strict less-than boundary)
        assertTrue(bucket.isPointIn(149, 229));
    }

    @Test
    public void testIsPointIn_outsideRight() {
        assertFalse(bucket.isPointIn(200, 200));
    }

    @Test
    public void testIsPointIn_outsideLeft() {
        assertFalse(bucket.isPointIn(0, 200));
    }

    @Test
    public void testIsPointIn_outsideTop() {
        assertFalse(bucket.isPointIn(100, 300));
    }

    @Test
    public void testIsPointIn_outsideBottom() {
        assertFalse(bucket.isPointIn(100, 100));
    }

    @Test
    public void testIsPointIn_onRightEdge_isExclusive() {
        // isPointIn uses strict < so the exact boundary is excluded
        assertFalse(bucket.isPointIn(150, 200));
    }

    @Test
    public void testIsPointIn_onLeftEdge_isExclusive() {
        assertFalse(bucket.isPointIn(50, 200));
    }

    // --- Vehicle list ---

    @Test
    public void testGetVehicles_initiallyEmpty() {
        assertTrue(bucket.getVehicles().isEmpty());
    }

    @Test
    public void testHasVehicles_initiallyFalse() {
        assertFalse(bucket.hasVehicles());
    }

    // --- toString ---

    @Test
    public void testToString_containsCoordinates() {
        String s = bucket.toString();
        assertTrue(s.contains("100.0"), "toString should contain x coordinate");
        assertTrue(s.contains("200.0"), "toString should contain y coordinate");
    }
}
