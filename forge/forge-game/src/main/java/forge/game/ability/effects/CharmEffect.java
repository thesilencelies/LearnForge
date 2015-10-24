package forge.game.ability.effects;

import com.google.common.collect.Iterables;
import forge.game.ability.AbilityFactory;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;
import forge.util.collect.FCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CharmEffect extends SpellAbilityEffect {

    public static List<AbilitySub> makePossibleOptions(final SpellAbility sa) {
        final Card source = sa.getHostCard();
        Iterable<Object> restriction = null;
        final String[] saChoices = sa.getParam("Choices").split(",");

        if (sa.hasParam("ChoiceRestriction")) {
            String rest = sa.getParam("ChoiceRestriction");
            if (rest.equals("NotRemembered")) {
                restriction = source.getRemembered();
            }
        }

        List<AbilitySub> choices = new ArrayList<AbilitySub>();
        int indx = 0;
        for (final String saChoice : saChoices) {
            if (restriction != null && Iterables.contains(restriction, saChoice)) {
                // If there is a choice restriction, and the current choice fails that, skip it.
                continue;
            }
            final String ab = source.getSVar(saChoice);
            AbilitySub sub = (AbilitySub) AbilityFactory.getAbility(ab, source);
            sub.setTrigger(sa.isTrigger());

            sub.setSVar("CharmOrder", Integer.toString(indx));
            choices.add(sub);
            indx++;
        }
        return choices;
    }

    @Override
    public void resolve(SpellAbility sa) {
        // all chosen modes have been chained as subabilities to this sa.
        // so nothing to do in this resolve
    }


    @Override
    protected String getStackDescription(SpellAbility sa) {
        return "";
    }

    public static void makeChoices(SpellAbility sa) {
        //this resets all previous choices
        sa.setSubAbility(null);

        final int num = Integer.parseInt(sa.hasParam("CharmNum") ? sa.getParam("CharmNum") : "1");
        final int min = sa.hasParam("MinCharmNum") ? Integer.parseInt(sa.getParam("MinCharmNum")) : num;

        Card source = sa.getHostCard();
        Player activator = sa.getActivatingPlayer();
        Player chooser = sa.getActivatingPlayer();

        if (sa.hasParam("Chooser")) {
            // Three modal cards require you to choose a player to make the modal choice'
            // Two of these also reference the chosen player during the spell effect
            
            //String choosers = sa.getParam("Chooser"); 
            FCollection<Player> opponents = activator.getOpponents(); // all cards have Choser$ Opponent, so it's hardcoded here
            chooser = activator.getController().chooseSingleEntityForEffect(opponents, sa, "Choose an opponent");
            source.setChosenPlayer(chooser);
        }
        
        List<AbilitySub> chosen = chooser.getController().chooseModeForAbility(sa, min, num);
        chainAbilities(sa, chosen);
    }

    private static void chainAbilities(SpellAbility sa, List<AbilitySub> chosen) {
        SpellAbility saDeepest = sa;
        while (saDeepest.getSubAbility() != null) {
            saDeepest = saDeepest.getSubAbility();
        }

        // Sort Chosen by SA order
        Collections.sort(chosen, new Comparator<AbilitySub>() {
            @Override
            public int compare(AbilitySub o1, AbilitySub o2) {
                return Integer.parseInt(o1.getSVar("CharmOrder")) - Integer.parseInt(o2.getSVar("CharmOrder"));
            }
        });

        for (AbilitySub sub : chosen) {
            saDeepest.setSubAbility(sub);
            sub.setActivatingPlayer(saDeepest.getActivatingPlayer());
            sub.setParent(saDeepest);

            // to chain the next one (but make sure it goes all the way at the end of the SA chain)
            saDeepest = sub;
            while (saDeepest.getSubAbility() != null) {
                saDeepest = saDeepest.getSubAbility();
            }
        }
    }


}
