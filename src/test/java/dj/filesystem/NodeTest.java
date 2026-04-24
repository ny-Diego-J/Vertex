package dj.filesystem;

import dj.Camera;
import org.joml.Vector4f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NodeTest {

    private Directory mockDirectory;
    private Camera mockCamera;
    private Vector4f defaultColor;
    private Node node;

    @BeforeEach
    void setUp() {
        mockDirectory = mock(Directory.class);
        mockCamera = mock(Camera.class);

        when(mockCamera.getZoom()).thenReturn(1.0f);
        when(mockCamera.getX()).thenReturn(0.0f);
        when(mockCamera.getY()).thenReturn(0.0f);

        defaultColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        node = new Node("testNode", 100.0f, 100.0f, defaultColor, mockDirectory, false);
    }

    @Test
    void testConstructorInitialization() {
        assertEquals("testNode", node.getName());
        assertEquals(100.0f, node.getX());
        assertEquals(100.0f, node.getY());
        assertEquals(100.0, node.getTargetX());
        assertEquals(100.0, node.getTargetY());
        assertEquals(mockDirectory, node.getParent());
        assertFalse(node.isParent);
        assertTrue(node.moveAngle >= 0 && node.moveAngle < 360, "Move angle should be random between 0 and 360");
    }

    @Test
    void testGettersAndSetters() {
        node.setName("newName");
        assertEquals("newName", node.getName());

        node.setX(50.0f);
        assertEquals(50.0f, node.getX());

        node.setY(60.0f);
        assertEquals(60.0f, node.getY());

        node.setVx(5.0f);
        assertEquals(5.0f, node.getVx());

        node.setVy(-2.0f);
        assertEquals(-2.0f, node.getVy());

        node.setIfParent(true);
        assertTrue(node.isParent);

        node.setRadius(30.0f);
        assertEquals(30.0f, node.getRadius());

        node.setTargetX(200.0);
        assertEquals(200.0, node.getTargetX());

        node.setTargetY(300.0);
        assertEquals(300.0, node.getTargetY());
    }

    @Test
    void testMoveTargetPos_AppliesTensionAndDampening() {
        node.setX(0f);
        node.setY(0f);
        node.setTargetX(100.0);
        node.setTargetY(100.0);
        node.setVx(0f);
        node.setVy(0f);

        node.moveTargetPos();

        assertTrue(node.getVx() > 0);
        assertTrue(node.getVy() > 0);

        assertTrue(node.getX() > 0);
        assertTrue(node.getY() > 0);
    }

    @Test
    void testMoveTargetPos_SnapsToTargetWhenClose() {
        node.setX(99.6f);
        node.setY(99.6f);
        node.setTargetX(100.0);
        node.setTargetY(100.0);
        node.setVx(0.05f);
        node.setVy(0.05f);

        node.moveTargetPos();

        assertEquals(100.0f, node.getX());
        assertEquals(100.0f, node.getY());
        assertEquals(0f, node.getVx());
        assertEquals(0f, node.getVy());
    }

    @Test
    void testMoveSelf_UpdatesTargetPositionWithinBounds() {
        node.setTargetX(100.0);
        node.setTargetY(100.0);
        node.moveAngle = 0;

        node.moveSelf(800, 600, mockCamera);

        // TargetX should increase by Node.moveSpeed
        assertTrue(node.getTargetX() > 100.0);
        assertEquals(100.0, node.getTargetY(), 0.001); // Y shouldn't change
    }

    @Test
    void testMoveSelf_BouncesOffRightEdge() {
        // Position right at the visual right edge
        int width = 800;
        int height = 600;
        float centerX = width / 2.0f;

        // Calculate max X boundary based on your class logic
        double visibleRight = (width - centerX) / mockCamera.getZoom() + mockCamera.getX();
        double maxX = visibleRight - node.getRadius();

        node.setTargetX(maxX + 1); // Exceeds boundary
        node.setTargetY(300.0);
        node.moveAngle = 0; // Moving exactly right

        node.moveSelf(width, height, mockCamera);

        // moveAngle should reflect/bounce
        assertNotEquals(0, node.moveAngle);
        assertEquals(180, node.moveAngle); // Should face exactly left after
                                           // bouncing
    }

    @Test
    void testMoveSelf_BouncesOffBottomEdge() {
        int width = 800;
        int height = 600;
        float centerY = height / 2.0f;

        // Calculate max Y boundary based on your class logic
        double visibleBottom = (height - centerY) / mockCamera.getZoom() + mockCamera.getY();
        double maxY = visibleBottom - node.getRadius();

        node.setTargetX(400.0);
        node.setTargetY(maxY + 1); // Exceeds boundary
        node.moveAngle = 90; // Moving exactly down

        node.moveSelf(width, height, mockCamera);

        // moveAngle should reflect/bounce
        assertNotEquals(90, node.moveAngle);
        assertEquals(270, node.moveAngle); // Should face exactly up after
                                           // bouncing
    }
}
