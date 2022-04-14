package SearchStrategy;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;

public class IterativeDeepeningAlphaBetaSearchTablut<S, A, P> extends IterativeDeepeningAlphaBetaSearch<S, A, P> {

	public IterativeDeepeningAlphaBetaSearchTablut(Game<S, A, P> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double eval(S state, P player) {
		super.eval(state, player);
		if (!game.isTerminal(state)) heuristicEvaluationUsed = true;
		return game.getUtility(state, player);
	}
}
