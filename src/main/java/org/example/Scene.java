package org.example;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private List<SceneObject> sceneObjectList = new ArrayList<SceneObject>();

    void add(SceneObject sceneObject){
        sceneObjectList.add(sceneObject);
    }
    void remove(SceneObject sceneObject){
        sceneObjectList.remove(sceneObject);
    }
    void  remove(int index){
        sceneObjectList.remove(index);
    }

    List<SceneObject> getSceneObjectList(){
        return sceneObjectList;
    }


}
