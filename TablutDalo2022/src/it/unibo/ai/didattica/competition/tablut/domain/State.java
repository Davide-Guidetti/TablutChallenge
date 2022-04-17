package it.unibo.ai.didattica.competition.tablut.domain;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract class for a State of a game We have a representation of the board
 * and the turn
 *
 * @author Andrea Piretti
 *
 */
public abstract class State {

	public enum Turn {
		WHITE((byte) 0), BLACK((byte) 1), WHITEWIN((byte) 2), BLACKWIN((byte) 3), DRAW((byte) 4);

		private final byte turn;

		private Turn(String s) {
			if (s.equals("W")) {
				turn = (byte) 0;
			} else if (s.equals("B")) {
				turn = (byte) 1;
			} else if (s.equals("WW")) {
				turn = (byte) 2;
			} else if (s.equals("BW")) {
				turn = (byte) 3;
			} else if (s.equals("D")) {
				turn = (byte) 4;
			} else {
				turn = (byte) 0;
			}
		}

		private Turn(byte b) {
			turn = b;
		}

		public boolean equalsTurn(String otherName) {
			if (otherName.equals("W") && turn == (byte) 0) {
				return true;
			} else if (otherName.equals("B") && turn == (byte) 1) {
				return true;
			} else if (otherName.equals("WW") && turn == (byte) 2) {
				return true;
			} else if (otherName.equals("BW") && turn == (byte) 3) {
				return true;
			} else if (otherName.equals("D") && turn == (byte) 4) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			if (turn == (byte) 0) {
				return "W";
			} else if (turn == (byte) 1) {
				return "B";
			} else if (turn == (byte) 2) {
				return "WW";
			} else if (turn == (byte) 3) {
				return "BW";
			} else if (turn == (byte) 4) {
				return "D";
			}
			return null;
		}
	}

	public enum Pawn {
		EMPTY((byte) 0), WHITE((byte) 1), BLACK((byte) 2), THRONE((byte) 3), KING((byte) 4);

		private final byte pawn;

		private Pawn (String s) {
			if (s.equals("O")) {
				pawn = (byte) 0;
			} else if (s.equals("W")) {
				pawn = (byte) 1;
			} else if (s.equals("B")) {
				pawn = (byte) 2;
			} else if (s.equals("T")) {
				pawn = (byte) 3;
			} else if (s.equals("K")) {
				pawn = (byte) 4;
			} else {
				pawn = (byte) 0;
			}	
		}
		
		private Pawn(byte s) {
			pawn = s;
		}

		public boolean equalsPawn(String otherPawn) {
			if (otherPawn.equals("O") && pawn == (byte) 0) {
				return true;
			} else if (otherPawn.equals("W") && pawn == (byte) 1) {
				return true;
			} else if (otherPawn.equals("B") && pawn == (byte) 2) {
				return true;
			} else if (otherPawn.equals("T") && pawn == (byte) 3) {
				return true;
			} else if (otherPawn.equals("K") && pawn == (byte) 4) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			if (pawn == (byte) 0) {
				return "O";
			} else if (pawn == (byte) 1) {
				return "W";
			} else if (pawn == (byte) 2) {
				return "B";
			} else if (pawn == (byte) 3) {
				return "T";
			} else if (pawn == (byte) 4) {
				return "K";
			}
			return null;
		}

	}

	protected Pawn board[][];
	protected Turn turn;

	public State() {
		super();
	}

	public Pawn[][] getBoard() {
		return board;
	}

	public String boardString() {
		StringBuffer result = new StringBuffer();
		for (Pawn[] element : this.board) {
			for (int j = 0; j < this.board.length; j++) {
				result.append(element[j].toString());
				if (j == 8) {
					result.append("\n");
				}
			}
		}
		return result.toString();
	}

