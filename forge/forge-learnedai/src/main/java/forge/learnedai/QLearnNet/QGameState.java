package forge.learnedai.QLearnNet;

import forge.learnedai.LearnedAiCardMemory;
import forge.game.card.Card;
import forge.game.spellability.SpellAbility;
import java.util.Vector;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.lang.String;
import java.io.IOException;

//probably deprecated until we condsider deckbuiling...
public class QGameState {
	
    protected int getEffectivePower(final Card c) {
        return c.getNetCombatDamage();
    }
    protected int getEffectiveToughness(final Card c) {
        return c.getNetToughness();
    }

	
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
	private metaQCard Phase;
	private metaQCard myTurn;

	
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
	}
	
	//function to save the card data currently in memory
	public void savecards(String filenames, String Cardnamesfile) throws IOException{
		//concantate the weights
		//TODO
		//construct the cardnames file
		
	}
	//function to load previously saved card data
	public void loadcards (String weightsfile, String cardfile)throws IOException{
		
		//TODO
	}
	//function to check the current board state and produce a correct list of observation values and return them
	//this should also look at the stack
	public Vector<? extends QCard> ProduceGamestate(){
		//TODO
		
		return myDeck;
	}
	
	//not currently used, but maybe later - for now we'll look ahead to stack resolution to understand our opponent's spells (and indeed the consequence of our own)
	private Matrix AssessSpell(final Card c){
		return null;
	}
	
	//possibly a useful featureset for creature assessment...
	private Matrix AssessCreature(final Card c){
		float[][] weights = new float[1][14];
        int power = getEffectivePower(c);
        final int toughness = getEffectiveToughness(c);
        for (String keyword : c.getKeywords()) {
            if (keyword.equals("Prevent all combat damage that would be dealt by CARDNAME.")
                    || keyword.equals("Prevent all damage that would be dealt by CARDNAME.")
                    || keyword.equals("Prevent all combat damage that would be dealt to and dealt by CARDNAME.")
                    || keyword.equals("Prevent all damage that would be dealt to and dealt by CARDNAME.")) {
                power = 0;
                break;
            }
        }
        if (c.hasKeyword("CARDNAME can't attack or block.") || (c.hasKeyword("CARDNAME doesn't untap during your untap step.") && c.isTapped() )) {
        	power = 0;
        }
        if (c.hasKeyword("Double Strike")) {
        	power = power *2;
        }
        weights[1][0] = power;
        weights[1][1] = toughness;
        weights[1][2] = c.getCMC();

        // Evasion keywords
        if (c.hasKeyword("Flying") || c.hasKeyword("Unblockable") || c.hasKeyword("You may have CARDNAME assign its combat damage as though it weren't blocked.") ||
        	c.hasKeyword("Intimidate") || c.hasStartOfKeyword("Menace") || c.hasKeyword("Trample")) {
        	weights[1][3] = 1;
        }

        // Other good keywords
        if (power > 0) {
            if (c.hasKeyword("Double Strike") ||c.hasKeyword("First Strike") ) {
            	weights[1][4] = 1;
            } 
            if (c.hasKeyword("Deathtouch")) {
            	weights[1][5] = 1;
            }
            if (c.hasKeyword("Lifelink")) {
            	weights[1][6] = 1;
            }
        }
        // Defensive Keywords
        if (c.hasKeyword("Reach") || c.hasKeyword("Flying")) {
        	weights[1][7] = 1;
        }

        // Protection
        if (c.hasKeyword("Indestructible") || c.hasKeyword("Prevent all combat damage that would be dealt to CARDNAME.")) {
        	weights[1][8] = 1;
        }
        if (c.hasKeyword("Hexproof") || c.hasStartOfKeyword("Protection")) {
        	weights[1][9] = 1;
        }

        // Bad keywords
        if (c.hasKeyword("Defender") || c.hasKeyword("CARDNAME can't attack.")) {
        	weights[1][10] = 1;
        }
        if (c.hasKeyword("CARDNAME can't block.") || c.hasKeyword("CARDNAME attacks each turn if able.")
                || c.hasKeyword("CARDNAME attacks each combat if able.")) {
        	weights[1][11] = 1 ;
        }


        if (c.isUntapped()) {
        	weights[1][12] = 1;
        }

		return TensorFactory.matrix(weights);
	}
	
	public QGameState(int maxN, int width){
		maxNoOppcards = maxN;
		nneuron = 19;  //width;   //set at a fixed number for now
		mylifetot = new metaQCard(nneuron);
		opplifetot = new metaQCard(nneuron);
		Phase = new metaQCard(nneuron);
		myTurn = new metaQCard(nneuron);
	}
}