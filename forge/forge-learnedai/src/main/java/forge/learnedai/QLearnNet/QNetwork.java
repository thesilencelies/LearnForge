package forge.learnedai.QLearnNet;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import java.util.Vector;
import java.lang.String;
import forge.game.card.Card;

class QNetNeuronMat{
	//non-linear function - tanh from MatrixFunctions
	
	//set of weights as a matrix
	public DoubleMatrix weights;
	//set of biases
	public DoubleMatrix biases;
	//set of activations
	public DoubleMatrix acti;
	
	public QNetNeuronMat(int nin, int nout){
		weights = DoubleMatrix.rand(nin,nout);
		biases = DoubleMatrix.rand(nin);
		acti = DoubleMatrix.zeros(nout);
	}
}

public class QNetwork {
	
	private QGameState gamestate;
	//some set of neurons for the layers
	private Vector<QNetNeuronMat> layers;
	
	//list of possible cards in your deck paired with their final values
	public Vector<myQCard> outcards;
	
	
	//previous input to the network
	private Vector<? extends QCard> prevstate;
	
	//function to allow it to make the evaluation
		//the first step of this may be tricky
	public Vector<myQCard> EvaluateChoice(){
		prevstate = gamestate.ProduceGamestate();
		
		//TODO
		return outcards;
	}
	//backpropagation functions
	public void backprop(myQCard cardchoice, double Qval){
		//TODO
	}
	
	//file i/o
		//calls the i/o for gamestate as well
	void load(int nlayers, Vector<String> layerfiles, Vector<String> cardweights,Vector<Card> cards){
		
	}
	void save(Vector<String> layerfiles, Vector<String> cardweights, String cardnamefile){
		
	}
	
	public QNetwork(){
		//maybe some setup will be done later
		//need to initialise gamestate here
		//could call the io here
	}
}