package org.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class Window {

    private int W = 1280;
    private int H = 720;
    private int width;
    private int height;

    long win = 0;

    Window(int W, int H){

        this.W = W;
        this.H = H;

        this.width = W;
        this.height = H;

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);


        win = glfwCreateWindow(this.W,this.H,"test",0,0);

        if(win == 0){
            throw new IllegalStateException("Failed to create GLFW window");
        }

        GLFWVidMode vidMod = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(win, (vidMod.width() - W)/2, (vidMod.height() - H)/2);


    }

    Window(){

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);


        win = glfwCreateWindow(W,H,"test",0,0);

        if(win == 0){
            throw new IllegalStateException("Failed to create GLFW window");
        }

        GLFWVidMode vidMod = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(win, (vidMod.width() - W)/2, (vidMod.height() - H)/2);


    }

    int getW(){
        return W;
    }
    int getH(){
        return H;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    void setWidth(int width){
        this.width = width;
    }
    void setHeight(int height){
        this.height = height;
    }
    long getWin(){
        return win;
    }
}
