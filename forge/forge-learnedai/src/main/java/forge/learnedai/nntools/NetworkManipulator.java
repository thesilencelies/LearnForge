package nntools;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.neuralnetworks.architecture.Connections;
import com.github.neuralnetworks.architecture.FullyConnected;
import com.github.neuralnetworks.architecture.Layer;
import com.github.neuralnetworks.architecture.NeuralNetwork;
import com.github.neuralnetworks.architecture.NeuralNetworkImpl;
import com.github.neuralnetworks.architecture.types.NNFactory;
import com.github.neuralnetworks.tensor.Matrix;
import com.github.neuralnetworks.tensor.TensorFactory;

public class NetworkManipulator {
	public static NeuralNetworkImpl CopySigmoidNN(NeuralNetworkImpl nn){
		//get the size parameters of the original network
		Set<Layer> layers = nn.getLayers();
		Class<? extends NeuralNetwork> cls = nn.getClass();
		List<Connections> connect = nn.getConnections();
		boolean bias = true;
		
		int nconn = connect.size();
		int[] layersize = new int[nconn/2 +1];
		//int[] coninsize = new int[nconn];
		//int[] conoutsize = new int[nconn];
		Iterator<Connections> conit = connect.iterator(); 
		for (int i = 0; i < nconn; i++){
			Connections c = conit.next();
			//coninsize[i] = c.getInputUnitCount();
			//conoutsize[i] = c.getOutputUnitCount();
			if(i%2 ==0){
				layersize[i/2] = c.getInputUnitCount();
			}
			if(i == nconn -1){
				layersize[nconn/2] = c.getOutputUnitCount();
			}
			//conoutsize[i] = c.getOutputUnitCount();
		}
		//create the new network (assume they have bias for now...)
		//also gonna assume they're a sigmoid
		NeuralNetworkImpl nnout = NNFactory.mlpSigmoid(layersize, bias);
		//for each layer produce a copy of it
		List<Connections> newconn = nnout.getConnections();
		Iterator nconit= newconn.iterator();
		conit = connect.iterator();
		while(conit.hasNext()){
			TensorFactory.copy(((FullyConnected) conit.next()).getWeights(),((FullyConnected)(nconit.next())).getWeights());
		}
		
		return nnout;
	}
}
