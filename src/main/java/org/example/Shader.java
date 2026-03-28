package org.example;


import org.joml.Matrix4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Shader {

    private String vertexSourcePatch = "";
    private String fragmentSourcePatch = "";

    private String vertexSource = "";
    private String fragmentSource = "";
    private int  programId;

    Shader(String vertexSourcePatch, String fragmentSourcePatch){
        this.vertexSourcePatch = vertexSourcePatch;
        this.fragmentSourcePatch = fragmentSourcePatch;
    }

    public boolean create(){

        vertexSource = "";
        fragmentSource = "";

        try {
            File vs = new File(vertexSourcePatch);
            File fs = new File(fragmentSourcePatch);

            Scanner scanner = new Scanner(vs);

            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                vertexSource += line + "\n";
            }

            scanner.close();

            scanner = new Scanner(fs);

            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                fragmentSource += line + "\n";
            }

            scanner.close();


            int vertexShader = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vertexShader, vertexSource);
            glCompileShader(vertexShader);

            int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragmentShader, fragmentSource);
            glCompileShader(fragmentShader);

            programId = glCreateProgram();
            glAttachShader(programId, vertexShader);
            glAttachShader(programId, fragmentShader);
            glLinkProgram(programId);

            glDetachShader(programId,vertexShader);
            glDetachShader(programId, fragmentShader);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);

        }catch (IOException e2){
            System.out.println(e2.toString());
            return false;
        }catch (Exception e1) {
            System.out.println(e1.toString());
            return false;
        }

        System.out.println("Vertex Source:\n" + vertexSource);
        System.out.println("Fragment Source:\n" + fragmentSource);

        return true;
    }

    public void use(){
        glUseProgram(programId);
    }

    public void cleanup(){
        if(programId != 0){
            glDeleteProgram(programId);
            programId = 0;
        }
    }

    public int getProgramId(){

        return programId;
    }

    public void setUniformMat4(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void setUniformFloat(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1f(location, value);
    }

    public void setUniformVec3(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programId, name);
        glUniform3f(location, x, y, z);
    }


}
