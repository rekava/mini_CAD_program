package org.example;

import java.util.List;
import java.util.ArrayList;

public class PasteCommand implements Command {
    private Scene scene;
    private List<Shape> pastedShapes;

    public PasteCommand(Scene scene, List<Shape> pastedShapes) {
        this.scene = scene;
        this.pastedShapes = new ArrayList<>(pastedShapes);
    }

    @Override
    public void execute() {
        for (Shape shape : pastedShapes) {
            scene.add(shape);
        }
    }

    @Override
    public void undo() {
        for (Shape shape : pastedShapes) {
            scene.remove(shape);
        }
    }
}
