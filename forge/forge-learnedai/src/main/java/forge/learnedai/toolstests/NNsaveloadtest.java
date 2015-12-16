package forge.learnedai.toolstests;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.github.neuralnetworks.architecture.FullyConnected;
import com.github.neuralnetworks.architecture.Layer;
import com.github.neuralnetworks.architecture.NeuralNetworkImpl;
import com.github.neuralnetworks.architecture.types.NNFactory;
import com.github.neuralnetworks.calculation.memory.ValuesProvider;
import com.github.neuralnetworks.input.MultipleNeuronsOutputError;
import com.github.neuralnetworks.input.SimpleInputProvider;
import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;
import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.training.TrainingInputDataImpl;
import com.github.neuralnetworks.util.Environment;
import com.github.neuralnetworks.util.UniqueList;

import nntools.NetworkManipulator;

public class NNsaveloadtest {
	public static void main(String [] args){
		NeuralNetworkImpl mlp = NNFactory.mlpSigmoid(new int[] { 4,3,2,1}, true);

//      [-5.744886, -5.7570715, -7.329507, -7.33055] - l1-l2
//      [8.59142, 3.1430812] - bias l2
//      [12.749131, -12.848652] - l2-l3
//      [-6.1552725] - bias l3

	// weights
	FullyConnected fc1 = (FullyConnected) mlp.getInputLayer().getConnections().get(0);
	fc1.getWeights().set(-5.744886f, new int[]{ 0, 0});
	fc1.getWeights().set(-5.7570715f,new int[]{ 0, 1});
	fc1.getWeights().set(-7.329507f,new int[]{ 1, 0});
	fc1.getWeights().set(-7.33055f, new int[]{1, 1});

	FullyConnected b1 = (FullyConnected) fc1.getOutputLayer().getConnections().get(1);
	b1.getWeights().set(8.59142f,new int[]{ 0, 0});
	b1.getWeights().set(3.1430812f,new int[]{ 1, 0});

	FullyConnected fc2 = (FullyConnected) mlp.getOutputLayer().getConnections().get(0);
	fc2.getWeights().set(12.749131f, new int[]{0, 0});
	fc2.getWeights().set(-12.848652f, new int[]{0, 1});

	FullyConnected b2 = (FullyConnected) fc2.getOutputLayer().getConnections().get(1);
	b2.getWeights().set(-6.1552725f, new int[]{0, 0});
	
	Path spath = Paths.get("/home/stephen/Documents/testnn.nn");
	NetworkManipulator.saveNetwork(mlp, spath);
	
	NeuralNetworkImpl nn2 = NetworkManipulator.loadNetwork(spath);
	
	b1 = (FullyConnected) fc1.getOutputLayer().getConnections().get(1);
	b1.getWeights().set(1f,new int[]{ 0, 0});
	b1.getWeights().set(1,new int[]{ 1, 0});
	// create training and testing input providers
	SimpleInputProvider input = new SimpleInputProvider(new float[][] { {0, 0}, {0, 1}, {1, 0}, {1, 1} }, new float[][] { {0}, {1}, {1}, {0} });
	for(int i = 0; i < input.getInputSize(); i += 1){
		Matrix M = runNN(mlp,input);
		float[] el = M.getElements();
		System.out.printf("elements for input on original %d were: ", i);
		for(int j= 0, len = el.length; j<len;j++){
			System.out.printf(" %1.4f ", el[j]);
		}
		System.out.printf("%n");
	}
	input.reset();
	for(int i = 0; i < input.getInputSize(); i += 1){
		Matrix M = runNN(nn2,input);
		float[] el = M.getElements();
		System.out.printf("elements for input on copy %d were: ", i);
		for(int j= 0, len = el.length; j<len;j++){
			System.out.printf(" %1.4f ", el[j]);
		}
		System.out.printf("%n");
	}
	}
	
	static private Matrix runNN(NeuralNetworkImpl nnchoice, SimpleInputProvider testprov){
		
		MultipleNeuronsOutputError oe =  new MultipleNeuronsOutputError();
		ValuesProvider results = TensorFactory.tensorProvider(nnchoice,1, Environment.getInstance().getUseDataSharedMemory());
		Set<Layer> calculatedLayers= new UniqueList<Layer>();
		TrainingInputData input = new TrainingInputDataImpl(results.get(nnchoice.getInputLayer()), results.get(oe));
	    if (oe != null) {
		oe.reset();
		results.add(oe, results.get(nnchoice.getOutputLayer()).getDimensions());
	    }
	    //apply the new observation
	    testprov.populateNext(input);
		calculatedLayers.clear();
		calculatedLayers.add(nnchoice.getInputLayer());
		nnchoice.getLayerCalculator().calculate(nnchoice, nnchoice.getOutputLayer(), calculatedLayers, results);

		return  results.get(nnchoice.getOutputLayer());
	}
}
