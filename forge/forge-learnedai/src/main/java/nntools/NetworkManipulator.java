package nntools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
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
		//Set<Layer> layers = nn.getLayers();
		//Class<? extends NeuralNetwork> cls = nn.getClass();
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
		Iterator<Connections> nconit= newconn.iterator();
		conit = connect.iterator();
		while(conit.hasNext()){
			TensorFactory.copy(((FullyConnected) conit.next()).getWeights(),((FullyConnected)(nconit.next())).getWeights());
		}
		
		return nnout;
	}
	
	public static void saveNetwork(NeuralNetworkImpl nn, Path savedst){
		List<Connections> connect = nn.getConnections();
		//need to work out how to make this more general later
		boolean bias = true;
		
		int nconn = connect.size();
		int[] layersize = new int[nconn/2 +1];
		Iterator<Connections> conit = connect.iterator(); 
		for (int i = 0; i < nconn; i++){
			Connections c = conit.next();
			if(i%2 ==0){
				layersize[i/2] = c.getInputUnitCount();
			}
			if(i == nconn -1){
				layersize[nconn/2] = c.getOutputUnitCount();
			}
		}
		
		
		FileOutputStream out = null;
		  try {
		    out = new FileOutputStream("fc.out");
		    FileChannel file = out.getChannel();
		    ByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, 4 * layersize.length);
		    //first element is the number of layers
		    buf.putInt(layersize.length);
		    //then there are the layers
		    for (int i : layersize) {
		      buf.putInt(i);
		    }
		    //then the contents of each layer (how big these are is calcuable from the layers)
		    conit = connect.iterator();
			while(conit.hasNext()){
				float[] weights = ((FullyConnected) conit.next()).getWeights().getElements();
				for (float i : weights){
					buf.putFloat(i);
				}
			}
			
		    file.close();
		  } catch (IOException e) {
		    throw new RuntimeException(e);
		  } finally {
		    if(out != null){
		    	try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		  }
	}
	public static NeuralNetworkImpl loadNetwork(Path savedst){
		Scanner scanner;
		try {
			scanner = new Scanner(savedst);	
			int nlayers = scanner.nextInt();
			int[] layers = new int[nlayers];
			for(int i = 0; i < nlayers; i++){
				layers[i] = scanner.nextInt();
			}
			List<float[]> connections = new ArrayList<float[]>();
			for(int i = 0; i < nlayers; i++){
				//work out how many connections there should be
				int nel = layers[i];
				float[] elements = new float[nel];
				for(int j = 0; j < nel; j++){
					elements[j] = scanner.nextFloat();
				}
				connections.add(elements);
			}
			//create the output nn - only sigmoid atm
			NeuralNetworkImpl nnout = NNFactory.mlpSigmoid(layers, true);
			//for each layer produce a copy of it
			List<Connections> newconn = nnout.getConnections();
			Iterator<Connections> nconit= newconn.iterator();
			Iterator<float[]>conit = connections.iterator();
			while(conit.hasNext()){
				TensorFactory.copy(TensorFactory.matrix(conit.next(),1),((FullyConnected)(nconit.next())).getWeights());
			}
			
			return nnout;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
