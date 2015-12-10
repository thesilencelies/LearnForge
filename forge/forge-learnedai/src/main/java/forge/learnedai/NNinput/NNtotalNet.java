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
import com.github.neuralnetworks.training.random.NNRandomInitializer;
import com.github.neuralnetworks.training.random.MersenneTwisterRandomInitializer;
import com.github.neuralnetworks.training.TrainerFactory;
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
public class NNtotalNet {
	
	private NeuralNetworkImpl nnchoice;
	private NNcardStateProvider mycsprov;
	private NNcardState mycs;
	private BackPropagationTrainer bpt;
	
	private float[] target;
	
	HashMap<String, Integer> obscards;
	
	public NNtotalNet (Player me){
		//multilayer perceptron for the final decision for now
		nnchoice = NNFactory.mlp(new int []{3600, 1800, 1600, 1200},true);
		//we'll choose between every card in the game for now and see how it goes
		target = new float[1200];
		mycs = new NNcardState(me);
		mycsprov = new NNcardStateProvider(me,target);
		bpt = TrainerFactory.backPropagation(nnchoice, mycsprov, mycsprov, null, new NNRandomInitializer(new MersenneTwisterRandomInitializer(-0.01f, 0.01f)), 0.02f, 0.5f, 0f, 0f, 0f, 1, 1000, 1);
		
	}
	
	public Tensor rankcards(Player me){
		
		mycs.setPlayer(me);
		mycsprov.setPlayer(me);	//need to work out what's actually going on and tidy up later
		
	    Set<Layer> calculatedLayers = new UniqueList<>();
	    ValuesProvider results = TensorFactory.tensorProvider(nnchoice, 1, Environment.getInstance().getUseDataSharedMemory());


		mycsprov.populateNext(mycsprov);
		calculatedLayers.clear();
		calculatedLayers.add(n.getInputLayer());
		nnchoice.getLayerCalculator().calculate(n, n.getOutputLayer(), calculatedLayers, results);
		
		return results.get(nnchoice.getOutputLayer());

	}

	public void udpatematrix(float [] target){
		mycsprov.setTarget(target);
		bpt.train();
	}
	
	void save (String filename){
		//TODO
	}
	void load (String filename[]){
		//TODO
	}
}