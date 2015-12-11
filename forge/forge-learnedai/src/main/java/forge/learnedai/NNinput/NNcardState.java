package forge.learnedai.NNinput;

import forge.learnedai.LearnedAiCardMemory;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardCollectionView;
import forge.game.card.CardCollection;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

import java.util.List;
import java.util.stream.IntStream;
import java.util.Vector;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.lang.String;
import java.io.IOException;

import com.github.neuralnetworks.training.TrainingInputProviderImpl;
import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.calculation.neuronfunctions.TensorFunction;
import com.github.neuralnetworks.tensor.Tensor;
import com.github.neuralnetworks.tensor.TensorFactory;

public class NNcardState implements TrainingInputData {
	
    protected int getEffectivePower(final Card c) {
        return c.getNetCombatDamage();
    }
    protected int getEffectiveToughness(final Card c) {
        return c.getNetToughness();
    }

	
	private HashMap<String, Integer> obscards;
	private int highestusedind;
	private Player me;
	
    private int evalSize;
    public int inputSize;
    private float[] currentZone;
    private float[] currentInput;
    private float[] currentTarget;

public NNcardState(Player p, float [] target, HashMap<String, Integer> oc){
	super();
	me = p;
	evalSize = 1200;	//Standard's card pool
	inputSize = 1;		//only one event considered at a time right now
    currentZone = new float[evalSize];
    currentInput = new float[4*evalSize];
    currentTarget = target;
    obscards = oc;
    highestusedind = oc.size();
}
    
    
public NNcardState(Player p, float[] target){
	super();
	me = p;
	evalSize = 1200;	//Standard's card pool
	inputSize = 3*evalSize;
    currentZone = new float[evalSize];
    currentInput = new float[inputSize];
    currentTarget = target;
    obscards = new HashMap(evalSize);
    highestusedind = 0;
}



private float[] zoneRead(int i){
	Player opp = me.getOpponent();
	switch (i){
		case 0:
			return zoneRead(ZoneType.Battlefield, me);
		case 1:
			return zoneRead(ZoneType.Battlefield, opp);
		case 2:
			return zoneRead(ZoneType.Stack, me);
		case 3:
			return zoneRead(ZoneType.Stack, opp);
		case 4:
			return zoneRead(ZoneType.Hand, me);
		default:
			return zoneRead(ZoneType.Battlefield, me);	
			//should I consider the graveyard at some point - may lead to over-fitting
	}
}

private float [] zoneRead(ZoneType z, Player p){
	CardCollectionView pcards = p.getCardsIn(z);
	currentZone = new float[evalSize];
	Iterator<Card> pi = pcards.iterator();
	int  k;
	while (pi.hasNext()){
		Card c = pi.next();
		if(obscards.get(c.getName()) == null){
			obscards.put(c.getName(),highestusedind);
			currentZone[highestusedind]++;
			highestusedind++;
		}
		else{
			currentZone[obscards.get(c.getName())]++;
		}
	}
	return currentZone;
}





public void setPlayer (Player p){	//might be needed to refresh the player before each pass of the NN
	me = p;
}



@Override
public Tensor getTarget(){
	for (int i = 0; i < 3; i++){
		currentZone = zoneRead(i);
		try{
			System.arraycopy(currentZone,0,currentInput,i*evalSize,evalSize);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	return TensorFactory.tensor(currentTarget,0, currentTarget.length	);
}
@Override
public Tensor getInput() {
	// TODO Auto-generated method stub
	return TensorFactory.tensor(currentInput,0,currentInput.length);
}

}
