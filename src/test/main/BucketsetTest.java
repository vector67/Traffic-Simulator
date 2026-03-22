package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BucketsetTest {

    // 3x3 grid of 100x100 buckets covering a 300x300 screen
    private Bucketset bs;
    private static final int COLS   = 3;
    private static final int ROWS   = 3;
    private static final int BWIDTH = 100;
    private static final int BHEIGHT= 100;

    /**
     * Fills a Bucketset with buckets whose positions match the grid layout expected
     * by surroundBuckets() and next():
     *   center of bucket[col][row] = (col*W + W/2, row*H + H/2)
     */
    @BeforeEach
    public void setUp() {
        bs = new Bucketset(COLS, ROWS, COLS * BWIDTH, ROWS * BHEIGHT);
        // add() scans outer-y / inner-x, filling row by row
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double cx = col * BWIDTH  + BWIDTH  / 2.0;
                double cy = row * BHEIGHT + BHEIGHT / 2.0;
                bs.add(new Bucket(cx, cy, BWIDTH, BHEIGHT));
            }
        }
    }

    // --- isEmpty / contains ---

    @Test
    public void testIsEmpty_afterFill() {
        assertFalse(bs.isEmpty());
    }

    @Test
    public void testIsEmpty_freshBucketset() {
        Bucketset empty = new Bucketset(2, 2);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void testContains_bucketAddedToGrid() {
        Bucket b = bs.get(0, 0);
        assertTrue(bs.contains(b));
    }

    @Test
    public void testContains_unknownBucket() {
        Bucket stranger = new Bucket(999, 999, 100, 100);
        assertFalse(bs.contains(stranger));
    }

    // --- getBucketIn ---

    @Test
    public void testGetBucketIn_topLeftCell() {
        // Point (50, 50) → col 0, row 0
        Bucket result = bs.getBucketIn(50, 50);
        assertEquals(bs.get(0, 0), result);
    }

    @Test
    public void testGetBucketIn_centerCell() {
        // Point (150, 150) → col 1, row 1
        Bucket result = bs.getBucketIn(150, 150);
        assertEquals(bs.get(1, 1), result);
    }

    @Test
    public void testGetBucketIn_bottomRightCell() {
        // Point (250, 250) → col 2, row 2
        Bucket result = bs.getBucketIn(250, 250);
        assertEquals(bs.get(2, 2), result);
    }

    // --- next() ---

    @Test
    public void testNext_right_fromCenter() {
        Bucket center = bs.get(1, 1);
        Bucket right  = bs.get(2, 1);
        assertEquals(right, bs.next(1, center));
    }

    @Test
    public void testNext_left_fromCenter() {
        Bucket center = bs.get(1, 1);
        Bucket left   = bs.get(0, 1);
        assertEquals(left, bs.next(3, center));
    }

    @Test
    public void testNext_up_fromCenter() {
        Bucket center = bs.get(1, 1);
        Bucket up     = bs.get(1, 0);
        assertEquals(up, bs.next(0, center));
    }

    @Test
    public void testNext_down_fromCenter() {
        Bucket center = bs.get(1, 1);
        Bucket down   = bs.get(1, 2);
        assertEquals(down, bs.next(2, center));
    }

    @Test
    public void testNext_rightEdge_returnsNull() {
        Bucket rightEdge = bs.get(2, 1);
        assertNull(bs.next(1, rightEdge));
    }

    @Test
    public void testNext_topEdge_returnsNull() {
        Bucket topEdge = bs.get(1, 0);
        assertNull(bs.next(0, topEdge));
    }

    // --- surroundBuckets ---

    @Test
    public void testSurroundBuckets_center_allNinePresent() {
        Bucket center = bs.get(1, 1);
        Bucket[] neighbors = bs.surroundBuckets(center);
        assertEquals(9, neighbors.length);
        for (int i = 0; i < 9; i++) {
            assertNotNull(neighbors[i], "Neighbor at index " + i + " should not be null for center bucket");
        }
    }

    @Test
    public void testSurroundBuckets_center_slot8IsSelf() {
        Bucket center = bs.get(1, 1);
        Bucket[] neighbors = bs.surroundBuckets(center);
        assertSame(center, neighbors[8]);
    }

    @Test
    public void testSurroundBuckets_cornerBucket_hasNullNeighbors() {
        // Top-left corner: no neighbors above or to the left
        Bucket corner = bs.get(0, 0);
        Bucket[] neighbors = bs.surroundBuckets(corner);
        // Slots 0 (top-left), 1 (top), 7 (left) should be null
        assertNull(neighbors[0]);
        assertNull(neighbors[1]);
        assertNull(neighbors[7]);
    }

    // --- remove ---

    @Test
    public void testRemove_bucketNoLongerContained() {
        Bucket b = bs.get(1, 1);
        bs.remove(b);
        assertFalse(bs.contains(b));
    }

    @Test
    public void testRemove_nonExistentReturnsFalse() {
        Bucket stranger = new Bucket(999, 999, 100, 100);
        assertFalse(bs.remove(stranger));
    }

    // --- clear ---

    @Test
    public void testClear_makesSetEmpty() {
        bs.clear();
        assertTrue(bs.isEmpty());
    }

    // --- iterator ---

    @Test
    public void testIterator_yieldsAllBuckets() {
        int count = 0;
        for (Bucket b : bs) {
            assertNotNull(b);
            count++;
        }
        assertEquals(COLS * ROWS, count);
    }
}
