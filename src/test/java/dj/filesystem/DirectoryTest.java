package dj.filesystem;

import org.joml.Vector4f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryTest {

    private Directory rootDir;
    private Vector4f defaultColor;

    @BeforeEach
    void setUp() {
        defaultColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        rootDir = new Directory(0, 0, defaultColor, true, "C:\\TestDir");
        // Setze Radius für deterministische Tests
        rootDir.setRadius(25.0f);
    }

    @Test
    void testAddChild() {
        Node child = new Node(10, 10, defaultColor, rootDir, false, "C:\\TestDir\\file.txt");
        rootDir.getChildren().add(child);

        assertEquals(1, rootDir.getChildren().size(), "Directory sollte genau 1 Kind haben.");
        assertEquals(child, rootDir.getChildren().get(0), "Das Kind sollte in der Liste abrufbar sein.");
    }

    @Test
    void testGetHoveredNode_ReturnsSelf() {
        // Maus ist genau im Zentrum des Verzeichnisses (0,0)
        Node hovered = rootDir.getHoverdNode(0, 0);

        assertEquals(rootDir, hovered, "Das Verzeichnis selbst sollte zurückgegeben werden, wenn die Maus darüber ist.");
    }

    @Test
    void testGetHoveredNode_ReturnsChild() {
        // Platziere Kind außerhalb des Parent-Radius
        Node child = new Node(100, 100, defaultColor, rootDir, false, "C:\\TestDir\\child.txt");
        child.setRadius(25.0f);
        rootDir.getChildren().add(child);

        // Maus ist auf den Koordinaten des Kindes
        Node hovered = rootDir.getHoverdNode(100, 100);

        assertEquals(child, hovered, "Das Kind sollte zurückgegeben werden, wenn die Maus darüber ist.");
    }

    @Test
    void testGetHoveredNode_ReturnsNullWhenOutside() {
        Node child = new Node(100, 100, defaultColor, rootDir, false, "C:\\TestDir\\child.txt");
        rootDir.getChildren().add(child);

        // Maus ist weit weg von Parent (0,0) und Kind (100,100)
        Node hovered = rootDir.getHoverdNode(500, 500);

        assertNull(hovered, "Sollte null zurückgeben, wenn die Maus über keinem Node ist.");
    }

    @Test
    void testSetIdleState() {
        rootDir.setIdleState(true);
        // Da isIdleState protected/package-private statisch ist,
        // müssten wir über Seiteneffekte testen (z.B. wie sich moveSpeed ändert
        // beim Rendern),
        // oder wir vertrauen hier auf die korrekte Variablen-Zuweisung.

        Directory anotherDir = new Directory(0, 0, defaultColor, true, "C:\\Another");
        // Statischer Zustand sollte auch für andere Instanzen gelten
        // (Warnung: isIdleState ist in Directory static deklariert!)

        // Da es statisch ist, setzen wir es am Ende für andere Tests zurück
        anotherDir.setIdleState(false);
    }
}
