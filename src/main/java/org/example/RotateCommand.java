package org.example;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class RotateCommand implements Command {
    private List<Shape> shapes;
    private List<Float> oldRotations;
    private List<Float> newRotations;
    private List<Vector2f> oldPositions;
    private List<Vector2f> newPositions;
    private Vector2f center;

    public RotateCommand(List<Shape> shapes, float deltaRot) {
        this.shapes = new ArrayList<>(shapes);
        this.oldRotations = new ArrayList<>();
        this.newRotations = new ArrayList<>();
        this.oldPositions = new ArrayList<>();
        this.newPositions = new ArrayList<>();

        // Вычисляем центр
        center = new Vector2f(0, 0);
        for (Shape s : shapes) {
            center.add(s.transform.position);
        }
        center.div(shapes.size());

        float cos = (float) Math.cos(deltaRot);
        float sin = (float) Math.sin(deltaRot);

        for (Shape shape : shapes) {
            oldRotations.add(shape.transform.rotation);
            oldPositions.add(new Vector2f(shape.transform.position));

            newRotations.add(shape.transform.rotation + deltaRot);

            Vector2f relative = new Vector2f(shape.transform.position).sub(center);
            float newX = relative.x * cos - relative.y * sin;
            float newY = relative.x * sin + relative.y * cos;
            newPositions.add(new Vector2f(center.x + newX, center.y + newY));
        }
    }

    @Override
    public void execute() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.rotation = newRotations.get(i);
            shapes.get(i).transform.position.set(newPositions.get(i));
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.rotation = oldRotations.get(i);
            shapes.get(i).transform.position.set(oldPositions.get(i));
        }
    }
}
