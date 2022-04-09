package it.unibo.ai.didattica.competition.tablut.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
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

public class GameDaloTablut extends GameAshtonTablut implements Game<State, Action, String> {
	private static final String[] PLAYERS = { "white", "black" };
	private boolean DEBUG = false;
	private Logger loggGame;

	public GameDaloTablut(State state, int repeated_moves_allowed, int cache_size, String logs_folder, String whiteName,
			String blackName) {
		super(state, repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName);
	}

	@Override
	public State getInitialState() {
		return new StateTablut();
	}

	@Override
	public String[] getPlayers() {
		return PLAYERS;
	}

	@Override
	public String getPlayer(State stato) {
		return stato.getTurn().name();
	}

	@Override
	public State getResult(State stateInital, Action a) {
		State state=stateInital.clone();
		State.Pawn pawn = state.getPawn(a.getRowFrom(), a.getColumnFrom());
		State.Pawn[][] newBoard = state.getBoard();
		// State newState = new State();
		//this.loggGame.fine("Movimento pedina");
		// libero il trono o una casella qualunque
		if (a.getColumnFrom() == 4 && a.getRowFrom() == 4) {
			newBoard[a.getRowFrom()][a.getColumnFrom()] = State.Pawn.THRONE;
		} else {
			newBoard[a.getRowFrom()][a.getColumnFrom()] = State.Pawn.EMPTY;
		}

		// metto nel nuovo tabellone la pedina mossa
		newBoard[a.getRowTo()][a.getColumnTo()] = pawn;
		// aggiorno il tabellone
		state.setBoard(newBoard);
		// cambio il turno
		if (state.getTurn().equalsTurn(State.Turn.WHITE.toString())) {
			state.setTurn(State.Turn.BLACK);
		} else {
			state.setTurn(State.Turn.WHITE);
		}

		return state;
	}

	@Override
	public double getUtility(State stato, String player) {
		if (stato.getTurn().equals(Turn.WHITEWIN) && player.equalsIgnoreCase("white")) {
			return 1.0;
		}
		if (stato.getTurn().equals(Turn.WHITEWIN) && player.equalsIgnoreCase("black")) {
			return 0.0;
		}
		if (stato.getTurn().equals(Turn.BLACKWIN) && player.equalsIgnoreCase("black")) {
			return 1.0;
		}
		if (stato.getTurn().equals(Turn.BLACKWIN) && player.equalsIgnoreCase("white")) {
			return 0;
		}
		if (stato.getTurn().equals(Turn.DRAW)) {
			return 0;
		}
		return 0.5;
	}

	@Override
	public boolean isTerminal(State stato) {
		return stato.getTurn().equals(Turn.WHITEWIN) || stato.getTurn().equals(Turn.BLACKWIN)
				|| stato.getTurn().equals(Turn.DRAW);
	}

