package forge.learnedai.ability;

import forge.learnedai.ComputerUtil;
import forge.learnedai.ComputerUtilCost;
import forge.learnedai.ComputerUtilMana;
import forge.learnedai.SpellAbilityAi;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.cost.Cost;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

import java.util.List;
import java.util.Random;

public class DiscardAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final Card source = sa.getHostCard();
        final Cost abCost = sa.getPayCosts();

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkRemoveCounterCost(abCost, source)) {
                return false;
            }

        }

        final boolean humanHasHand = ai.getOpponent().getCardsIn(ZoneType.Hand).size() > 0;

        if (tgt != null) {
            if (!discardTargetAI(ai, sa)) {
                return false;
            }
        } else {
            // TODO: Add appropriate restrictions
            final List<Player> players = AbilityUtils.getDefinedPlayers(sa.getHostCard(), sa.getParam("Defined"), sa);

            if (players.size() == 1) {
                if (players.get(0) == ai) {
                    // the ai should only be using something like this if he has
                    // few cards in hand,
                    // cards like this better have a good drawback to be in the
                    // AIs deck
                } else {
                    // defined to the human, so that's fine as long the human
                    // has cards
                    if (!humanHasHand) {
                        return false;
                    }
                }
            } else {
                // Both players discard, any restrictions?
            }
        }

        if (sa.hasParam("NumCards")) {
           if (sa.getParam("NumCards").equals("X") && source.getSVar("X").equals("Count$xPaid")) {
                // Set PayX here to maximum value.
                final int cardsToDiscard = Math.min(ComputerUtilMana.determineLeftoverMana(sa, ai), ai.getOpponent()
                        .getCardsIn(ZoneType.Hand).size());
                if (cardsToDiscard < 1) {
                    return false;
                }
                source.setSVar("PayX", Integer.toString(cardsToDiscard));
            } else {
                if (AbilityUtils.calculateAmount(sa.getHostCard(), sa.getParam("NumCards"), sa) < 1) {
                    return false;
                }
            }
        }

        // TODO: Implement support for Discard AI for cards with AnyNumber set to true.

        // Don't use draw abilities before main 2 if possible
        if (ai.getGame().getPhaseHandler().getPhase().isBefore(PhaseType.MAIN2)
                && !sa.hasParam("ActivationPhases")) {
            return false;
        }

        // Don't tap creatures that may be able to block
        if (ComputerUtil.waitForBlocking(sa)) {
            return false;
        }

        final Random r = MyRandom.getRandom();
        boolean randomReturn = r.nextFloat() <= Math.pow(0.9, sa.getActivationsThisTurn());

        // some other variables here, like handsize vs. maxHandSize

        return randomReturn;
    } // discardCanPlayAI()

    private boolean discardTargetAI(final Player ai, final SpellAbility sa) {
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        Player opp = ai.getOpponent();
        if (opp.getCardsIn(ZoneType.Hand).isEmpty() && !ComputerUtil.activateForCost(sa, ai)) {
            return false;
        }
        if (tgt != null) {
            if (sa.canTarget(opp)) {
                sa.getTargets().add(opp);
                return true;
            }
        }
        return false;
    } // discardTargetAI()



    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt != null) {
            Player opp = ai.getOpponent();
            if (!discardTargetAI(ai, sa)) {
                if (mandatory && sa.canTarget(opp)) {
                    sa.getTargets().add(opp);
                } else if (mandatory && sa.canTarget(ai)) {
                    sa.getTargets().add(ai);
                } else {
                    return false;
                }
            }
        } else {
            if (sa.hasParam("AILogic")) {
            	if ("AtLeast2".equals(sa.getParam("AILogic"))) {
            		final List<Player> players = AbilityUtils.getDefinedPlayers(sa.getHostCard(), sa.getParam("Defined"), sa);
            		if (players.isEmpty() || players.get(0).getCardsIn(ZoneType.Hand).size() < 2) {
            			return false;
            		}
            	}
            }
            if ("X".equals(sa.getParam("RevealNumber")) && sa.getHostCard().getSVar("X").equals("Count$xPaid")) {
                // Set PayX here to maximum value.
                final int cardsToDiscard = Math.min(ComputerUtilMana.determineLeftoverMana(sa, ai), ai.getOpponent()
                        .getCardsIn(ZoneType.Hand).size());
                sa.getHostCard().setSVar("PayX", Integer.toString(cardsToDiscard));
            }
        }

        return true;
    } // discardTrigger()

    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player ai) {
        // Drawback AI improvements
        // if parent draws cards, make sure cards in hand + cards drawn > 0
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt != null) {
            return discardTargetAI(ai, sa);
        }
        // TODO: check for some extra things
        return true;
    } // discardCheckDrawbackAI()


    public boolean confirmAction(Player player, SpellAbility sa, PlayerActionConfirmMode mode, String message) {
        if ( mode == PlayerActionConfirmMode.Random ) { //
            // TODO For now AI will always discard Random used currently with: Balduvian Horde and similar cards
            return true;
        }
        return super.confirmAction(player, sa, mode, message);
    }
}
