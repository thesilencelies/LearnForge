package forge.learnedai.NNinput;

public class CardStateExp {
	public NNcardState startstate, endstate;
	public double reward;
	
	public CardStateExp(NNcardState ss,NNcardState es, double r){
		startstate = ss.clone();
		endstate = es.clone();
		reward = r;
	}
			
	public boolean equals(Object obj){
		if (obj instanceof CardStateExp) {
			CardStateExp s = (CardStateExp)obj;
            	if(s.reward == reward){
            		if(s.startstate.equals(startstate)){
            			if(s.endstate.equals(endstate)){
            				return true;
            			}
            		}
            	}
            return false;

        }
        return super.equals(obj);
	}
}
