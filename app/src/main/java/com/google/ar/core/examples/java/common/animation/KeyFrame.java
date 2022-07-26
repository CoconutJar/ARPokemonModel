package com.google.ar.core.examples.java.common.animation;

import java.util.Map;

public class KeyFrame {

    private final float timeStamp;
    private final Map<String, JointTransform> pose;

    public KeyFrame(float timeS, Map<String, JointTransform> jointKeyFrames){
        this.timeStamp = timeS;
        this.pose = jointKeyFrames;
    }

    protected float getTimeStamp(){
        return  timeStamp;
    }

    protected Map<String, JointTransform> getPose(){
        return  pose;
    }
}
