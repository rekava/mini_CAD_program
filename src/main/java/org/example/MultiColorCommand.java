package org.example;

import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class MultiColorCommand implements Command {
    private List<Shape> shapes;
    private List<Vector3f> oldColors;
    private List<Vector3f> newColors;

    public MultiColorCommand(List<Shape> shapes, List<Vector3f> oldColors, List<Vector3f> newColors) {
        this.shapes = new ArrayList<>(shapes);
        this.oldColors = new ArrayList<>(oldColors);
        this.newColors = new ArrayList<>(newColors);
    }

    @Override
    public void execute() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).color.set(newColors.get(i));
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).color.set(oldColors.get(i));
        }
    }
}
