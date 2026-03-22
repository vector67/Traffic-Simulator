package main;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    @Test
    public void testDistanceto_samePoint() {
        Node n = new Node(5.0, 10.0, 1);
        assertEquals(0.0, n.distanceto(n), 1e-9);
    }

    @Test
    public void testDistanceto_knownDistance() {
        Node a = new Node(0.0, 0.0, 1);
        Node b = new Node(3.0, 4.0, 2);
        assertEquals(5.0, a.distanceto(b), 1e-9);
    }

    @Test
    public void testDistanceto_isSymmetric() {
        Node a = new Node(10.0, 20.0, 1);
        Node b = new Node(40.0, 60.0, 2);
        assertEquals(a.distanceto(b), b.distanceto(a), 1e-9);
    }

    @Test
    public void testDirto_east() {
        // Node directly to the right: run > 0, rise = 0 → atan(0) = 0
        Node origin = new Node(0.0, 0.0, 1);
        Node east   = new Node(10.0, 0.0, 2);
        assertEquals(0.0, origin.dirto(east), 1e-9);
    }

    @Test
    public void testDirto_west() {
        // Node directly to the left: run < 0 → poss = PI, rise = 0 → atan(0)+PI = PI
        Node origin = new Node(0.0, 0.0, 1);
        Node west   = new Node(-10.0, 0.0, 2);
        double result = origin.dirto(west);
        // atan(0) + PI = PI, which is not > PI, so returned as PI
        assertEquals(Math.PI, result, 1e-9);
    }

    @Test
    public void testDirto_northeast() {
        Node origin    = new Node(0.0, 0.0, 1);
        Node northeast = new Node(1.0, 1.0, 2);
        double expected = Math.atan(1.0); // run>0, rise/run = 1
        assertEquals(expected, origin.dirto(northeast), 1e-9);
    }

    @Test
    public void testHasChild_newNodeHasNoChildren() {
        Node n = new Node(0.0, 0.0, 1);
        assertFalse(n.hasChild());
    }

    @Test
    public void testHasParents_newNodeHasNoParents() {
        Node n = new Node(0.0, 0.0, 1);
        assertFalse(n.hasParents());
    }

    @Test
    public void testAddChild_hasChildBecomesTrue() {
        Node parent = new Node(0.0, 0.0, 1);
        Node child  = new Node(10.0, 0.0, 2);
        parent.addChild(child);
        assertTrue(parent.hasChild());
    }

    @Test
    public void testAddChild_childIsPresent() {
        Node parent = new Node(0.0, 0.0, 1);
        Node child  = new Node(10.0, 0.0, 2);
        parent.addChild(child);
        assertTrue(parent.children.contains(child));
    }

    @Test
    public void testAddChild_setsParentOnChild() {
        Node parent = new Node(0.0, 0.0, 1);
        Node child  = new Node(10.0, 0.0, 2);
        parent.addChild(child);
        assertTrue(child.hasParents());
        assertTrue(child.parents.contains(parent));
    }

    @Test
    public void testAddChild_noDuplicates() {
        Node parent = new Node(0.0, 0.0, 1);
        Node child  = new Node(10.0, 0.0, 2);
        parent.addChild(child);
        parent.addChild(child);
        assertEquals(1, parent.children.size());
    }

    @Test
    public void testAddParent_hasParentsBecomesTrue() {
        Node parent = new Node(0.0, 0.0, 1);
        Node child  = new Node(10.0, 0.0, 2);
        child.addParent(parent);
        assertTrue(child.hasParents());
    }

    @Test
    public void testGetChild_returnsNullWhenEmpty() {
        Node n = new Node(0.0, 0.0, 1);
        assertNull(n.getChild());
    }

    @Test
    public void testGetChild_returnsChildWhenPresent() {
        Node parent = new Node(0.0, 0.0, 1);
        Node child  = new Node(10.0, 0.0, 2);
        parent.addChild(child);
        assertEquals(child, parent.getChild());
    }

    @Test
    public void testGetChildRoute_returnsNullWhenNoRoutes() {
        Node n = new Node(0.0, 0.0, 1);
        assertNull(n.getChildRoute());
    }

    @Test
    public void testRemoveChild() {
        Node parent = new Node(0.0, 0.0, 1);
        Node child  = new Node(10.0, 0.0, 2);
        parent.addChild(child);
        parent.removeChild(child);
        assertFalse(parent.children.contains(child));
    }

    @Test
    public void testClone_sameCoordinatesAndId() {
        Node original = new Node(7.5, 3.2, 42);
        Node cloned   = original.clone();
        assertEquals(original.x, cloned.x, 1e-9);
        assertEquals(original.y, cloned.y, 1e-9);
        assertEquals(original.ID, cloned.ID);
    }

    @Test
    public void testClone_isDifferentObject() {
        Node original = new Node(1.0, 2.0, 1);
        Node cloned   = original.clone();
        assertNotSame(original, cloned);
    }

    @Test
    public void testSave_format() {
        Node n = new Node(10.0, 20.0, 5);
        String saved = n.save();
        // Expected: "5;10.0;20.0;;"
        assertTrue(saved.startsWith("5;10.0;20.0;"), "save() format mismatch: " + saved);
    }

    @Test
    public void testToString_containsId() {
        Node n = new Node(1.0, 2.0, 99);
        assertTrue(n.toString().contains("99"));
    }

    /**
     * Documents a known bug: hasCollisionPoints() returns collisionpoints.isEmpty()
     * instead of !collisionpoints.isEmpty(). So on a fresh node with no collision
     * points, it incorrectly returns true.
     */
    @Test
    public void testHasCollisionPoints_bugInvertedLogic() {
        Node n = new Node(0.0, 0.0, 1);
        // Bug: should be false (no collision points), but returns true
        assertTrue(n.hasCollisionPoints(),
            "Known bug: hasCollisionPoints() returns isEmpty() instead of !isEmpty()");
    }
}
