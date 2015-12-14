package forge.learnedai.NNinput;

public class CardStateExp {
	public NNcardState startstate, endstate;
	public double reward;
	
	public CardStateExp(NNcardState ss,NNcardState es, double r){
		startstate = ss;
		endstate = es;
		reward = r;
	}

}
