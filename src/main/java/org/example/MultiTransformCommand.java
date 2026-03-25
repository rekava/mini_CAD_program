package org.example;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class MultiTransformCommand implements Command {
    private List<Shape> shapes;
    private List<Vector2f> oldPositions;
    private List<Vector2f> newPositions;
    private List<Vector2f> oldScales;
    private List<Vector2f> newScales;
    private List<Float> oldRotations;
    private List<Float> newRotations;

    public MultiTransformCommand(List<Shape> shapes,
                                 List<Vector2f> oldPositions, List<Vector2f> oldScales, List<Float> oldRotations,
                                 List<Vector2f> newPositions, List<Vector2f> newScales, List<Float> newRotations) {
        this.shapes = new ArrayList<>(shapes);
        this.oldPositions = new ArrayList<>(oldPositions);
        this.oldScales = new ArrayList<>(oldScales);
        this.oldRotations = new ArrayList<>(oldRotations);
        this.newPositions = new ArrayList<>(newPositions);
        this.newScales = new ArrayList<>(newScales);
        this.newRotations = new ArrayList<>(newRotations);
    }

    @Override
    public void execute() {
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
