package forge.learnedai.NNinput;

import forge.learnedai.LearnedAiCardMemory;
import forge.game.card.Card;
import forge.game.card.CardCollection;

import java.util.List;
import java.util.stream.IntStream;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.lang.String;
import java.io.IOException;

import com.github.neuralnetworks.training.TrainingInputProviderImpl;
import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.calculation.neuronfunctions.TensorFunction;
import com.github.neuralnetworks.tensor.Tensor.TensorIterator;

public class NNcreaturestate extends TrainingInputProviderImpl {
	
    protected int getEffectivePower(final Card c) {
        return c.getNetCombatDamage();
    }
    protected int getEffectiveToughness(final Card c) {
        return c.getNetToughness();
    }

	
	private CardCollection creatures;
    private int evalSize;
    public int inputSize;
    private float[] currentCreature;
    private float[] currentInput;
    private float[] currentTarget;

    
public NNcreaturestate(CardCollection c, float[] target){
	super();
	creatures = c;
	evalSize = 37;
	inputSize = evalSize*c.size();
    currentCreature = new float[evalSize];
    currentInput = new float[inputSize];
    currentTarget = target;
}

@Override
public float[] getNextInput(){
	for (int i = 0; i < creatures.size(); i++){
		currentCreature = AssessCreature(creatures.get(i));
		try{
			System.arraycopy(currentCreature,0,currentInput,i*evalSize,evalSize);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	return currentInput;
}

@Override
public float[] getNextTarget(){	
	//this needs to be the updated utility estimate
	//which should be clamped to the modified values from the higher layers?
	//how to update this? - really it won't actually be used as this is an unsupervised section
	return currentTarget;
}

@Override
public void reset() {
super.reset();
}
@Override
public int getInputSize() {
return inputSize;
}

private float[] AssessCreature(final Card c){
		float[] weights = new float[evalSize];
        if (c.isToken()) {
        	weights[1]=1;
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
        weights[2]=power;
        weights[3]=toughness;
        weights[4]=c.getCMC();

        // Evasion keywords
        if (c.hasKeyword("Flying")) {
        	weights[5]=1;
        }
        if (c.hasKeyword("Unblockable")) {
            weights[6]=1;
        }
        if (c.hasKeyword("You may have CARDNAME assign its combat damage as though it weren't blocked.")) {
            weights[7]=1;
        }
        if (c.hasKeyword("Intimidate")) {
            weights[9]=1;
        }
        if (c.hasStartOfKeyword("Menace")) {
            weights[8]=1;
        }
        if (c.hasStartOfKeyword("CantBeBlockedBy")) {
        	weights[9]=1;
        }

        // Other good keywords
        if (power > 0) {
            if (c.hasKeyword("Double Strike")) {
            	weights[10]=1;
            	weights[11]=1;
            } else if (c.hasKeyword("First Strike")) {
            	weights[11]=1;
            }
            if (c.hasKeyword("Deathtouch")) {
            	weights[12]=1;
            }
            if (c.hasKeyword("Lifelink")) {
            	weights[13]=1;
            }
            if (c.hasKeyword("Trample")) {
            	weights[14]=1;
            }
            if (c.hasKeyword("Vigilance")) {
            	weights[15]=1;
            }
        }
        weights[16]=c.getAmountOfKeyword("Exalted");

        //lords
        if(c.hasKeyword("creatures get +") || c.hasKeyword("creatures you control get +")){
        	weights[36]=1;
        }
        
        // Defensive Keywords
        if (c.hasKeyword("Reach")) {
        	weights[0]=1;
        }

        // Protection
        if (c.hasKeyword("Indestructible")) {
        	weights[17]=1;
        }
        if (c.hasKeyword("Prevent all combat damage that would be dealt to CARDNAME.")) {
        	weights[18]=1;
        }
        if (c.hasKeyword("Hexproof")) {
        	weights[19]=1;
        }
        if (c.hasStartOfKeyword("Protection")) {
        	weights[20]=1;
        }
        if (c.hasStartOfKeyword("PreventAllDamageBy")) {
        	weights[21]=1;
        }

        // Bad keywords
        if (c.hasKeyword("Defender") || c.hasKeyword("CARDNAME can't attack.")) {
        	weights[22]=1;
        }
        if (c.getSVar("SacrificeEndCombat").equals("True")) {
        	weights[23]=1;
        }
        if (c.hasKeyword("CARDNAME can't block.")) {
        	weights[24]=1;
        }
        if (c.hasKeyword("CARDNAME attacks each turn if able.")
                || c.hasKeyword("CARDNAME attacks each combat if able.")) {
        	weights[25]=1;
        }
        if (c.hasKeyword("CARDNAME can block only creatures with flying.")) {
        	weights[26]=1;
        }

        if (c.hasSVar("DestroyWhenDamaged")) {
        	weights[27]=1;
        }

        if (c.hasKeyword("CARDNAME can't attack or block.")) {
        	weights[28]=1;
        }
        if (c.hasKeyword("CARDNAME doesn't untap during your untap step.")) {
        	weights[29]=1;
        }
        if (c.hasSVar("EndOfTurnLeavePlay")) {
        	weights[30]= 1;
        }
        if (c.hasStartOfKeyword("At the beginning of your upkeep, sacrifice CARDNAME unless you pay")) {
        	weights[31] = 1;
        }

        if (c.hasStartOfKeyword("At the beginning of your upkeep, CARDNAME deals")) {
        	weights[32] = 1;
        }
        if (c.getSVar("Targeting").equals("Dies")) {
        	weights[33]= 1;
        }


        if (!c.getManaAbilities().isEmpty()) {
        	weights[34] = 1;
        }

        if (c.isUntapped()) {
        	weights[35] = 1;
        }

		return weights;
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