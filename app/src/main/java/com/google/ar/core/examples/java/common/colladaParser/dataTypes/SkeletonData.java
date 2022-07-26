package com.google.ar.core.examples.java.common.colladaParser.dataTypes;

public class SkeletonData {

    public final int jointCount;
    public final JointData headJoint;

    public SkeletonData(int jointCount, JointData headJoint){
        this.jointCount = jointCount;
        this.headJoint = headJoint;
    }

}
