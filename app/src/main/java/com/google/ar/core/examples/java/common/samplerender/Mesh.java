/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.examples.java.common.samplerender;

import android.opengl.GLES30;
import android.util.Log;

import com.google.ar.core.examples.java.common.colladaParser.ColladaParser;
import com.google.ar.core.examples.java.common.animation.Animation;
import com.google.ar.core.examples.java.common.animation.Animator;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


/**
 * A collection of vertices, faces, and other attributes that define how to render a 3D object.
 *
 * <p>To render the mesh, use {@link SampleRender#draw(Mesh, Shader)}.
 */
public class Mesh implements Closeable {
  private static final String TAG = Mesh.class.getSimpleName();

  /**
   * The kind of primitive to render.
   *
   * <p>This determines how the data in {@link VertexBuffer}s are interpreted. See <a
   * href="https://www.khronos.org/opengl/wiki/Primitive">here</a> for more on how primitives
   * behave.
   */
  public enum PrimitiveMode {
    POINTS(GLES30.GL_POINTS),
    LINE_STRIP(GLES30.GL_LINE_STRIP),
    LINE_LOOP(GLES30.GL_LINE_LOOP),
    LINES(GLES30.GL_LINES),
    TRIANGLE_STRIP(GLES30.GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GLES30.GL_TRIANGLE_FAN),
    TRIANGLES(GLES30.GL_TRIANGLES);

    /* package-private */
    final int glesEnum;

    private PrimitiveMode(int glesEnum) {
      this.glesEnum = glesEnum;
    }
  }

  private final int[] vertexArrayId = {0};
  private final PrimitiveMode primitiveMode;
  private final IndexBuffer indexBuffer;
  private final VertexBuffer[] vertexBuffers;

  // skeleton
  private final Joint rootJoint;
  private final int jointCount;

  private final Animator animator;


