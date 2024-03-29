package forge.learnedai.ability;

import forge.learnedai.ComputerUtil;
import forge.learnedai.ComputerUtilCombat;
import forge.learnedai.ComputerUtilCost;
import forge.learnedai.SpellAbilityAi;
import forge.game.Game;
import forge.game.GameObject;
import forge.game.card.Card;
import forge.game.card.CardCollectionView;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.combat.Combat;
import forge.game.cost.Cost;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

import java.util.List;

public class RegenerateAllAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        final Card hostCard = sa.getHostCard();
        boolean chance = false;
        final Cost abCost = sa.getPayCosts();
        final Game game = ai.getGame();
        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, hostCard)) {
                return false;
            }

            if (!ComputerUtilCost.checkCreatureSacrificeCost(ai, abCost, hostCard)) {
                return false;
            }

            if (!ComputerUtilCost.checkLifeCost(ai, abCost, hostCard, 4, null)) {
                return false;
            }
        }

        // filter AIs battlefield by what I can target
        String valid = "";

        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }

        CardCollectionView list = game.getCardsIn(ZoneType.Battlefield);
        list = CardLists.getValidCards(list, valid.split(","), hostCard.getController(), hostCard);
        list = CardLists.filter(list, CardPredicates.isController(ai));

        if (list.size() == 0) {
            return false;
        }

        int numSaved = 0;
        if (!game.getStack().isEmpty()) {
            final List<GameObject> objects = ComputerUtil.predictThreatenedObjects(sa.getActivatingPlayer(), sa);

            for (final Card c : list) {
                if (objects.contains(c) && c.getShieldCount() == 0) {
                    numSaved++;
                }
            }
        } else {
            if (game.getPhaseHandler().is(PhaseType.COMBAT_DECLARE_BLOCKERS)) {
                final List<Card> combatants = CardLists.filter(list, CardPredicates.Presets.CREATURES);
                final Combat combat = game.getCombat();
                for (final Card c : combatants) {
                    if (c.getShieldCount() == 0 && ComputerUtilCombat.combatantWouldBeDestroyed(ai, c, combat)) {
                        numSaved++;
                    }
                }
            }
        }

        if (numSaved > 1) {
            chance = true;
        }

        return chance;
    }

    @Override
    protected boolean doTriggerAINoCost(Player aiPlayer, SpellAbility sa, boolean mandatory) {
        boolean chance = true;

        return chance;
    }

}
