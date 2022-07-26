package com.google.ar.core.examples.java.common.colladaParser;


import android.opengl.Matrix;

import java.util.List;

import com.google.ar.core.examples.java.common.colladaParser.dataTypes.SkeletonData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.SkinningData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.JointData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.XmlNode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SkeletonLoader {

    private XmlNode armatureData;

    private List<String> boneOrder;

    private int jointCount = 0;

    private static final float[] CORRECTION = new float[16];


    public SkeletonLoader(XmlNode visualSceneNode, List<String> boneOrder) {
        Matrix.rotateM(CORRECTION,0, (float) Math.toRadians(-90), 1, 0, 0);
        this.armatureData = visualSceneNode.getChild("visual_scene").getChildWithAttribute("node", "id", "Armature");
        this.boneOrder = boneOrder;
    }

    public SkeletonData extractBoneData(){
        XmlNode headNode = armatureData.getChild("node");
        JointData headJoint = loadJointData(headNode, true);
        return new SkeletonData(jointCount, headJoint);
    }

    private JointData loadJointData(XmlNode jointNode, boolean isRoot){
        JointData joint = extractMainJointData(jointNode, isRoot);
        for(XmlNode childNode : jointNode.getChildren("node")){
            joint.addChild(loadJointData(childNode, false));
        }
        return joint;
    }

    private JointData extractMainJointData(XmlNode jointNode, boolean isRoot){
        String nameId = jointNode.getAttribute("id");
        int index = boneOrder.indexOf(nameId);
        String[] matrixData = jointNode.getChild("matrix").getData().split(" ");
        //Matrix4f matrix = new Matrix4f();
        float[] matrix = convertData(matrixData);
        Matrix.transposeM(matrix,0, matrix,0);
        if(isRoot){
            //because in Blender z is up, but in our game y is up.
            Matrix.multiplyMM(matrix, 0, CORRECTION, 0, matrix, 0);
        }
        jointCount++;
        return new JointData(index, nameId, matrix);
    }

    private float[] convertData(String[] rawData){
        float[] matrixData = new float[16];
        for(int i=0;i<matrixData.length;i++){
            matrixData[i] = Float.parseFloat(rawData[i]);
        }

        return matrixData;
    }

}
