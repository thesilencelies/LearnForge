package forge.learnedai.NNinput;

import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardCollectionView;
import forge.game.card.CardCollection;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

import com.github.neuralnetworks.architecture.types.NNFactory;
import com.github.neuralnetworks.architecture.NeuralNetworkImpl;

import java.util.List;
import java.util.Iterator;

//there are a lot of definitional magic numbers here right now
//how would loading from a file be handled?
public class NNcreatureNet {
	private NeuralNetworkImpl nncreature;
	private NeuralNetworkImpl nnchoice;
	private NNcreaturestate mycs;
	private NNcreaturestate oppcs;
	
	private float[] mytarget, opptarget;
	
	public NNcreatureNet (){
	//has to be fixed size, so we'll fill the gaps from input with 0s (37*40) 
		nncreature = NNFactory.convNN(new int[][] { { 37, 40, 1 }, { 37, 1, 20, 37 }, { 4, 4 }, { 5, 5, 16, 1 }, { 2, 2 }, { 5, 5, 120, 1 }, {84}, {40} }, true);
		//multilayer perceptron for the final decision for now
		nnchoice = NNFactory.mlp(new int []{85, 120, 80, 40},true);
		mytarget = new float[40];
		opptarget = new float[40];
	}
	
	public float[] rankcards(Player me){
		//when do I populate the targets?
		
		Player opp = me.getOpponent();
		CardCollectionView mybf = me.getCardsIn(ZoneType.Battlefield);
		CardCollectionView oppbf = opp.getCardsIn(ZoneType.Battlefield);
		//sort into creatures and non
		CardCollection myCreatures = new CardCollection();
		CardCollection oppCreatures = new CardCollection();
		Iterator<Card> mi = mybf.iterator();
		Iterator<Card> oi = oppbf.iterator();
		
		while (mi.hasNext()){
			Card c = mi.next();
			if(c.isCreature()){
				myCreatures.add(c);
			}
		}
		while (oi.hasNext()){
			Card c = mi.next();
			if(c.isCreature()){
				oppCreatures.add(c);
			}
		}
		mycs = new NNcreaturestate(myCreatures, mytarget);
		oppcs = new NNcreaturestate(oppCreatures, opptarget);
		
		float[] myCweights = new float [40];
		float[] oppCweights = new float [40];
		//produce the output of the CNN
		
		//concantate these outputs, append the life totals and phase information
		//pass these through the fully connected decision network
		
		//return the output from the neural network
		return new float[40];
	}
}