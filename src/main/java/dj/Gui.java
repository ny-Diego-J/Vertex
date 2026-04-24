package dj;

import dj.filesystem.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFont;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.nanovg.NanoVG.*;

public class Gui {
    public static long window;
    public static long handCursor;
    public static long arrowCursor;
    private final String title = "Vertex";
    private final int[] winWidth = new int[1];
    private final int[] winHeight = new int[1];
    private final Controller ct;
    private final Camera camera;
    private boolean leftMouseButtonPressed = false;
    private boolean rightMouseButtonPressed = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private int width = 1280;
    private int height = 720;
    private boolean wasLeftMouseButtonPressed = false;
    private boolean wasRightMouseButtonPressed = false;
    private boolean isControlPressed = false;
    private boolean isDragging = false;

    public Gui(Controller ct) {
        this.ct = ct;
        this.camera = new Camera(0, 0);
    }

    /**
     * main controller
     */
    public void run() {
        init();
        loop();

        // Cleanup after close
        GLFW.glfwDestroyCursor(arrowCursor);
        GLFW.glfwDestroyCursor(handCursor);
        ct.timeThread.interrupt();
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * initiation of the window and of the input callbacks
     */
    private void init() {
        // Error-Callback (prints error in the console)
        GLFWErrorCallback.createPrint(System.err).set();

        // GLFW initiation
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW could not get loadet");
        }
        handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);

