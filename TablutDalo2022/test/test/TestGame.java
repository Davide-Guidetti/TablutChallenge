package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import SearchStrategy.IterativeDeepeningAlphaBetaSearchTablut;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.exceptions.ActionException;
import it.unibo.ai.didattica.competition.tablut.exceptions.BoardException;
import it.unibo.ai.didattica.competition.tablut.exceptions.CitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingCitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingException;
import it.unibo.ai.didattica.competition.tablut.exceptions.DiagonalException;
import it.unibo.ai.didattica.competition.tablut.exceptions.OccupitedException;
import it.unibo.ai.didattica.competition.tablut.exceptions.PawnException;
import it.unibo.ai.didattica.competition.tablut.exceptions.StopException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ThroneException;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class TestGame {

	@Test
	void test() throws IOException, BoardException, ActionException, StopException, PawnException, DiagonalException, ClimbingException, ThroneException, OccupitedException, ClimbingCitadelException, CitadelException {
		
		GameDaloTablut rulesBlack_ours;
		GameAshtonTablut rulesBlack_theirs;
		GameDaloTablut rulesWhite_ours;
		GameAshtonTablut rulesWhite_theirs;
		
		rulesBlack_ours = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.BLACK);
		rulesWhite_ours = new GameDaloTablut(new StateTablut(), 2, 2, "log", "White", "Black", State.Turn.WHITE);
		rulesBlack_theirs = new GameAshtonTablut(2, 2, "log", "White", "Black");
		rulesWhite_theirs = new GameAshtonTablut(2, 2, "log", "White", "Black");
		
		/*public GameDaloTablut(State state, int repeated_moves_allowed, int cache_size, String logs_folder, String whiteName,
				String blackName, State.Turn player)
		public GameAshtonTablut(int repeated_moves_allowed, int cache_size, String logs_folder, String whiteName,
				String blackName)*/
		
		State currentState_ours = new StateTablut();
		State currentState_theirs = new StateTablut();
		currentState_ours.setTurn(Turn.WHITE);
		currentState_theirs.setTurn(Turn.WHITE);
		Action chosenMove;
		
		chosenMove = new Action("c5","c6",Turn.WHITE);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
		chosenMove = new Action("b5","c5",Turn.BLACK);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
		chosenMove = new Action("e4","c4",Turn.WHITE);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
		chosenMove = new Action("a6","a7",Turn.BLACK);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
		chosenMove = new Action("c4","e4",Turn.WHITE);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
		chosenMove = new Action("a7","d7",Turn.BLACK);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
		chosenMove = new Action("e7","f7",Turn.WHITE);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
		chosenMove = new Action("d7","d6",Turn.BLACK);
		currentState_ours = rulesWhite_ours.getResult(currentState_ours, chosenMove);
		currentState_theirs = rulesWhite_theirs.checkMove(currentState_theirs, chosenMove);
		assertEquals(currentState_theirs, currentState_ours);
	}

}
