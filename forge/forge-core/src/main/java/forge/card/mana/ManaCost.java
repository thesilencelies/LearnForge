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
package forge.card.mana;

import forge.card.MagicColor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * CardManaCost class.
 * </p>
 * 
 * @author Forge
 * @version $Id: CardManaCost.java 9708 2011-08-09 19:34:12Z jendave $
 */

public final class ManaCost implements Comparable<ManaCost>, Iterable<ManaCostShard>, Serializable {
    private static final long serialVersionUID = -2477430496624149226L;

    private static final char DELIM = (char)6;

    private List<ManaCostShard> shards;
    private final int genericCost;
    private final boolean hasNoCost; // lands cost
    private String stringValue; // precalculated for toString;

    private Float compareWeight = null;

    /** The Constant empty. */
    public static final ManaCost NO_COST = new ManaCost(-1);
    public static final ManaCost ZERO = new ManaCost(0);
    public static final ManaCost ONE = new ManaCost(1);
    public static final ManaCost TWO = new ManaCost(2);
    public static final ManaCost THREE = new ManaCost(3);
    public static final ManaCost FOUR = new ManaCost(4);

    public static ManaCost get(int cntColorless) {
        switch (cntColorless) {
            case 0: return ZERO;
            case 1: return ONE;
            case 2: return TWO;
            case 3: return THREE;
            case 4: return FOUR;
        }
        return cntColorless > 0 ? new ManaCost(cntColorless) : NO_COST;
    }

    // pass mana cost parser here
    private ManaCost(int cmc) {
        this.hasNoCost = cmc < 0;
        this.genericCost = cmc < 0 ? 0 : cmc;
        sealClass(new ArrayList<ManaCostShard>());
    }

    private void sealClass(List<ManaCostShard> shards0) {
        this.shards = Collections.unmodifiableList(shards0);
        this.stringValue = this.getSimpleString();
    }

    // public ctor, should give it a mana parser
    /**
     * Instantiates a new card mana cost.
     * 
     * @param parser
     *            the parser
     */
    public ManaCost(final IParserManaCost parser) {
        final List<ManaCostShard> shardsTemp = new ArrayList<ManaCostShard>();
        this.hasNoCost = false;
        while (parser.hasNext()) {
            final ManaCostShard shard = parser.next();
            if (shard != null && shard != ManaCostShard.COLORLESS) {
                shardsTemp.add(shard);
            } // null is OK - that was generic mana
        }
        this.genericCost = parser.getTotalColorlessCost(); // collect generic mana
        // here
        sealClass(shardsTemp);
    }

    private String getSimpleString() {
        if (this.hasNoCost) {
            return "no cost";
        }
        if (this.shards.isEmpty()) {
            return "{" + this.genericCost + "}";
        }

        final StringBuilder sb = new StringBuilder();
        if (this.genericCost > 0) {
            sb.append("{" + this.genericCost + "}");
        }
        for (final ManaCostShard s : this.shards) {
            if (s == ManaCostShard.X) {
                sb.insert(0, s.toString());
            } else {
                sb.append(s.toString());
            }
        }
        return sb.toString();
    }

    /**
     * Gets the cMC.
     * 
     * @return the cMC
     */
    public int getCMC() {
        int sum = 0;
        for (final ManaCostShard s : this.shards) {
            sum += s.getCmc();
        }
        return sum + this.genericCost;
    }

    /**
     * Gets the color profile.
     * 
     * @return the color profile
     */
    public byte getColorProfile() {
        byte result = 0;
        for (final ManaCostShard s : this.shards) {
            result |= s.getColorMask();
        }
        return result;
    }

    public int getShardCount(ManaCostShard which) {
        if (which == ManaCostShard.COLORLESS) {
            return genericCost;
        }

        int res = 0;
        for (ManaCostShard shard : shards) {
            if (shard == which) {
                res++;
            }
        }
        return res;
    }

