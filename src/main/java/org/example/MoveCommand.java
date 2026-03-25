package org.example;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class MoveCommand implements Command {
    private List<Shape> shapes;
    private List<Vector2f> oldPositions;
    private List<Vector2f> newPositions;

    public MoveCommand(List<Shape> shapes, float dx, float dy) {
        this.shapes = new ArrayList<>(shapes);
        this.oldPositions = new ArrayList<>();
        this.newPositions = new ArrayList<>();

        for (Shape shape : shapes) {
            oldPositions.add(new Vector2f(shape.transform.position));
            newPositions.add(new Vector2f(shape.transform.position.x + dx, shape.transform.position.y + dy));
        }
    }

    @Override
    public void execute() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.position.set(newPositions.get(i));
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.position.set(oldPositions.get(i));
        }
    }
}
