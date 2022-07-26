package com.google.ar.core.examples.java.common.colladaParser;

import android.opengl.Matrix;

import com.google.ar.core.examples.java.common.colladaParser.dataTypes.MeshData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.Vertex;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.VertexSkinData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.XmlNode;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;


/**
 * Loads the mesh data for a model from a collada XML file.
 * @author Karl
 *
 */
public class GeometryLoader {

    private static final float[] CORRECTION = new float[16];

    private final XmlNode meshData;

    private final List<VertexSkinData> vertexWeights;

    private float[] verticesArray;
    private float[] normalsArray;
    private float[] texturesArray;
    private int[] indicesArray;
    private int[] jointIdsArray;
    private float[] weightsArray;

    List<Vertex> vertices = new ArrayList<Vertex>();
    List<float[]> textures = new ArrayList<float[]>();
    List<float[]> normals = new ArrayList<float[]>();
    List<Integer> indices = new ArrayList<Integer>();

    public GeometryLoader(XmlNode geometryNode, List<VertexSkinData> vertexWeights) {
        Matrix.rotateM(CORRECTION,0, (float) Math.toRadians(-90), 1, 0, 0);
        this.vertexWeights = vertexWeights;
        this.meshData = geometryNode.getChild("geometry").getChild("mesh");
    }

    public MeshData extractModelData(){
        readRawData();
        assembleVertices();
        removeUnusedVertices();
        initArrays();
        convertDataToArrays();
        convertIndicesListToArray();
        return new MeshData(verticesArray, texturesArray, normalsArray, indicesArray, jointIdsArray, weightsArray);
    }

    private void readRawData() {
        readPositions();
        readNormals();
        readTextureCoords();
    }

    private void readPositions() {
        String positionsId = meshData.getChild("vertices").getChild("input").getAttribute("source").substring(1);
        XmlNode positionsData = meshData.getChildWithAttribute("source", "id", positionsId).getChild("float_array");
        int count = Integer.parseInt(positionsData.getAttribute("count"));
        String[] posData = positionsData.getData().split(" ");
        for (int i = 0; i < count/3; i++) {
            float x = Float.parseFloat(posData[i * 3]);
            float y = Float.parseFloat(posData[i * 3 + 1]);
            float z = Float.parseFloat(posData[i * 3 + 2]);
            float[] position = new float[]{x, y, z, 1f};
            // Matrix math
            position = transform(CORRECTION, position, position);
            vertices.add(new Vertex(vertices.size(), new float[]{position[0], position[1], position[2]}, vertexWeights.get(vertices.size())));
        }
    }

    private void readNormals() {
        String normalsId = meshData.getChild("triangles").getChildWithAttribute("input", "semantic", "NORMAL")
                .getAttribute("source").substring(1);
        XmlNode normalsData = meshData.getChildWithAttribute("source", "id", normalsId).getChild("float_array");
        int count = Integer.parseInt(normalsData.getAttribute("count"));
        String[] normData = normalsData.getData().split(" ");
        for (int i = 0; i < count/3; i++) {
            float x = Float.parseFloat(normData[i * 3]);
            float y = Float.parseFloat(normData[i * 3 + 1]);
            float z = Float.parseFloat(normData[i * 3 + 2]);
            float[] norm = new float[]{x, y, z, 0f};
            // Matrix math
            norm = transform(CORRECTION, norm, norm);
            normals.add(new float[]{norm[0], norm[1], norm[2]});
        }
    }

    private void readTextureCoords() {
        String texCoordsId = meshData.getChild("triangles").getChildWithAttribute("input", "semantic", "TEXCOORD")
                .getAttribute("source").substring(1);
        XmlNode texCoordsData = meshData.getChildWithAttribute("source", "id", texCoordsId).getChild("float_array");
        int count = Integer.parseInt(texCoordsData.getAttribute("count"));
        String[] texData = texCoordsData.getData().split(" ");
        for (int i = 0; i < count/2; i++) {
            float s = Float.parseFloat(texData[i * 2]);
            float t = Float.parseFloat(texData[i * 2 + 1]);
            textures.add(new float[]{s, t});
        }
    }

    private void assembleVertices(){
        XmlNode poly = meshData.getChild("triangles");
        int typeCount = poly.getChildren("input").size();
        String[] indexData = poly.getChild("p").getData().split(" ");
        for(int i=0;i<indexData.length/typeCount;i++){
            int positionIndex = Integer.parseInt(indexData[i * typeCount]);
            int normalIndex = Integer.parseInt(indexData[i * typeCount + 1]);
            int texCoordIndex = Integer.parseInt(indexData[i * typeCount + 2]);
            processVertex(positionIndex, normalIndex, texCoordIndex);
        }
    }


