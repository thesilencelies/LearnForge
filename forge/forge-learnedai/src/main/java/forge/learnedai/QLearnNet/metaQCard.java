package forge.learnedai.QLearnNet;

import forge.game.card.Card;
import org.jblas.DoubleMatrix;

//this class is there to allow the system to handle things like life totals
public class metaQCard extends QCard{

	public metaQCard(int nneuron){
		weights = DoubleMatrix.rand(nneuron);
	}
	public metaQCard(DoubleMatrix DM){
		weights = DM;
	}
}
