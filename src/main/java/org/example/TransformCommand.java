package org.example;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class TransformCommand implements Command {
    private List<Shape> shapes;
    private List<Vector2f> oldPositions;
    private List<Vector2f> newPositions;
    private List<Vector2f> oldScales;
    private List<Vector2f> newScales;
    private List<Float> oldRotations;
    private List<Float> newRotations;
    private boolean hasChanges;

    public TransformCommand(List<Shape> shapes) {
        this.shapes = new ArrayList<>(shapes);
        this.oldPositions = new ArrayList<>();
        this.newPositions = new ArrayList<>();
        this.oldScales = new ArrayList<>();
        this.newScales = new ArrayList<>();
        this.oldRotations = new ArrayList<>();
        this.newRotations = new ArrayList<>();
        this.hasChanges = false;

        // Сохраняем начальное состояние
        for (Shape shape : shapes) {
            oldPositions.add(new Vector2f(shape.transform.position));
            oldScales.add(new Vector2f(shape.transform.scale));
            oldRotations.add(shape.transform.rotation);
        }
    }

    public void captureNewState() {
        newPositions.clear();
        newScales.clear();
        newRotations.clear();

        for (Shape shape : shapes) {
            newPositions.add(new Vector2f(shape.transform.position));
            newScales.add(new Vector2f(shape.transform.scale));
            newRotations.add(shape.transform.rotation);
        }

        // Проверяем, были ли изменения
        hasChanges = false;
        for (int i = 0; i < shapes.size(); i++) {
            if (!oldPositions.get(i).equals(newPositions.get(i)) ||
                !oldScales.get(i).equals(newScales.get(i)) ||
                Math.abs(oldRotations.get(i) - newRotations.get(i)) > 0.001f) {
                hasChanges = true;
                break;
            }
        }
    }

    public boolean hasChanges() {
        return hasChanges;
    }

    @Override
    public void execute() {
        if (!hasChanges) return;
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.position.set(newPositions.get(i));
            shapes.get(i).transform.scale.set(newScales.get(i));
            shapes.get(i).transform.rotation = newRotations.get(i);
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.position.set(oldPositions.get(i));
            shapes.get(i).transform.scale.set(oldScales.get(i));
            shapes.get(i).transform.rotation = oldRotations.get(i);
        }
    }
}
