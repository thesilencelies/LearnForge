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
import com.github.neuralnetworks.calculation.neuronfunctions.ConnectionCalculatorFullyConnected;
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
	public static NeuralNetworkImpl CopyRELUNN(NeuralNetworkImpl nn){
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
		NeuralNetworkImpl nnout = NNFactory.mlpRelu(layersize, bias, new ConnectionCalculatorFullyConnected());
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
		int connectsize = 0;
		Iterator<Connections> conit = connect.iterator(); 
		for (int i = 0; i < nconn; i++){
			Connections c = conit.next();
			connectsize += ((FullyConnected)c).getWeights().getElements().length;
			if(i%2 ==0){
				layersize[i/2] = c.getInputUnitCount();
			}
			if(i == nconn -1){
				layersize[nconn/2] = c.getOutputUnitCount();
			}
		}
		
		  try {
		    ByteBuffer buf = ByteBuffer.allocate(4 * (layersize.length + connectsize + 1));
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
			Files.write(savedst, buf.array());
		  } catch (IOException e) {
			  e.printStackTrace();
		    throw new RuntimeException(e);
		  } 
	}
	private static class simplescanner {
		byte[] data;
		int it = 0;
		public simplescanner(Path p) throws IOException{
			try{
				data = Files.readAllBytes(p);
			}catch(IOException e){
				throw e;
			}
		}
		//this is such a hack. if the file is remotely wrong,
		//this will give array out of bounds exceptions all over
		//but then the only fix is manual anyway
		public float nextFloat(){
			int retval = 0;
			retval = retval | (data[it++] & 0xff) << 24;
			retval = retval | (data[it++] & 0xff) << 16;
			retval = retval | (data[it++] & 0xff) << 8;
			retval = retval | (data[it++] & 0xff) << 0;
			return Float.intBitsToFloat(retval);
		}
		public int nextInt(){
			int retval = 0;
			retval = retval |(data[it++] & 0xff) << 24;
			retval = retval | (data[it++] & 0xff) << 16;
			retval = retval | (data[it++] & 0xff) << 8;
			retval = retval | (data[it++] & 0xff) << 0;
			return retval;
		}
	}
	
	
	public static NeuralNetworkImpl loadNetwork(Path savedst, boolean RELU) throws IOException{
		simplescanner scanner;
			scanner = new simplescanner(savedst);	
			int nlayers = scanner.nextInt();
			int[] layers = new int[nlayers];
			for(int i = 0; i < nlayers; i++){
				layers[i] = scanner.nextInt();
			}
			List<float[]> connections = new ArrayList<float[]>();
			for(int i = 0; i < nlayers -1; i++){
				//work out how many connections there should be
				int nel = layers[i] * layers[i + 1];
				float[] elements = new float[nel];
				for(int j = 0; j < nel; j++){
					elements[j] = scanner.nextFloat();
				}
				connections.add(elements);
				//odd layers means odd connections
				nel = layers[i+1];
				float[] elements2 = new float[nel];
				for(int j = 0; j < nel; j++){
					elements2[j] = scanner.nextFloat();
				}
				connections.add(elements2);
			}
			//create the output nn
			NeuralNetworkImpl nnout;
			if(RELU){
				nnout = NNFactory.mlpRelu(layers, true, new ConnectionCalculatorFullyConnected());
			}
			else{
				nnout = NNFactory.mlpSigmoid(layers, true);
			}

			//for each layer produce a copy of it
			List<Connections> newconn = nnout.getConnections();
			Iterator<Connections> nconit= newconn.iterator();
			Iterator<float[]>conit = connections.iterator();
			int i = 0;
			while(conit.hasNext()){
				TensorFactory.copy(TensorFactory.matrix(conit.next(),layers[i]),((FullyConnected)(nconit.next())).getWeights());
				i++;
				TensorFactory.copy(TensorFactory.matrix(conit.next(),1),((FullyConnected)(nconit.next())).getWeights());
			}
			
			return nnout;
	}
}
