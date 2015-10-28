package forge.learnedai.QLearnNet;

import forge.learnedai.LearnedAiCardMemory;
import forge.game.card.Card;
import forge.game.spellability.SpellAbility;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.lang.String;
import java.io.IOException;
import org.jblas.DoubleMatrix;

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
	public void savecards(String filenames, String Cardnamesfile) throws IOException{
		//concantate the weights
		
		//construct the cardnames file
		
	}
	//function to load previously saved card data
	public void loadcards (String weightsfile, String cardfile)throws IOException{
		
		
		
		//need to impliment this using just one file for each instead
		/*
		DoubleMatrix tempmat;
		Iterator<String> fnameitr = filenames.iterator();
		//load the gamestate metavariables.
		tempmat = new DoubleMatrix();
		tempmat.load(fnameitr.next());
		mylifetot.weights = tempmat;
		tempmat = new DoubleMatrix();
		tempmat.load(fnameitr.next());
		opplifetot.weights = tempmat;
		tempmat = new DoubleMatrix();
		tempmat.load(fnameitr.next());
		MainPhase.weights  = tempmat;
		tempmat = new DoubleMatrix();
		tempmat.load(fnameitr.next());
		myTurn.weights  = tempmat;
		tempmat = new DoubleMatrix();
		tempmat.load(fnameitr.next());
		DecAttack.weights  = tempmat;
		tempmat = new DoubleMatrix();
		tempmat.load(fnameitr.next());
		DecBlock.weights  = tempmat;
		tempmat = new DoubleMatrix();
		tempmat.load(fnameitr.next());
		
		Iterator<Card> mycnameitr = mycardnames.iterator();
		//load all the cards from your deck
		myDeck.clear();
		while(mycnameitr.hasNext()){
			//load the card
			tempmat = new DoubleMatrix();
			tempmat.load(fnameitr.next());
			myDeck.add(new myQCard(mycnameitr.next(),tempmat));
		}
		//load all the remaining cards into the mid term memory
		observedcards.clear();
		while(fnameitr.hasNext()){
			observedcards.add(new oppQCard(fnameitr.next()));
		}
		//load the most seen cards into the short term memory
		considerCards.clear();*/
	}
	//function to check the current board state and produce a correct list of observation values and return them
	//this should also look at the stack
	public Vector<? extends QCard> ProduceGamestate(){
		//TODO
		
		return myDeck;
	}
	
	private DoubleMatrix AssessCreature(final Card c){
		DoubleMatrix weights = DoubleMatrix.zeros(37);
        if (c.isToken()) {
        	weights.put(1,1);
        }
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
        weights.put(2,power);
        weights.put(3,toughness);
        weights.put(4,c.getCMC());

        // Evasion keywords
        if (c.hasKeyword("Flying")) {
        	weights.put(5,1);
        }
        if (c.hasKeyword("Unblockable")) {
            weights.put(6,1);
        }
        if (c.hasKeyword("You may have CARDNAME assign its combat damage as though it weren't blocked.")) {
            weights.put(7,1);
        }
        if (c.hasKeyword("Intimidate")) {
            weights.put(8,1);
        }
        if (c.hasStartOfKeyword("Menace")) {
            weights.put(8,1);
        }
        if (c.hasStartOfKeyword("CantBeBlockedBy")) {
        	weights.put(9,1);
        }

        // Other good keywords
        if (power > 0) {
            if (c.hasKeyword("Double Strike")) {
            	weights.put(10,1);
            	weights.put(11,1);
            } else if (c.hasKeyword("First Strike")) {
            	weights.put(11,1);
            }
            if (c.hasKeyword("Deathtouch")) {
            	weights.put(12,1);
            }
            if (c.hasKeyword("Lifelink")) {
            	weights.put(13,1);
            }
            if (c.hasKeyword("Trample")) {
            	weights.put(14,1);
            }
            if (c.hasKeyword("Vigilance")) {
            	weights.put(15,1);
            }
        }
        weights.put(16,c.getAmountOfKeyword("Exalted"));

        //lords
        if(c.hasKeyword("creatures get +") || c.hasKeyword("creatures you control get +")){
        	weights.put(36,1);
        }
        
        // Defensive Keywords
        if (c.hasKeyword("Reach")) {
        	weights.put(37,1);;
        }

        // Protection
        if (c.hasKeyword("Indestructible")) {
        	weights.put(17,1);
        }
        if (c.hasKeyword("Prevent all combat damage that would be dealt to CARDNAME.")) {
        	weights.put(18,1);
        }
        if (c.hasKeyword("Hexproof")) {
        	weights.put(19,1);
        }
        if (c.hasStartOfKeyword("Protection")) {
        	weights.put(20,1);
        }
        if (c.hasStartOfKeyword("PreventAllDamageBy")) {
        	weights.put(21,1);
        }

        // Bad keywords
        if (c.hasKeyword("Defender") || c.hasKeyword("CARDNAME can't attack.")) {
        	weights.put(22,1);
        }
        if (c.getSVar("SacrificeEndCombat").equals("True")) {
        	weights.put(23,1);;
        }
        if (c.hasKeyword("CARDNAME can't block.")) {
        	weights.put(24,1);
        }
        if (c.hasKeyword("CARDNAME attacks each turn if able.")
                || c.hasKeyword("CARDNAME attacks each combat if able.")) {
        	weights.put(25,1);
        }
        if (c.hasKeyword("CARDNAME can block only creatures with flying.")) {
        	weights.put(26,1);;
        }

        if (c.hasSVar("DestroyWhenDamaged")) {
        	weights.put(27,1);;
        }

        if (c.hasKeyword("CARDNAME can't attack or block.")) {
        	weights.put(28,1);
        }
        if (c.hasKeyword("CARDNAME doesn't untap during your untap step.")) {
        	weights.put(29,1);
        }
        if (c.hasSVar("EndOfTurnLeavePlay")) {
        	weights.put(30,1);;
        }
        if (c.hasStartOfKeyword("At the beginning of your upkeep, sacrifice CARDNAME unless you pay")) {
        	weights.put(31,1);;
        }

        if (c.hasStartOfKeyword("At the beginning of your upkeep, CARDNAME deals")) {
        	weights.put(32,1);
        }
        if (c.getSVar("Targeting").equals("Dies")) {
        	weights.put(33,1);
        }


        if (!c.getManaAbilities().isEmpty()) {
        	weights.put(34,1);
        }

        if (c.isUntapped()) {
        	weights.put(35,1);;
        }

		return weights;
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