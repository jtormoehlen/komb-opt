package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Solution;

public class SimulatedAnnealingTest extends GenericExactSolverTest<Solution> {

  public SimulatedAnnealingTest() {
    super(new SimulatedAnnealing());
  }
}
