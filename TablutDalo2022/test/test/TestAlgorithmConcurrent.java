package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablut;
import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablutWithoutFuture;
import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class TestAlgorithmConcurrent {
	
	Turn currentTurn = Turn.WHITE;
	IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategy1, searchStrategy2;
	
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
		
		System.out.println("Evaluating state");
		System.out.println(state.boardStringWithCellIndex());
		System.out.println();
		
		copare_NoConcurrentNoGraph_ConcurrentNoGraph(state);
		copare_NoConcurrentNoGraph_NoConcurrentGraph(state);
		copare_NoConcurrentNoGraph_ConcurrentGraph(state);
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
		
		System.out.println("Evaluating state");
		System.out.println(state.boardStringWithCellIndex());
		System.out.println();
		
		copare_NoConcurrentNoGraph_ConcurrentNoGraph(state);
		copare_NoConcurrentNoGraph_NoConcurrentGraph(state);
		copare_NoConcurrentNoGraph_ConcurrentGraph(state);
	}
	
	
	
	
	
	private void copare_NoConcurrentNoGraph_ConcurrentNoGraph(State state) {
		searchStrategy1 = constructSrategy(false,false); //basic (noConcurrentNoGraph)
		searchStrategy2 = constructSrategy(true,false); //concurrent no graph
		assertTrue(testIfStrategiesFindSameUtility(state, searchStrategy1, searchStrategy2, 3));
	}
	
	private void copare_NoConcurrentNoGraph_NoConcurrentGraph(State state) {
		searchStrategy1 = constructSrategy(false,false); //basic (noConcurrentNoGraph)
		searchStrategy2 = constructSrategy(false,true); //(no concurrent) with graph
		assertTrue(testIfStrategiesFindSameUtility(state, searchStrategy1, searchStrategy2, 3));
	}
	
	private void copare_NoConcurrentNoGraph_ConcurrentGraph(State state) {
		searchStrategy1 = constructSrategy(false,false); //basic (noConcurrentNoGraph)
		searchStrategy2 = constructSrategy(true,true); //concurrent with graph
		assertTrue(testIfStrategiesFindSameUtility(state, searchStrategy1, searchStrategy2, 3));
	}
	
	
	/**
	 * Test whether or not the two expansion strategies find a new state with the same utility. 
	 * If not the method returns false, meaning that one of the 2 strategies is bloken, since it failed to find (one of) the best move
	 * @param intialState the initial state the expansion starts from
	 * @param searchStrategy1 algorithm 1
	 * @param searchStrategy2 algorithm 2
	 * @param depth expand until this depth (taking all the necessary time to do that.. see apiNotes)
	 * @apiNote make sure that both strategie take their time to expand all the states up to a prefixed depth, 
	 * otherwise it's (correctly) possible that they find an action with a different utility
	 */
	private boolean testIfStrategiesFindSameUtility(
		State intialState,
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategy1,
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategy2,
		int depth
	) {
		searchStrategy1.setMaxTime(10000);
		searchStrategy2.setMaxTime(10000);
		searchStrategy1.maxDepth = depth;
		searchStrategy2.maxDepth = depth;
		searchStrategy1.makeDecision(intialState);
		searchStrategy2.makeDecision(intialState);
		double Utility1 = searchStrategy1.getLastResultsUtilities().get(0);
		double Utility2 = searchStrategy2.getLastResultsUtilities().get(0);
		if(Utility1!=Utility2) {
			System.err.println("Utility1 = " + Utility1 + "; " + "Utility2 = " + Utility2);
			return false;
		}else {
			System.out.println("Both strategies gave value " + Utility1);
		}
		return true;
	}
	
	
	private IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> constructSrategy(boolean concurrent, boolean graphOprimization) {
		Game<State, Action, String> rules = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", Turn.WHITE);
		IterativeDeepeningAlphaBetaSearchTablut<State, Action, String> searchStrategy;
		if(concurrent) {
			searchStrategy = new IterativeDeepeningAlphaBetaSearchTablut<State, Action, String>(rules, 0.0, GameDaloTablut.getMaxValueHeuristic(), 60){
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
		}else {
			searchStrategy = new IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<State, Action, String>(rules, 0.0, GameDaloTablut.getMaxValueHeuristic(), 60){
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
		}
		searchStrategy.printStatistics = true;
		searchStrategy.logEnabled = false;
		searchStrategy.graphOptimization = graphOprimization;
		return searchStrategy;
	}

}
