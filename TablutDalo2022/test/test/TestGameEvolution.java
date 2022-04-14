package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablut;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

class TestGameEvolution {

	@Test
	void test() {
		int timeout = 10000;
		int maxDepth = 5;
		
		GameDaloTablut rulesBlack;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyBlack;
		GameDaloTablut rulesWhite;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyWhite;
		
		// ------------- RECORD EVOLUTION WITHOUT USING OPTIMIZATION --------------
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyBlack.printStatistics=false;
		searchStrategyBlack.graphOptimization=false;
		searchStrategyBlack.maxDepth = maxDepth;
		
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyWhite.printStatistics=false;
		searchStrategyWhite.graphOptimization=false;
		searchStrategyWhite.maxDepth = maxDepth;
		
		List<Action> withoutOptimization = gameEvolution(rulesWhite, rulesBlack, searchStrategyWhite, searchStrategyBlack);
		
		// ----------------- RECORD EVOLUTION USING OPTIMIZATION ------------------
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyBlack.printStatistics=false;
		searchStrategyBlack.graphOptimization=true;
		searchStrategyBlack.maxDepth = maxDepth;
		
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyWhite.printStatistics=false;
		searchStrategyWhite.graphOptimization=true;
		searchStrategyWhite.maxDepth = maxDepth;
		
		List<Action> withOptimization = gameEvolution(rulesWhite, rulesBlack, searchStrategyWhite, searchStrategyBlack);
		
		System.out.println("Without optimization: ");
		System.out.println(withoutOptimization);
		System.out.println();
		System.out.println("With optimization: ");
		System.out.println(withOptimization);
		System.out.println();
		assertEquals(withOptimization, withoutOptimization);
	}
	
	
	public static List<Action> gameEvolution(
			GameDaloTablut whiteGame, 
			GameDaloTablut blackGame, 
			IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchWhite, 
			IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchBlack
			){
		List<Action> actions = new ArrayList<>();
		State currentState = new StateTablut();
		Action chosenMove = null;
		while(true) {
			//WHITE TURN
			chosenMove = searchWhite.makeDecision(currentState);
			if(searchWhite.statistics.skippedSameNodes>0) System.out.println("skippedSameNodes! \n"+searchWhite.statistics);
			if(searchWhite.timedOut) System.out.println("Timed out search");
			actions.add(chosenMove);
			currentState = whiteGame.getResult(currentState, chosenMove);
			if(whiteGame.isTerminal(currentState)) break;
			//BLACK TURN
			chosenMove = searchBlack.makeDecision(currentState);
			if(searchBlack.statistics.skippedSameNodes>0) System.out.println("skippedSameNodes! \n"+searchWhite.statistics);
			if(searchBlack.timedOut) System.out.println("Timed out search");
			actions.add(chosenMove);
			currentState = blackGame.getResult(currentState, chosenMove);
			if(blackGame.isTerminal(currentState)) break;
		}
		return actions;
	}

}
