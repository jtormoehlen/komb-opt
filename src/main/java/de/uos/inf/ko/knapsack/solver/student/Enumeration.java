package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

/**
 * A full enumeration algorithm for the binary knapsack problem.
 * 
 * @author jtormoehlen, luhaupt
 */
public class Enumeration implements SolverInterface<Solution> {

  /**
   * generate all binary numbers from 0 to 2^n
   * and apply each to a knapsack instance;
   * pick the optimal solution such that:
   * sum(weight_i) <= W for all i=1,...,n
   * and max value
   * @param instance The given knapsack instance
   * @return optimal solution
   */
  @Override
  public Solution solve(Instance instance) {

    int n = instance.getSize();

    Solution solution = new Solution(instance);
    Solution optimal = solution;

    //convert all integers from 0 to 2^n...
    for (int i = 0; i < Math.pow(2, n); i++) {

      // ...to binary numbers
      String binaryString = Integer.toBinaryString(i);

      solution = binaryStringToSolution(binaryString, instance);

      if (solution.isFeasible()) {

        //update optimal solution if possible
        if ((solution.getValue() > optimal.getValue())) {
          optimal = binaryStringToSolution(binaryString, instance);
        }
      }
    }

    //print weight and value of valid solution
    System.out.println("Weight=" + solution.getWeight() + "|" + "Value=" + solution.getValue());

    return optimal;
  }

  /**
   * apply a binary number (0,1) represented as string
   * to a knapsack instance
   * @param binaryString binary number as string
   * @param instance given knapsack instance
   * @return solution with (0,1) quantities for each item
   */
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

  /**
   * for test purpose; please ignore
   * @param w
   * @param v
   * @param n
   * @param W
   * @return
   */
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
