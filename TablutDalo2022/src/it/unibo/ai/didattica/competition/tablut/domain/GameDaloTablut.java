package it.unibo.ai.didattica.competition.tablut.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.client.TablutDalo;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class GameDaloTablut extends GameAshtonTablut implements Game<State, Action, String> {
	private static final String[] PLAYERS = { "white", "black" };
	private boolean DEBUG = false;
	private Pawn[][] b;
	private State.Turn player;

	public static final double soldierNearCastleValue = 0.4; // CASO E
	public static final double soldierNearCampValue = 0.5; // CASO F
	public static final double kingUnderAttackValue = 0.8; // CASO G
	public static final double remainSoldierValue = 0.6; // CASO B
	public static final double kingCanEscapeValue = 0.8;
	public static final double pawnCanBlockEscapeValue = 0.15;
	public static final double kingProtectValue = 0.4;
	public static final double blackSoldierInAngleValue = 0.1;

	public static double getMaxValueHeuristic() {
		return soldierNearCastleValue + soldierNearCampValue + kingUnderAttackValue + remainSoldierValue
				+ kingCanEscapeValue + kingProtectValue + pawnCanBlockEscapeValue + blackSoldierInAngleValue;
	}

	public GameDaloTablut(State state, int repeated_moves_allowed, int cache_size, String logs_folder, String whiteName,
			String blackName, State.Turn player) {
		super(state, repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName);
		this.player = player;
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
		State state = stateInital.clone();
		State.Pawn pawn = state.getPawn(a.getRowFrom(), a.getColumnFrom());
		State.Pawn[][] newBoard = state.getBoard();
		// State newState = new State();
		// this.loggGame.fine("Movimento pedina");
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

		// SECONDA PARTE CHECK MOVE

		// a questo punto controllo lo stato per eventuali catture
		if (state.getTurn().equalsTurn("W")) {
			state = this.checkCaptureBlack(state, a);
		} else if (state.getTurn().equalsTurn("B")) {
			state = this.checkCaptureWhite(state, a);
		}

		// if something has been captured, clear cache for draws
		if (state.getMovesWithutCapturing() == 0) {
			state.clearDrawConditions();
		}

		// controllo pareggio
		int trovati = 0;
		for (State s : state.getDrawConditions()) {
			if (s.equals(state)) {
				trovati++;
				if (trovati > repeated_moves_allowed) {
					state.setTurn(State.Turn.DRAW);
					break;
				}
			} else {
			}
		}
		if (trovati > 0) {
			this.loggGame.fine("Equal states found: " + trovati);
		}
		if (cache_size >= 0 && state.getDrawConditions().size() > cache_size) {
			state.getDrawConditions().remove(0);
		}
		state.getDrawConditions().add(state.clone());

		// this.loggGame.fine("Current draw cache size: " + this.drawConditions.size());

		// this.loggGame.fine("Stato:\n" + state.toString());
		// System.out.println("Stato:\n" + state.toString());

		return state;
	}

	// HEURISTIC FUNCTION
	@Override
	public double getUtility(State stato, String playerCurrent) {
		// System.out.println("MAXVALUE:="+soldierNearCastleValue + soldierNearCampValue
		// + kingUnderAttack + remainSoldierValue
		// + kingCanEscapeValue + (pawnCanBlockEscapeValue * 16));
		// System.out.println("state: " + stato.boardString());

		// check terminal state
		if (stato.getTurn().equals(Turn.WHITEWIN) && player.equals(Turn.WHITE)) {
			return GameDaloTablut.getMaxValueHeuristic();
		}
		if (stato.getTurn().equals(Turn.WHITEWIN) && player.equals(Turn.BLACK)) {
			return 0.0;
		}
		if (stato.getTurn().equals(Turn.BLACKWIN) && player.equals(Turn.BLACK)) {
			return GameDaloTablut.getMaxValueHeuristic();
		}
		if (stato.getTurn().equals(Turn.BLACKWIN) && player.equals(Turn.WHITE)) {
			return 0;
		}
		if (stato.getTurn().equals(Turn.DRAW)) {
			return GameDaloTablut.getMaxValueHeuristic() / 2;
		}

		double value = 0;
		int contWhiteSoldier = 0;
		int contBlackSoldier = 0;
		b = stato.getBoard();
		boolean found = false;

		for (int i = 0; i < stato.getBoard().length; i++) {
			for (int j = 0; j < stato.getBoard().length; j++) {
				// CASO A
				// calcolo distanza re da piastrelle di salvezza
				if (b[i][j].equals(Pawn.KING)) {
					found = true;
					// re in escape box
					if ((i == 0 && (j == 2 || j == 3 || j == 6 || j == 7))
							|| (i == 8 && (j == 2 || j == 3 || j == 6 || j == 7))
							|| (j == 0 && (i == 2 || i == 3 || i == 6 || i == 7))
							|| (j == 8 && (i == 2 || i == 3 || i == 6 || i == 7))) {
						return player.equals(Turn.WHITE) ? GameDaloTablut.getMaxValueHeuristic() : 0.0;
					}
					// check if king can escape
					if (!((i >= 3 && i <= 5) || (j >= 3 && j <= 5))) {
						value += this.kingCanEscape(i, j, kingCanEscapeValue);
					}
					// check king under attack
					value += this.kingUnderAttack(i, j, stato, kingUnderAttackValue);
					// check soldier protect king
					value += this.kingProtect(i, j, stato, kingProtectValue);
				}
				// cont white soldier
				if (b[i][j].equals(Pawn.WHITE)) {
					contWhiteSoldier++;
					value += pawnCanBlockEscape(i, j, stato, pawnCanBlockEscapeValue / 24); // diviso per il numero di
																							// pedine
				}
				// cont black soldier
				if (b[i][j].equals(Pawn.BLACK)) {
					contBlackSoldier++;
					// controllo se il pedone nero � in un angolo
					value += this.blackSoldierInAngle(i, j, blackSoldierInAngleValue);
					// controllo se il pedone blocca una uscita
					value += this.pawnCanBlockEscape(i, j, stato, pawnCanBlockEscapeValue / 24); // diviso per il numero
																									// di pedine
				}

			}
		}
		// check if king is alive
		if (!found)
			return player.equals(Turn.WHITE) ? 0.0 : GameDaloTablut.getMaxValueHeuristic();
		// CASO B soldati rimanenti
		value += ((2 * contWhiteSoldier - contBlackSoldier) / 32 + 0.5) * remainSoldierValue;
		// CASO E controllo castello
		value += this.soldierNearCastle(soldierNearCastleValue);
		// CASO F controllo campi
		value += this.soldierNearCamp(soldierNearCampValue);

		// if (this.getPlayer(stato).equalsIgnoreCase("black"))
		if (player.equals(Turn.BLACK))
			value = GameDaloTablut.getMaxValueHeuristic() - value;

		System.out.println("value: " + value / GameDaloTablut.getMaxValueHeuristic());

		return value;
	}

	private double blackSoldierInAngle(int i, int j, double weight) {
		if ((i == 0 && j == 0) || (i == 0 && j == 8) || (i == 8 && j == 8) || (i == 8 && j == 0)) {
			return  weight / 4; // 4 angle
		} else
			return 0.0;
	}

	private double kingProtect(int i, int j, State stato, double weight) {
		double value = 0;
		// controllo se non ci sono neri vicino al re
		if (!stato.getBox(i + 1, j).equals(Pawn.BLACK)) {
			value += weight / 8;
		} else if (!stato.getBox(i - 1, j).equals(Pawn.BLACK)) {
			value += weight / 8;
		} else if (!stato.getBox(i, j + 1).equals(Pawn.BLACK)) {
			value += weight / 8;
		} else if (!stato.getBox(i, j - 1).equals(Pawn.BLACK)) {
			value += weight / 8;
		} else
		// controllo se il bianco di fianco al re
		if (stato.getBox(i + 1, j).equals(Pawn.WHITE)) {
			value += weight / 4;
		} else if (stato.getBox(i - 1, j).equals(Pawn.WHITE)) {
			value += weight / 4;
		} else if (stato.getBox(i, j + 1).equals(Pawn.WHITE)) {
			value += weight / 4;
		} else if (stato.getBox(i, j - 1).equals(Pawn.WHITE)) {
			value += weight / 4;
		}
		// controllo se il bianco sta bloccando un possibile attacco
		else if (stato.getBox(i + 1, j).equals(Pawn.BLACK) && stato.getBox(i - 1, j).equals(Pawn.WHITE)) {
			value += weight / 4;
		} else if (stato.getBox(i - 1, j).equals(Pawn.BLACK) && stato.getBox(i + 1, j).equals(Pawn.WHITE)) {
			value += weight / 4;
		} else if (stato.getBox(i, j + 1).equals(Pawn.BLACK) && stato.getBox(i, j - 1).equals(Pawn.WHITE)) {
			value += weight / 4;
		} else if (stato.getBox(i, j - 1).equals(Pawn.BLACK) && stato.getBox(i, j + 1).equals(Pawn.WHITE)) {
			value += weight / 4;
		}
		return value;
	}

	private double kingUnderAttack(int i, int j, State stato, double weight) {
		// re nel castello
		int soldierBlack = 0;
		double value = 0;
		if (i == 4 && j == 4) {
			if (b[3][4].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[4][5].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[4][3].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[4][5].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			value += (4 - soldierBlack) / 4 * weight;
		} else
		// re adiacente al castello
		if (i == 3 && j == 4) {
			if (b[3][5].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[3][3].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[2][4].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			value += (3 - soldierBlack) / 3 * weight;
		} else if (i == 5 && j == 4) {
			if (b[5][5].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[5][3].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[6][4].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			value += (3 - soldierBlack) / 3 * weight;
		} else if (i == 4 && j == 3) {
			if (b[3][3].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[4][2].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[5][3].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			value += (3 - soldierBlack) / 3 * weight;
		} else if (i == 4 && j == 5) {
			if (b[4][6].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[3][5].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			if (b[5][5].equals(Pawn.BLACK)) {
				soldierBlack++;
			}
			value += (3 - soldierBlack) / 3 * weight;
		}
		// re vicino ad un campo
		else if ((i == 1 && j == 3) || (i == 1 && j == 5) || (i == 2 && j == 4) || (i == 3 && j == 1)
				|| (i == 4 && j == 2) || (i == 5 && j == 1) || (i == 6 && j == 4) || (i == 7 && j == 5)
				|| (i == 7 && j == 3) || (i == 3 && j == 7) || (i == 4 && j == 6) || (i == 5 && j == 7)) {
			value += 0;
		} else
		// re con neri vicino
		if (stato.getBox(i + 1, j).equals(Pawn.BLACK)) {
			value += 0;
		} else if (stato.getBox(i - 1, j).equals(Pawn.BLACK)) {
			value += 0;
		} else if (stato.getBox(i, j + 1).equals(Pawn.BLACK)) {
			value += 0;
		} else if (stato.getBox(i, j - 1).equals(Pawn.BLACK)) {
			value += 0;
		} else
		// controllo cattura re da parte del nero
		if (this.getPlayer(stato).equalsIgnoreCase("blak")) {
			if ((stato.getBox(i + 1, j).equals(Pawn.BLACK) && stato.getBox(i - 1, j).equals(Pawn.BLACK))
					|| (stato.getBox(i, j + 1).equals(Pawn.BLACK) && stato.getBox(i, j - 1).equals(Pawn.BLACK))) {
				value += 0.0;
			}
		}
		// tutti gli altri casi
		else {
			value += weight;
		}
		return value;
	}

	private double pawnCanBlockEscape(int i, int j, State stato, double weight) {
		if (this.getPlayer(stato).equalsIgnoreCase("black")) {
			if ((i == 1 && j == 6) || (i == 0 && j == 6) || (i == 0 && j == 7) || (i == 1 && j == 7)
					|| (i == 2 && j == 7) || (i == 1 && j == 8) || (i == 2 && j == 8) || (i == 6 && j == 7)
					|| (i == 6 && j == 8) || (i == 7 && j == 6) || (i == 7 && j == 7) || (i == 7 && j == 8)
					|| (i == 8 && j == 6) || (i == 8 && j == 7) || (i == 6 && j == 0) || (i == 6 && j == 1)
					|| (i == 7 && j == 0) || (i == 7 && j == 1) || (i == 7 && j == 2) || (i == 8 && j == 1)
					|| (i == 8 && j == 2) || (i == 0 && j == 1) || (i == 1 && j == 1) || (i == 0 && j == 2)
					|| (i == 1 && j == 2) || (i == 2 && j == 1) || (i == 1 && j == 0) || (i == 2 && j == 0)) {
				return 0.0;
			} else {
				return weight;
			}
		} else {
			if (!(i == 1 && j == 6) && !(i == 0 && j == 6) && !(i == 0 && j == 7) && !(i == 1 && j == 7)
					&& !(i == 2 && j == 7) && !(i == 1 && j == 8) && !(i == 2 && j == 8) && !(i == 6 && j == 7)
					&& !(i == 6 && j == 8) && !(i == 7 && j == 6) && !(i == 7 && j == 7) && !(i == 7 && j == 8)
					&& !(i == 8 && j == 6) && !(i == 8 && j == 7) && !(i == 6 && j == 0) && !(i == 6 && j == 1)
					&& !(i == 7 && j == 0) && !(i == 7 && j == 1) && !(i == 7 && j == 2) && !(i == 8 && j == 1)
					&& !(i == 8 && j == 2) && !(i == 0 && j == 1) && !(i == 1 && j == 1) && !(i == 0 && j == 2)
					&& !(i == 1 && j == 2) && !(i == 2 && j == 1) && !(i == 1 && j == 0) && !(i == 2 && j == 0)) {
				return weight;
			} else {
				return 0.0;
			}
		}
	}

	private double kingCanEscape(int i, int j, double weight) {
		System.out.println("CHECK SE IN TRAIETTORIA");
		int contPawn = 0;
		for (int row = 0; row < i; row++) {
			if (b[row][j].equals(Pawn.WHITE) || b[row][j].equals(Pawn.BLACK)) {
				contPawn++;
			}
		}
		if (contPawn == 0)
			return weight;
		contPawn = 0;
		for (int row = i + 1; row < 9; row++) {
			if (b[row][j].equals(Pawn.WHITE) || b[row][j].equals(Pawn.BLACK)) {
				contPawn++;
			}
		}
		if (contPawn == 0)
			return weight;
		contPawn = 0;
		for (int column = 0; column < j; column++) {
			if (b[i][column].equals(Pawn.WHITE) || b[i][column].equals(Pawn.BLACK)) {
				contPawn++;
			}
		}
		if (contPawn == 0)
			return weight;
		contPawn = 0;
		for (int column = j + 1; column < 9; column++) {
			if (b[i][column].equals(Pawn.WHITE) || b[i][column].equals(Pawn.BLACK)) {
				contPawn++;
			}
		}
		if (contPawn == 0)
			return weight;
		return 0.0;
	}

	private double soldierNearCastle(double weight) {
		int contCastleWhite = 0;
		int contCastleBlack = 0;
		if (b[4][3].equals(Pawn.WHITE)) {
			contCastleWhite++;
		}
		if (b[4][5].equals(Pawn.WHITE)) {
			contCastleWhite++;
		}
		if (b[3][4].equals(Pawn.WHITE)) {
			contCastleWhite++;
		}
		if (b[5][4].equals(Pawn.WHITE)) {
			contCastleWhite++;
		}
		if (b[4][3].equals(Pawn.BLACK)) {
			contCastleBlack++;
		}
		if (b[4][5].equals(Pawn.BLACK)) {
			contCastleBlack++;
		}
		if (b[3][4].equals(Pawn.BLACK)) {
			contCastleBlack++;
		}
		if (b[5][4].equals(Pawn.BLACK)) {
			contCastleBlack++;
		}
		return (((contCastleBlack - contCastleWhite * 2) + 8) / 12) * weight;
	}

	private double soldierNearCamp(double weight) {
		int contCampleWhite = 0;
		int contCampleBlack = 0;
		if (b[1][3].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[1][5].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[2][4].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[6][4].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[7][4].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[7][5].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[3][1].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[4][2].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[5][1].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[3][7].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[4][6].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[5][7].equals(Pawn.WHITE)) {
			contCampleWhite++;
		}
		if (b[1][3].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[1][5].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[2][4].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[6][4].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[7][4].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[7][5].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[3][1].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[4][2].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[5][1].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[3][7].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[4][6].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		if (b[5][7].equals(Pawn.BLACK)) {
			contCampleBlack++;
		}
		return ((contCampleBlack - contCampleWhite * 2) / 32 + 0.5) * weight;
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
			if (this.checkAction(stato, action)) {
				// System.out.println("STATO CORRENTE\n"+stato.boardString());
				resultChecked.add(action);
			}

		}
		if (resultChecked.size() <= 0) {
			DEBUG = true;
			for (Action action : result)
				this.checkAction(stato, action);

			throw new IllegalArgumentException("no moves");
		}
		//Collections.shuffle(resultChecked);
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
		if (DEBUG)
			System.out.println(a.toString());
		// controllo la mossa
		if (a.getTo().length() != 2 || a.getFrom().length() != 2) {
			if (DEBUG)
				System.out.println("Formato mossa errato");
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
			if (DEBUG)
				System.out.println("Mossa fuori tabellone");
			return false;
		}

		// controllo che non vada sul trono
		if (state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.THRONE.toString())) {
			if (DEBUG)
				System.out.println("Mossa sul trono");
			return false;
		}

		// controllo la casella di arrivo
		if (!state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.EMPTY.toString())) {
			if (DEBUG)
				System.out.println("Mossa sopra una casella occupata");
			return false;
		}
		if (this.citadels.contains(state.getBox(rowTo, columnTo))
				&& !this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
			if (DEBUG)
				System.out.println("Mossa che arriva sopra una citadel");
			return false;
		}
		if (this.citadels.contains(state.getBox(rowTo, columnTo))
				&& this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
			if (rowFrom == rowTo) {
				if (columnFrom - columnTo > 5 || columnFrom - columnTo < -5) {
					if (DEBUG)
						System.out.println("Mossa che arriva sopra una citadel");
					return false;
				}
			} else {
				if (rowFrom - rowTo > 5 || rowFrom - rowTo < -5) {
					if (DEBUG)
						System.out.println("Mossa che arriva sopra una citadel");
					return false;
				}
			}

		}

		// controllo se cerco di stare fermo
		if (rowFrom == rowTo && columnFrom == columnTo) {
			if (DEBUG)
				System.out.println("Nessuna mossa");
			return false;
		}

		// controllo se sto muovendo una pedina giusta
		if (state.getTurn().equalsTurn(State.Turn.WHITE.toString())) {
			if (!state.getPawn(rowFrom, columnFrom).equalsPawn("W")
					&& !state.getPawn(rowFrom, columnFrom).equalsPawn("K")) {
				if (DEBUG)
					System.out.println("Giocatore " + a.getTurn() + " cerca di muovere una pedina avversaria");
				return false;
			}
		}
		if (state.getTurn().equalsTurn(State.Turn.BLACK.toString())) {
			if (!state.getPawn(rowFrom, columnFrom).equalsPawn("B")) {
				if (DEBUG)
					System.out.println("Giocatore " + a.getTurn() + " cerca di muovere una pedina avversaria");
				return false;
			}
		}

		// controllo di non muovere in diagonale
		if (rowFrom != rowTo && columnFrom != columnTo) {
			if (DEBUG)
				System.out.println("Mossa in diagonale");
			return false;
		}

		// controllo di non scavalcare pedine
		if (rowFrom == rowTo) {
			if (columnFrom > columnTo) {
				for (int i = columnTo; i < columnFrom; i++) {
					if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
							if (DEBUG)
								System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							if (DEBUG)
								System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(rowFrom, i))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if (DEBUG)
							System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			} else {
				for (int i = columnFrom + 1; i <= columnTo; i++) {
					if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
							if (DEBUG)
								System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							if (DEBUG)
								System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(rowFrom, i))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if (DEBUG)
							System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			}
		} else {
			if (rowFrom > rowTo) {
				for (int i = rowTo; i < rowFrom; i++) {
					if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
							// if(DEBUG) System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							if (DEBUG)
								System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(i, columnFrom))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if (DEBUG)
							System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			} else {
				for (int i = rowFrom + 1; i <= rowTo; i++) {
					if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
							if (DEBUG)
								System.out.println("Mossa che scavalca il trono");
							return false;
						} else {
							// if(DEBUG) System.out.println("Mossa che scavalca una pedina");
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(i, columnFrom))
							&& !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
						if (DEBUG)
							System.out.println("Mossa che scavalca una citadel");
						return false;
					}
				}
			}
		}
		return true;
	}
}
