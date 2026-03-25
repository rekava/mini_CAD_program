package org.example;

public class CreateShapeCommand implements Command {
    private Scene scene;
    private Shape shape;

    public CreateShapeCommand(Scene scene, Shape shape) {
        this.scene = scene;
        this.shape = shape;
    }

    @Override
    public void execute() {
        scene.add(shape);
    }

    @Override
    public void undo() {
        scene.remove(shape);
    }
}
