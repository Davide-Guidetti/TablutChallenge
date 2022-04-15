package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablut;
import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablutWithoutFuture;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

class TestGameEvolution {

	@Test
	void test_no_cuncurrent() {
		int timeout = 10;
		int maxDepth = 6;
		
		GameDaloTablut rulesBlack;
		IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<State, Action, String> searchStrategyBlack;
		GameDaloTablut rulesWhite;
		IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<State, Action, String> searchStrategyWhite;
		
		// ------------- RECORD EVOLUTION WITHOUT USING OPTIMIZATION --------------
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyBlack.logEnabled=true;
		searchStrategyBlack.printStatistics=true;
		searchStrategyBlack.graphOptimization=false;
		searchStrategyBlack.maxDepth = maxDepth;
		
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<>(rulesWhite, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyWhite.printStatistics=false;
		searchStrategyWhite.graphOptimization=false;
		searchStrategyWhite.maxDepth = maxDepth;
		
		//List<Action> withoutOptimization = null;
		long withoutOptimizationStartTime = System.currentTimeMillis();
		List<Action> withoutOptimization = gameEvolution(rulesWhite, rulesBlack, searchStrategyWhite, searchStrategyBlack);
		long withoutOptimizationEndTime = System.currentTimeMillis();
		
		// ----------------- RECORD EVOLUTION USING OPTIMIZATION ------------------
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyBlack.printStatistics=true;
		searchStrategyBlack.graphOptimization=true;
		searchStrategyBlack.maxDepth = maxDepth;
		
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<>(rulesWhite, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyWhite.printStatistics=true;
		searchStrategyWhite.graphOptimization=true;
		searchStrategyWhite.maxDepth = maxDepth;
		
		long withOptimizationStartTime = System.currentTimeMillis();
		List<Action> withOptimization = gameEvolution(rulesWhite, rulesBlack, searchStrategyWhite, searchStrategyBlack);
		long withOptimizationEndTime = System.currentTimeMillis();
		
		System.out.println("Without optimization: (" + (withoutOptimizationEndTime-withoutOptimizationStartTime)/1000 + "s)");
		System.out.println(withoutOptimization);
		System.out.println();
		System.out.println("With optimization: (" + (withOptimizationEndTime-withOptimizationStartTime)/1000 + "s)");
		System.out.println(withOptimization);
		System.out.println();
		assertEquals(withOptimization, withoutOptimization);
	}
	
	
	@Test
	void test_cuncurrent() {
		int timeout = 10;
		int maxDepth = 6;
		
		GameDaloTablut rulesBlack;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyBlack;
		GameDaloTablut rulesWhite;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyWhite;
		
		// ------------- RECORD EVOLUTION NO CONCURRENCY WITH OPTIMIZATION --------------
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyBlack.logEnabled=true;
		searchStrategyBlack.printStatistics=true;
		searchStrategyBlack.graphOptimization=true;
		searchStrategyBlack.maxDepth = maxDepth;
		
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<>(rulesWhite, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyWhite.printStatistics=false;
		searchStrategyWhite.graphOptimization=true;
		searchStrategyWhite.maxDepth = maxDepth;
		
		long withoutOptimizationStartTime = System.currentTimeMillis();
		List<Action> withoutOptimization = gameEvolution(rulesWhite, rulesBlack, searchStrategyWhite, searchStrategyBlack);
		long withoutOptimizationEndTime = System.currentTimeMillis();
		
		// ----------------- RECORD EVOLUTION WITH CONCURRENCY USING OPTIMIZATION ------------------
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyBlack.printStatistics=true;
		searchStrategyBlack.graphOptimization=true;
		searchStrategyBlack.maxDepth = maxDepth;
		
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesWhite, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyWhite.printStatistics=false;
		searchStrategyWhite.graphOptimization=true;
		searchStrategyWhite.maxDepth = maxDepth;
		
		long withOptimizationStartTime = System.currentTimeMillis();
		List<Action> withOptimization = gameEvolution(rulesWhite, rulesBlack, searchStrategyWhite, searchStrategyBlack);
		long withOptimizationEndTime = System.currentTimeMillis();
		
		System.out.println("Without optimization: (" + (withoutOptimizationEndTime-withoutOptimizationStartTime)/1000 + "s)");
		System.out.println(withoutOptimization);
		System.out.println();
		System.out.println("With optimization: (" + (withOptimizationEndTime-withOptimizationStartTime)/1000 + "s)");
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
			if(searchWhite.outOfMemoryOccurred) System.out.println("Insufficient memory");
			actions.add(chosenMove);
			currentState = whiteGame.getResult(currentState, chosenMove);
			if(whiteGame.isTerminal(currentState)) break;
			//BLACK TURN
			chosenMove = searchBlack.makeDecision(currentState);
			if(searchBlack.statistics.skippedSameNodes>0) System.out.println("skippedSameNodes! \n"+searchWhite.statistics);
			if(searchBlack.timedOut) System.out.println("Timed out search");
			if(searchBlack.outOfMemoryOccurred) System.out.println("Insufficient memory");
			actions.add(chosenMove);
			currentState = blackGame.getResult(currentState, chosenMove);
			if(blackGame.isTerminal(currentState)) break;
		}
		return actions;
	}
	
	@Test
	void canEqualStatesActuallyHappen() throws IOException {
		int timeout = 10000;
		int maxDepth = 4;
		
		GameDaloTablut rulesBlack;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyBlack;
		GameDaloTablut rulesWhite;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyWhite;
		
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesBlack, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyBlack.printStatistics=false;
		searchStrategyBlack.graphOptimization=false;
		searchStrategyBlack.maxDepth = maxDepth;
		
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesWhite, 0.0, GameDaloTablut.getMaxValueHeuristic(), timeout  - 1);
		searchStrategyWhite.printStatistics=false;
		searchStrategyWhite.graphOptimization=false;
		searchStrategyWhite.maxDepth = maxDepth;
		
		State currentState = new StateTablut();
		currentState.setTurn(Turn.WHITE);
		Action chosenMove;
		chosenMove = new Action("c5","c6",Turn.WHITE);
		currentState = rulesWhite.getResult(currentState, chosenMove);
		chosenMove = new Action("a4","b4",Turn.BLACK);
		currentState = rulesBlack.getResult(currentState, chosenMove);
		
		State stateA = currentState;
		
		chosenMove = new Action("c6","c7",Turn.WHITE);
		currentState = rulesWhite.getResult(currentState, chosenMove);
		chosenMove = new Action("b4","c4",Turn.BLACK);
		currentState = rulesBlack.getResult(currentState, chosenMove);
		chosenMove = new Action("c7","c6",Turn.WHITE);
		currentState = rulesWhite.getResult(currentState, chosenMove);
		chosenMove = new Action("c4","b4",Turn.BLACK);
		currentState = rulesBlack.getResult(currentState, chosenMove);
		
		assertEquals(stateA, currentState);
		assertEquals(stateA.hashCode(), currentState.hashCode());
	}
	
	@Test
	void testHashCodeState() {
		StateTablut a = new StateTablut();
		StateTablut b = new StateTablut();
		
		assertEquals(a, b);
		assertEquals(a.getBoard()[0][0].hashCode(), b.getBoard()[0][0].hashCode());
		//assertEquals(a.getBoard().hashCode(), b.getBoard().hashCode());
		assertEquals(a.hashCode(), b.hashCode());
	}

}
