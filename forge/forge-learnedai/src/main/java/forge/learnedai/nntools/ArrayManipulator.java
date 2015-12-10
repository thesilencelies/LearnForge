package nntools;

public class ArrayManipulator {
	/**
	 * Clones the provided array
	 * 
	 * @param src
	 * @return a new clone of the provided array
	 */
	public static float[][][] deepCloneArray(float[][][] src) {
	    int length = src.length;
	    float[][][] target = new float[length][src[0].length][src[0][0].length];
	    for (int i = 0; i < length; i++) {
	    	for(int j = 0, len = src[0].length; j < len; j ++){
	        System.arraycopy(src[i][j], 0, target[i][j], 0, src[i][j].length);
	    	}
	    }
	    return target;
	}
}
