package forge.learnedai.ability;

import forge.learnedai.ComputerUtil;
import forge.learnedai.ComputerUtilCost;
import forge.learnedai.SpellAbilityAi;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.cost.Cost;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;

import java.util.List;

public class PoisonAi extends SpellAbilityAi {

    /*
     * (non-Javadoc)
     *
     * @see
     * forge.card.abilityfactory.AbilityFactoryAlterLife.SpellAiLogic#canPlayAI
     * (forge.game.player.Player, java.util.Map,
     * forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getHostCard();
        // int humanPoison = AllZone.getHumanPlayer().getPoisonCounters();
        // int humanLife = AllZone.getHumanPlayer().getLife();
        // int aiPoison = AllZone.getComputerPlayer().getPoisonCounters();

        // TODO handle proper calculation of X values based on Cost and what
        // would be paid
        // final int amount =
        // AbilityFactory.calculateAmount(af.getHostCard(),
        // amountStr, sa);

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 1, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }
        }

        // Don't use poison before main 2 if possible
        if (ai.getGame().getPhaseHandler().getPhase().isBefore(PhaseType.MAIN2)
                && !sa.hasParam("ActivationPhases")) {
            return false;
        }

        // Don't tap creatures that may be able to block
        if (ComputerUtil.waitForBlocking(sa)) {
            return false;
        }


        if (sa.usesTargeting()) {
            sa.resetTargets();
            sa.getTargets().add(ai.getOpponent());
        }

        return true;
    }

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {

        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt != null) {
            sa.getTargets().add(ai.getOpponent());
        } else {
            final List<Player> players = AbilityUtils.getDefinedPlayers(sa.getHostCard(), sa.getParam("Defined"), sa);
            for (final Player p : players) {
                if (!mandatory && p == ai && (p.getPoisonCounters() > p.getOpponent().getPoisonCounters())) {
                    return false;
                }
            }
        }

        return true;
    }
}
