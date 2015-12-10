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
import com.github.neuralnetworks.tensor.Tensor.TensorIterator;

public class NNcardStateProvider extends TrainingInputProviderImpl {
	
	private HashMap<String, Integer> obscards;
	private int highestusedind;
	private Player me;
	
    private int evalSize;
    public int inputSize;
    private float[] currentZone;
    private float[] currentInput;
    private float[] currentTarget;

public NNcardStateProvider(Player p, float [] target, HashMap<String, Integer> oc){
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
    
    
public NNcardStateProvider(Player p, float[] target){
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

@Override
public float[] getNextInput(){
	for (int i = 0; i < 3; i++){
		currentZone = zoneRead(i);
		try{
			System.arraycopy(currentZone,0,currentInput,i*evalSize,evalSize);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	return currentInput;
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
			//should I consider the graveyard at some point - may lead to over-fitting
	}
	return new float[1200];
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




@Override
public float[] getNextTarget(){	
 
	return currentTarget;
}

public void setNextTarget (float[] target){
	currentTarget = target;
}
public void setPlayer (Player p){	//might be needed to refresh the player before each pass of the NN
	me = p;
}

@Override
public void reset() {
super.reset();
}
@Override
public int getInputSize() {
return inputSize;
}


@Override
public void populateNext(TrainingInputData ti) {
	beforeBatch(ti);

	// batch size
	int batchSize = 0;
	if (ti.getInput() != null && ti.getTarget() != null && ti.getInput().getDimensions()[ti.getInput().getDimensions().length - 1] != ti.getInput().getDimensions()[ti.getInput().getDimensions().length - 1]) {
		throw new IllegalArgumentException("Input and target batch size don't match");
	}

	if (ti.getInput() != null) {
		batchSize = ti.getInput().getDimensions()[ti.getInput().getDimensions().length - 1];
	} else if (ti.getTarget() != null) {
		batchSize = ti.getTarget().getDimensions()[ti.getTarget().getDimensions().length - 1];
	}

	int[] inputDims = null;
	int[][] inputLimits = null;
	int[] targetDims = null;
	int[][] targetLimits = null;

	if (ti.getInput() != null) {
		inputDims = ti.getInput().getDimensions();
		inputLimits = new int[2][inputDims.length];
		for (int i = 0; i < inputDims.length - 1; i++) {
			inputLimits[1][i] = inputDims[i] - 1;
		}
	}

	if (ti.getTarget() != null) {
		targetDims = ti.getTarget().getDimensions();
		targetLimits = new int[2][targetDims.length];
		for (int i = 0; i < targetDims.length - 1; i++) {
			targetLimits[1][i] = targetDims[i] - 1;
		}
	}

	// data population
	for (int i = 0; i < batchSize; i++) {
		beforeSample();

		if (ti.getInput() != null) {
			inputLimits[0][inputDims.length - 1] = inputLimits[1][inputDims.length - 1] = i;
			TensorIterator inputIt = ti.getInput().iterator(inputLimits);
			float[] inputEl = getNextInput();
			IntStream.range(0, inputEl.length).forEach((j) -> ti.getInput().getElements()[inputIt.next()] = inputEl[j]);
		}

		if (ti.getTarget() != null) {
			targetLimits[0][targetDims.length - 1] = targetLimits[1][targetDims.length - 1] = i;
			TensorIterator targetIt = ti.getTarget().iterator(targetLimits);
			float[] targetEl = getNextTarget();
			IntStream.range(0, targetEl.length).forEach((j) -> ti.getTarget().getElements()[targetIt.next()] = targetEl[j]);
		}

		afterSample();
	}

	afterBatch(ti);
}

}
