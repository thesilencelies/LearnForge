package forge.learnedai.NNinput;

import nntools.NetworkManipulator;

import com.github.neuralnetworks.architecture.types.NNFactory;
import com.github.neuralnetworks.architecture.NeuralNetworkImpl;
import com.github.neuralnetworks.architecture.Layer;
import com.github.neuralnetworks.training.backpropagation.BackPropagationTrainer;
import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.Tensor;
import com.github.neuralnetworks.tensor.TensorFactory;
import com.github.neuralnetworks.training.random.NNRandomInitializer;
import com.github.neuralnetworks.training.random.MersenneTwisterRandomInitializer;
import com.github.neuralnetworks.training.TrainerFactory;
import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.training.TrainingInputDataImpl;
import com.github.neuralnetworks.calculation.memory.ValuesProvider;
import com.github.neuralnetworks.calculation.neuronfunctions.ConnectionCalculatorFullyConnected;
import com.github.neuralnetworks.input.MultipleNeuronsOutputError;
import com.github.neuralnetworks.util.Environment;
import com.github.neuralnetworks.util.Properties;
import com.github.neuralnetworks.util.UniqueList;

import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

public final class NNevalNet {
	private static NeuralNetworkImpl nnmem;	//to allow the system to record what's happening
	private static NeuralNetworkImpl nnchoice;
	private static TrainingInputData input;
	private static TrainingInputData meminput;
	private static NNcardStateProvider mycsprov;
	private static NNSingleCardStateProvider testprov;
			
	private static BackPropagationTrainer<?> bpt;
	private static MultipleNeuronsOutputError oe;
	private static ValuesProvider results;
	private static ValuesProvider memresults;
	private static Set<Layer> calculatedLayers;
	
	private static double gamma, alpha;
	
	private static List<CardStateExp> expreplay, replaybatch;
	
	private static int maxexplen, batchlen;
	
	private NNevalNet(){
		//stop anyone from intialising the class
	}
		
	public static void initialize(){
		gamma = 0.99;
		alpha = 0.8;
		maxexplen = 400;
	    batchlen = 12;
		//multilayer perceptron for the final decision for now
		//base and final layers are fixed by the size of the maze and number of outputs
		mycsprov = new NNcardStateProvider();
		testprov = new NNSingleCardStateProvider();
		oe = new MultipleNeuronsOutputError();
		try{
			load(Paths.get("/home/stephen/Documents/MagicNN.nn"));
		}catch(IOException e){
			//if we can't load, we default initialise
			nnchoice = NNFactory.mlpRelu(new int []{160,200,200,1},true, new ConnectionCalculatorFullyConnected());
			bpt = TrainerFactory.backPropagation(nnchoice, mycsprov, testprov, oe, new NNRandomInitializer(new MersenneTwisterRandomInitializer(-0.1f,0.1f)),0.0001f, 0.6f, 0f, 0f, 0, batchlen, batchlen, 12);
			bpt.train();
			bpt = TrainerFactory.backPropagation(nnchoice, mycsprov, testprov, oe,null,0.0001f, 0.6f, 0f, 0f, 0, batchlen, batchlen, 12);
		}
		results = TensorFactory.tensorProvider(nnchoice,1, Environment.getInstance().getUseDataSharedMemory());
		calculatedLayers  = new UniqueList<Layer>();
		//connect the input to the neural network
		input = new TrainingInputDataImpl(results.get(nnchoice.getInputLayer()), results.get(oe));
		expreplay = new ArrayList<CardStateExp>();
	    replaybatch = new ArrayList<CardStateExp>();
		storeMem();
	}
	public static void load(Path nnp) throws IOException{
		//functions to load the neural net itself as well
		nnchoice = nntools.NetworkManipulator.loadNetwork(nnp, true);
		//don't want to randomly initialise it
		bpt =  TrainerFactory.backPropagation(nnchoice, mycsprov, testprov, oe, null, 0.0001f, 0f, 0.8f, 0f, 0, batchlen, batchlen, 12);
	}
	public static void save(){
		save(Paths.get("/home/stephen/Documents/MagicNN.nn"));
	}
	public static void save(Path nnp){
		//functions to save the neural net itself as well
		nntools.NetworkManipulator.saveNetwork(nnchoice, nnp);
	}
	