	public String boardStringWithCellIndex() {
		StringBuffer result = new StringBuffer();
		result.append(" ABCDEFGHI\n");
		for (int i = 0; i < this.board.length; i++) {
			result.append(i + 1);
			for (int j = 0; j < this.board[0].length; j++) {
				result.append(this.board[i][j].toString());
			}
			result.append("\n");
		}
		return result.toString();
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		// board
		result.append("");
		result.append(this.boardString());

		result.append("-");
		result.append("\n");

		// TURNO
		result.append(this.turn.toString());

		return result.toString();
	}

	public String toLinearString() {
		StringBuffer result = new StringBuffer();

		// board
		result.append("");
		result.append(this.boardString().replace("\n", ""));
		result.append(this.turn.toString());

		return result.toString();
	}

	/**
	 * this function tells the pawn inside a specific box on the board
	 *
	 * @param row    represents the row of the specific box
	 * @param column represents the column of the specific box
	 * @return is the pawn of the box
	 */
	public Pawn getPawn(int row, int column) {
		return this.board[row][column];
	}

	/**
	 * this function remove a specified pawn from the board
	 *
	 * @param row    represents the row of the specific box
	 * @param column represents the column of the specific box
	 *
	 */
	public void removePawn(int row, int column) {
		this.board[row][column] = Pawn.EMPTY;
	}

	public void setBoard(Pawn[][] board) {
		this.board = board;
	}

	public Turn getTurn() {
		return turn;
	}

	public void setTurn(Turn turn) {
		this.turn = turn;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (this.getClass() != obj.getClass()))
			return false;
		State other = (State) obj;
		if (this.board == null) {
			if (other.board != null)
				return false;
		} else {
			if (other.board == null)
				return false;
			if (this.board.length != other.board.length)
				return false;
			if (this.board[0].length != other.board[0].length)
				return false;
			for (int i = 0; i < other.board.length; i++)
				for (int j = 0; j < other.board[i].length; j++)
					if (!this.board[i][j].equals(other.board[i][j]))
						return false;
		}
		if (this.turn != other.turn)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.board == null) ? 0 : deepHashCode(board));
		result = prime * result + ((this.turn == null) ? 0 : this.turn.hashCode());
		return result;
	}

	private static <T> int deepHashCode(T[][] matrix) {
		int tmp[] = new int[matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			tmp[i] = Arrays.hashCode(matrix[i]);
		}
		return Arrays.hashCode(tmp);
	}

	public static String getBox(int row, int column) {
		String ret;
		char col = (char) (column + 97);
		ret = col + "" + (row + 1);
		return ret;
	}

	@Override
	public State clone() {
		Class<? extends State> stateclass = this.getClass();
		Constructor<? extends State> cons = null;
		State result = null;
		try {
			cons = stateclass.getConstructor(stateclass);
			result = cons.newInstance(new Object[0]);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		Pawn oldboard[][] = this.getBoard();
		Pawn newboard[][] = result.getBoard();

		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[i].length; j++) {
				newboard[i][j] = oldboard[i][j];
			}
		}

		result.setBoard(newboard);
		result.setTurn(this.turn);
		return result;
	}

	/**
	 * Counts the number of checkers of a specific color on the board. Note: the
	 * king is not taken into account for white, it must be checked separately
	 * 
	 * @param color The color of the checker that will be counted. It is possible
	 *              also to use EMPTY to count empty cells.
	 * @return The number of cells of the board that contains a checker of that
	 *         color.
	 */
	public int getNumberOf(Pawn color) {
		int count = 0;
		for (Pawn[] element : board) {
			for (int j = 0; j < element.length; j++) {
				if (element[j] == color)
					count++;
			}
		}
		return count;
	}

	public abstract List<State> getDrawConditions();

	public abstract void setDrawConditions(List<State> drawConditions);

	public abstract void clearDrawConditions();

	public abstract int getMovesWithutCapturing();

	public abstract void setMovesWithutCapturing(int movesWithutCapturing);

}
