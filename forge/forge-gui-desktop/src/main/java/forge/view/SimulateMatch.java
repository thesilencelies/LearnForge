package forge.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

import forge.deck.Deck;
import forge.deck.io.DeckSerializer;
import forge.game.Game;
import forge.game.GameLogEntry;
import forge.game.GameRules;
import forge.game.GameType;
import forge.game.Match;
import forge.game.player.RegisteredPlayer;
import forge.learnedai.NNinput.NNevalNet;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.util.Lang;

public class SimulateMatch {
    public static void simulate(String[] args) {
        FModel.initialize(null);

        System.out.println("Simulation mode");
        if(args.length < 3 ) {
            System.out.println("Syntax: forge.exe sim <deck1[.dck]> <deck2[.dck]> [N]");
            System.out.println("\tsim - stands for simulation mode");
            System.out.println("\tdeck1 (or deck2) - constructed deck name or filename (has to be quoted when contains multiple words)");
            System.out.println("\tdeck is treated as file if it ends with a dot followed by three numbers or letters");
            System.out.println("\tN - number of games, defaults to 1");
            return;
        }
        Deck d1 = deckFromCommandLineParameter(args[1]);
        Deck d2 = deckFromCommandLineParameter(args[2]);
        if(d1 == null || d2 == null) {
            System.out.println("One of decks could not be loaded, match cannot start");
            return;
        }
        
        int nGames = args.length >= 4 ? Integer.parseInt(args[3]) : 1;
        
        System.out.println(String.format("Ai-%s vs Ai_%s - %s", d1.getName(), d2.getName(), Lang.nounWithNumeral(nGames, "game")));
        
        List<RegisteredPlayer> pp = new ArrayList<RegisteredPlayer>();
        pp.add(new RegisteredPlayer(d1).setPlayer(GamePlayerUtil.createAiPlayer("Ai-" + d1.getName(), 0)));
        pp.add(new RegisteredPlayer(d2).setPlayer(GamePlayerUtil.createAiPlayer("Ai_" + d2.getName(), 1)));
        GameRules rules = new GameRules(GameType.Constructed);
        Match mc = new Match(rules, pp, "Test");
        int p1wins = 0;
        for (int iGame = 0; iGame < nGames; iGame++) {
            p1wins += simulateSingleMatch(mc, iGame);
            System.out.println(String.format("current score is %s : %d , %s : %d \n", d1.getName() ,p1wins,d2.getName(), (iGame +1 - p1wins) ));
            if(iGame%10 == 9){
            	mc = new Match(rules, pp, "Test");
            }
        }
        System.out.flush();
    }
    
    public static void simulatelearn(String[] args) {
        FModel.initialize(null);

        System.out.println("Training Ai on sims");
        if(args.length < 4 ) {
            System.out.println("Syntax: forge.exe simai <learnedainame[.rai]> <decklearned[.dck]> <decktest[.dck]> [N]");
            System.out.println("\tsimai - stands for simulation ai mode");
            System.out.println("\tlearnedainame - name of the ai to be trained");	//this is currently not needed at all (may be later tho)
            System.out.println("\tdecklearned (or decktest) - constructed deck name or filename (has to be quoted when contains multiple words)");
            System.out.println("\tdeck is treated as file if it ends with a dot followed by three numbers or letters");
            System.out.println("\tN - number of games, defaults to 1");
            return;
        }
        
        int nGames = args.length >= 5 ? Integer.parseInt(args[4]) : 1;

        //load the learned ai state
        
        List<RegisteredPlayer> pp = new ArrayList<RegisteredPlayer>();
        pp.add(new RegisteredPlayer(deckFromCommandLineParameter(args[2])).setPlayer(GamePlayerUtil.createLearnedAiPlayer("learned AI", 0)));
        pp.add(new RegisteredPlayer(deckFromCommandLineParameter(args[3])).setPlayer(GamePlayerUtil.createAiPlayer("Hardcoded AI", 1)));
        GameRules rules = new GameRules(GameType.Constructed);
        Match mc = new Match(rules, pp, "Test");
        int p1wins = 0;
        for (int iGame = 0; iGame < nGames; iGame++) {
            p1wins += simulateSingleMatch(mc, iGame);
            System.out.println(String.format("current score is LearnedAI : %d , RulesAI : %d \n",p1wins, (iGame +1 - p1wins) ));
            if(iGame%5 == 4){
            		mc = new Match(rules, pp, "Test");
            }

            
        }
        
        System.out.flush();
    }
    
    /**
     * TODO: Write javadoc for this method.
     * @param sw
     * @param pp
     */
    private static int simulateSingleMatch(Match mc, int iGame) {
        StopWatch sw = new StopWatch();
        sw.start();

        Game g1 = mc.createGame();
        // will run match in the same thread
        mc.startGame(g1);
        sw.stop();
        
       // List<GameLogEntry> log = g1.getGameLog().getLogEntries(null);
       // Collections.reverse(log);
        
       // for(GameLogEntry l : log)
         //   System.out.println(l);

        System.out.println(String.format("\nGame %d ended in %d ms. %s has won!\n", 1+iGame, sw.getTime(), g1.getOutcome().getWinningLobbyPlayer().getName()));
        
        if(g1.getOutcome().getWinningPlayer().getName() == mc.getPlayers().get(0).getPlayer().getName()){
        	return 1;
        }
        else return 0;
        //g1 = null;
        //this is poor form, but possibly necesarry
       // System.gc();
    }


    private static Deck deckFromCommandLineParameter(String deckname) {
        int dotpos = deckname.lastIndexOf('.');
        if(dotpos > 0 && dotpos == deckname.length()-4)
            return DeckSerializer.fromFile(new File(deckname));
        return FModel.getDecks().getConstructed().get(deckname);
    }
}