  /**
   * Construct a {@link Mesh}.
   *
   * <p>The data in the given {@link IndexBuffer} and {@link VertexBuffer}s does not need to be
   * finalized; they may be freely changed throughout the lifetime of a {@link Mesh} using their
   * respective {@code set()} methods.
   *
   * <p>The ordering of the {@code vertexBuffers} is significant. Their array indices will
   * correspond to their attribute locations, which must be taken into account in shader code. The
   * <a href="https://www.khronos.org/opengl/wiki/Layout_Qualifier_(GLSL)">layout qualifier</a> must
   * be used in the vertex shader code to explicitly associate attributes with these indices.
   */
  public Mesh(
      SampleRender render,
      PrimitiveMode primitiveMode,
      IndexBuffer indexBuffer,
      VertexBuffer[] vertexBuffers) {
    if (vertexBuffers == null || vertexBuffers.length == 0) {
      throw new IllegalArgumentException("Must pass at least one vertex buffer");
    }

    this.jointCount = 0;
    this.rootJoint = null;
    this.animator = null;
    this.primitiveMode = primitiveMode;
    this.indexBuffer = indexBuffer;
    this.vertexBuffers = vertexBuffers;

    try {
      // Create vertex array
      GLES30.glGenVertexArrays(1, vertexArrayId, 0);
      GLError.maybeThrowGLException("Failed to generate a vertex array", "glGenVertexArrays");

      // Bind vertex array
      GLES30.glBindVertexArray(vertexArrayId[0]);
      GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");

      if (indexBuffer != null) {
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
      }

      for (int i = 0; i < vertexBuffers.length; ++i) {
        // Bind each vertex buffer to vertex array
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffers[i].getBufferId());
        GLError.maybeThrowGLException("Failed to bind vertex buffer", "glBindBuffer");
        if(vertexBuffers[i].getIsInt()) {
          GLES30.glVertexAttribIPointer(
                  i, vertexBuffers[i].getNumberOfEntriesPerVertex(), GLES30.GL_INT, 0, 0);
        } else {
          GLES30.glVertexAttribPointer(
                  i, vertexBuffers[i].getNumberOfEntriesPerVertex(), GLES30.GL_FLOAT, false, 0, 0);
        }
        GLError.maybeThrowGLException(
            "Failed to associate vertex buffer with vertex array", "glVertexAttribPointer");
        GLES30.glEnableVertexAttribArray(i);
        GLError.maybeThrowGLException(
            "Failed to enable vertex buffer", "glEnableVertexAttribArray");
      }
    } catch (Throwable t) {
      close();
      throw t;
    }
  }

  /** Construct Mesh from DAE file. **/
  public Mesh(
          SampleRender render,
          PrimitiveMode primitiveMode,
          IndexBuffer indexBuffer,
          VertexBuffer[] vertexBuffers,
          Joint rootJoint,
          int jointCount) {
    if (vertexBuffers == null || vertexBuffers.length == 0) {
      throw new IllegalArgumentException("Must pass at least one vertex buffer");
    }

    this.animator = new Animator(this);
    this.jointCount = jointCount;
    this.rootJoint = rootJoint;

    this.primitiveMode = primitiveMode;
    this.indexBuffer = indexBuffer;
    this.vertexBuffers = vertexBuffers;

    try {
      // Create vertex array
      GLES30.glGenVertexArrays(1, vertexArrayId, 0);
      GLError.maybeThrowGLException("Failed to generate a vertex array", "glGenVertexArrays");

      // Bind vertex array
      GLES30.glBindVertexArray(vertexArrayId[0]);
      GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");

      if (indexBuffer != null) {
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
      }

      for (int i = 0; i < vertexBuffers.length; ++i) {
        // Bind each vertex buffer to vertex array
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffers[i].getBufferId());
        GLError.maybeThrowGLException("Failed to bind vertex buffer", "glBindBuffer");
        if(vertexBuffers[i].getIsInt()) {
          GLES30.glVertexAttribIPointer(
                  i, vertexBuffers[i].getNumberOfEntriesPerVertex(), GLES30.GL_INT, 0, 0);
        } else {
          GLES30.glVertexAttribPointer(
                  i, vertexBuffers[i].getNumberOfEntriesPerVertex(), GLES30.GL_FLOAT, false, 0, 0);
        }
        GLError.maybeThrowGLException(
                "Failed to associate vertex buffer with vertex array", "glVertexAttribPointer");
        GLES30.glEnableVertexAttribArray(i);
        GLError.maybeThrowGLException(
                "Failed to enable vertex buffer", "glEnableVertexAttribArray");
      }
    } catch (Throwable t) {
      close();
      throw t;
    }
  }

  /**
   * Constructs a {@link Mesh} from the given Collada DAE file.
   *
   * <p>The {@link Mesh} will be constructed with 5 attributes, indexed in the order of local
   * coordinates (location 0, vec3), texture coordinates (location 1, vec2), vertex normals
   * (location 2, vec3), joint IDS (location 3, ivec3) and vertex weights (location 4, vec3).
   */
  public static Mesh createFromDAEAsset(SampleRender render, String assetFileName) throws IOException {
    // remove if not needed
    try (InputStream inputStream = render.getAssets().open(assetFileName)) {

      ColladaParser daeData = new ColladaParser(inputStream);


      // Obtain the data from the DAE, as direct buffers:
      // Index Buffer data
      IntBuffer vertexIndices = daeData.getFaceVertexIndices(/*numVerticesPerFace=*/ 3);
      // Vertex Buffer data
      FloatBuffer localCoordinates = daeData.getVertices();
      FloatBuffer textureCoordinates = daeData.getTexCoords(/*dimensions=*/ 2);
      FloatBuffer normals = daeData.getNormals();
      IntBuffer jointIDs = daeData.getJointIDs();
      FloatBuffer vertexWeights = daeData.getVertexWeights();
      Joint rJoint = daeData.getRootJoint();
      int jCount = daeData.getJointCount();


      // Create GPU buffers
      VertexBuffer[] vertexBuffers = {
              new VertexBuffer(render, 3, localCoordinates),
              new VertexBuffer(render, 2, textureCoordinates),
              new VertexBuffer(render, 3, normals),
              new VertexBuffer(render, 3, jointIDs, true),
              new VertexBuffer(render, 3, vertexWeights),
      };

      IndexBuffer indexBuffer = new IndexBuffer(render, vertexIndices);

      return new Mesh(render, Mesh.PrimitiveMode.TRIANGLES, indexBuffer, vertexBuffers, rJoint, jCount);
    }
  }

  /**
   * Constructs a {@link Mesh} from the given Wavefront OBJ file.
   *
   * <p>The {@link Mesh} will be constructed with three attributes, indexed in the order of local
   * coordinates (location 0, vec3), texture coordinates (location 1, vec2), and vertex normals
   * (location 2, vec3).
   */
  public static Mesh createFromAsset(SampleRender render, String assetFileName) throws IOException {
    try (InputStream inputStream = render.getAssets().open(assetFileName)) {
      Obj obj = ObjUtils.convertToRenderable(ObjReader.read(inputStream));

      // Obtain the data from the OBJ, as direct buffers:
      IntBuffer vertexIndices = ObjData.getFaceVertexIndices(obj, /*numVerticesPerFace=*/ 3);
      FloatBuffer localCoordinates = ObjData.getVertices(obj);
      FloatBuffer textureCoordinates = ObjData.getTexCoords(obj, /*dimensions=*/ 2);
      FloatBuffer normals = ObjData.getNormals(obj);

      // Create GPU buffers
      VertexBuffer[] vertexBuffers = {
              new VertexBuffer(render, 3, localCoordinates),
              new VertexBuffer(render, 2, textureCoordinates),
              new VertexBuffer(render, 3, normals),
      };

      IndexBuffer indexBuffer = new IndexBuffer(render, vertexIndices);

      return new Mesh(render, Mesh.PrimitiveMode.TRIANGLES, indexBuffer, vertexBuffers);
    }
  }

  @Override
  public void close() {
    if (vertexArrayId[0] != 0) {
      GLES30.glDeleteVertexArrays(1, vertexArrayId, 0);
      GLError.maybeLogGLError(
          Log.WARN, TAG, "Failed to free vertex array object", "glDeleteVertexArrays");
    }
  }


  /**
   * @return The root joint of the joint hierarchy. This joint has no parent,
   *         and every other joint in the skeleton is a descendant of this
   *         joint.
   */
  public Joint getRootJoint() {
    return rootJoint;
  }

  /**
   * Instructs this entity to carry out a given animation. To do this it
   * basically sets the chosen animation as the current animation in the
   * {@link Animator} object.
   *
   * @param animation
   *            - the animation to be carried out.
   */
  public void doAnimation(Animation animation) {
    animator.doAnimation(animation);
  }

  /**
   * Updates the animator for this entity, basically updating the animated
   * pose of the entity. Must be called every frame.
   */
  public void update() {
    animator.update();
  }

  /**
   * Gets an array of the all important model-space transforms of all the
   * joints (with the current animation pose applied) in the entity. The
   * joints are ordered in the array based on their joint index. The position
   * of each joint's transform in the array is equal to the joint's index.
   *
   * @return The array of model-space transforms of the joints in the current
   *         animation pose.
   */
  public float[][] getJointTransforms() {
    float[][] jointMatrices = new float[jointCount][];
    addJointsToArray(rootJoint, jointMatrices);
    return jointMatrices;
  }

  /**
   * This adds the current model-space transform of a joint (and all of its
   * descendants) into an array of transforms. The joint's transform is added
   * into the array at the position equal to the joint's index.
   *
   * @param headJoint
   *            - the current joint being added to the array. This method also
   *            adds the transforms of all the descendents of this joint too.
   * @param jointMatrices
   *            - the array of joint transforms that is being filled.
   */
  private void addJointsToArray(Joint headJoint, float[][] jointMatrices) {
    jointMatrices[headJoint.index] = headJoint.getAnimatedTransform();
    for (Joint childJoint : headJoint.children) {
      addJointsToArray(childJoint, jointMatrices);
    }
  }

  /**
   * Draws the mesh. Don't call this directly unless you are doing low level OpenGL code; instead,
   * prefer {@link SampleRender#draw}.
   */
  public void lowLevelDraw() {
    if (vertexArrayId[0] == 0) {
      throw new IllegalStateException("Tried to draw a freed Mesh");
    }

    GLES30.glBindVertexArray(vertexArrayId[0]);
    GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");
    if (indexBuffer == null) {
      // Sanity check for debugging
      int numberOfVertices = vertexBuffers[0].getNumberOfVertices();
      for (int i = 1; i < vertexBuffers.length; ++i) {
        if (vertexBuffers[i].getNumberOfVertices() != numberOfVertices) {
          throw new IllegalStateException("Vertex buffers have mismatching numbers of vertices");
        }
      }
      GLES30.glDrawArrays(primitiveMode.glesEnum, 0, numberOfVertices);
      GLError.maybeThrowGLException("Failed to draw vertex array object", "glDrawArrays");
    } else {
      GLES30.glDrawElements(
          primitiveMode.glesEnum, indexBuffer.getSize(), GLES30.GL_UNSIGNED_INT, 0);
      GLError.maybeThrowGLException(
          "Failed to draw vertex array object with indices", "glDrawElements");
    }
  }
}
