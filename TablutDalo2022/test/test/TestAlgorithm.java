package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablut;
import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch.ActionStore;
import aima.core.search.framework.Metrics;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class TestAlgorithm {

	@Test
	void testStateExpansion() {
		State state = new StateTablut();
		state.setTurn(Turn.BLACK);
		
		Pawn [][] board = new Pawn[9][9];

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				board[i][j] = Pawn.EMPTY;
			}
		}

		board[4][4] = Pawn.THRONE;
		board[4][4] = Pawn.KING;

		board[2][4] = Pawn.WHITE;
		board[3][4] = Pawn.WHITE;
		board[5][4] = Pawn.WHITE;
		board[6][4] = Pawn.WHITE;
		board[4][2] = Pawn.WHITE;
		board[4][3] = Pawn.WHITE;
		board[4][5] = Pawn.WHITE;
		board[4][6] = Pawn.WHITE;

		board[0][3] = Pawn.BLACK;
		board[0][4] = Pawn.BLACK;
		board[0][5] = Pawn.BLACK;
		board[1][4] = Pawn.BLACK;
		board[8][3] = Pawn.BLACK;
		board[8][4] = Pawn.BLACK;
		board[8][5] = Pawn.BLACK;
		board[7][4] = Pawn.BLACK;
		board[3][0] = Pawn.BLACK;
		board[4][0] = Pawn.BLACK;
		board[5][0] = Pawn.BLACK;
		board[4][1] = Pawn.BLACK;
		board[3][8] = Pawn.BLACK;
		board[4][8] = Pawn.BLACK;
		board[5][8] = Pawn.BLACK;
		board[4][7] = Pawn.BLACK;
		
		state.setBoard(board);
		
		Game<State, Action, String> rules = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black");
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategy = new IterativeDeepeningAlphaBetaSearchTablut<>(rules, 0.0, GameDaloTablut.getMaxValueHeuristic(), 60) {
			
			private boolean logEnabled = true;
			
			private static class ActionStore<ACTION> {
				private List<ACTION> actions = new ArrayList<ACTION>();
				private List<Double> utilValues = new ArrayList<Double>();

				void add(ACTION action, double utilValue) {
					int idx;
					for (idx = 0; idx < actions.size() && utilValue <= utilValues.get(idx); idx++)
						;
					actions.add(idx, action);
					utilValues.add(idx, utilValue);
				}

				int size() {
					return actions.size();
				}
			}
			
			@Override
			public Action makeDecision(State state) {
				StringBuffer logText = null;
				String player = game.getPlayer(state);
				List<Action> results = orderActions(state, game.getActions(state), player, 0);
				currDepthLimit = 0;
				do {
					incrementDepthLimit();
					if (logEnabled)
						logText = new StringBuffer("depth " + currDepthLimit + ": ");
					heuristicEvaluationUsed = false;
					ActionStore<Action> newResults = new ActionStore<Action>();
					for (Action action : results) {
						double value = minValue(game.getResult(state, action), player, Double.NEGATIVE_INFINITY,
								Double.POSITIVE_INFINITY, 1);
						newResults.add(action, value);
						if (logEnabled)
							logText.append(action + "->" + value + " ");
					}
					if (logEnabled)
						System.out.println(logText);
					if (newResults.size() > 0) {
						results = newResults.actions;
						if (!timer.timeOutOccured()) {
							if (hasSafeWinner(newResults.utilValues.get(0)))
								break; // exit from iterative deepening loop
							else if (newResults.size() > 1
									&& isSignificantlyBetter(newResults.utilValues.get(0), newResults.utilValues.get(1)))
								break; // exit from iterative deepening loop
						}
					}
				} while (!timer.timeOutOccured() && heuristicEvaluationUsed);
				return results.get(0);
			}
		};
	
	}

}
