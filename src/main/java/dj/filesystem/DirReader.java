package dj.filesystem;

import org.joml.Vector4f;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class DirReader {

    public Directory getDirectories(Directory current, String pathString, int recursionState) throws IOException {
        Path path = Paths.get(pathString);
        current.children = new ArrayList<>();

        try (Stream<Path> stream = Files.list(path)) {
            final float[] currentX = {50};
            final float[] currentY = {50};
            float spacing = 20;

            stream.forEach(p -> {
                String name = p.getFileName().toString();
                boolean isDir = Files.isDirectory(p);
                try {
                    if (Files.isHidden(p)) return;
                } catch (IOException e) {
                }


                Vector4f color = isDir ? new Vector4f(0.2f, 0.4f, 0.8f, 0.8f) :
                        new Vector4f(0.5f, 0.5f, 0.5f, 0.8f);

                if (isDir) {
                    try {
                        Directory subDir = new Directory(name, current.x, current.y, color, current, false);
                        current.children.add(subDir);

                        if (recursionState < 0) {
                            getDirectories(subDir, p.toString(), recursionState + 1);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    current.children.add(new Node(name, current.x, current.y, color, current, false));
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
            String name = cur.getName();
            if (name != null && !name.equals("/") && !name.isBlank()) {
                parts.add(name);
            }
            cur = cur.getParent();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = parts.size() - 1; i >= 0; i--) {
            sb.append(parts.get(i));
            if (i != 0) sb.append("/");
        }

        return sb.toString();
    }

    public void openFile(Node node) {
        if (Desktop.isDesktopSupported()) {
            try {
                String fullPath = getPath(node);
                File fileToOpen = new File(fullPath);

                if (fileToOpen.exists()) {
                    Desktop.getDesktop().open(fileToOpen);
                    System.out.println("opened file: " + fileToOpen.getName());
                } else {
                    System.err.println("Error: File does not exist");
                }
            } catch (IOException e) {
                System.err.println("Error: failed to open file: " + e.getMessage());
            }
        } else {
            System.err.println("Error: desktop not supported");
        }
    }
}