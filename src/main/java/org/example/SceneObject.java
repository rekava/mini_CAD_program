package org.example;

import jdk.swing.interop.DragSourceContextWrapper;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL20.*;
import  static org.lwjgl.opengl.GL33.*;

public abstract class SceneObject {
    private String name = "New Object";
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Transform transform = new Transform();
    private boolean selected;

    SceneObject(){
    }

    abstract void render(Camera camera);

    boolean isSelected(){
        return selected;
    }




}
