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

class TestMatchDifferentTimeout {

	public static String match(GameDaloTablut whiteGame, GameDaloTablut blackGame,
			IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchWhite,
			IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchBlack) {
		List<Action> actions = new ArrayList<>();
		State currentState = new StateTablut();
		Action chosenMove = null;
		while (true) {
			// WHITE TURN
			chosenMove = searchWhite.makeDecision(currentState);
			/*
			if (searchWhite.statistics.skippedSameNodes > 0)
				System.out.println("skippedSameNodes! \n" + searchWhite.statistics);
			if (searchWhite.timedOut)
				System.out.println("Timed out search");
			if (searchWhite.outOfMemoryOccurred)
				System.out.println("Insufficient memory");
			*/
			actions.add(chosenMove);
			currentState = whiteGame.getResult(currentState, chosenMove);
			if (whiteGame.isTerminal(currentState)) {
				//System.out.println("CURRENTSTATE " + currentState.getTurn().toString());
				if (currentState.getTurn().equals(Turn.WHITEWIN)) {
					return "WHITEWIN";
				}
				if (currentState.getTurn().equals(Turn.BLACKWIN)) {
					return "BLACKWIN";
				}
				if (currentState.getTurn().equals(Turn.DRAW)) {
					return "DRAW";
				}
				break;
			}

			// BLACK TURN
			chosenMove = searchBlack.makeDecision(currentState);
			/*
			if (searchBlack.statistics.skippedSameNodes > 0)
				System.out.println("skippedSameNodes! \n" + searchWhite.statistics);
			if (searchBlack.timedOut)
				System.out.println("Timed out search");
			if (searchBlack.outOfMemoryOccurred)
				System.out.println("Insufficient memory");
			*/
			actions.add(chosenMove);
			currentState = blackGame.getResult(currentState, chosenMove);
			if (blackGame.isTerminal(currentState)) {
				//System.out.println("CURRENTSTATE " + currentState.getTurn().toString());
				if (currentState.getTurn().equals(Turn.WHITEWIN)) {
					return "WHITEWIN";
				}
				if (currentState.getTurn().equals(Turn.BLACKWIN)) {
					return "BLACKWIN";
				}
				if (currentState.getTurn().equals(Turn.DRAW)) {
					return "DRAW";
				}
				break;
			}

		}
		return "ERRORE";
	}

	@Test
	void testMatch() {
		int timeout = 1;
		int maxDepth = 10;
		int winWhite = 0;
		int winBlack = 0;
		int draw = 0;
		int error = 0;
		GameDaloTablut rulesBlack;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyBlack;
		GameDaloTablut rulesWhite;
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategyWhite;
		rulesBlack = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		rulesWhite = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);

		for (timeout = 2; timeout <= 62; timeout+=10) {
			System.out.println("MATCH MAXDEPTH 10, TIMEOUT "+timeout);
			// BLACK PLAYER
			searchStrategyBlack = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesBlack, 0.0,
					GameDaloTablut.getMaxValueHeuristic(), timeout - 1);
			searchStrategyBlack.logEnabled = false;
			searchStrategyBlack.printStatistics = false;
			searchStrategyBlack.graphOptimization = false;
			searchStrategyBlack.maxDepth = maxDepth;
			// WHITE PLAYER
			searchStrategyWhite = new IterativeDeepeningAlphaBetaSearchTablut<>(rulesWhite, 0.0,
					GameDaloTablut.getMaxValueHeuristic(), timeout - 1);
			searchStrategyWhite.logEnabled = false;
			searchStrategyWhite.printStatistics = false;
			searchStrategyWhite.graphOptimization = false;
			searchStrategyWhite.maxDepth = maxDepth;

			String result = match(rulesWhite, rulesBlack, searchStrategyWhite, searchStrategyBlack);
			System.out.println("RESULT: " + result);
			if (result.equals("WHITEWIN")) {
				winWhite++;
			}
			if (result.equals("BLACKWIN")) {
				winBlack++;
			}
			if (result.equals("DRAW")) {
				draw++;
			}
			if (result.equals("ERRORE")) {
				error++;
			}
			System.out.println("WW: " + winWhite + " BW: " + winBlack + " D: " + draw);
			assertEquals(error, 0);
		}

	}
}
