package forge.learnedai.QLearnNet;

import forge.game.card.Card;
import org.jblas.DoubleMatrix;
import java.lang.String;

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
		weights = DoubleMatrix.rand(nneuron);
	}
	public oppQCard(Card cid, int nob, boolean TG, DoubleMatrix DM){
		cardid = cid;
		noObserve = nob;
		THISGAME = TG;
		weights = DM;
	}
}