	public static void ObserveAndTrain(NNcardState start, NNcardState end, double reward){
		experience(start, end, reward);
		setTargets();
		bpt.train();
	}
	
	public static double rankchoice(NNcardState state){
		//use the remembered network for decision making
		return runNNmem(state).get(0);
	}
	private static Matrix runNN(NNcardState state){
		 if (oe != null) {
		oe.reset();
		results.add(oe, results.get(nnchoice.getOutputLayer()).getDimensions());
	    }
		//apply the new observation
	    testprov.SetState(state);
	    testprov.populateNext(input);
		calculatedLayers.clear();
		calculatedLayers.add(nnchoice.getInputLayer());
		nnchoice.getLayerCalculator().calculate(nnchoice, nnchoice.getOutputLayer(), calculatedLayers, results);

		return  results.get(nnchoice.getOutputLayer());
	}
	private static Matrix runNNmem(NNcardState state){
		if (oe != null) {
		oe.reset();
		memresults.add(oe, memresults.get(nnmem.getOutputLayer()).getDimensions());
	    }
	    //apply the new observation
		testprov.SetState(state);
	    testprov.populateNext(meminput);
		calculatedLayers.clear();
		calculatedLayers.add(nnmem.getInputLayer());
		nnmem.getLayerCalculator().calculate(nnmem, nnmem.getOutputLayer(), calculatedLayers, memresults);

		return  memresults.get(nnmem.getOutputLayer());
	}
	public static void storeMem(){
		nnmem = NetworkManipulator.CopyRELUNN(nnchoice);
		memresults = TensorFactory.tensorProvider(nnmem,1, Environment.getInstance().getUseDataSharedMemory());
		meminput = new TrainingInputDataImpl(memresults.get(nnmem.getInputLayer()), memresults.get(oe));
	}
	
	protected static void experience(NNcardState startstate, NNcardState endstate, double reward){
		//moves the position, then stores the results in expreplay, then prepares a random batch from experience
		//create the random batch
		if(expreplay.size() < batchlen){
			replaybatch = new ArrayList<CardStateExp>(expreplay);
		}
		else{
			replaybatch.clear();
		//select batchlen members from expreplay with equal probability
			double batchleft = batchlen;
			for(int i = 0, len = expreplay.size(); i < len;i++){
				double prob = batchleft/(len-i);
				CardStateExp se = expreplay.get(i);
				if(Math.random() < prob){
					replaybatch.add(se);
					batchleft--;
					if(batchleft < 1){
						break;
					}
				}
			}
		}
		//add the new experience
		CardStateExp s = new CardStateExp(startstate.clone(), endstate.clone(),reward);
		//don't add repeat experiences
		if(!expreplay.contains(s)){
			expreplay.add(s);
		}
		if(expreplay.size() > maxexplen){
			expreplay.remove(0);
		}
		//always include the most recent experience in the batch.
		replaybatch.add(s);
	}
	protected static void setTargets(){
		//sets the target values for each of the points in replaybatch
		Iterator<CardStateExp> it = replaybatch.iterator();
		List<NNcardState> learnbatch = new ArrayList<NNcardState>();
		while(it.hasNext()){
			CardStateExp i = it.next();
			float [] target = runNNmem(i.startstate).getElements();
			float val = 0;
			val = (float)rankchoice(i.endstate);
			//normalise the target
			val =  (float)((i.reward + gamma*val));
			target[0] = target[0] + (float)(alpha*(val - target[0]));
			NNcardState s = i.startstate;
			s.setTarget(TensorFactory.matrix(target,1));
			learnbatch.add(s);
		}
		//apply this list to the provider
		mycsprov.SetReplayBatch(learnbatch);
	}

}