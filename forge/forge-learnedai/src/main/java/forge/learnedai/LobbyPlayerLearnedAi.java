package forge.learnedai;

import java.util.Set;

import forge.AIOption;
import forge.LobbyPlayer;
import forge.game.Game;
import forge.game.player.IGameEntitiesFactory;
import forge.game.player.Player;
import forge.game.player.PlayerController;
import forge.learnedai.NNinput.NNevalNet;

public class LobbyPlayerLearnedAi extends LobbyPlayer implements IGameEntitiesFactory {

    private String aiProfile = "";
    private boolean rotateProfileEachGame;
    private boolean allowCheatShuffle;
    private boolean useSimulation;
    private NNevalNet nn;

    public LobbyPlayerLearnedAi(String name, Set<AIOption> options, NNevalNet _nn) {
        super(name);
        if (options != null && options.contains(AIOption.USE_SIMULATION)) {
            this.useSimulation = true;
        }
        nn = _nn;
    }
    public NNevalNet getNN(){
    	return nn;
    }

    public boolean isAllowCheatShuffle() {
        return allowCheatShuffle;
    }

    public void setAllowCheatShuffle(boolean allowCheatShuffle) {
        this.allowCheatShuffle = allowCheatShuffle;
    }

    public void setAiProfile(String profileName) {
        aiProfile = profileName;
    }

    public String getAiProfile() {
        return aiProfile;
    }

    public void setRotateProfileEachGame(boolean rotateProfileEachGame) {
        this.rotateProfileEachGame = rotateProfileEachGame;
    }

    private LearnedPlayerControllerAi createControllerFor(Player ai) {
        LearnedPlayerControllerAi result = new LearnedPlayerControllerAi(ai.getGame(), ai, this, nn);
        result.setUseSimulation(useSimulation);
        result.allowCheatShuffle(allowCheatShuffle);
        return result;
    }

    @Override
    public PlayerController createMindSlaveController(Player master, Player slave) {
        return createControllerFor(slave);
    }

    @Override
    public Player createIngamePlayer(Game game, final int id) {
        Player ai = new Player(getName(), game, id);
        ai.setFirstController(createControllerFor(ai));

        if (rotateProfileEachGame) {
            setAiProfile("Default");
            System.out.println(String.format("AI profile %s was chosen for the lobby player %s.", getAiProfile(), getName()));
        }
        return ai;
    }

    @Override
    public void hear(LobbyPlayer player, String message) { /* Local AI is deaf. */ }
}
