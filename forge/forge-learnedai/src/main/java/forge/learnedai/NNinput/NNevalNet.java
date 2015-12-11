package forge.learnedai.NNinput;

import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardCollectionView;
import forge.game.card.CardCollection;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

import com.github.neuralnetworks.architecture.types.NNFactory;
import com.github.neuralnetworks.architecture.NeuralNetworkImpl;
import com.github.neuralnetworks.architecture.Layer;
import com.github.neuralnetworks.training.backpropagation.BackPropagationTrainer;
import com.github.neuralnetworks.tensor.Tensor;
import com.github.neuralnetworks.tensor.TensorFactory;
import com.github.neuralnetworks.training.random.NNRandomInitializer;
import com.github.neuralnetworks.training.random.MersenneTwisterRandomInitializer;
import com.github.neuralnetworks.training.TrainerFactory;
import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.calculation.memory.ValuesProvider;
import com.github.neuralnetworks.util.Environment;
import com.github.neuralnetworks.util.Properties;
import com.github.neuralnetworks.util.UniqueList;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;

//there are a lot of definitional magic numbers here right now
//how would loading from a file be handled?
public class NNevalNet {
	
	private NeuralNetworkImpl nnchoice;
	private NNcardStateProvider mycsprov;
	private TrainingInputData input;
	private BackPropagationTrainer bpt;
	
	private float[] target;
	
	HashMap<String, Integer> obscards;
	
	public NNevalNet (){
		load("Default filepath");
	}
	
	//given that in the end we are strictly evaluating the states against each other, we only need a single output
	public double rankcards(Player me){
		
		mycsprov.setPlayer(me);	//need to work out what's actually going on and tidy up later
		
	    Set<Layer> calculatedLayers = new UniqueList<>();
	    ValuesProvider results = TensorFactory.tensorProvider(nnchoice, 1, Environment.getInstance().getUseDataSharedMemory());


		mycsprov.populateNext((TrainingInputData) mycsprov);
		calculatedLayers.clear();
		calculatedLayers.add(nnchoice.getInputLayer());
		nnchoice.getLayerCalculator().calculate(nnchoice, nnchoice.getOutputLayer(), calculatedLayers, results);
		
		return results.get(nnchoice.getOutputLayer()).get(0);

	}

	//TODO - make these the correct setup, using
	public void udpatematrix(float [] target){
		mycsprov.setNextTarget(target);
		bpt.train();
	}
	
	//when is this called??
	void save (String filename){
		//TODO
	}
	void load (String filename){
		//TODO
	}
}