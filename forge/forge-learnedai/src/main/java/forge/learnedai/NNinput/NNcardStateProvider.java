package forge.learnedai.NNinput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.training.TrainingInputProviderImpl;


public class NNcardStateProvider extends TrainingInputProviderImpl {
	private static final long serialVersionUID = 3247533855332357907L;
    private List<NNcardState> exp;
    private Iterator<NNcardState> inputit,targit;

public NNcardStateProvider(){
	super();
	exp = new ArrayList<NNcardState>();
	inputit = exp.iterator();
	targit = exp.iterator();
}

public void SetReplayBatch(List<NNcardState> bat){
	exp = bat;
	inputit = exp.iterator();
	targit = exp.iterator();
}

@Override
public void reset() {
//currentInput = 0;	I feel like this line is needed
inputit = exp.iterator();
targit = exp.iterator();
}
@Override
public int getInputSize() {
	return exp.size();
}
protected TrainingInputData getNextUnmodifiedInput() {
	if(!inputit.hasNext()) reset();
	
	return inputit.next();
}
@Override
public float[] getNextInput() {
	return getNextUnmodifiedInput().getInput().getElements();
}

@Override
public float[] getNextTarget() {
	if(!targit.hasNext()) reset();
	return targit.next().getTarget().getElements();
}


}
