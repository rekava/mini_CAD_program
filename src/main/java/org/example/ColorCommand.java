package org.example;

import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class ColorCommand implements Command {
    private List<Shape> shapes;
    private List<Vector3f> oldColors;
    private Vector3f newColor;

    public ColorCommand(List<Shape> shapes, float[] color) {
        this.shapes = new ArrayList<>(shapes);
        this.newColor = new Vector3f(color[0], color[1], color[2]);
        this.oldColors = new ArrayList<>();

        for (Shape shape : shapes) {
            oldColors.add(new Vector3f(shape.color));
        }
    }

    @Override
    public void execute() {
        for (Shape shape : shapes) {
            shape.color.set(newColor);
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).color.set(oldColors.get(i));
        }
    }
}
