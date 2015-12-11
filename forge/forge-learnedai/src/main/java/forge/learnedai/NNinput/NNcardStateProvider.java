package forge.learnedai.NNinput;

import com.github.neuralnetworks.training.TrainingInputProviderImpl;

//this may not even really be needed.
//would it be better to create the input externally and attach it, or run that in here?

public class NNcardStateProvider extends TrainingInputProviderImpl {

	private static final long serialVersionUID = 1L;
	private float[] currentInput;
    private float[] currentTarget;

public NNcardStateProvider(){
	super();
}

@Override
public float[] getNextInput(){

	//TODO
	return currentInput;
}



@Override
public float[] getNextTarget(){	
 
	return currentTarget;
}

public void setNextTarget (float[] target){
	currentTarget = target;
}

@Override
public void reset() {
super.reset();
}
@Override
public int getInputSize() {
return currentInput.length;
}

}
