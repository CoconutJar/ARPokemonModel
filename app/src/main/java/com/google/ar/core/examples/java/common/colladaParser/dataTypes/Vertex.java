package com.google.ar.core.examples.java.common.colladaParser.dataTypes;

import java.util.ArrayList;
import java.util.List;


public class Vertex {

    private static final int NO_INDEX = -1;

    private float[] position;
    private int textureIndex = NO_INDEX;
    private int normalIndex = NO_INDEX;
    private Vertex duplicateVertex = null;
    private int index;
    private float length;
    private List<float[]> tangents = new ArrayList<float[]>();
    private float[] averagedTangent;


    private VertexSkinData weightsData;

    public Vertex(int index, float[] position, VertexSkinData weightsData){
        this.index = index;
        this.weightsData = weightsData;
        this.position = position;
        this.length = position.length;
    }

    public VertexSkinData getWeightsData(){
        return weightsData;
    }

    public void addTangent(float[] tangent){
        tangents.add(tangent);
    }

//    public void averageTangents(){
//        if(tangents.isEmpty()){
//            return;
//        }
//        for(float[] tangent : tangents){
//            // vector math
//            Vector3f.add(averagedTangent, tangent, averagedTangent);
//        }
//        // more vector math
//        averagedTangent.normalise();
//    }

//    public float[] getAverageTangent(){
//        return averagedTangent;
//    }

    public int getIndex(){
        return index;
    }

    public float getLength(){
        return length;
    }

    public boolean isSet(){
        return textureIndex!=NO_INDEX && normalIndex!=NO_INDEX;
    }

    public boolean hasSameTextureAndNormal(int textureIndexOther,int normalIndexOther){
        return textureIndexOther==textureIndex && normalIndexOther==normalIndex;
    }

    public void setTextureIndex(int textureIndex){
        this.textureIndex = textureIndex;
    }

    public void setNormalIndex(int normalIndex){
        this.normalIndex = normalIndex;
    }

    public float[] getPosition() {
        return position;
    }

    public int getTextureIndex() {
        return textureIndex;
    }

    public int getNormalIndex() {
        return normalIndex;
    }

    public Vertex getDuplicateVertex() {
        return duplicateVertex;
    }

    public void setDuplicateVertex(Vertex duplicateVertex) {
        this.duplicateVertex = duplicateVertex;
    }

}
