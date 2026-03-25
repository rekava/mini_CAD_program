package org.example;

import java.util.ArrayList;
import java.util.List;

public class DeleteCommand implements Command {
    private Scene scene;
    private List<Shape> shapes;
    private List<Shape> deletedShapes;

    public DeleteCommand(Scene scene, List<Shape> shapes) {
        this.scene = scene;
        this.shapes = new ArrayList<>(shapes);
        this.deletedShapes = new ArrayList<>();
    }

    public DeleteCommand(Scene scene, Shape shape) {
        this.scene = scene;
        this.shapes = new ArrayList<>();
        this.shapes.add(shape);
        this.deletedShapes = new ArrayList<>();
    }

    @Override
    public void execute() {
        deletedShapes.clear();
        for (Shape shape : shapes) {
            if (scene.getSceneObjectList().contains(shape)) {
                scene.remove(shape);
                deletedShapes.add(shape);
            }
        }
    }

    @Override
    public void undo() {
        for (Shape shape : deletedShapes) {
            scene.add(shape);
        }
    }
}
