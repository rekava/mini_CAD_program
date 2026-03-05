package org.example;

import org.joml.*;

public class Transform {
    public Vector2f position = new Vector2f(0f,0f);
    public float rotation = 0f;
    public Vector2f scale = new Vector2f(1f,1f);

    private Matrix4f model = new Matrix4f();

    public Matrix4f getModelMatrix(){
        return model.identity().translate(position.x,position.y,0f)
                .rotateZ(rotation)
                .scale(scale.x,scale.y,1f);
    }

    public void setPosition(float x, float y){
        this.position.x = x;
        this.position.y = y;
    }

    public void setPosition(Vector2f newPosition){
        this.position = newPosition;
    }

    public void  translate(float dx,float dy){
        this.position.x += dx;
        this.position.y += dy;

    }

    public Vector2f getPosition(){
        return  new Vector2f(position);
    }

}
