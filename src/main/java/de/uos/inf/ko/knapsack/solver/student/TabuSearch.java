package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Solver for the binary knapsack problem based on tabu search.
 *
 * @author
 */
public class TabuSearch implements SolverInterface<Solution> {

  @Override
  public Solution solve(Instance instance) {
    if (false) {
      throw new UnsupportedOperationException();
    }

    List<Integer> tabuList = new ArrayList<>();

    int[] x = generateRandomStartSolution(instance.getSize(), instance);
    for (int i = 0; i < instance.getSize(); i++) {
      if (i % 3 == 0) tabuList.add(new Integer(i));
    }
    printSolution(x);
    System.out.println(add(x, tabuList, instance) + " add");
    System.out.println(clear(x, tabuList, instance) + " clear");

    Solution solution = new Solution(arrayToSolution(x, instance));
    return solution;
  }

  @Override
  public String getName() {
    return "Tabu(s)";
  }

  private static int add(int[] x, List<Integer> tabuList, Instance instance) {
    int bestItem = -1;
    int bestValue = 0;
    int bestWeight = Integer.MAX_VALUE;

    for (int i = 0; i < x.length; i++) {
      if (x[i] == 0) {
        if(!isTabu(tabuList, i)) {
          int w = 0;
          int c = 0;

          for (int j = 0; j < x.length; j++) {
            if (x[j] == 1) {
              w += instance.getWeight(j);
              c += instance.getValue(j);
            }
          }

          w += instance.getWeight(i);
          c += instance.getValue(i);

          if (w <= instance.getCapacity() && c >= bestValue) {

            if (w <= bestWeight) {
              bestItem = i;
              bestValue = c;
              bestWeight = w;
            }
          }
        }
      }
    }

    return bestItem;
  }

  private static boolean isTabu(List<Integer> tabuList, int item) {
    for (Integer iterator : tabuList) {
      if (iterator.intValue() == item) {
        return true;
      }
    }

    return false;
  }

  private static int clear(int[] x, List<Integer> tabuList, Instance instance) {
    int worstItem = -1;
    int worstWeight = Integer.MAX_VALUE;

    for (int i = 0; i < x.length; i++) {
      if (x[i] == 1) {
        if (!isTabu(tabuList, i)) {
          for (int j = 0; j < x.length; j++) {
            if (instance.getValue(j) <= instance.getValue(i)) {

              if (instance.getWeight(j) < worstWeight) {
                worstItem = j;
              }
            }
          }
        }
      }
    }

    return worstItem;
  }

  private static void printSolution(int[] x) {
    String s = "";

    for (int i = 0; i < x.length; i++) {
      s += x[i] + "$";
    }

    System.out.println(s);
  }

  /**
   * generate a random start solution
   *
   * @param n array size
   * @return start solution array
   */
  private static int[] generateRandomStartSolution(int n, Instance instance) {
    int[] x = new int[n];
    Random random = new Random();

    do {
      for (int i = 0; i < n; i++) {
        x[i] = random.nextInt(2);
      }
    } while (!arrayToSolution(x, instance).isFeasible());

    return x;
  }

  /**
   * copy an array to a solution object
   *
   * @param x        array to copy
   * @param instance related instance to array
   * @return solution
   */
  private static Solution arrayToSolution(int[] x, Instance instance) {
    Solution solution = new Solution(instance);

    for (int i = 0; i < x.length; i++) {
      solution.set(i, x[i]);
    }

    return solution;
  }
}
