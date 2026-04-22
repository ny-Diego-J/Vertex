package dj;

import dj.filesystem.DirReader;
import dj.filesystem.Directory;
import org.joml.Vector4f;

public class Controller {
    private final int TIME = 3;
    protected Directory currentDir;
    protected DirReader dr = new DirReader();
    private Directory root;
    private Gui gui;
    private int counter = TIME;

    public void run() {
        reloadRoot();
        gui = new Gui(this);
        initialize();
        gui.run();
    }

    /**
     * reloads the root directory
     */
    public void reloadRoot() {
        try {
            root = new Directory("c:\\", 0, 0, new Vector4f(1, 0, 0, 1), null, true);
            root = dr.getDirectories(root, root.getName(), 0);
            currentDir = root;
            currentDir.moveAngle = (float) (-Math.PI / 2.0);
            currentDir.angleStep = (float) (2 * Math.PI / currentDir.getChildren().size());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * reloads the current directory
     */
    public void reloadCurrentDir() {
        try {
            currentDir.setIfParent(false);
            currentDir = dr.getDirectories(currentDir, dr.getPath(currentDir), 0);
            currentDir.setIfParent(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void setCurrentDir(Directory dir) {
        currentDir = dir;
    }

    private void initialize() {
        new Thread(() -> {
            while (counter >= 0) {
                try {
                    Thread.sleep(1000);
                    counter--;
                    if (counter == 0) {
                        currentDir.setIdleState(true);
                        counter = TIME;
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }

    public void resetTime() {
        currentDir.setIdleState(false);
        counter = TIME;
    }
}
