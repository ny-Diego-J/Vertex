package dj.filesystem;

import org.joml.Vector4f;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class DirReader {

    public ArrayList<Node> getDirectories(String pathString) throws IOException {
        ArrayList<Node> nodes = new ArrayList<>();
        Path path = Paths.get(pathString);

        // Wir löschen die alten Nodes, bevor wir neue laden
        nodes.clear();

        try (Stream<Path> stream = Files.list(path)) {
            final float[] currentX = {50}; // Startposition X
            final float[] currentY = {50}; // Startposition Y
            float spacing = 20;  // Abstand zwischen den Nodes

            stream.forEach(p -> {
                String name = p.getFileName().toString();
                boolean isDir = Files.isDirectory(p);

                // Wir entscheiden die Farbe basierend auf dem Typ
                Vector4f color = isDir ?
                        new Vector4f(0.2f, 0.4f, 0.8f, 1.0f) : // Blau für Ordner
                        new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);  // Grau für Dateien

                // Erstelle die Node (Größe z.B. 150x50)
                if (isDir) {
                    nodes.add(new Directory(name, currentX[0], currentY[0], color, new HashMap<>()));
                } else {
                    nodes.add(new Node(name, currentX[0], currentY[0], color));
                }

                // Simples Layout: Untereinander stapeln
                // (Später kannst du hier ein Grid-System bauen)
                // currentY += 50 + spacing;

                // Oder Nebeneinander:
                currentX[0] += 150 + spacing;
                if (currentX[0] > 1000) { // Umbruch nach 1000 Pixeln
                    currentX[0] = 50;
                    currentY[0] += 70;
                }
            });
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen des Verzeichnisses: " + e.getMessage());
        }
        return nodes;
    }
}