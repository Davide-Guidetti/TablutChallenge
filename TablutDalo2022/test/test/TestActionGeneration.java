package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;

class TestActionGeneration {

	@Test
	void test() throws IOException {
		
		testActionGeneration(new StateTablut());
		
		State s;
		Pawn[][] board;
		
		//BLACKS ONLY
		s = new StateTablut();
		board = s.getBoard();
		for (int row = 0; row < board.length; row++) {
			for (int column = 0; column < board[row].length; column++) {
				Pawn pawn = board[row][column];
				if (pawn.equals(Pawn.WHITE)) {
					s.removePawn(row, column);
				}
			}
		}
		System.out.println(s.boardStringWithCellIndex());
		testActionGeneration(s);
		
		//WHITE ONLY
		s = new StateTablut();
		board = s.getBoard();
		for (int row = 0; row < board.length; row++) {
			for (int column = 0; column < board[row].length; column++) {
				Pawn pawn = board[row][column];
				if (pawn.equals(Pawn.BLACK)) {
					s.removePawn(row, column);
				}
			}
		}
		System.out.println(s.boardStringWithCellIndex());
		testActionGeneration(new StateTablut());
	}
	
	/*
	 * test if action generation or all pawns in a state coincides with the checks made by the professors 
	 */
	static void testActionGeneration(State state) throws IOException {
		Pawn[][] board = state.getBoard();
		List<Action> allLegalActions = new ArrayList<>();
		List<Action> generatedActions;
		
		for(Turn turn : new Turn[] {Turn.BLACK, Turn.WHITE}) {
			state.setTurn(turn);
			for (int row = 0; row < board.length; row++) {
				for (int column = 0; column < board[row].length; column++) {
					Pawn pawn = board[row][column];
					if ((turn.equals(Turn.WHITE) && (pawn.equals(Pawn.WHITE) || pawn.equals(Pawn.KING)))
							|| (turn.equals(Turn.BLACK) && pawn.equals(Pawn.BLACK))) {
						//get all actions and filter the bood ones with professor method
						List<Action> allActions = getAllPawnActions(row, column, board, turn);
						allLegalActions.clear();
						for(Action a : allActions) {
							if(GameDaloTablut.checkAction(state,a)) {
								allLegalActions.add(a);
							}
						}
						
						//directly generate actions
						generatedActions = GameDaloTablut.getPawnActions(row, column, board, turn);
						
						//compare results
						for(Action genAction : generatedActions) {
							assertTrue(allLegalActions.contains(genAction), "Generated action \"" + genAction + "\" is illegal");
						}
						for(Action legalAction : allLegalActions) {
							assertTrue(generatedActions.contains(legalAction), "Legal action \"" + legalAction + "\" not generated");
						}
						assertEquals(generatedActions.size(), allLegalActions.size());
					}
				}
			}
		}
	}
	
	
	/*
	 * Generate ALL moves of a paws until he encounter or another pawn, or the wall. Those include illegal states
	 */
	private static List<Action> getAllPawnActions(int row, int column, Pawn[][] board, Turn turn) throws IOException {
		List<Action> result = new ArrayList<>();

		// Check at the bottom
		for (int i = row + 1; i < board.length && (!board[i][column].equals(Pawn.WHITE)
				&& !board[i][column].equals(Pawn.BLACK) && !board[i][column].equals(Pawn.KING)); i++) {
			result.add(new Action(row, column, i, column, turn));
		}

		// Check at the top
		for (int i = row - 1; i >= 0 && (!board[i][column].equals(Pawn.WHITE) && !board[i][column].equals(Pawn.BLACK)
				&& !board[i][column].equals(Pawn.KING)); i--) {
			result.add(new Action(row, column, i, column, turn));
		}

		// Check at the right
		for (int j = column + 1; j < board[row].length && (!board[row][j].equals(Pawn.WHITE)
				&& !board[row][j].equals(Pawn.BLACK) && !board[row][j].equals(Pawn.KING)); j++) {
			result.add(new Action(row, column, row, j, turn));
		}

		// Check at the left
		for (int j = column - 1; j >= 0 && (!board[row][j].equals(Pawn.WHITE) && !board[row][j].equals(Pawn.BLACK)
				&& !board[row][j].equals(Pawn.KING)); j--) {
			result.add(new Action(row, column, row, j, turn));
		}

		return result;
	}

}
