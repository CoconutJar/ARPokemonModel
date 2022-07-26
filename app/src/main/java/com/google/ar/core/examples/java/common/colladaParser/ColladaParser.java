package com.google.ar.core.examples.java.common.colladaParser;

import com.google.ar.core.examples.java.common.colladaParser.dataTypes.JointData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.XmlNode;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.XmlParser;
import com.google.ar.core.examples.java.common.samplerender.Joint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.ar.core.examples.java.common.colladaParser.dataTypes.MeshData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.SkeletonData;
import com.google.ar.core.examples.java.common.colladaParser.dataTypes.SkinningData;

public class ColladaParser {

    private IntBuffer fvi = null;
    private FloatBuffer vertices = null;
    private FloatBuffer texCoords = null;
    private FloatBuffer normals = null;
    private IntBuffer jointIDs = null;
    private FloatBuffer vw = null;
    private Joint rootJoint = null;

    private int maxWeights = 3;

    public ColladaParser(InputStream path){
        try {
            // Assign the Class Variables the values from the Collada DAE file.
            //DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            //DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            //Document doc = dBuilder.parse(inputStream);
            //doc.getDocumentElement().normalize();

            XmlNode node = XmlParser.loadXmlFile(path);

            SkinLoader skinLoader = new SkinLoader(node.getChild("library_controllers"), maxWeights);
            SkinningData skinningData = skinLoader.extractSkinData();

            SkeletonLoader jointsLoader = new SkeletonLoader(node.getChild("library_visual_scenes"), skinningData.jointOrder);
            SkeletonData jointsData = jointsLoader.extractBoneData();


            GeometryLoader g = new GeometryLoader(node.getChild("library_geometries"), skinningData.verticesSkinData);
            MeshData meshData = g.extractModelData();



            // Assign stuff
            // Saw allocateDirect(meshData.getIndices().length * 4) maybe needed
            //fvi = IntBuffer.allocate(meshData.getIndices().length);
            fvi = ByteBuffer.allocateDirect(meshData.getIndices().length * 4)
                    .order(ByteOrder.nativeOrder()).asIntBuffer();
            fvi.put(meshData.getIndices());
            fvi.rewind();

            //jointIDs = IntBuffer.allocate(meshData.getJointIds().length);
            jointIDs = ByteBuffer.allocateDirect(meshData.getJointIds().length * 4)
                    .order(ByteOrder.nativeOrder()).asIntBuffer();
            jointIDs.put(meshData.getJointIds());
            jointIDs.rewind();

            vertices =  ByteBuffer.allocateDirect(meshData.getVertices().length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            //vertices = FloatBuffer.allocate(meshData.getVertices().length);
            vertices.put(meshData.getVertices());
            vertices.rewind();

            //texCoords = FloatBuffer.allocate(meshData.getTextureCoords().length);
            texCoords = ByteBuffer.allocateDirect(meshData.getTextureCoords().length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            texCoords.put(meshData.getTextureCoords());
            texCoords.rewind();

            //normals = FloatBuffer.allocate(meshData.getNormals().length);
            normals = ByteBuffer.allocateDirect(meshData.getNormals().length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            normals.put(meshData.getNormals());
            normals.rewind();

            //vw = FloatBuffer.allocate(meshData.getVertexWeights().length);
            vw = ByteBuffer.allocateDirect(meshData.getVertexWeights().length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            vw.put(meshData.getVertexWeights());
            vw.rewind();


            rootJoint = createJoints(jointsData.headJoint);

        } catch (Exception e) {e.printStackTrace();}

    }

    Joint createJoints(JointData data){
        Joint joint = new Joint(data.index, data.nameId, data.bindLocalTransform);
        for (JointData child : data.children) {
            joint.addChild(createJoints(child));
        }
        return joint;
    }

    // Getters for mesh data
    public IntBuffer getFaceVertexIndices(int vertPerFace){
        return fvi;
    }
    public FloatBuffer getVertices(){
        return vertices;
    }
    public FloatBuffer getTexCoords(int d){
        return texCoords;
    }
    public FloatBuffer getNormals(){
        return normals;
    }
    public IntBuffer getJointIDs(){
        return jointIDs;
    }
    public FloatBuffer getVertexWeights(){
        return vw;
    }

    public Joint getRootJoint() {
        return rootJoint;
    }
    public int getJointCount() {
        return 0;
    }
}
