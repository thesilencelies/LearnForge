package forge.learnedai.NNinput;

import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.training.TrainingInputProviderImpl;


public class NNSingleCardStateProvider extends TrainingInputProviderImpl {
	private static final long serialVersionUID = 3247533855332357907L;
    private NNcardState exp;

public NNSingleCardStateProvider(){
	super();
}

public void SetState(NNcardState bat){
	exp = bat;
}

@Override
public void reset() {
//currentInput = 0;	I feel like this line is needed
}
@Override
public int getInputSize() {
	return 1;
}
protected TrainingInputData getNextUnmodifiedInput() {
	return exp;
}
@Override
public float[] getNextInput() {
	return getNextUnmodifiedInput().getInput().getElements();
}

@Override
public float[] getNextTarget() {
	return exp.getTarget().getElements();
}


}
