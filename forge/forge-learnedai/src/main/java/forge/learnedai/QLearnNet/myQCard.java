package forge.learnedai.QLearnNet;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

import forge.game.card.Card;

public class myQCard implements QCard {
	//the cid here is probably inefficient
	//so I'll use a metaQCard instead where I don't need the cid
	public Card cardid;
	public myQCard(Card cid, int nneuron){
		cardid = cid;
		weights = TensorFactory.matrix(new float[1][nneuron]);
	}
	public myQCard(Card cid, Matrix DM){
		cardid = cid;
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