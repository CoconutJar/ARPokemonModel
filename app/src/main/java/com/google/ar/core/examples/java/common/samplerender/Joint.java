package com.google.ar.core.examples.java.common.samplerender;

import java.util.ArrayList;
import java.util.List;

public class Joint {
    public final int index;// ID
    public final String name;
    public final List<Joint> children = new ArrayList<Joint>();

    private float[] animatedTransform;

    // Fix ?
    private final float[] localBindTransform;
    private float[] inverseBindTransform;

    /**
     * @param index
     *            - the joint's index (ID).
     * @param name
     *            - the name of the joint. This is how the joint is named in the
     *            collada file, and so is used to identify which joint a joint
     *            transform in an animation keyframe refers to.
     * @param bindLocalTransform
     *            - the bone-space transform of the joint in the bind position.
     */
    public Joint(int index, String name, float[] bindLocalTransform) {
        this.index = index;
        this.name = name;
        this.localBindTransform = bindLocalTransform;
    }

    /**
     * Adds a child joint to this joint. Used during the creation of the joint
     * hierarchy. Joints can have multiple children, which is why they are
     * stored in a list (e.g. a "hand" joint may have multiple "finger" children
     * joints).
     *
     * @param child
     *            - the new child joint of this joint.
     */
    public void addChild(Joint child) {
        this.children.add(child);
    }

    /**
     * The animated transform is the transform that gets loaded up to the shader
     * and is used to deform the vertices of the "skin". It represents the
     * transformation from the joint's bind position (original position in
     * model-space) to the joint's desired animation pose (also in model-space).
     * This matrix is calculated by taking the desired model-space transform of
     * the joint and multiplying it by the inverse of the starting model-space
     * transform of the joint.
     *
     * @return The transformation matrix of the joint which is used to deform
     *         associated vertices of the skin in the shaders.
     */
    public float[] getAnimatedTransform() {
        return animatedTransform;
    }

    /**
     * This method allows those all important "joint transforms" (as I referred
     * to them in the tutorial) to be set by the animator. This is used to put
     * the joints of the animated model in a certain pose.
     *
     * @param animationTransform - the new joint transform.
     */
    public void setAnimationTransform(float[] animationTransform) {
        this.animatedTransform = animationTransform;
    }

    /**
     * This returns the inverted model-space bind transform. The bind transform
     * is the original model-space transform of the joint (when no animation is
     * applied). This returns the inverse of that, which is used to calculate
     * the animated transform matrix which gets used to transform vertices in
     * the shader.
     *
     * @return The inverse of the joint's bind transform (in model-space).
     */
    public float[] getInverseBindTransform() {
        return inverseBindTransform;
    }
}
