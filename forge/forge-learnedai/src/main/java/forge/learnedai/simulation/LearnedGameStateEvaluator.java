package forge.learnedai.simulation;

import forge.learnedai.AiAttackController;
import forge.learnedai.CreatureEvaluator;
import forge.learnedai.NNinput.NNcardState;
import forge.learnedai.NNinput.NNevalNet;
import forge.learnedai.QLearnNet.QGameState;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CounterType;
import forge.game.combat.Combat;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class LearnedGameStateEvaluator {
    private boolean debugging;
    
    public LearnedGameStateEvaluator(){
    	debugging = false;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    private static void debugPrint(String s) {
        //System.err.println(s);
        LearnedGameSimulator.debugPrint(s);
    }

    private Combat simulateUpcomingCombatThisTurn(Game game) {
        PhaseHandler handler = game.getPhaseHandler();
        if (handler.getPhase().isAfter(PhaseType.COMBAT_DAMAGE)) {
            return null;
        }
        AiAttackController aiAtk = new AiAttackController(handler.getPlayerTurn());
        Combat combat = new Combat(handler.getPlayerTurn());
        aiAtk.declareAttackers(combat);
        return combat;
    }

    public Score getScoreForGameState(Game game, Player aiPlayer) {
    	//obiously win if you can 
        if (game.isGameOver()) {
            return game.getOutcome().getWinningPlayer() == aiPlayer ? new Score(Integer.MAX_VALUE) : new Score(Integer.MIN_VALUE);
        }

        //this looks like it should be useful, but let's see if we can run without
    //    Combat combat = simulateUpcomingCombatThisTurn(game);
        
        NNcardState state = QGameState.ProduceGamestate(aiPlayer);
        double score = NNevalNet.rankchoice(state);
        return new Score(score);
    }

  

    public static class Score {
        public final double value;
        public final double summonSickValue;

        public Score(double score) {
            this.value = score;
            summonSickValue = 0;
        }

        public boolean equals(Score other) {
            if (other == null)
                return false;
            return value == other.value;
        }
    }
}
