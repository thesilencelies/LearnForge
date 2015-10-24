package forge.game;

import forge.game.player.Player;
import forge.game.trigger.TriggerType;

import java.util.HashMap;

import com.google.common.collect.ImmutableList;

/** 
 * Represents the planar dice for Planechase games.
 *
 */
public enum PlanarDice {
    Planeswalk,
    Chaos,
    Blank;
    
    public static PlanarDice roll(Player roller, PlanarDice riggedResult)
    {
        PlanarDice res = Blank;
        int i = forge.util.MyRandom.getRandom().nextInt(6);
        if (riggedResult != null)
            res = riggedResult;
        else if (i == 0)
            res = Planeswalk;
        else if (i == 1)
            res = Chaos;
        
        
        PlanarDice trigRes = res;
        
        if(roller.getGame().getStaticEffects().getGlobalRuleChange(GlobalRuleChange.blankIsChaos)
                && res == Blank)
        {
            trigRes = Chaos;
        }
        
        HashMap<String,Object> runParams = new HashMap<String,Object>();
        runParams.put("Player", roller);
        runParams.put("Result", trigRes);
        roller.getGame().getTriggerHandler().runTrigger(TriggerType.PlanarDice, runParams,false);
    
        
        return res;
    }
    
    /**
     * Parses a string into an enum member.
     * @param string to parse
     * @return enum equivalent
     */
    public static PlanarDice smartValueOf(String value) {

        final String valToCompate = value.trim();
        for (final PlanarDice v : PlanarDice.values()) {
            if (v.name().compareToIgnoreCase(valToCompate) == 0) {
                return v;
            }
        }

        throw new RuntimeException("Element " + value + " not found in PlanarDice enum");
    }

    public static final ImmutableList<PlanarDice> values = ImmutableList.copyOf(values());
}
