package nntools;

import java.util.Arrays;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.Tensor.TensorIterator;
import com.github.neuralnetworks.tensor.TensorFactory;

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
	public static boolean MatEquals(Matrix m1, Matrix m2){
		TensorIterator it1 = m1.iterator();
		TensorIterator it2 = m2.iterator();
		while(it1.hasNext()){
			if(!it2.hasNext()){
				return false;
			}
			if((it1.next() != it2.next())){
				return false;
			}
		}
		if(it2.hasNext()) return false;
		else return true;
	}
	
	//sums the two input matrixes into the first
	public static void SumMatrixInPlace(Matrix srcdst, Matrix addval){
		if (!Arrays.equals(srcdst.getDimensions(), addval.getDimensions())) {
		    throw new IllegalArgumentException("Dimensions don't match");
		}

		TensorIterator srcIt = srcdst.iterator();
		TensorIterator destIt = addval.iterator();
		while (srcIt.hasNext() && destIt.hasNext()) {
			Integer srcind = srcIt.next();
		    srcdst.getElements()[srcind] =  srcdst.getElements()[srcind] + addval.getElements()[destIt.next()];
		}
	}
	//turns the first into the max of the two
	public static void MaxMatrixInPlace(Matrix srcdst, Matrix addval){
		if (!Arrays.equals(srcdst.getDimensions(), addval.getDimensions())) {
		    throw new IllegalArgumentException("Dimensions don't match");
		}

		TensorIterator srcIt = srcdst.iterator();
		TensorIterator destIt = addval.iterator();
		while (srcIt.hasNext() && destIt.hasNext()) {
			Integer srcind = srcIt.next();
		    srcdst.getElements()[srcind] =  Math.max(srcdst.getElements()[srcind],addval.getElements()[destIt.next()]);
		}
	}
	//appendds the matrices vertically (which happens to be easier to do)
	public static Matrix appendMatrices(Matrix a, Matrix b){
		if(a.getDimensions()[1] !=  b.getDimensions()[1]){
			throw new IllegalArgumentException("Dimensions don't match");
		}
		int aLen = a.getElements().length;
		int bLen = b.getElements().length;
		float[] elements = new float [aLen + bLen];
		System.arraycopy(a.getElements(), 0, elements, 0, aLen);
		System.arraycopy(b.getElements(), 0, elements, aLen, bLen);
		return TensorFactory.matrix(elements, a.getDimensions()[1]);
	}
}
