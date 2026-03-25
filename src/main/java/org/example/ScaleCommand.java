package org.example;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class ScaleCommand implements Command {
    private List<Shape> shapes;
    private List<Vector2f> oldScales;
    private List<Vector2f> newScales;
    private List<Vector2f> oldPositions;
    private List<Vector2f> newPositions;
    private Vector2f center;

    public ScaleCommand(List<Shape> shapes, float sx, float sy) {
        this.shapes = new ArrayList<>(shapes);
        this.oldScales = new ArrayList<>();
        this.newScales = new ArrayList<>();
        this.oldPositions = new ArrayList<>();
        this.newPositions = new ArrayList<>();

        // Вычисляем центр
        center = new Vector2f(0, 0);
        for (Shape s : shapes) {
            center.add(s.transform.position);
        }
        center.div(shapes.size());

        for (Shape shape : shapes) {
            oldScales.add(new Vector2f(shape.transform.scale));
            oldPositions.add(new Vector2f(shape.transform.position));

            Vector2f newScale = new Vector2f(shape.transform.scale.x * sx, shape.transform.scale.y * sy);
            newScales.add(newScale);

            Vector2f relative = new Vector2f(shape.transform.position).sub(center);
            relative.mul(sx, sy);
            newPositions.add(new Vector2f(center).add(relative));
        }
    }

    @Override
    public void execute() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.scale.set(newScales.get(i));
            shapes.get(i).transform.position.set(newPositions.get(i));
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).transform.scale.set(oldScales.get(i));
            shapes.get(i).transform.position.set(oldPositions.get(i));
        }
    }
}