    /**
     * Gets the amount of color shards in the card's mana cost.
     * 
     * @return an array of five integers containing the amount of color shards in the card's mana cost in WUBRG order 
     */
    public int[] getColorShardCounts() {
        int[] counts = new int[5]; // in WUBRG order

        for (int i = 0; i < stringValue.length(); i++) {
            char symbol = stringValue.charAt(i);
            switch (symbol) {
                case 'W': 
                case 'U':
                case 'B':
                case 'R':
                case 'G':
                    counts[MagicColor.getIndexOfFirstColor(MagicColor.fromName(symbol))]++;
                    break;
            }
            
        }

        return counts;
    }

    /**
     * Gets the generic cost.
     * 
     * @return the generic cost
     */
    public int getGenericCost() {
        return this.genericCost;
    }

    /**
     * Checks if is empty.
     * 
     * @return true, if is empty
     */
    public boolean isNoCost() {
        return this.hasNoCost;
    }

    /**
     * Checks if is pure generic.
     * 
     * @return true, if is pure generic
     */
    public boolean isPureGeneric() {
        return this.shards.isEmpty() && !this.isNoCost();
    }

    public boolean isZero() {
        return genericCost == 0 && isPureGeneric();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ManaCost o) {
        return this.getCompareWeight().compareTo(o.getCompareWeight());
    }

    private Float getCompareWeight() {
        if (this.compareWeight == null) {
            float weight = this.genericCost;
            for (final ManaCostShard s : this.shards) {
                weight += s.getCmpc();
            }
            if (this.hasNoCost) {
                weight = -1; // for those who doesn't even have a 0 sign on card
            }
            this.compareWeight = Float.valueOf(weight);
        }
        return this.compareWeight;
    }

    public static String serialize(ManaCost mc) {
        StringBuilder builder = new StringBuilder();
        builder.append(mc.hasNoCost ? -1 : mc.genericCost);
        for (ManaCostShard shard : mc.shards) {
            builder.append(DELIM + shard.name());
        }
        return builder.toString();
    }
    public static ManaCost deserialize(String value) {
        String[] pieces = StringUtils.split(value, DELIM);
        ManaCost mc = new ManaCost(Integer.parseInt(pieces[0]));
        List<ManaCostShard> sh = new ArrayList<ManaCostShard>();
        for (int i = 1; i < pieces.length; i++) {
            sh.add(ManaCostShard.valueOf(pieces[i]));
        }
        mc.sealClass(sh);
        return mc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.stringValue;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public boolean hasPhyrexian() {
        for (ManaCostShard shard : shards) {
            if (shard.isPhyrexian()) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public int countX() {
        int iX = 0;
        for (ManaCostShard shard : shards) {
            if (shard == ManaCostShard.X) {
                iX++;
            }
        }
        return iX;
    }

    /**
     * Can this mana cost be paid with unlimited mana of given color set.
     * @param colorCode
     * @return
     */
    public boolean canBePaidWithAvaliable(byte colorCode) {
        for (ManaCostShard shard : shards) {
            if (!shard.isPhyrexian() && !shard.canBePaidWithManaOfColor(colorCode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param manaCost
     * @param manaCost2
     * @return
     */
    public static ManaCost combine(ManaCost a, ManaCost b) {
        ManaCost res = new ManaCost(a.genericCost + b.genericCost);
        List<ManaCostShard> sh = new ArrayList<ManaCostShard>();
        sh.addAll(a.shards);
        sh.addAll(b.shards);
        res.sealClass(sh);
        return res;
    }

    @Override
    public Iterator<ManaCostShard> iterator() {
        return this.shards.iterator();
    }
    
    public int getGlyphCount() { // counts all colored shards or 1 for {0} costs 
        int width = shards.size();
        if (genericCost > 0 || (genericCost == 0 && width == 0)) {
            width++;
        }
        return width;
    }
}
