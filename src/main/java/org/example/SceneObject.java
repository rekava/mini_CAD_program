package org.example;

public abstract class SceneObject {
    private String name = "New Object";
    public Transform transform = new Transform();
    private boolean selected;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public abstract void render(Camera camera);
    public abstract boolean containsPoint(float worldX, float worldY);
}