	@Override
	public List<Action> getActions(State stato) {
		List<Action> result = new ArrayList<>();

		Turn turn = stato.getTurn();
		Pawn[][] board = stato.getBoard();
		// Check king's actions first
		if (turn.equals(Turn.WHITE)) {
			for (int row = 0; row < board.length; row++) {
				for (int column = 0; column < board[row].length; column++) {
					if (board[row][column].equals(Pawn.KING)) {
						try {
							result.addAll(getPawnActions(row, column, board, turn));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		for (int row = 0; row < board.length; row++) {
			for (int column = 0; column < board[row].length; column++) {
				Pawn pawn = board[row][column];
				if ((turn.equals(Turn.WHITE) && pawn.equals(Pawn.WHITE))
						|| (turn.equals(Turn.BLACK) && pawn.equals(Pawn.BLACK))) {
					try {
						result.addAll(getPawnActions(row, column, board, turn));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		List<Action> resultChecked = new ArrayList<>(result.size());

		for (Action action : result) {
			if(this.checkAction(stato, action)) {
				//System.out.println("STATO CORRENTE\n"+stato.boardString());
				resultChecked.add(action);
			}
				
		}
		if(resultChecked.size() <= 0) {
			DEBUG = true;
			for (Action action : result) this.checkAction(stato, action);
			
			throw new IllegalArgumentException("no moves");
		}
		
		return resultChecked;
	}

	private List<Action> getPawnActions(int row, int column, Pawn[][] board, Turn turn) throws IOException {
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

	public boolean checkAction(State state, Action a) {
		if(DEBUG) System.out.println(a.toString());
		// controllo la mossa
		if (a.getTo().length() != 2 || a.getFrom().length() != 2) {
			if(DEBUG) System.out.println("Formato mossa errato");
			return false;
		}
		int columnFrom = a.getColumnFrom();
		int columnTo = a.getColumnTo();
		int rowFrom = a.getRowFrom();
		int rowTo = a.getRowTo();

		// controllo se sono fuori dal tabellone
		if (columnFrom > state.getBoard().length - 1 || rowFrom > state.getBoard().length - 1
				|| rowTo > state.getBoard().length - 1 || columnTo > state.getBoard().length - 1 || columnFrom < 0
				|| rowFrom < 0 || rowTo < 0 || columnTo < 0) {
			if(DEBUG) System.out.println("Mossa fuori tabellone");
			return false;
		}

		// controllo che non vada sul trono
		if (state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.THRONE.toString())) {
			if(DEBUG) System.out.println("Mossa sul trono");
			return false;
		}

		// controllo la casella di arrivo
		if (!state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.EMPTY.toString())) {
			if(DEBUG) System.out.println("Mossa sopra una casella occupata");
			return false;
		}
		if (this.citadels.contains(state.getBox(rowTo, columnTo))
				&& !this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
			if(DEBUG) System.out.println("Mossa che arriva sopra una citadel");
			return false;
		}
		if (this.citadels.contains(state.getBox(rowTo, columnTo))
				&& this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
			if (rowFrom == rowTo) {
				if (columnFrom - columnTo > 5 || columnFrom - columnTo < -5) {
					if(DEBUG) System.out.println("Mossa che arriva sopra una citadel");
					return false;
				}
			} else {
				if (rowFrom - rowTo > 5 || rowFrom - rowTo < -5) {
					if(DEBUG) System.out.println("Mossa che arriva sopra una citadel");
					return false;
				}
			}

		}

		// controllo se cerco di stare fermo
		if (rowFrom == rowTo && columnFrom == columnTo) {
			if(DEBUG) System.out.println("Nessuna mossa");
			return false;
		}

		// controllo se sto muovendo una pedina giusta
		if (state.getTurn().equalsTurn(State.Turn.WHITE.toString())) {
			if (!state.getPawn(rowFrom, columnFrom).equalsPawn("W")
					&& !state.getPawn(rowFrom, columnFrom).equalsPawn("K")) {
				if(DEBUG) System.out.println("Giocatore " + a.getTurn() + " cerca di muovere una pedina avversaria");
				return false;
			}
		}
		if (state.getTurn().equalsTurn(State.Turn.BLACK.toString())) {
			if (!state.getPawn(rowFrom, columnFrom).equalsPawn("B")) {
				if(DEBUG) System.out.println("Giocatore " + a.getTurn() + " cerca di muovere una pedina avversaria");
				return false;
			}
		}

		// controllo di non muovere in diagonale
		if (rowFrom != rowTo && columnFrom != columnTo) {
			if(DEBUG) System.out.println("Mossa in diagonale");
			return false;
		}

		// controllo di non scavalcare pedine
		if (rowFrom == rowTo) {
			if (columnFrom > columnTo) {
				for (int i = columnTo; i < columnFrom; i++) {
					if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
							if(DEBUG) System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							if(DEBUG) System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(rowFrom, i))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if(DEBUG) System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			} else {
				for (int i = columnFrom + 1; i <= columnTo; i++) {
					if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
							if(DEBUG) System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							if(DEBUG) System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(rowFrom, i))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if(DEBUG) System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			}
		} else {
			if (rowFrom > rowTo) {
				for (int i = rowTo; i < rowFrom; i++) {
					if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
							//if(DEBUG) System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							if(DEBUG) System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(i, columnFrom))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if(DEBUG) System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			} else {
				for (int i = rowFrom + 1; i <= rowTo; i++) {
					if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
							if(DEBUG) System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							//if(DEBUG) System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(i, columnFrom))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if(DEBUG) System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			}
		}
		return true;
	}
}
