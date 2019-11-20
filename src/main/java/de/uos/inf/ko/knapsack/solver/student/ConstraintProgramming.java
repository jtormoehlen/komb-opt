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
 * A constraint-based solver for the binary knapsack problem.
 *
 * @author jtormoehlen
 */
public class ConstraintProgramming implements SolverInterface<Solution> {
  public Solution solve(Instance instance) {
    Solution solution = new Solution(instance);
    int n = instance.getSize();
    int w = instance.getCapacity();
    int[] weights = instance.getWeightArray();
    int[] values = instance.getValueArray();

    // 1. create model
    Model m = new Model("knapsack " + n);

    // 2. create variables
    IntVar[] x = m.boolVarArray("x", n);
    IntVar p = m.intVar("p", 0, n * Arrays.stream(values).max().getAsInt());

    // 3. add constraints
    /** constraints handmade: sum x*weights <= w and sum x*values = p **/
    m.scalar(x, weights, "<=", w).post();
    m.scalar(x, values, "=", p).post();

    // 4. get solver and solve model
    Solver s = m.getSolver();
    List<org.chocosolver.solver.Solution> solutions = s.findAllSolutions();

    // 5. put variable values in solution
    int maxP = 0;
    org.chocosolver.solver.Solution optimal = null;
    /** iterate through all solutions and pick the most profitable **/
    for (org.chocosolver.solver.Solution solutionIterator : solutions) {
      int tmpP = solutionIterator.getIntVal(p);

      /** update optimal solution if possible **/
      if (tmpP >= maxP) {
        maxP = tmpP;
        optimal = solutionIterator.copySolution();
      }
    }

    /** put the best result in solution **/
    for (int j = 0; j < n; j++) {
      solution.set(j, optimal.getIntVal(x[j]));
    }

    return solution;
  }

  /**
   * @author Sven Boge
   * (online ressource!)
   * @param solutions
   * @param x
   */
  public static void printSolutions(List<org.chocosolver.solver.Solution> solutions, IntVar[] x) {
    int cnt = 1;
    for (org.chocosolver.solver.Solution solution : solutions) {
      StringBuilder builder = new StringBuilder();
      builder.append(cnt + ". Solution: (");
      for(int i = 0; i < x.length; ++i) {
        builder.append(solution.getIntVal(x[i]) + (i+1 == x.length ? "" : ","));
      }
      builder.append(")");
      System.out.println(builder.toString());
      ++cnt;
    }
  }

  @Override
  public String getName() {
    return "CP(s)";
  }
}
