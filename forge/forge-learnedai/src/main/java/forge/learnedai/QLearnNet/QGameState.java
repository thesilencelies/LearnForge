package forge.learnedai.QLearnNet;

import forge.learnedai.LearnedAiCardMemory;
import forge.game.card.Card;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.lang.String;
import java.io.IOException;
import org.jblas.DoubleMatrix;

public class QGameState {
	
	static int nneuron;
	static int maxNoOppcards;
	//fixed order list of cards in your deck
	public Vector<myQCard> myDeck;

	//some mutable fixed order list of cards you've seen your opponents play
	public Vector<oppQCard> considerCards;
	
	//life totals
	private metaQCard mylifetot;
	private metaQCard opplifetot;	//currently will only consider 2 player games
	
	//relevant phase distinctions
	private metaQCard MainPhase;
	private metaQCard myTurn;
	private metaQCard DecAttack;
	private metaQCard DecBlock;
	
	//this doesn't handle any of the layer multiplication etc so that the backpropagation is simple enough
	
	//local (dynamic) storage for mid term memory of cards (shouldn't take up too much space if we're limited to standard)
	 private Set<oppQCard> observedcards = new HashSet<oppQCard>();
	
	//function called whenever an opponent casts a spell
	public void spellcastcheck(Card spell){
	//TODO	
	}
	 
	//function to shift a newly observed card to the current use list
	private void obsSpellNotConsidered(Card spell){
		//TODO
	}
	
	//function to allow the backprop to edit the weights on the cards here
	public void adjustWeights(Vector<QCard> adjustedweights){
		
		//TODO
		//adjust state weights
		
		//adjust my cards
		
		//adjust opponent's cards
	}
	
	//function to save the card data currently in memory
	public void savecards(Vector<String> filenames, String Cardnamesfile){
		if (filenames.size() < 6+ myDeck.size() + observedcards.size()) {
			throw new IOException("not enough filenames");
		}
		
	}
	//function to load previously saved card data
	public void loadcards (Vector<String>filenames, Vector<Card> mycardnames)throws IOException{
		if (filenames.size() < 6+ mycardnames.size()) {
			throw new IOException("not enough filenames");
		}
		
		Doublematrix tempmat;
		Iterator fnameitr = filenames.iterator();
		//load the gamestate metavariables.
		tempmat = new DoubleMatrix;
		tempmat.load(fnameitr.next());
		mylifetot.weights = tempmat;
		tempmat = new DoubleMatrix;
		tempmat.load(fnameitr.next());
		opplifetot.weights  tempmat;
		tempmat = new DoubleMatrix;
		tempmat.load(fnameitr.next());
		MainPhase.weights  = tempmat;
		tempmat = new DoubleMatrix;
		tempmat.load(fnameitr.next());
		myTurn.weights  = tempmat;
		tempmat = new DoubleMatrix;
		tempmat.load(fnameitr.next());
		DecAttack.weights  = tempmat;
		tempmat = new DoubleMatrix;
		tempmat.load(fnameitr.next());
		DecBlock.weights  = tempmat;
		tempmat = new DoubleMatrix;
		tempmat.load(fnameitr.next());
		
		Iterator mycnameitr = mycardnames.iterator();
		//load all the cards from your deck
		myDeck.clear();
		while(mycnameitr.hasNext()){
			//load the card
			tempmat = new DoubleMatrix;
			tempmat.load(fnameitr.next());
			myDeck.add(new myQCard(mycnameitr.next(),tempmat));
		}
		//load all the remaining cards into the mid term memory
		observedcards.clear();
		while(fnameitr.hasNext()){
			observedcards.add(new oppQCard(fnameitr.next()));
		}
		//load the most seen cards into the short term memory
		considerCards.clear();
	}
	//function to check the current board state and produce a correct list of observation values and return them
	//this should also look at the stack
	public Vector<? extends QCard> ProduceGamestate(){
		//TODO
		
		return myDeck;
	}
	
	public QGameState(int maxN, int width){
		maxNoOppcards = maxN;
		nneuron = width;
		mylifetot = new metaQCard(width);
		opplifetot = new metaQCard(width);
		MainPhase = new metaQCard(width);
		myTurn = new metaQCard(width);
		DecAttack = new metaQCard(width);
		DecBlock = new metaQCard(width);
	}
}