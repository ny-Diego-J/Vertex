package dj.filesystem;

import org.joml.Vector4f;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class DirReader {

    public Directory getDirectories(Directory current, String pathString, int recursionState) throws IOException {
        Path path = Paths.get(pathString);

        if (current.children == null) current.children = new ArrayList<>();

        try (Stream<Path> stream = Files.list(path)) {
            final float[] currentX = {50};
            final float[] currentY = {50};
            float spacing = 20;

            stream.forEach(p -> {
                String name = p.getFileName().toString();
                boolean isDir = Files.isDirectory(p);

                Vector4f color = isDir ? new Vector4f(0.2f, 0.4f, 0.8f, 1.0f) :
                        new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);

                if (isDir) {
                    try {
                        Directory subDir = new Directory(name, currentX[0], currentY[0], color, current);
                        current.children.add(subDir);

                        if (recursionState < 1) {
                            getDirectories(subDir, p.toString(), recursionState + 1);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    current.children.add(new Node(name, currentX[0], currentY[0], color, current));
                }

                currentX[0] += 150 + spacing;
                if (currentX[0] > 1000) {
                    currentX[0] = 50;
                    currentY[0] += 150 + spacing;
                }
            });
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen von " + pathString + ": " + e.getMessage());
        }
        return current;
    }

    public String getPath(Node pos) {
        ArrayList<String> parts = new ArrayList<>();
        Node cur = pos;
        while (cur != null) {
            String name = cur.name;
            if (name != null && !name.equals("/") && !name.isBlank()) {
                parts.add(name);
            }
            cur = cur.parent;
        }
        StringBuilder sb = new StringBuilder("/");
        for (int i = parts.size() - 1; i >= 0; i--) {
            sb.append(parts.get(i));
            if (i != 0) sb.append("/");
        }
        return sb.toString();
    }
}