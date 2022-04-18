
package SearchStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aima.core.search.adversarial.AdversarialSearch;
import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.adversarial.Metrics;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameDaloTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class IterativeDeepeningAlphaBetaSearchTablutWithoutFuture<S, A, P>
		extends IterativeDeepeningAlphaBetaSearchTablut<S, A, P> implements AdversarialSearch<S, A> {

	HashSet<S> expandedStates = new HashSet<>();

	public IterativeDeepeningAlphaBetaSearchTablutWithoutFuture(Game<S, A, P> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
	}

	@Override
	public A makeDecision(S state) {
		StringBuffer logText = null;
		P player = game.getPlayer(state);
		results = game.getActions(state);
		timer.start();
		timedOut = false;
		outOfMemoryOccurred = false;
		currDepthLimit = 0;
		metrics = new Metrics();
		do {
			currDepthLimit++;
			if (logEnabled)
				logText = new StringBuffer("Starting search up to depth " + currDepthLimit + "\n");
			heuristicEvaluationUsed = false;
			runningStatistics = new Statistics();
			runningStatistics.reachedDepth = currDepthLimit;
			ActionStore<A> newResults = new ActionStore<>();
			expandedStates.clear();
			System.gc();
			//long startTime = System.currentTimeMillis();
			for (A action : results) {
				try {
					S newState = game.getResult(state, action);
					if (graphOptimization) {
						if (expandedStates.add(newState) == false) { // this state has already been expanded by the same
																		// player, and so previously evaluated. Continue
																		// with the next move
							// expandedStates.remove(newState);
							runningStatistics.skippedSameNodes++;
							continue;
						}
					}
					double value = minValue(
							(logEnabled) ? logExpansion(newState, action, player, logText)
									: game.getResult(state, action),
							player, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
					if (timer.timeOutOccurred())
						break; // exit from action loop
					newResults.add(action, value);
					if (logEnabled)
						logText.append("value for top level action " + action + " = " + value + " \n");
				} catch (OutOfMemoryError e) {
					outOfMemoryOccurred = true;
					break;
				}
			}
			//long endTime = System.currentTimeMillis();
			//System.out.println("Elapsed Time: " + (endTime - startTime) + "\n");
			if (newResults.size() > 0) {
				results = newResults.actions;
				utilities = newResults.utilValues;
				runningStatistics.maxResultUtility = newResults.utilValues.get(0);
				if (logEnabled)
					logText.append(
							"Action chosen: \"" + results.get(0) + "\", utility = " + newResults.utilValues.get(0)
									+ " (max possible value: " + GameDaloTablut.getMaxValueHeuristic() + ")\n");
			} else {
				// TODO
				// maybe choose a random correct action by generatig all of them and returning
				// the first feasible one by checking it with prof's checkmove
				if (logEnabled)
					logText.append("No action to chose from");
			}
			if (logEnabled)
				System.out.println(logText);
			statistics = runningStatistics;
			if (printStatistics)
				System.out.println(statistics);
		} while ( // exit if:
				!timer.timeOutOccurred() && // time elapse OR
				heuristicEvaluationUsed && // heuristic not used = all evaluated states was terminal, or maximun depth has been reacked OR
				currDepthLimit < maxDepth && // currentDepth reached maxDepth
				!outOfMemoryOccurred // ram not sufficient
		);
		if (timer.timeOutOccurred())
			timedOut = true;
		return results.get(0);
	}

	// returns an utility value
	public double maxValue(S state, P player, double alpha, double beta, int depth) {
		runningStatistics.expandedNodes++;
		updateMetrics(depth);
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			return eval(state, player);
		} else {
			double value = Double.NEGATIVE_INFINITY;
			for (A action : game.getActions(state)) {
				S newState = game.getResult(state, action);
				if (graphOptimization) {
					boolean jump = false;
					if (depth <= RECORD_STATES_UP_TO_DEPTH)
						jump = expandedStates.add(newState) == false;
					else
						jump = expandedStates.contains(newState) == true;
					if (jump) { // this state has already been expanded by the same player, and so previously
								// evaluated. Continue with the next move
						// expandedStates.remove(newState);
						runningStatistics.skippedSameNodes++;
						continue;
					}
				}
				value = Math.max(value, minValue(newState, player, alpha, beta, depth + 1));
				if (value >= beta)
					return value;
				alpha = Math.max(alpha, value);
			}
			return value;
		}
	}

	// returns an utility value
	public double minValue(S state, P player, double alpha, double beta, int depth) {
		runningStatistics.expandedNodes++;
		updateMetrics(depth);
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			return eval(state, player);
		} else {
			double value = Double.POSITIVE_INFINITY;
			for (A action : game.getActions(state)) {
				S newState = game.getResult(state, action);
				if (graphOptimization) {
					boolean jump = false;
					if (depth <= RECORD_STATES_UP_TO_DEPTH)
						jump = expandedStates.add(newState) == false;
					else
						jump = expandedStates.contains(newState) == true;
					if (jump) { // this state has already been expanded by the same player, and so previously
								// evaluated. Continue with the next move
						// expandedStates.remove(newState);
						runningStatistics.skippedSameNodes++;
						continue;
					}
				}
				value = Math.min(value, maxValue(newState, player, alpha, beta, depth + 1));
				if (value <= alpha)
					return value;
				beta = Math.min(beta, value);
			}
			return value;
		}
	}

	// @Override
	protected double eval(S state, P player) {
		// System.out.println(game.isTerminal(state));
		if (!game.isTerminal(state))
			heuristicEvaluationUsed = true;
		// System.out.println("HF: "+heuristicEvaluationUsed);
		return game.getUtility(state, player);
	}

	// empty default implementation
	protected S logExpansion(S state, A action, P player, StringBuffer logText) {
		return state;
	}

	private void updateMetrics(int depth) {
		metrics.incrementInt(METRICS_NODES_EXPANDED);
		metrics.set(METRICS_MAX_DEPTH, Math.max(metrics.getInt(METRICS_MAX_DEPTH), depth));
	}

	@Override
	public Metrics getMetrics() {
		return metrics;
	}
}