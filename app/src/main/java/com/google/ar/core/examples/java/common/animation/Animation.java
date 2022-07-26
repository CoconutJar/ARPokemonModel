package com.google.ar.core.examples.java.common.animation;

// "model/representation" of an Animation that can be used on a mesh
public class Animation {
    private final KeyFrame[] keyFrames;
    private final float length;

    public Animation(KeyFrame[] keys, float len){
        this.keyFrames = keys;
        this.length = len;
    }

    public KeyFrame[] getKeyFrames() {
        return keyFrames;
    }

    public float getLength() {
        return length;
    }
}
