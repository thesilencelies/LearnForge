package forge.learnedai.QLearnNet;

import forge.game.card.Card;
import java.lang.String;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

public class oppQCard extends QCard{
	public int noObserve;
	public boolean THISGAME;
	public Card cardid;
	
	//loads the details of the card from the given file
	public oppQCard(String filename){
		//TODO
	}
	
	public oppQCard(Card cid, int nob, boolean TG,int nneuron){
		cardid = cid;
		noObserve = nob;
		THISGAME = TG;
		weights = TensorFactory.matrix(new float[1][nneuron]);
	}
	public oppQCard(Card cid, int nob, boolean TG, Matrix DM){
		cardid = cid;
		noObserve = nob;
		THISGAME = TG;
		weights = DM;
	}
}
