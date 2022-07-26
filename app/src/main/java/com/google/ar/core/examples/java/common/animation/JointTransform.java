package com.google.ar.core.examples.java.common.animation;

import android.opengl.Matrix;

public class JointTransform {
    private final float[] position;
    private final Quaternion rotation;

    /**
     *
     * @param pos
     *            - the position of the joint relative to the parent joint
     *            (bone-space) at a certain keyframe. For example, if this joint
     *            is at (5, 12, 0) in the model's coordinate system, and the
     *            parent of this joint is at (2, 8, 0), then the position of
     *            this joint relative to the parent is (3, 4, 0).
     * @param rotat
     *            - the rotation of the joint relative to the parent joint
     *            (bone-space) at a certain keyframe.
     */
    public JointTransform(float[] pos, Quaternion rotat){
        this.position = pos;
        this.rotation = rotat;
    }


    /**
     * In this method the bone-space transform matrix is constructed by
     * translating an identity matrix using the position variable and then
     * applying the rotation. The rotation is applied by first converting the
     * quaternion into a rotation matrix, which is then multiplied with the
     * transform matrix.
     *
     * @return This bone-space joint transform as a matrix. The exact same
     *         transform as represented by the position and rotation in this
     *         instance, just in matrix form.
     */
    protected float[] getLocalTransform() {
        float[] matrix = {};
        Matrix.translateM(matrix,0,position[0],position[1],position[2]);
        Matrix.multiplyMM(matrix,0, matrix,0,rotation.toRotationMatrix(),0);
        return matrix;
    }

    /**
     * Interpolates between two transforms based on the progression value. The
     * result is a new transform which is part way between the two original
     * transforms. The translation can simply be linearly interpolated, but the
     * rotation interpolation is slightly more complex, using a method called
     * "SLERP" to spherically-linearly interpolate between 2 quaternions
     * (rotations). This gives a much much better result than trying to linearly
     * interpolate between Euler rotations.
     *
     * @param frameA
     *            - the previous transform
     * @param frameB
     *            - the next transform
     * @param progression
     *            - a number between 0 and 1 indicating how far between the two
     *            transforms to interpolate. A progression value of 0 would
     *            return a transform equal to "frameA", a value of 1 would
     *            return a transform equal to "frameB". Everything else gives a
     *            transform somewhere in-between the two.
     * @return
     */
    protected static JointTransform interpolate(JointTransform frameA, JointTransform frameB, float progression) {
        float[] pos = interpolate(frameA.position, frameB.position, progression);
        Quaternion rot = Quaternion.interpolate(frameA.rotation, frameB.rotation, progression);
        return new JointTransform(pos, rot);
    }

    /**
     * Linearly interpolates between two translations based on a "progression"
     * value.
     *
     * @param start
     *            - the start translation.
     * @param end
     *            - the end translation.
     * @param progression
     *            - a value between 0 and 1 indicating how far to interpolate
     *            between the two translations.
     * @return
     */
    private static float[] interpolate(float[] start, float[] end, float progression) {
        float x = start[0] + (end[0] - start[0]) * progression;
        float y = start[1] + (end[1] - start[1]) * progression;
        float z = start[2] + (end[2] - start[2]) * progression;
        return new float[]{x, y, z};
    }



}
