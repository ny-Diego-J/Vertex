package dj;

import org.joml.Vector4f;

import dj.filesystem.DirReader;
import dj.filesystem.Directory;

public class Controller {
    private final int TIME = 60;
    protected Directory currentDir;
    protected DirReader dr = new DirReader();
    protected Directory root = new Directory("C:\\", 0, 0, null, new Vector4f(1, 0, 0, 1), true);
    private Gui gui;
    private int counter = TIME;
    public Thread timeThread;

    public void run(String[] args) {

        gui = new Gui(this);
        reloadRoot();
        if (args.length >= 1) {
            setCurrentDir(new Directory(0, 0, new Vector4f(1, 0, 0, 1), true, args[0]));
            currentDir.setParent(getParent(args[0]));
        }
        reloadCurrentDir();
        initialize();
        gui.run();
    }

    /**
     * reloads the root directory
     */
    public void reloadRoot() {
        try {
            root = dr.getDirectories(root, root.getPath(), 0);
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
            currentDir = dr.getDirectories(currentDir, currentDir.getPath(), 0);
            currentDir.setIfParent(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void setCurrentDir(Directory dir) {
        currentDir = dir;
    }

    private Directory getParent(String path) {
        String[] paths = path.split("\\\\");
        if (paths.length > 1) {
            String name = paths[paths.length - 1];
            String newPath = path.replace("\\" + name, "");
            System.out.println(newPath);
            if (newPath.equals("C:")) newPath = "C:\\";
            System.out.println(newPath);
            return new Directory(newPath, 0, 0, getParent(newPath), new Vector4f(1, 0, 0, 1), true);
        } else {
            return null;
        }
    }

    private void initialize() {
        timeThread = new Thread(() -> {
            while (counter >= 0) {
                try {
                    Thread.sleep(1000);
                    counter--;
                    if (counter == 0) {
                        currentDir.setIdleState(true);
                        counter = TIME;
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }

    public void resetTime() {
        currentDir.setIdleState(false);
        counter = TIME;
    }
}
