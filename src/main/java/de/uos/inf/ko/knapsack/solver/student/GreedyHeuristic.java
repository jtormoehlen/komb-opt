package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

/**
 * A sorting-based heuristic for the binary knapsack problem.
 *
 * @author
 */
public class GreedyHeuristic implements SolverInterface<Solution> {

  @Override
  public Solution solve(Instance instance) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "Greedy(s)";
  }
}
