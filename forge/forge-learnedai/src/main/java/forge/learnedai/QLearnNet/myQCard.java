package forge.learnedai.QLearnNet;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

import forge.game.card.Card;

public class myQCard extends QCard {
	
	public Card cardid;
	public myQCard(Card cid, int nneuron){
		cardid = cid;
		weights = TensorFactory.matrix(new float[1][nneuron]);
	}
	public myQCard(Card cid, Matrix DM){
		cardid = cid;
		weights = DM;
	}
}