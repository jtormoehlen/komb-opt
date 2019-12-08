package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Solution;

public class TabuSearchTest extends GenericExactSolverTest<Solution> {

  public TabuSearchTest() {
    super(new TabuSearch());
  }
}
