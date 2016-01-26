package forge.learnedai.toolstests;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

import forge.learnedai.NNinput.NNcardState;
import forge.learnedai.QLearnNet.QCard;
import forge.learnedai.QLearnNet.metaQCard;
import nntools.ArrayManipulator;

public class NNcopytest {
	public static void main(String[] args){
		equalityCheck();
		GCCheck();
	}
	public static void GCCheck(){
		NNcardState state, s2;
		List<NNcardState> ls = new ArrayList<NNcardState>();
		for(int i = 0; i < Integer.MAX_VALUE; i ++){
			float[][] elements = new float[1][20];
			Vector<QCard> v = new Vector<QCard>();
			v.addElement(new metaQCard(TensorFactory.matrix(elements)));		
			state = new NNcardState(v.iterator(), v.iterator());
			ls.add(state); 
			System.out.printf("%d %n", i);
		}
	}
	public static void equalityCheck(){
		float[][] elements = new float[1][20];
		float[][] elements2 = new float[1][20];
		Matrix m1 = TensorFactory.matrix(elements);
		Matrix m2 = TensorFactory.matrix(elements);
		if(ArrayManipulator.MatEquals(m1, m2))System.out.println("the two matrices are equal");
		else System.out.println("the two matrices are not equal");
		
		
		Vector<QCard> v = new Vector<QCard>();

		v.addElement(new metaQCard(TensorFactory.matrix(elements)));		
		NNcardState d1 = new NNcardState(v.iterator(), v.iterator());
		Vector<QCard> v2 = new Vector<QCard>();
		v2.addElement(new metaQCard(TensorFactory.matrix(elements)));
		NNcardState d2 = new NNcardState(v2.iterator(),v2.iterator());
		if(d1.equals(d2))System.out.println("the two data are equal");
		else System.out.println("the two data are not equal");
	}
}
