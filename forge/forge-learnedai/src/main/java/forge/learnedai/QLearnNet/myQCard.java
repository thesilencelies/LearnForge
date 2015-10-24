package forge.learnedai.QLearnNet;

import forge.game.card.Card;
import org.jblas.DoubleMatrix;

public class myQCard extends QCard {
	
	public Card cardid;
	public myQCard(Card cid, int nneuron){
		cardid = cid;
		weights = DoubleMatrix.rand(nneuron);
	}
	public myQCard(Card cid, DoubleMatrix DM){
		cardid = cid;
		weights = DM;
	}
}