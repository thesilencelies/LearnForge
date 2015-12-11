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
	private Matrix summedWeights;
	private Matrix maxWeights;
	private Tensor target;

public NNcardState(Vector<? extends QCard> cards){
	super();
	//process the cards and produce the matrix
	
	//currently the state is just the sum then the max
	summedWeights = TensorFactory.matrix(new float[1][20]);
	if (cards.size() > 0){
		Iterator<? extends QCard> it = cards.iterator();
		Matrix cweights = it.next().weights;
		TensorFactory.copy(cweights, summedWeights);
		TensorFactory.copy(cweights, maxWeights);
		
		while(it.hasNext()){
			cweights = it.next().weights;
			ArrayManipulator.SumMatrixInPlace(summedWeights, cweights);
			ArrayManipulator.MaxMatrixInPlace(maxWeights, cweights);
		}
	}
	target = TensorFactory.matrix(new float[][]{{0.5f}});
}

@Override
public Tensor getTarget(){

	return target;
}
@Override
public Tensor getInput() {
	
	return ArrayManipulator.appendMatrices(summedWeights,maxWeights);
}

public void setTarget(Matrix target) {
    this.target = target;
}

}
