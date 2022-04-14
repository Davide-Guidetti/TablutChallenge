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
	void testKingEscape() {
		//king has a clear path for white victory
//		  ABCDEFGHI
//		 1OOOBBBOOO
//		 2OOOOBOOOO
//		 3OOOOWOOOO
//		 4BOOOWOOOB
//		 5BBKWTWWBB
//		 6BOWOWOOOB
//		 7OOOOWOOOO
//		 8OOOOBOOOO
//		 9OOOBBBOOO
		
		Turn currentTurn = Turn.WHITE;
		
		State state = new StateTablut();
		state.setTurn(currentTurn);
		
		Pawn [][] board = state.getBoard();
		// ---------- START PLACING PAWNS ----------
		//put the king in a position in which he could escape
		board[4][4] = Pawn.THRONE; //replace king
		board[4][2] = Pawn.KING;
		board[5][2] = Pawn.WHITE;// put a soldier so that the king have only one way to escape
		// ----------- PLACEMENT DONE --------------
		state.setBoard(board);
		
		constructObjects();
		searchStrategy.maxDepth = 1;
		
		System.out.println("finding action for state");
		System.out.println(state.boardStringWithCellIndex());
		System.out.println();
		
		//EXPECTED BEHAVIOUR: king escapes going from 4,2 to 0,2
		Action chosenAction = searchStrategy.makeDecision(state);
		try {
			assertEquals(new Action(4,2,0,2,currentTurn), chosenAction);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	void testEatKing() {
		//blacks have a save victory
//		  ABCDEFGHI
//		 1OOOBBBOOO
//		 2OOOOBOOOO
//		 3OOOOWOOOO
//		 4BOOOWOOOB
//		 5BBWWTKBOB
//		 6BOOOWBOOB
//		 7OOOOWOOOO
//		 8OOOOBOOOO
//		 9OOOBBOOOO
		
		Turn currentTurn = Turn.BLACK;
		
		State state = new StateTablut();
		state.setTurn(currentTurn);
		
		Pawn [][] board = state.getBoard();
		// ---------- START PLACING PAWNS ----------
		//put the king in a position in which he could escape
		board[4][4] = Pawn.THRONE; //replace king
		board[4][5] = Pawn.KING;  //king adjacent to castle
		//get 2 blask out of camps, and place them around the king
		board[8][5] = Pawn.EMPTY;
		board[5][5] = Pawn.BLACK;
		board[4][7] = Pawn.EMPTY;
		board[4][6] = Pawn.BLACK;
		// ----------- PLACEMENT DONE --------------
		state.setBoard(board);
		
		constructObjects();
		searchStrategy.maxDepth = 1;
		
		System.out.println("finding action for state");
		System.out.println(state.boardStringWithCellIndex());
		System.out.println();
		
		Action chosenAction = searchStrategy.makeDecision(state);
		
		//EXPECTED BEHAVIOUR: black from 0,5 eats king gointo to 3,5
		try {
			assertEquals(new Action(0,5,3,5,currentTurn), chosenAction);
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
