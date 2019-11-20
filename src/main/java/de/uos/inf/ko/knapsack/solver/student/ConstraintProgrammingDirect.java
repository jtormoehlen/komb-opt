package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.List;

/**
 * A constraint-based solver for the binary knapsack problem using an existing method of Choco.
 *
 * @author jtormoehlen
 */
public class ConstraintProgrammingDirect implements SolverInterface<Solution> {
  public Solution solve(Instance instance) {
    Solution solution = new Solution(instance);
    int n = instance.getSize();
    int[] weights = instance.getWeightArray();
    int[] values = instance.getValueArray();

    // 1. create model
    Model m = new Model("knapsack " + n);

    // 2. create variables
    IntVar[] x = m.boolVarArray("x", n);
    IntVar p = m.intVar("p", 0, n * Arrays.stream(values).max().getAsInt());
    IntVar w = m.intVar("w", 0, instance.getCapacity());

    // 3. add constraints
    /** let the magic happen... **/
    m.knapsack(x, w, p, weights, values).post();

    // 4. get solver and solve model
    Solver s = m.getSolver();

    // 5. put variable values in solution
    List<org.chocosolver.solver.Solution> solutions = s.findAllSolutions();
    ConstraintProgramming.printSolutions(solutions, x);

    // 5. put variable values in solution
    int maxP = 0;
    org.chocosolver.solver.Solution optimal = null;
    for (org.chocosolver.solver.Solution solutionIterator : solutions) {
      int tmpP = solutionIterator.getIntVal(p);

      if (tmpP >= maxP) {
        maxP = tmpP;
        optimal = solutionIterator.copySolution();
      }
    }

    for (int j = 0; j < n; j++) {
      solution.set(j, optimal.getIntVal(x[j]));
    }

    return solution;
  }

  @Override
  public String getName() {
    return "CPD(s)";
  }
}
