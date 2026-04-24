package dj.filesystem;

import org.joml.Vector4f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DirectoryTest {

    private Directory rootDir;
    private Vector4f defaultColor;

    @BeforeEach
    void setUp() {
        defaultColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        rootDir = new Directory("root", 100.0f, 100.0f, defaultColor, null, true);

        rootDir.setIdleState(false);
    }

    @AfterEach
    void tearDown() {
        Directory.isIdleState = false;
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("root", rootDir.getName());
        assertTrue(rootDir.isParent);
        assertNotNull(rootDir.getChildren());
        assertTrue(rootDir.getChildren().isEmpty());
    }

    @Test
    void testSetIdleState() {
        assertFalse(Directory.isIdleState);
        rootDir.setIdleState(true);
        assertTrue(Directory.isIdleState);
    }

    @Test
    void testGetHoverdNode_HoverSelf() {
        try (MockedStatic<GLFW> glfwMock = mockStatic(GLFW.class)) {
            rootDir.setRadius(25.0f);

            Node hovered = rootDir.getHoverdNode(105.0f, 105.0f);

            assertEquals(rootDir, hovered);
            glfwMock.verify(() -> GLFW.glfwSetCursor(anyLong(), anyLong()), atLeastOnce());
        }
    }

    @Test
    void testGetHoverdNode_HoverChild() {
        try (MockedStatic<GLFW> glfwMock = mockStatic(GLFW.class)) {
            Node child = new Node("child", 200.0f, 200.0f, defaultColor, rootDir, false);
            child.setRadius(20.0f);
            rootDir.getChildren().add(child);

            Node hovered = rootDir.getHoverdNode(205.0f, 205.0f);

            assertEquals(child, hovered);
        }
    }

    @Test
    void testGetHoverdNode_NoHover() {
        try (MockedStatic<GLFW> glfwMock = mockStatic(GLFW.class)) {
            Node hovered = rootDir.getHoverdNode(500.0f, 500.0f);

            assertNull(hovered);
            glfwMock.verify(() -> GLFW.glfwSetCursor(anyLong(), anyLong()), atLeastOnce());
        }
    }

    @Test
    void testReflectVelocity_ViaReflection() throws Exception {
        Method reflectVelocityMethod = Directory.class.getDeclaredMethod("reflectVelocity", Node.class, float.class, float.class);
        reflectVelocityMethod.setAccessible(true);

        Node node = new Node("test", 0, 0, defaultColor, rootDir, false);
        node.vx = 10.0f;
        node.vy = 5.0f;

        reflectVelocityMethod.invoke(rootDir, node, 1.0f, 0.0f);

        assertEquals(-10.0f, node.vx, 0.001f);
        assertEquals(5.0f, node.vy, 0.001f);
    }

    @Test
    void testReflectNodeAngle_ViaReflection() throws Exception {
        Method reflectAngleMethod = Directory.class.getDeclaredMethod("reflectNodeAngle", Node.class, float.class, float.class);
        reflectAngleMethod.setAccessible(true);

        Node node = new Node("test", 0, 0, defaultColor, rootDir, false);
        node.moveAngle = 45.0;

        reflectAngleMethod.invoke(rootDir, node, 0.0f, 1.0f);

        assertEquals(315.0, node.moveAngle, 0.001);
    }

    @Test
    void testCheckNormalCollision_AppliesRepulsionForce() throws Exception {
        Method checkCollisionMethod = Directory.class.getDeclaredMethod("checkNormalCollision", Node.class, Node.class);
        checkCollisionMethod.setAccessible(true);

        Node n1 = new Node("n1", 0, 0, defaultColor, rootDir, false);
        Node n2 = new Node("n2", 30.0f, 0, defaultColor, rootDir, false);

        n1.vx = 0;
        n1.vy = 0;
        n2.vx = 0;
        n2.vy = 0;

        checkCollisionMethod.invoke(rootDir, n1, n2);

        assertTrue(n1.vx < 0, "n1 should be pushed left");
        assertTrue(n2.vx > 0, "n2 should be pushed right");

        // Keine vertikale Abweichung
        assertEquals(0, n1.vy, 0.001f);
        assertEquals(0, n2.vy, 0.001f);
    }

    @Test
    void testCheckIdleCollision_SeparatesOverlappingNodes() throws Exception {
        Method checkIdleCollisionMethod = Directory.class.getDeclaredMethod("checkIdleCollision", Node.class, Node.class);
        checkIdleCollisionMethod.setAccessible(true);

        Node n1 = new Node("n1", 100f, 100f, defaultColor, rootDir, false);
        n1.setRadius(20f);
        Node n2 = new Node("n2", 110f, 100f, defaultColor, rootDir, false);
        n2.setRadius(20f);

        checkIdleCollisionMethod.invoke(rootDir, n1, n2);

        assertTrue(n1.x < 100f, "n1 should have moved left to fix overlap");
        assertTrue(n2.x > 110f, "n2 should have moved right to fix overlap");
    }
}