        // window config (Hints)
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_STENCIL_BITS, 8);

        // create Window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Window could not be created");
        }

        glfwSetFramebufferSizeCallback(window, (windowHandle, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            // directly inform OpenGL
            glViewport(0, 0, newWidth, newHeight);
        });

        glfwSetCursorPosCallback(window, (windowHandle, xPos, yPos) -> {
            if (rightMouseButtonPressed) {
                double diffX = xPos - lastMouseX;
                double diffY = yPos - lastMouseY;

                camera.addToX((float) -(diffX / camera.getZoom()));
                camera.addToY((float) -(diffY / camera.getZoom()));
            }
        });

        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                ct.resetTime();
                if (action == GLFW_PRESS) {
                    leftMouseButtonPressed = true;
                } else if (action == GLFW_RELEASE) {
                    leftMouseButtonPressed = false;
                }
            }
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                ct.resetTime();
                if (action == GLFW_PRESS) {
                    rightMouseButtonPressed = true;
                } else if (action == GLFW_RELEASE) {
                    rightMouseButtonPressed = false;
                }
            }
        });

        glfwSetScrollCallback(window, (w, xOffset, yOffset) -> {
            ct.resetTime();
            if (isControlPressed) {
                camera.multiplyToZoom(yOffset > 0 ? 1.02f : 0.98f);
            } else {
                camera.addToY(-((float) yOffset * 20));
            }
        });

        glfwMakeContextCurrent(window);
        // activate V Sync
        // IMPORTANT DO NOT DEACTIVATE OR YOUR GRAPHICS CARD WILL GO
        // WROOOOOOOOOOM
        glfwSwapInterval(1);

        glfwShowWindow(window);

        GL.createCapabilities();

        glViewport(0, 0, width, height);
        // Set background color
        glClearColor(0.1f, 0.1f, 0.12f, 1.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    /**
     * main loop for printing
     */
    private void loop() {
        long nvg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (nvg == 0) {
            throw new RuntimeException("NanoVG initialization has failed!!");
        }

        int font = nvgCreateFont(nvg, "jbm", "src/main/resources/fonts/jbm.ttf");
        if (font == -1) {
            System.err.println("Warning: Font could not be loaded. Text won't display.");
        }
        double[] rawMouseX = { 0 };
        double[] rawMouseY = { 0 };

        long lastTime = System.nanoTime();
        int frames = 0;

        float lastWorldX = 0, lastWorldY = 0;
        Node hoveredNode = null;

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            frames++;
            if (System.nanoTime() - lastTime >= 1_000_000_000) {
                System.out.println("FPS: " + frames);
                frames = 0;
                lastTime = System.nanoTime();
            }

            // int[] fbWidth = new int[1];
            // int[] fbHeight = new int[1];

            // glfwGetFramebufferSize(window, fbWidth, fbHeight);

            glfwGetWindowSize(window, winWidth, winHeight);

            // float pxRatio = (float) fbWidth[0] / (float) winWidth[0];

            glOrtho(0, width, height, 0, -1, 1);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            float centerX = width / 2.0f;
            float centerY = height / 2.0f;

            glTranslatef(centerX, centerY, 0);
            glScalef(camera.getZoom(), camera.getZoom(), 1.0f);
            glTranslatef(-camera.getX(), -camera.getY(), 0);

            nvgBeginFrame(nvg, width, height, 1.0f);

            nvgSave(nvg);
            nvgTranslate(nvg, centerX, centerY);
            nvgScale(nvg, camera.getZoom(), camera.getZoom());
            nvgTranslate(nvg, -camera.getX(), -camera.getY());

            ct.currentDir.printSelf(nvg, winWidth[0], winHeight[0], camera);

            glfwGetCursorPos(window, rawMouseX, rawMouseY);

            float mouseWorldX = (float) ((rawMouseX[0] - centerX) / camera.getZoom() + camera.getX());
            float mouseWorldY = (float) ((rawMouseY[0] - centerY) / camera.getZoom() + camera.getY());

            if (isDragging) {
                if (leftMouseButtonPressed) {
                    ct.currentDir.setX(mouseWorldX);
                    ct.currentDir.setTargetX(mouseWorldX);
                    ct.currentDir.setY(mouseWorldY);
                    ct.currentDir.setTargetY(mouseWorldY);
                } else {
                    double dx = rawMouseX[0] - lastMouseX;
                    double dy = rawMouseY[0] - lastMouseY;

                    if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
                        double angle = Math.toDegrees(Math.atan2(dy, dx));
                        ct.currentDir.moveAngle = angle;
                    }
                    isDragging = false;
                }
            } else {
                if (mouseWorldX != lastWorldX || mouseWorldY != lastWorldY) {
                    hoveredNode = ct.currentDir.getHoverdNode(mouseWorldX, mouseWorldY);
                    lastWorldX = mouseWorldX;
                    lastWorldY = mouseWorldY;
                }
                Node clicked = hoveredNode;
                if (clicked != null) {
                    if (leftMouseButtonPressed && !wasLeftMouseButtonPressed) {
                        if (clicked != ct.currentDir) {
                            System.out.println("sub");
                            if (clicked instanceof Directory) {
                                glfwSetWindowTitle(window, title + " - " + ct.currentDir.getName());
                                Directory nextDir = (Directory) clicked;
                                updateCurrentDir(nextDir);
                            } else {
                                ct.dr.openFile(clicked);
                            }
                        } else {
                        }
                    } else if (leftMouseButtonPressed && wasLeftMouseButtonPressed) {
                        if (clicked == ct.currentDir) {
                            ct.currentDir.setX(mouseWorldX);
                            ct.currentDir.setTargetX(mouseWorldX);
                            ct.currentDir.setY(mouseWorldY);
                            ct.currentDir.setTargetY(mouseWorldY);
                            isDragging = true;
                        }
                    } else if (rightMouseButtonPressed && !wasRightMouseButtonPressed) {
                        if (clicked == ct.currentDir && clicked.getParent() != null) {
                            Directory nextDir = clicked.getParent();
                            updateCurrentDir(nextDir);
                            System.out.println("navigate to parent directory: " + nextDir.getName());
                            glfwSetWindowTitle(window, title + " - " + ct.currentDir.getName());
                        }
                    }
                }
            }
            wasLeftMouseButtonPressed = leftMouseButtonPressed;
            wasRightMouseButtonPressed = rightMouseButtonPressed;
            lastMouseX = rawMouseX[0];
            lastMouseY = rawMouseY[0];
            nvgRestore(nvg);
            nvgEndFrame(nvg);

            handleInput();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        nvgDelete(nvg);
    }

    /**
     * updates the current directory and also what comes with it
     * 
     * @param newDir new current directory new current directory
     */
    private void updateCurrentDir(Directory newDir) {
        newDir.setX(0);
        newDir.setY(0);
        ct.setCurrentDir(newDir);
        ct.reloadCurrentDir();
        ct.currentDir.setTargetX(0);
        ct.currentDir.setTargetY(0);
        ct.currentDir.startAngle = (float) (-Math.PI / 2.0);
        ct.currentDir.angleStep = (float) (2 * Math.PI / ct.currentDir.getChildren().size());
        camera.setX(0);
        camera.setY(0);

    }

    /**
     * secondary input handle for short and simple inputs
     */
    private void handleInput() {
        float speed = 5.0f / camera.getZoom();
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.addToY(-speed);
            ct.resetTime();

        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.addToY(speed);
            ct.resetTime();

        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.addToX(-speed);
            ct.resetTime();
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.addToX(speed);
            ct.resetTime();
        }
        if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS && glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            ct.reloadCurrentDir();
            ct.resetTime();
        }

        // Zoom with Q and E
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            camera.multiplyToZoom(1.02f);
            ct.resetTime();
        }
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            camera.multiplyToZoom(1f / 1.02f);
            ct.resetTime();
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            isControlPressed = true;
            ct.resetTime();
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_RELEASE) {
            isControlPressed = false;
        }
    }
}
