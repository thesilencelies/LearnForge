package forge.learnedai.QLearnNet;

import com.github.neuralnetworks.tensor.Matrix;

import forge.game.card.Card;

public abstract class QCard {
	
	//matrix of weights (a 1 x m matrix)
	public Matrix weights;
	//some function to load the matrix of weights??
	public double quantity;
}
