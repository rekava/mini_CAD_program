package org.example;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;


public class Renderer {

    Scene currentScene;

    Renderer(){

    }

    Renderer(Scene scene){
        this.currentScene = scene;
    }

    void setScene(Scene scene){
        this.currentScene = scene;
    }

    void render(Camera camera){
        for (SceneObject sceneObject: currentScene.getSceneObjectList()){
            sceneObject.render(camera);
        }
    }

    void clear(){
        glClearColor(0,0,0,1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    }

}





















