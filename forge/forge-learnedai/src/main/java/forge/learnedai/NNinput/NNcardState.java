package forge.learnedai.NNinput;

import forge.learnedai.QLearnNet.QCard;
import nntools.ArrayManipulator;
import java.util.Vector;
import java.util.Iterator;
import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.Tensor;
import com.github.neuralnetworks.tensor.TensorFactory;

public class NNcardState implements TrainingInputData {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Matrix myWeights;
	private Matrix oppWeights;
	private Tensor target;

public NNcardState(Vector<QCard> mycards, Vector<QCard> oppcards){
	super();
	//process the cards and produce the matrix
	//currently the state is just the sum then the max

	if (mycards.size() > 0){
		Iterator<QCard> it = mycards.iterator();
		myWeights = sumMaxWeights(it);
	}
	if (oppcards.size() > 0){
		Iterator<QCard> it = oppcards.iterator();
		oppWeights = sumMaxWeights(it);
	}
	target = TensorFactory.matrix(new float[][]{{0.5f}});
}
public NNcardState(Iterator<QCard> mycardit, Iterator<QCard> oppcardit){
	super();
	//process the cards and produce the matrix
	//currently the state is just the sum then the max
	myWeights = sumMaxWeights(mycardit);
	oppWeights = sumMaxWeights(oppcardit);
	target = TensorFactory.matrix(new float[][]{{0.5f}});
}

private Matrix sumMaxWeights(Iterator<QCard> it){
	Matrix cweights = it.next().getweights();
	Matrix summedWeights = TensorFactory.matrix(new float[1][20]);
	Matrix maxWeights = TensorFactory.matrix(new float[1][20]);
	TensorFactory.copy(cweights, summedWeights);
	TensorFactory.copy(cweights, maxWeights);
	while(it.hasNext()){
		cweights = it.next().getweights();
		ArrayManipulator.SumMatrixInPlace(summedWeights, cweights);
		ArrayManipulator.MaxMatrixInPlace(maxWeights, cweights);
	}
	return ArrayManipulator.appendMatrices(summedWeights,maxWeights);
}

@Override
public Tensor getTarget(){

	return target;
}
@Override
public Tensor getInput() {
	
	return ArrayManipulator.appendMatrices(myWeights,oppWeights);
}

public void setTarget(Matrix target) {
    this.target = target;
}

}
