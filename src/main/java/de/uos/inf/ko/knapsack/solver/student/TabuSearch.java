package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Solver for the binary knapsack problem based on tabu search.
 *
 * @author jtormoehlen
 */
public class TabuSearch implements SolverInterface<Solution> {

    /**
     * @param instance The given knapsack instance
     * @return solution
     */
    @Override
    public Solution solve(Instance instance) {
        long t_0 = System.currentTimeMillis();
        Solution timeSolution = solve(instance, 1, false);
        long t_1 = System.currentTimeMillis();
        Solution iterationSolution = solve(instance, 0, true);
        long t_2 = System.currentTimeMillis();

        printStatistics(timeSolution, t_1 - t_0, "feasible allowed");
        printStatistics(iterationSolution, t_2 - t_1, "infeasible allowed");

        return timeSolution.getValue() >= iterationSolution.getValue() ? timeSolution : iterationSolution;
    }

    @Override
    public String getName() {
        return "Tabu(s)";
    }

    /**
     * solves the knapsack problem based on TabuSearch with
     * given stopCriterion (specific runtime or iterations) and attribute
     * (i.e. save whole solution x, save best/worst items,
     * allow infeasible solutions);
     * solution is found by constantly add the next most
     * profitable item to the knapsack when possible (add that item
     * to TabuList after) or remove the least profitable item from
     * knapsack when possible (and add that item to tabuList after);
     * in each iteration update the TabuList based on a cooldown
     * parameter (i.e. number of iterations) that determines the
     * duration the item remains in TabuList
     *
     * @param instance      knapsack instance
     * @param stopCriterion runtime or iterations {0,1}
     * @param all           dont deny feasible solutions
     * @return solution
     */
    private Solution solve(Instance instance, int stopCriterion, boolean all) {
        if (false) {
            throw new UnsupportedOperationException();
        }

        int[] s = generateRandomStartSolution(instance.getSize(), instance);
        int[] sStar = Arrays.copyOf(s, s.length);
        int cStar = new Solution(arrayToSolution(s, instance)).getValue();
        List<Item> tabuList = new ArrayList<>();

        /** parameters: cooldown, time interval in ms, maximal iterations **/
        int duration = 5;
        long timeInterval = 1000;
        long maxIterations = 100000;

        /** TabuSearch stop criterion: time or iterations **/
        long c0, cMax;
        if (stopCriterion == 0) {
            c0 = System.currentTimeMillis();
            cMax = c0 + timeInterval;
        } else {
            c0 = 0;
            cMax = maxIterations;
        }

        /** main loop **/
        while (c0 <= cMax) {
            int added = add(s, tabuList, instance, all);
            int cleared = clear(s, tabuList, instance);

            /** try to add/clear most/least valuable item **/
            if (added > -1) {
                s[added] = 1;
                tabuList.add(new Item(added, duration));
            } else if (cleared > -1) {
                s[cleared] = 0;
                tabuList.add(new Item(cleared, duration));
            }

            /** update TabuList **/
            updateList(tabuList);

            /** update best solution **/
            Solution sTmp = new Solution(arrayToSolution(s, instance));
            if ((sTmp.getValue() >= cStar) && sTmp.isFeasible()) {
                sStar = Arrays.copyOf(s, s.length);
                cStar = sTmp.getValue();
            }

            if (stopCriterion == 0) {
                c0 = System.currentTimeMillis();
            } else {
                c0++;
            }
        }

        /** return result **/
        Solution solution = new Solution(arrayToSolution(sStar, instance));
        return solution;
    }

    /**
     * search the most valuable of all unselected items
     * that are not tabu and return it
     *
     * @param x        solution vector
     * @param tabuList TabuList
     * @param instance Instance
     * @return result
     */
    private static int add(int[] x, List<Item> tabuList, Instance instance, boolean all) {
        int bestItem = -1;
        int bestValue = 0;
        int bestWeight = instance.getCapacity() - new Solution(arrayToSolution(x, instance)).getWeight();

        for (int i = 0; i < x.length; i++) {
            if ((x[i] == 0) && (!isTabu(tabuList, i))) {
                if ((instance.getValue(i) > bestValue) && (instance.getWeight(i) <= bestWeight || all)) {
                    bestItem = i;
                    bestValue = instance.getValue(i);
                    bestWeight = instance.getWeight(i);
                } else if ((instance.getValue(i) == bestValue) && (instance.getWeight(i) < bestWeight || all)) {
                    bestItem = i;
                    bestValue = instance.getValue(i);
                    bestWeight = instance.getWeight(i);
                }
            }
        }

        return bestItem;
    }

    /**
     * search the least valuable of all selected items
     * that are not tabu and return it
     *
     * @param x        solution vector
     * @param tabuList TabuList
     * @param instance Instance
     * @return result
     */
    private static int clear(int[] x, List<Item> tabuList, Instance instance) {
        int worstItem = -1;
        int worstValue = Integer.MAX_VALUE;
        int worstWeight = 0;

        for (int i = 0; i < x.length; i++) {
            if ((x[i] == 1) && (!isTabu(tabuList, i))) {
                if (instance.getValue(i) < worstValue) {
                    worstItem = i;
                    worstValue = instance.getValue(i);
                    worstWeight = instance.getWeight(i);
                } else if ((instance.getValue(i) == worstValue) && (instance.getWeight(i) > worstWeight)) {
                    worstItem = i;
                    worstValue = instance.getValue(i);
                    worstWeight = instance.getWeight(i);
                }
            }
        }

        return worstItem;
    }

    /**
     * updates the TabuList:
     * delete items with duration zero and
     * reduce duration of all items by one
     *
     * @param tabuList
     * @return
     */
    private static List<Item> updateList(List<Item> tabuList) {
        List<Item> result = tabuList;

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).duration == 0) {
                result.remove(i);
            }

            result.get(i).duration--;
        }

        return result;
    }

    /**
     * returns if the tabuList contains an item
     *
     * @param tabuList TabuList
     * @param item     Item
     * @return result as boolean
     */
    private static boolean isTabu(List<Item> tabuList, int item) {
        for (Item iterator : tabuList) {
            if (iterator.identity == item) {
                return true;
            }
        }

        return false;
    }

    private static void printStatistics(Solution solution, long time, String description) {
        String s = "";

        s += "TabuSearch knapsack: " + solution.getInstance().getSize() + "\n";
        s += description + "\n";
        s += "tot_value\tdelta_time\n";
        s += solution.getValue() + "\t\t" + time + "ms\n";

        System.out.println(s);
    }

    /**
     * generate a random start solution
     * bound elem {1,2}: 1 for zeros and
     * 2 for random elements between zero and one
     * attention: may result in high computing time!
     *
     * @param n array size
     * @return start solution array
     */
    private static int[] generateRandomStartSolution(int n, Instance instance) {
        int bound = 1;
        int[] x = new int[n];
        Random random = new Random();

        do {
            for (int i = 0; i < n; i++) {
                x[i] = random.nextInt(bound);
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

    /**
     * Item Object with Identity: i elem {0,...,n-1}
     * and duration (number of iterations
     * item remains in TabuList)
     */
    private class Item {
        int identity;
        int duration;

        public Item(int identity, int duration) {
            this.identity = identity;
            this.duration = duration;
        }
    }
}
