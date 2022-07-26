package com.google.ar.core.examples.java.common.animation;

/**
 * A quaternion simply represents a 3D rotation. The maths behind it is quite
 * complex (literally; it involves complex numbers) so I wont go into it in too
 * much detail. The important things to note are that it represents a 3d
 * rotation, it's very easy to interpolate between two quaternion rotations
 * (which would not be easy to do correctly with Euler rotations or rotation
 * matrices), and you can convert to and from matrices fairly easily. So when we
 * need to interpolate between rotations we'll represent them as quaternions,
 * but when we need to apply the rotations to anything we'll convert back to a
 * matrix.
 *
 *
 *
 * @author Karl
 *
 */

public class Quaternion {

    private float x, y, z, w;

    /**
     * Creates a quaternion and normalizes it.
     *
     * @param x
     * @param y
     * @param z
     * @param w
     */
    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        normalize();
    }

    /**
     * Normalizes the quaternion.
     */
    public void normalize() {
        float mag = (float) Math.sqrt(w * w + x * x + y * y + z * z);
        w /= mag;
        x /= mag;
        y /= mag;
        z /= mag;
    }

    // Fixed ?
    public float[] toRotationMatrix() {
        float[] matrix = new float[15];
        final float xy = x * y;
        final float xz = x * z;
        final float xw = x * w;
        final float yz = y * z;
        final float yw = y * w;
        final float zw = z * w;
        final float xSquared = x * x;
        final float ySquared = y * y;
        final float zSquared = z * z;
        matrix[0] = 1 - 2 * (ySquared + zSquared);
        matrix[1] = 2 * (xy - zw);
        matrix[2] = 2 * (xz + yw);
        matrix[3] = 0;
        matrix[4] = 2 * (xy + zw);
        matrix[5] = 1 - 2 * (xSquared + zSquared);
        matrix[6] = 2 * (yz - xw);
        matrix[7] = 0;
        matrix[8] = 2 * (xz - yw);
        matrix[9] = 2 * (yz + xw);
        matrix[10] = 1 - 2 * (xSquared + ySquared);
        matrix[11] = 0;
        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1;
        return matrix;
    }

    /*
    From 4x4 Matrix to a float Array
    if (rowMajor) {
      m00 = matrix[0];
      m01 = matrix[1];
      m02 = matrix[2];
      m03 = matrix[3];
      m10 = matrix[4];
      m11 = matrix[5];
      m12 = matrix[6];
      m13 = matrix[7];
      m20 = matrix[8];
      m21 = matrix[9];
      m22 = matrix[10];
      m23 = matrix[11];
      m30 = matrix[12];
      m31 = matrix[13];
      m32 = matrix[14];
      m33 = matrix[15];
     } else {
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
     }

     */

    public static Quaternion fromMatrix(float[] matrix) {
        float w, x, y, z;
        float diagonal = matrix[0] + matrix[5] + matrix[10];
        if (diagonal > 0) {
            float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
            w = w4 / 4f;
            x = (matrix[9] - matrix[6]) / w4;
            y = (matrix[2] - matrix[8]) / w4;
            z = (matrix[4] - matrix[1]) / w4;
        } else if ((matrix[0] > matrix[5]) && (matrix[0] > matrix[10])) {
            float x4 = (float) (Math.sqrt(1f + matrix[0] - matrix[5] - matrix[10]) * 2f);
            w = (matrix[9] - matrix[6]) / x4;
            x = x4 / 4f;
            y = (matrix[1] + matrix[4]) / x4;
            z = (matrix[2] + matrix[8]) / x4;
        } else if (matrix[5] > matrix[10]) {
            float y4 = (float) (Math.sqrt(1f + matrix[5] - matrix[0] - matrix[10]) * 2f);
            w = (matrix[2] - matrix[8]) / y4;
            x = (matrix[1] + matrix[4]) / y4;
            y = y4 / 4f;
            z = (matrix[6] + matrix[9]) / y4;
        } else {
            float z4 = (float) (Math.sqrt(1f + matrix[10] - matrix[0] - matrix[5]) * 2f);
            w = (matrix[4] - matrix[1]) / z4;
            x = (matrix[2] + matrix[8]) / z4;
            y = (matrix[6] + matrix[9]) / z4;
            z = z4 / 4f;
        }
        return new Quaternion(x, y, z, w);
    }

    /**
     * Interpolates between two quaternion rotations and returns the resulting
     * quaternion rotation. The interpolation method here is "nlerp", or
     * "normalized-lerp". Another mnethod that could be used is "slerp", and you
     * can see a comparison of the methods here:
     * https://keithmaggio.wordpress.com/2011/02/15/math-magician-lerp-slerp-and-nlerp/
     *
     * and here:
     * http://number-none.com/product/Understanding%20Slerp,%20Then%20Not%20Using%20It/
     *
     * @param a
     * @param b
     * @param blend
     *            - a value between 0 and 1 indicating how far to interpolate
     *            between the two quaternions.
     * @return The resulting interpolated rotation in quaternion format.
     */
    public static Quaternion interpolate(Quaternion a, Quaternion b, float blend) {
        Quaternion result = new Quaternion(0, 0, 0, 1);
        float dot = a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;
        float blendI = 1f - blend;
        if (dot < 0) {
            result.w = blendI * a.w + blend * -b.w;
            result.x = blendI * a.x + blend * -b.x;
            result.y = blendI * a.y + blend * -b.y;
            result.z = blendI * a.z + blend * -b.z;
        } else {
            result.w = blendI * a.w + blend * b.w;
            result.x = blendI * a.x + blend * b.x;
            result.y = blendI * a.y + blend * b.y;
            result.z = blendI * a.z + blend * b.z;
        }
        result.normalize();
        return result;
    }

}
