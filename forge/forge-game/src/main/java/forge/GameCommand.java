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
package forge;

/**
 * <p>
 * Command interface.
 * </p>
 * 
 * @author Forge
 * @version $Id: GameCommand.java 24794 2014-02-10 08:11:32Z Max mtg $
 */
public interface GameCommand extends java.io.Serializable, Runnable {
    /** Constant <code>Blank</code>. */
    public final GameCommand BLANK = new GameCommand() {

        private static final long serialVersionUID = 2689172297036001710L;

        @Override
        public void run() {
        }

    };
}
