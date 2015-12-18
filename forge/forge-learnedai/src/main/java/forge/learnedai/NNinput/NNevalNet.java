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

public class NNevalNet {
	private NeuralNetworkImpl nnmem;	//to allow the system to record what's happening
	private NeuralNetworkImpl nnchoice;
	private TrainingInputData input;
	private TrainingInputData meminput;
	private NNcardStateProvider mycsprov;
	private NNSingleCardStateProvider testprov;
			
	private BackPropagationTrainer<?> bpt;
	private MultipleNeuronsOutputError oe;
	private ValuesProvider results;
	private ValuesProvider memresults;
	private Set<Layer> calculatedLayers;
	
	private double gamma, alpha;
	
	private List<CardStateExp> expreplay, replaybatch;
	
	private int maxexplen, batchlen;
	
	public NNevalNet(){
		this(Paths.get("/home/stephen/Documents/MagicNN.nn"));
	}
	public NNevalNet(Path p){
		this(p, 0.99,0.6);
	}
	public NNevalNet(Path p, double _gamma, double _alpha){
		gamma = _gamma;
		alpha = _alpha;
		maxexplen = 1000;
	    batchlen = 12;
		//multilayer perceptron for the final decision for now
		//base and final layers are fixed by the size of the maze and number of outputs
		mycsprov = new NNcardStateProvider();
		testprov = new NNSingleCardStateProvider();
		oe = new MultipleNeuronsOutputError();
		try{
			load(p);
		}catch(IOException e){
			//if we can't load, we default initialise
			nnchoice = NNFactory.mlpSigmoid(new int []{160, 1},true);
			bpt = TrainerFactory.backPropagation(nnchoice, mycsprov, testprov, oe, new NNRandomInitializer(new MersenneTwisterRandomInitializer(0,0.1f)),0.1f, 0.3f, 0f, 0f, 0, batchlen, batchlen, 20);
		}
		results = TensorFactory.tensorProvider(nnchoice,1, Environment.getInstance().getUseDataSharedMemory());
		calculatedLayers  = new UniqueList<Layer>();
		//connect the input to the neural network
		input = new TrainingInputDataImpl(results.get(nnchoice.getInputLayer()), results.get(oe));
		expreplay = new ArrayList<CardStateExp>();
	    replaybatch = new ArrayList<CardStateExp>();
		storeMem();
	}
	public NNevalNet (double _gamma, double _alpha){
		gamma = _gamma;
		alpha = _alpha;
		maxexplen = 1000;
	    batchlen = 12;
		//multilayer perceptron for the final decision for now
		//base and final layers are fixed by the size of the maze and number of outputs
		nnchoice = NNFactory.mlpSigmoid(new int []{160, 1},true);
		mycsprov = new NNcardStateProvider();
		testprov = new NNSingleCardStateProvider();
		oe = new MultipleNeuronsOutputError();
		results = TensorFactory.tensorProvider(nnchoice,1, Environment.getInstance().getUseDataSharedMemory());
		calculatedLayers  = new UniqueList<Layer>();				//float learningRate, float momentum, float l1weightDecay, float l2weightDecay, float dropoutRate, int trainingBatchSize, int testBatchSize, int epochs
		bpt = TrainerFactory.backPropagation(nnchoice, mycsprov, testprov, oe, new NNRandomInitializer(new MersenneTwisterRandomInitializer(0,0.1f)),0.1f, 0.3f, 0f, 0f, 0, batchlen, batchlen, 20);
		//connect the input to the neural network
		input = new TrainingInputDataImpl(results.get(nnchoice.getInputLayer()), results.get(oe));
		expreplay = new ArrayList<CardStateExp>();
	    replaybatch = new ArrayList<CardStateExp>();
		storeMem();
	}
	public NNevalNet (int[] layers, double _gamma, double _alpha){
		gamma = _gamma;
		alpha = _alpha;
		maxexplen = 1000;
	    batchlen = 12;

		//multilayer perceptron for the final decision for now
		//base and final layers are fixed by the size of the maze and number of outputs
		nnchoice = NNFactory.mlpSigmoid(layers,true);
		mycsprov =  new NNcardStateProvider();
		testprov = new NNSingleCardStateProvider();
		oe = new MultipleNeuronsOutputError();
		results = TensorFactory.tensorProvider(nnchoice,1, Environment.getInstance().getUseDataSharedMemory());
		calculatedLayers  = new UniqueList<Layer>();				//float learningRate, float momentum, float l1weightDecay, float l2weightDecay, float dropoutRate, int trainingBatchSize, int testBatchSize, int epochs
		bpt = TrainerFactory.backPropagation(nnchoice, mycsprov, testprov, oe, new NNRandomInitializer(new MersenneTwisterRandomInitializer(0,0.1f)), 0.1f, 0f, 0.1f, 0f, 0, batchlen, batchlen, 20);
		//connect the input to the neural network
	    input = new TrainingInputDataImpl(results.get(nnchoice.getInputLayer()), results.get(oe));
		expreplay = new ArrayList<CardStateExp>();
	    replaybatch = new ArrayList<CardStateExp>();
		storeMem();
	}
	public void load(Path nnp) throws IOException{
		//functions to load the neural net itself as well
		nnchoice = nntools.NetworkManipulator.loadNetwork(nnp);
		//don't want to randomly initialise it
		bpt =  TrainerFactory.backPropagation(nnchoice, mycsprov, testprov, oe, null, 0.1f, 0f, 0.1f, 0f, 0, batchlen, batchlen, 20);
	}
	public void save(){
		save(Paths.get("/home/stephen/Documents/MagicNN.nn"));
	}
	public void save(Path nnp){
		//functions to save the neural net itself as well
		nntools.NetworkManipulator.saveNetwork(nnchoice, nnp);
	}
	
	public void ObserveAndTrain(NNcardState start, NNcardState end, double reward){
		experience(start, end, reward);
		setTargets();
		bpt.train();
	}
	
	public double rankchoice(NNcardState state){
		//use the remembered network for decision making
		return runNNmem(state).get(0);
	}
	private Matrix runNN(NNcardState state){
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
	private Matrix runNNmem(NNcardState state){
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
	private void storeMem(){
		nnmem = NetworkManipulator.CopySigmoidNN(nnchoice);
		memresults = TensorFactory.tensorProvider(nnmem,1, Environment.getInstance().getUseDataSharedMemory());
		meminput = new TrainingInputDataImpl(memresults.get(nnmem.getInputLayer()), memresults.get(oe));
	}
	
	protected void experience(NNcardState startstate, NNcardState endstate, double reward){
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
		CardStateExp s = new CardStateExp(startstate, endstate,reward);
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
	protected void setTargets(){
		//sets the target values for each of the points in replaybatch
		Iterator<CardStateExp> it = replaybatch.iterator();
		List<NNcardState> learnbatch = new ArrayList<NNcardState>();
		while(it.hasNext()){
			CardStateExp i = it.next();
			float [] target = runNNmem(i.startstate).getElements();
			float val = 0;
			val = (float)rankchoice(i.endstate);
			//remove the normalisation
			val = (val - 0.5f)*2;
			//normalise the target
			val =  (float)((i.reward + gamma*val)/2)+0.5f;
			target[0] = target[0] + (float)(alpha*(val - target[0]));
			NNcardState s = i.startstate;
			s.setTarget(TensorFactory.matrix(target,1));
			learnbatch.add(s);
		}
		//apply this list to the provider
		mycsprov.SetReplayBatch(learnbatch);
	}

}