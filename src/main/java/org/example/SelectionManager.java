package org.example;

public class SelectionManager {
    private SceneObject selectedObject;

    public SceneObject getSelectedObject(){
        return selectedObject;
    }

    public void select(SceneObject obj){
        selectedObject = obj;
    }

    public void clear(){
        selectedObject = null;
    }

    public boolean isSelected(SceneObject obj){
        return selectedObject == obj;
    }

    public SceneObject getSelected(){
        return selectedObject;
    }
}
