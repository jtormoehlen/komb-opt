package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.Arrays;
import java.util.Random;

/**
 * A solver for the binary knapsack problem based on simulated annealing.
 *
 * @author jtormoehlen
 */
public class SimulatedAnnealing implements SolverInterface<Solution> {

  @Override
  public Solution solve(Instance instance) {
//    GreedyHeuristic greedyHeuristic = new GreedyHeuristic();
//    Solution greedySolution = greedyHeuristic.solve(instance);

    int n = instance.getSize();

    int t_0 = 100;
    int c_max = 200000;
    double alpha = .599d;

    int t = t_0;
    int c = 0;
    int[] x = generateRandomStartSolution(n);
    int currW = 0;
    int[] bestX = Arrays.copyOf(x, n);

    while (c <= c_max) {
      Random random = new Random();
      int j = random.nextInt(n - 1);

      int[] y = Arrays.copyOf(x, n);
      y[j] = 1 - x[j];

      if (!((y[j] == 1) && (currW + instance.getWeight(j) > instance.getCapacity()))) {
        if (y[j] == 1) {
          x = Arrays.copyOf(y, n);
          currW += instance.getWeight(j);

          Solution tmpSol = arrayToSolution(x, instance);
          Solution bestSol = arrayToSolution(bestX, instance);
          if (tmpSol.getValue().intValue() > bestSol.getValue().intValue()) {
            bestX = Arrays.copyOf(x, n);
          } else {
            double r = random.nextDouble();

            if (r < Math.exp(-instance.getValue(j)) / (double)t) {
              x = Arrays.copyOf(y, n);
              currW -= instance.getWeight(j);
            }
          }
        }
      }

      c++;
      t *= alpha;
    }

    Solution optimal = arrayToSolution(bestX, instance);

    System.out.println("$" + optimal.getValue() + "$");

    return optimal;
  }

  private static int[] generateRandomStartSolution(int n) {
    int[] x = new int[n];
    Random random = new Random();

    for (int i = 0; i < n; i++) {
      x[i] = random.nextInt(1);
    }

    return x;
  }

  private static Solution arrayToSolution(int[] x, Instance instance) {
    Solution solution = new Solution(instance);

    for (int i = 0; i < x.length; i++) {
      solution.set(i, x[i]);
    }

    return solution;
  }

  @Override
  public String getName() {
    return "SA(s)";
  }
}
