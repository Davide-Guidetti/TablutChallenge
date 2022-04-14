package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablut;
import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class TestAlgorithm {
	
	Turn currentTurn = Turn.WHITE;
	Game<State, Action, String> rules;
	IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategy;
	

	@Test
	void testStateExpansion() {
		State state = new StateTablut();
		
		Turn currentTurn = Turn.WHITE;
		state.setTurn(currentTurn);
		
		
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
		board[4][2] = Pawn.KING;
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
		
		
		//put the king in a position in which he could escape
		board[4][4] = Pawn.THRONE; //replace king
		board[4][2] = Pawn.KING;
		board[5][2] = Pawn.WHITE;// put a soldire so that the kink have only one way to escape
		
		state.setBoard(board);
		
		constructObjects();
		searchStrategy.maxDepth = 1;
		
		System.out.println("finding action for state");
		System.out.println(state.boardString());
		System.out.println();
		
		Action chosenAction = searchStrategy.makeDecision(state);
		
		try {
			assertEquals(new Action(4,2,0,2,currentTurn), chosenAction);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	private void constructObjects() {
		rules = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", Turn.WHITE);
		searchStrategy = new IterativeDeepeningAlphaBetaSearchTablut<State, Action, String>(rules, 0.0, GameDaloTablut.getMaxValueHeuristic(), 60) {
			
			@Override
			protected State logExpansion(State state, Action action, String player, StringBuffer logText) {
				logText.append("\n");
				logText.append("Evaluation for action: \"" + action+"\"\n");
				logText.append(state.boardStringWithCellIndex());
				double utility = game.getUtility(state, player);
				logText.append("State utility for " + player + ": " + utility +"\n");
				checkUtilityInRange(utility);
				return state;
			}

			private void checkUtilityInRange(double utility) {
				if(utility < 0 || utility > GameDaloTablut.getMaxValueHeuristic()) {
					fail("Utility out of range: utility=" + utility + " out of range [" + 0.0 + "," + GameDaloTablut.getMaxValueHeuristic() + "]");
				}				
			}
		};
		
		searchStrategy.printStatistics = true;
		searchStrategy.logEnabled = true;
	}

}
