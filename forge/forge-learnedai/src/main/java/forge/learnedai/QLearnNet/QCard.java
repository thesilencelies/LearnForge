package forge.learnedai.QLearnNet;

import forge.game.card.Card;
import org.jblas.DoubleMatrix;

public abstract class QCard {
	
	//matrix of weights (a 1 x m matrix)
	public DoubleMatrix weights;
	//some function to load the matrix of weights??
	public double quantity;
}
