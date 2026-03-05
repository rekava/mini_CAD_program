package org.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33.*;

public class Application {
    Window window;
    Triangle tri;
    Rectangle rec;
    Renderer renderer;
    Camera camera;
    GuiManager guiManager;
    SelectionManager selectionManager;

    void init() {
        if (!glfwInit()) {
            throw new IllegalStateException();
        }

        window = new Window(1280, 720);
        camera = new Camera(window.getW(), window.getH());
        glfwMakeContextCurrent(window.getWin());
        GL.createCapabilities();

        guiManager = new GuiManager(null);
        guiManager.init(window.getWin());

        setupCallbacks();

        Shader shader = new Shader("src/main/resources/shaders/vertex.glsl", "src/main/resources/shaders/fragment.glsl");
        shader.create();

        tri = new Triangle(shader);
        rec = new Rectangle(shader);
        tri.transform.setPosition(-0.5f, 0);
        rec.transform.setPosition(0.5f, 0);

        Scene scene = new Scene();
        renderer = new Renderer(scene);
        scene.add(tri);
        scene.add(rec);

        selectionManager = new SelectionManager(scene, camera);
        guiManager.setSelectionManager(selectionManager);

        glfwShowWindow(window.getWin());
    }

    private void setupCallbacks() {
        glfwSetFramebufferSizeCallback(window.getWin(), (w, width, height) -> {
            glViewport(0, 0, width, height);
            camera.setViewportSize(width, height);
            window.setWidth(width);
            window.setHeight(height);
        });

        glfwSetKeyCallback(window.getWin(), (w, key, scancode, action, mods) -> {
            if (imgui.ImGui.getIO().getWantCaptureKeyboard()) return;
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                if (key == GLFW_KEY_KP_ADD || key == GLFW_KEY_EQUAL) {
                    camera.zoom -= 0.1f;
                } else if (key == GLFW_KEY_KP_SUBTRACT || key == GLFW_KEY_MINUS) {
                    camera.zoom += 0.1f;
                }
                if (camera.zoom < 0.1f) camera.zoom = 0.1f;
                if (camera.zoom > 10f) camera.zoom = 10f;
                camera.updateProjection(window.getWidth(), window.getHeight());
            }
        });

        glfwSetScrollCallback(window.getWin(), (w, xoffset, yoffset) -> {
            if (imgui.ImGui.getIO().getWantCaptureMouse()) return;
            camera.zoom -= yoffset * 0.1f;
            if (camera.zoom < 0.1f) camera.zoom = 0.1f;
            if (camera.zoom > 10f) camera.zoom = 10f;
            camera.updateProjection(window.getWidth(), window.getHeight());
        });

        glfwSetMouseButtonCallback(window.getWin(), (w, button, action, mods) -> {
            if (imgui.ImGui.getIO().getWantCaptureMouse()) return;

            double[] x = new double[1];
            double[] y = new double[1];
            glfwGetCursorPos(window.getWin(), x, y);

            selectionManager.mouseButtonCallback(button, action, x[0], y[0]);
        });
    }

    void loop() {
        while (!glfwWindowShouldClose(window.getWin())) {
            float time = (float) glfwGetTime();

            rec.transform.rotation = time * 0.5f;
            tri.transform.rotation = time * 0.5f;

            renderer.clear();
            renderer.render(camera);

            guiManager.renderUI(renderer.currentScene, window.getWidth(), window.getHeight());

            glfwSwapBuffers(window.getWin());
            glfwPollEvents();
        }
        terminate();
    }

    void terminate() {
        glfwTerminate();
    }
}