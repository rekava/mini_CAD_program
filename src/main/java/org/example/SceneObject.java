package org.example;

import jdk.swing.interop.DragSourceContextWrapper;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL20.*;
import  static org.lwjgl.opengl.GL33.*;

public abstract class SceneObject {

    public Transform transform = new Transform();
    private boolean selected;
    SceneObject(){
    }

    abstract void render();

    boolean isSelected(){
        return selected;
    }
}
