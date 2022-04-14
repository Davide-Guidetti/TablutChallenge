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

public class IterativeDeepeningAlphaBetaSearchTablut<S, A, P> /*extends IterativeDeepeningAlphaBetaSearch<S, A, P> */ implements AdversarialSearch<S, A> {
	
	public final static String METRICS_NODES_EXPANDED = "nodesExpanded";
	public final static String METRICS_MAX_DEPTH = "maxDepth";
	protected Metrics metrics = new Metrics();

	protected Game<S, A, P> game;
	protected double utilMax;
	protected double utilMin;
	protected int currDepthLimit;
	protected boolean heuristicEvaluationUsed; // indicates that non-terminal nodes have been evaluated.
	
	public boolean graphOptimization = true; //keeps references to expanded states, in order to check if the same state has already been expanded
	HashSet<S> expandedStates = new HashSet<>();
	
	private Timer timer;
	public boolean timedOut; //algorithm stopped search because of timeout
	public Statistics statistics;
	protected Statistics runningStatistics;
	
	public boolean logEnabled = false;
	public boolean printStatistics = false;
	public int maxDepth = Integer.MAX_VALUE;
	
	
	
	public IterativeDeepeningAlphaBetaSearchTablut(Game<S, A, P> game, double utilMin, double utilMax, int time) {
		//super(game, utilMin, utilMax, time);
		this.game = game;
		this.utilMin = utilMin;
		this.utilMax = utilMax;
		this.timer = new Timer(time);
		this.statistics = new Statistics();
	}
	
	@Override
	public A makeDecision(S state) {
		StringBuffer logText = null;
		P player = game.getPlayer(state);
		List<A> results = game.getActions(state);
		timer.start();
		timedOut = false;
		currDepthLimit = 0;
		metrics = new Metrics();
		do {
			currDepthLimit++;
			if (logEnabled) logText = new StringBuffer("Starting search up to depth " + currDepthLimit + "\n");
			heuristicEvaluationUsed = false;
			runningStatistics = new Statistics();
			runningStatistics.reachedDepth = currDepthLimit;
			ActionStore<A> newResults = new ActionStore<>();
			expandedStates.clear();
			for (A action : results) {
				double value = minValue(
						(logEnabled) ? logExpansion(
									game.getResult(state, action), 
									action,
									player,
									logText
							) : game.getResult(state, action),
						player, 
						Double.NEGATIVE_INFINITY, 
						Double.POSITIVE_INFINITY, 
						1
				);
				if (timer.timeOutOccurred()) break; // exit from action loop
				newResults.add(action, value);
				if (logEnabled) logText.append("value for top level action " + action + " = " + value + " \n");
			}
			if (newResults.size() > 0) {
				results = newResults.actions;
				if (logEnabled) logText.append("Action chosen: \"" + results.get(0) + "\", utility = " + newResults.utilValues.get(0) + " (max possible value: " + GameDaloTablut.getMaxValueHeuristic() + ")\n");
			}else {
				//TODO
				//maybe choose a random correct action by generatig all of them and returning the first feasible one by checking it with prof's checkmove
				if (logEnabled) logText.append("No action to chose from");
			}
			if (logEnabled) System.out.println(logText);
			statistics = runningStatistics;
			if (printStatistics ) System.out.println(statistics);
		} while (                           //exit if:
				!timer.timeOutOccurred() && //time elapse OR
				heuristicEvaluationUsed &&  //heuristic not used = all evaluated states was terminal, or maximun depth has been reacked OR
				currDepthLimit < maxDepth   //currentDepth reached maxDepth
		);
		if(timer.timeOutOccurred()) timedOut=true;
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
				if(graphOptimization) {
					if(expandedStates.add(newState)==false) { //this state has already been expanded by the same player, and so previously evaluated. Continue with the next move
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
				if(graphOptimization) {
					if(expandedStates.add(newState)==false) { //this state has already been expanded by the same player, and so previously evaluated. Continue with the next move
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
	
	//@Override
	protected double eval(S state, P player) {
		//System.out.println(game.isTerminal(state));
		if (!game.isTerminal(state)) heuristicEvaluationUsed = true;
		//System.out.println("HF: "+heuristicEvaluationUsed);
		return game.getUtility(state, player);
	}
	
	//empty default implementation
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
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// nested helper classes

	private static class Timer {
		private long duration;
		private long startTime;

		Timer(int maxSeconds) {
			this.duration = 1000 * maxSeconds;
		}

		void start() {
			startTime = System.currentTimeMillis();
		}

		boolean timeOutOccurred() {
			return System.currentTimeMillis() > startTime + duration;
		}
	}

	/**
	 * Orders actions by utility.
	 */
	private static class ActionStore<A> {
		private List<A> actions = new ArrayList<>();
		private List<Double> utilValues = new ArrayList<>();

		void add(A action, double utilValue) {
			int idx = 0;
			while (idx < actions.size() && utilValue <= utilValues.get(idx))
				idx++;
			actions.add(idx, action);
			utilValues.add(idx, utilValue);
		}

		int size() {
			return actions.size();
		}
	}
	
	public static class Statistics {
		public long expandedNodes;
		public long reachedDepth;
		public long skippedSameNodes;
		
		public Statistics() {
			expandedNodes = 0;
			reachedDepth = 0;
			skippedSameNodes = 0;
		}
		
		public String toString() {
			return "SEARCH STATISTICS:\n" +
					"expandedNodes: " + expandedNodes + "\n" +
					"skippedSameNodes: " + skippedSameNodes + "\n" +
					"reachedDepth: " + reachedDepth + "\n" ;
		}
	}
}
