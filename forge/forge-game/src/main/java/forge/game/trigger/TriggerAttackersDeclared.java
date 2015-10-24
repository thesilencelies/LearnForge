/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.game.trigger;

import forge.game.GameEntity;
import forge.game.card.Card;
import forge.game.spellability.SpellAbility;

import java.util.List;
import java.util.Map;

/**
 * TODO Write javadoc for this type.
 * 
 */
public class TriggerAttackersDeclared extends Trigger {

    /**
     * Instantiates a new trigger_ attackers declared.
     * 
     * @param params
     *            the params
     * @param host
     *            the host
     * @param intrinsic
     *            the intrinsic
     */
    public TriggerAttackersDeclared(final java.util.Map<String, String> params, final Card host, final boolean intrinsic) {
        super(params, host, intrinsic);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean performTest(final Map<String, Object> runParams2) {
        if (this.mapParams.containsKey("AttackingPlayer")) {
            if (!matchesValid(runParams2.get("AttackingPlayer"),
                    this.mapParams.get("AttackingPlayer").split(","), this.getHostCard())) {
                return false;
            }
        }
        if (this.mapParams.containsKey("AttackedTarget")) {
            boolean valid = false;
            @SuppressWarnings("unchecked")
            List<GameEntity> list = (List<GameEntity>) runParams2.get("AttackedTarget");
            for (GameEntity b : list) {
                if (matchesValid(b, this.mapParams.get("AttackedTarget").split(","), this.getHostCard())) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTriggeringObjects(final SpellAbility sa) {
        sa.setTriggeringObject("Attackers", this.getRunParams().get("Attackers"));
        sa.setTriggeringObject("AttackingPlayer", this.getRunParams().get("AttackingPlayer"));
    }
}
