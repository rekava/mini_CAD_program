package org.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33.*;
import org.lwjgl.system.MemoryUtil;


public class Application {

    Window window;
    Triangle tri;
    void init(){

        if(!glfwInit()){
            throw new IllegalStateException();
        }

        window = new Window(800,400);

        glfwMakeContextCurrent(window.getWin());
        GL.createCapabilities();



        Shader shader = new Shader("src/main/resources/shaders/vertex.glsl", "src/main/resources/shaders/fragment.glsl" );
        shader.create();

        tri = new Triangle(shader);

        glfwShowWindow(window.getWin());

    }

    void loop(){

        while (!glfwWindowShouldClose(window.getWin())){
            glClearColor(0,0,0,1);
            glClear(GL_COLOR_BUFFER_BIT);

            tri.render();

            glfwSwapBuffers(window.getWin());
            glfwPollEvents();
        }
        terminate();
    }

    void terminate(){
        glfwTerminate();
    }

}
