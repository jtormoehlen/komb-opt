package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A solver for the binary knapsack problem based on simulated annealing.
 *
 * @author jtormoehlen
 */
public class SimulatedAnnealing implements SolverInterface<Solution> {

    /**
     * solves knapsack problem via simulated annealing
     * [physical (exponential) temperature decrease of
     * heated metal]
     * depending on various parameters like start
     * temperature, temperature decreasing rate,
     * max loop counter and starting solution
     * @param instance instance to solve
     * @param start starting solution i.e. random
     * @param t_0 starting temperature
     * @param c_max max loop count
     * @param alpha temperature decreasing coefficient
     * @return solution
     */
    public Solution solve(Instance instance, int[] start, int t_0, int c_max, double alpha) {

        int n = instance.getSize();
        int t = t_0;                                    //start temp
        int c = 0;                                      //loop count
        int[] x = Arrays.copyOf(start, n);              //start solution
        int currW = 0;                                  //current weight
        int[] bestX = Arrays.copyOf(x, n);              //best solution

        /** count to max **/
        while (c <= c_max) {
            /** generate random index **/
            Random random = new Random();
            int j = random.nextInt(n - 1);

            /** swap the corresponding bit **/
            int[] y = Arrays.copyOf(x, n);
            y[j] = 1 - x[j];

            /** check for left capacity in knapsack **/
            if (!((y[j] == 1) && (currW + instance.getWeight(j) > instance.getCapacity()))) {

                /** move to solution y... **/
                if (y[j] == 1) {

                    x = Arrays.copyOf(y, n);
                    currW += instance.getWeight(j);

                    /** update best solution if possible **/
                    Solution tmpSol = arrayToSolution(x, instance);
                    Solution bestSol = arrayToSolution(bestX, instance);
                    if (tmpSol.getValue().intValue() > bestSol.getValue().intValue()) {
                        bestX = Arrays.copyOf(x, n);
                    }

                } else {

                    /** ...or move to y with probability exp((value(y)-values(x))/T) **/
                    double r = random.nextDouble();

                    if (r < Math.exp(-instance.getValue(j)) / (double) t) {
                        x = Arrays.copyOf(y, n);
                        currW -= instance.getWeight(j);
                    }
                }
            }

            /** update counter and temp **/
            c++;
            t *= alpha;
        }

        /** return best result **/
        Solution optimal = arrayToSolution(bestX, instance);
        return optimal;
    }

    @Override
    public Solution solve(Instance instance) {
        int t_0 = 1000;

        int c_max = 1000000;
        double alpha = .95d;
        int[] start = generateRandomStartSolution(instance.getSize());
        Solution best = new Solution(instance);
        String statistics = "";

        for (int i = 0; i < 10; i++) {

            /** alternate between alpha=5% and alpha=1% **/
            if (alpha == .95d) {
                alpha = .99d;
            } else {
                alpha = .95d;
            }

            /** half counter every second loop count **/
            if (i % 2 == 0) {
                c_max /= 10;
            }

            /** solve with current parameter setup **/
            Solution next = solve(instance, start, t_0, c_max, alpha);

            if (next.getValue() > best.getValue()) {
                best = new Solution(next);
            }

            /** build some statistics **/
            statistics = extendStatistics(statistics, t_0, c_max, alpha, next);
        }

        /** print statistics to console **/
        System.out.println(statistics);

        /** return best result **/
        return best;
    }

    /**
     * append statistics about parameters and
     * corresponding values to a string
     * @param s string
     * @param t_0 start temperature
     * @param c_max max count
     * @param alpha alpha coefficient for temperature fall
     * @param solution current solution
     * @return extended string
     */
    private static String extendStatistics(String s, int t_0, int c_max, double alpha, Solution solution) {
        String res = "";

        if (s == "") {
            s += "size\t\tt_0\t\tc_max\t\talpha\t\tvalue\n";
        }

        res += s + solution.getInstance().getSize() + "\t\t" + t_0 + "\t\t" + c_max + "\t\t" + alpha + "\t\t" + solution.getValue() + "\n";

        return res;
    }

    /**
     * generate a random start solution
     * @param n array size
     * @return start solution array
     */
    private static int[] generateRandomStartSolution(int n) {
        int[] x = new int[n];
        Random random = new Random();

        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(1);
        }

        return x;
    }

    /**
     * copy an array to a solution object
     * @param x array to copy
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

    @Override
    public String getName() {
        return "SA(s)";
    }
}
