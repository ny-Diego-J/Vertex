package dj.filesystem;

import org.joml.Vector4f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    private Node testNode;
    private Vector4f defaultColor;

    @BeforeEach
    void setUp() {
        defaultColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        // Initialisiere eine Node mit einem typischen Windows-Pfad
        testNode = new Node(0, 0, defaultColor, false, "C:\\Users\\Desktop\\file.txt");
    }

    @Test
    void testGetName_ExtractsFileNameCorrectly() {
        assertEquals("file.txt", testNode.getName(), "Der Name sollte korrekt aus dem Pfad extrahiert werden.");

        Node rootNode = new Node(0, 0, defaultColor, true, "C:");
        assertEquals("C:", rootNode.getName(), "Bei Laufwerken sollte der Name das Laufwerk selbst sein.");
    }

    @Test
    void testGetParent_CreatesCorrectParentDirectory() {
        Directory parent = testNode.getParent();

        assertNotNull(parent, "Parent sollte nicht null sein, wenn der Pfad Unterordner enthält.");
        assertEquals("Desktop", parent.getName(), "Der Name des generierten Parents sollte 'Desktop' sein.");
        assertTrue(parent.isParent, "Das generierte Parent sollte als isParent markiert sein.");
    }

    @Test
    void testGetParent_ReturnsNullForRoot() {
        Node rootNode = new Node(0, 0, defaultColor, true, "C:");
        assertNull(rootNode.getParent(), "Ein Root-Verzeichnis sollte kein Parent haben.");
    }

    @Test
    void testMoveTargetPos_CalculatesVelocityCorrectly() {
        testNode.setX(0);
        testNode.setY(0);
        testNode.setTargetX(100);
        testNode.setTargetY(50);

        testNode.moveTargetPos();

        // Da Tension 0.045f und Dampening 0.85f ist, sollte vx > 0 und vy > 0
        // sein
        assertTrue(testNode.getVx() > 0, "Velocity X sollte positiv sein, da TargetX rechts liegt.");
        assertTrue(testNode.getVy() > 0, "Velocity Y sollte positiv sein, da TargetY unten liegt.");

        assertTrue(testNode.getX() > 0, "X-Position sollte sich in Richtung Target bewegt haben.");
        assertTrue(testNode.getY() > 0, "Y-Position sollte sich in Richtung Target bewegt haben.");
    }

    @Test
    void testMoveTargetPos_SnapsToTargetWhenClose() {
        testNode.setX(99.6f);
        testNode.setY(49.6f);
        testNode.setTargetX(100);
        testNode.setTargetY(50);
        testNode.setVx(0.05f);
        testNode.setVy(0.05f);

        testNode.moveTargetPos();

        assertEquals(100.0, testNode.getX(), 0.001, "Node sollte auf TargetX einrasten, wenn sie sehr nah ist.");
        assertEquals(50.0, testNode.getY(), 0.001, "Node sollte auf TargetY einrasten, wenn sie sehr nah ist.");
        assertEquals(0.0f, testNode.getVx(), "Velocity X sollte nach dem Einrasten 0 sein.");
    }
}
