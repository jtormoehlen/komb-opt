package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

/**
 * A full enumeration algorithm for the binary knapsack problem.
 * 
 * @author
 */
public class Enumeration implements SolverInterface<Solution> {

  @Override
  public Solution solve(Instance instance) {

    int n = instance.getSize();

    Solution solution = new Solution(instance);
    Solution optimal = solution;

    for (int i = 0; i < Math.pow(2, n); i++) {
      String binaryString = Integer.toBinaryString(i);

      solution = binaryStringToSolution(binaryString, instance);

      if (solution.isFeasible()) {
        if ((solution.getValue() > optimal.getValue())) {
          optimal = binaryStringToSolution(binaryString, instance);
        }
      }
    }

    System.out.println("W=" + optimal.getWeight() + "|" + "P=" + optimal.getValue() + "\n");

    return optimal;
  }

  private static Solution binaryStringToSolution(String binaryString, Instance instance) {
    Solution solution = new Solution(instance);

    for (int j = 0; j < binaryString.length(); j++) {
      if (j >= binaryString.length()) {
        solution.set(j, 0);
      } else {
        solution.set(j, Character.getNumericValue(binaryString.charAt(j)));
      }
    }

    return solution;
  }

  private static int knapsackRec(int[] w, int[] v, int n, int W) {
    if (n <= 0) {
      return 0;
    } else if (w[n - 1] > W) {
      return knapsackRec(w, v, n - 1, W);
    } else {
      return Math.max(knapsackRec(w, v, n - 1, W), v[n - 1]
              + knapsackRec(w, v, n - 1, W - w[n - 1]));
    }
  }

  @Override
  public String getName() {
    return "Enum(s)";
  }
}