    private Vertex processVertex(int posIndex, int normIndex, int texIndex) {
        Vertex currentVertex = vertices.get(posIndex);
        if (!currentVertex.isSet()) {
            currentVertex.setTextureIndex(texIndex);
            currentVertex.setNormalIndex(normIndex);
            indices.add(posIndex);
            return currentVertex;
        } else {
            return dealWithAlreadyProcessedVertex(currentVertex, texIndex, normIndex);
        }
    }

    private int[] convertIndicesListToArray() {
        this.indicesArray = new int[indices.size()];
        for (int i = 0; i < indicesArray.length; i++) {
            indicesArray[i] = indices.get(i);
        }
        return indicesArray;
    }

    private float convertDataToArrays() {
        float furthestPoint = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Vertex currentVertex = vertices.get(i);
            if (currentVertex.getLength() > furthestPoint) {
                furthestPoint = currentVertex.getLength();
            }
            float[] position = currentVertex.getPosition();
            float[] textureCoord = textures.get(currentVertex.getTextureIndex());
            float[] normalVector = normals.get(currentVertex.getNormalIndex());
            verticesArray[i * 3] = position[0];
            verticesArray[i * 3 + 1] = position[1];
            verticesArray[i * 3 + 2] = position[2];
            texturesArray[i * 2] = textureCoord[0];
            texturesArray[i * 2 + 1] = 1 - textureCoord[1];
            normalsArray[i * 3] = normalVector[0];
            normalsArray[i * 3 + 1] = normalVector[1];
            normalsArray[i * 3 + 2] = normalVector[2];
            VertexSkinData weights = currentVertex.getWeightsData();
            jointIdsArray[i * 3] = weights.jointIds.get(0);
            jointIdsArray[i * 3 + 1] = weights.jointIds.get(1);
            jointIdsArray[i * 3 + 2] = weights.jointIds.get(2);
            weightsArray[i * 3] = weights.weights.get(0);
            weightsArray[i * 3 + 1] = weights.weights.get(1);
            weightsArray[i * 3 + 2] = weights.weights.get(2);

        }
        return furthestPoint;
    }

    private Vertex dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex, int newNormalIndex) {
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            indices.add(previousVertex.getIndex());
            return previousVertex;
        } else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null) {
                return dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex);
            } else {
                Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition(), previousVertex.getWeightsData());
                duplicateVertex.setTextureIndex(newTextureIndex);
                duplicateVertex.setNormalIndex(newNormalIndex);
                previousVertex.setDuplicateVertex(duplicateVertex);
                vertices.add(duplicateVertex);
                indices.add(duplicateVertex.getIndex());
                return duplicateVertex;
            }

        }
    }

    private void initArrays(){
        this.verticesArray = new float[vertices.size() * 3];
        this.texturesArray = new float[vertices.size() * 2];
        this.normalsArray = new float[vertices.size() * 3];
        this.jointIdsArray = new int[vertices.size() * 3];
        this.weightsArray = new float[vertices.size() * 3];
    }

    private void removeUnusedVertices() {
        for (Vertex vertex : vertices) {
            //vertex.averageTangents();
            if (!vertex.isSet()) {
                vertex.setTextureIndex(0);
                vertex.setNormalIndex(0);
            }
        }
    }


    /**
     * 4x4 matrix to float array helper
    m00 = matrix[0];
    m01 = matrix[4];
    m02 = matrix[8];
    m03 = matrix[12];
    m10 = matrix[1];
    m11 = matrix[5];
    m12 = matrix[9];
    m13 = matrix[13];
    m20 = matrix[2];
    m21 = matrix[6];
    m22 = matrix[10];
    m23 = matrix[14];
    m30 = matrix[3];
    m31 = matrix[7];
    m32 = matrix[11];
    m33 = matrix[15];

     * Transform a Vector by a matrix and return the result in a destination
     * vector.
     * @param left The left matrix
     * @param right The right vector
     * @param dest The destination vector, or null if a new one is to be created
     * @return the destination vector
     */
    private float[] transform(float[] left, float[] right, float[] dest) {
        if (dest == null)
            dest = new float[4];

        // Math might not be correct here

        float x = left[0] * right[0] + left[1] * right[1] + left[2] * right[2] + left[3] * right[3];
        float y = left[4] * right[0] + left[5] * right[1] + left[6] * right[2] + left[7] * right[3];
        float z = left[8] * right[0] + left[9] * right[1] + left[10] * right[2] + left[11] * right[3];
        float w = left[12] * right[0] + left[13] * right[1] + left[14] * right[2] + left[15] * right[3];

        dest[0] = x;
        dest[1] = y;
        dest[2] = z;
        dest[3] = w;

        return dest;
    }

}
