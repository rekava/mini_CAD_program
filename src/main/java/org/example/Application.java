package org.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;


public class Application {

    Window window;

    void init(){

        if(!glfwInit()){
            throw new IllegalStateException();
        }

        window = new Window(800,400);

        glfwMakeContextCurrent(window.getWin());
        GL.createCapabilities();

        glfwShowWindow(window.getWin());

    }

    void loop(){

        while (!glfwWindowShouldClose(window.getWin())){

            glfwPollEvents();
        }

        terminate();
    }

    void terminate(){
        glfwTerminate();
    }

}
