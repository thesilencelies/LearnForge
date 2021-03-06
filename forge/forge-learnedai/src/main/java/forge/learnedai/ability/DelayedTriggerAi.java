package forge.learnedai.ability;

import forge.learnedai.LearnedAiController;
import forge.learnedai.AiPlayDecision;
import forge.learnedai.LearnedPlayerControllerAi;
import forge.learnedai.SpellAbilityAi;
import forge.learnedai.SpellApiToAi;
import forge.game.ability.AbilityFactory;
import forge.game.player.Player;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;

public class DelayedTriggerAi extends SpellAbilityAi {

    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player ai) {
        if ("Always".equals(sa.getParam("AILogic"))) {
            // TODO: improve ai
            return true;
        }
        final String svarName = sa.getParam("Execute");
        final SpellAbility trigsa = AbilityFactory.getAbility(sa.getHostCard().getSVar(svarName), sa.getHostCard());
        trigsa.setActivatingPlayer(ai);

        if (trigsa instanceof AbilitySub) {
            return SpellApiToAi.Converter.get(((AbilitySub) trigsa).getApi()).chkDrawbackWithSubs(ai, (AbilitySub)trigsa);
        } else {
            return AiPlayDecision.WillPlay == ((LearnedPlayerControllerAi)ai.getController()).getAi().canPlaySa(trigsa);
        }
    }

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
        final String svarName = sa.getParam("Execute");
        final SpellAbility trigsa = AbilityFactory.getAbility(sa.getHostCard().getSVar(svarName), sa.getHostCard());
        LearnedAiController aic = ((LearnedPlayerControllerAi)ai.getController()).getAi();
        trigsa.setActivatingPlayer(ai);

        if (!sa.hasParam("OptionalDecider")) {
            return aic.doTrigger(trigsa, true);
        } else {
            return aic.doTrigger(trigsa, !sa.getParam("OptionalDecider").equals("You"));
        }
    }

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        final String svarName = sa.getParam("Execute");
        final SpellAbility trigsa = AbilityFactory.getAbility(sa.getSVar(svarName), sa.getHostCard());
        trigsa.setActivatingPlayer(ai);
        return AiPlayDecision.WillPlay == ((LearnedPlayerControllerAi)ai.getController()).getAi().canPlaySa(trigsa);
    }

}
