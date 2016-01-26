package forge.learnedai.QLearnNet;

import forge.learnedai.LearnedAiCardMemory;
import forge.learnedai.NNinput.NNcardState;
import forge.game.card.Card;
import forge.game.card.CardCollectionView;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

import java.util.Vector;

import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.lang.String;
import java.io.IOException;

//probably deprecated until we condsider deckbuiling...
public class QGameState {
	
    protected static int getEffectivePower( Card c) {
        return c.getNetCombatDamage();
    }
    protected static int getEffectiveToughness( Card c) {
        return c.getNetToughness();
    }
    
	//function to check the current board state and produce a correct list of observation values and return them
	//this should also look at the stack
	
	public static NNcardState ProduceGamestate(Player me){
		Player opp;
		try{
		opp = me.getOpponent();
		}catch (IllegalStateException e){
			opp = me.getOtherPlayer();
		}
		//put in the evaluation of the creatures
		Vector<QCard> mycards = new Vector<QCard>();
		CardCollectionView mystuff = me.getCardsIn(ZoneType.Battlefield);
		Iterator<Card> it = mystuff.iterator();
		while (it.hasNext()){
			Card c = it.next();
			if(c.isCreature()){
				mycards.addElement(new metaQCard(assessCreature(c)));
			}
		}
		//evaluate what's left in hand (should be useful to encourage preserving valuable resources
		mystuff = me.getCardsIn(ZoneType.Hand);
		it = mystuff.iterator();
		while (it.hasNext()){
			Card c = it.next();
			mycards.addElement(new metaQCard(assessHandCard(c)));
		}
		
		//put in the game state variables
		float[][] statevals = new float[1][20];
		statevals[0][17] = me.getLife();
		statevals[0][18] = opp.getLife();
		statevals[0][19] = me.getTurn();
		mycards.addElement(new metaQCard(TensorFactory.matrix(statevals)));
		
		Vector<QCard> oppcards = new Vector<QCard>();
		CardCollectionView oppstuff = opp.getCardsIn(ZoneType.Battlefield);
		it = oppstuff.iterator();
		while (it.hasNext()){
			Card c = it.next();
			if(c.isCreature()){
				oppcards.addElement(new metaQCard(assessCreature(c)));
			}
		}
		
		//available mana is considered in the 18-19 slots of oppQCards
		statevals = new float[1][20];
		statevals[0][17] = me.getManaPool().totalMana();
		statevals[0][18] = opp.getManaPool().totalMana();
		statevals[0][19] = opp.getCardsIn(ZoneType.Hand).size();
		oppcards.addElement(new metaQCard(TensorFactory.matrix(statevals)));
		
		//ideally I'd run the features through a conv net that would produce the featureset directly, but I'm not sure how to train that atm
		return new NNcardState(mycards.iterator(),oppcards.iterator());
	}

	private static Matrix assessHandCard(final Card c){
		//use coarse features - do they target?, do they boardwipe?, what's their CMC?
		
		//uses the remaining slots left over from below to consider the cards left in hand
		float[][] weights = new float[1][20];
		weights[0][13] = c.getCMC();
		if(c.isCreature()) weights[0][14] = 1;
		if(c.hasKeyword("Target")) weights[0][15]=1;
		if(c.isLand())weights[0][16] =1;
		return TensorFactory.matrix(weights);
	}

	
	//not currently used, but maybe later - for now we'll look ahead to stack resolution to understand our opponent's spells (and indeed the consequence of our own)
	private static Matrix assessSpell( Card c){
		return null;
	}
	
	//possibly a useful featureset for creature assessment...
	private static Matrix assessCreature(Card c){
		float[][] weights = new float[1][20];
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
        weights[0][0] = power;
        weights[0][1] = toughness;
        weights[0][2] = c.getCMC();

        // Evasion keywords
        if (c.hasKeyword("Flying") || c.hasKeyword("Unblockable") || c.hasKeyword("You may have CARDNAME assign its combat damage as though it weren't blocked.") ||
        	c.hasKeyword("Intimidate") || c.hasStartOfKeyword("Menace") || c.hasKeyword("Trample")) {
        	weights[0][3] = 1;
        }

        // Other good keywords
        if (power > 0) {
            if (c.hasKeyword("Double Strike") ||c.hasKeyword("First Strike") ) {
            	weights[0][4] = 1;
            } 
            if (c.hasKeyword("Deathtouch")) {
            	weights[0][5] = 1;
            }
            if (c.hasKeyword("Lifelink")) {
            	weights[0][6] = 1;
            }
        }
        // Defensive Keywords
        if (c.hasKeyword("Reach") || c.hasKeyword("Flying")) {
        	weights[0][7] = 1;
        }

        // Protection
        if (c.hasKeyword("Indestructible") || c.hasKeyword("Prevent all combat damage that would be dealt to CARDNAME.")) {
        	weights[0][8] = 1;
        }
        if (c.hasKeyword("Hexproof") || c.hasStartOfKeyword("Protection")) {
        	weights[0][9] = 1;
        }

        // Bad keywords
        if (c.hasKeyword("Defender") || c.hasKeyword("CARDNAME can't attack.")) {
        	weights[0][10] = 1;
        }
        if (c.hasKeyword("CARDNAME can't block.") || c.hasKeyword("CARDNAME attacks each turn if able.")
                || c.hasKeyword("CARDNAME attacks each combat if able.")) {
        	weights[0][11] = 1 ;
        }


        if (c.isUntapped()) {
        	weights[0][12] = 1;
        }

		return TensorFactory.matrix(weights);
	}
}