package forge.learnedai.QLearnNet;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

import forge.game.card.Card;

//this class is there to allow the system to handle things like life totals
public class metaQCard implements QCard{

	
	public metaQCard(int nneuron){
		weights = TensorFactory.matrix(new float[1][nneuron]);
	}
	public metaQCard(Matrix DM){
		weights = DM;
	}
	Matrix weights;
	@Override
	public Matrix getweights() {
		return weights ;
	}
	@Override
	public double getQuantity() {
		return 0;
	}
}
