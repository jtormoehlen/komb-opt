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
     * solves the knapsack problem based on tabuSearch with
     * given stopCriterion (time or steps) and attribute
     * (i.e. best or first fit);
     * solution is found by constantly add the next most
     * profitable item to the knapsack when possible (add that item
     * to tabuList after) or remove the least profitable item from
     * knapsack when possible (and add that item to tabuList after);
     * in each iteration update the tabuList based on a cooldown
     * parameter (i.e. number of iterations) that determines the
     * duration the item remains in tabuList
     * @param instance knapsack instance
     * @param stopCriterion time or steps
     * @param bestFit best or first fit
     * @return solution
     */
    public Solution solve(Instance instance, int stopCriterion, boolean bestFit) {
        if (false) {
            throw new UnsupportedOperationException();
        }

        int duration = 3;
        int[] s = generateRandomStartSolution(instance.getSize(), instance);
        int[] sStar = Arrays.copyOf(s, s.length);
        int cStar = new Solution(arrayToSolution(s, instance)).getValue();
        List<Item> tabuList = new ArrayList<>();

        long c0, cMax;
        if (stopCriterion == 0) {
            c0 = System.currentTimeMillis();
            cMax = c0 + 2000;
        } else {
            c0 = 0;
            cMax = 10000;
        }

        while (c0 <= cMax) {
            int added = add(s, tabuList, instance);
            int cleared = clear(s, tabuList, instance);

            if (added > -1) {
                s[added] = 1;
                tabuList.add(new Item(added, duration));
            } else if (cleared > -1) {
                s[cleared] = 0;
                tabuList.add(new Item(cleared, duration));
            }

            int cTmp = new Solution(arrayToSolution(s, instance)).getValue();
            if (cTmp >= cStar) {
                sStar = Arrays.copyOf(s, s.length);
                cStar = cTmp;
            }

            updateList(tabuList);

            if (stopCriterion == 0) {
                c0 = System.currentTimeMillis();
            } else {
                c0++;
            }
        }

        Solution solution = new Solution(arrayToSolution(sStar, instance));
        return solution;
    }

    /**
     *
     * @param instance The given knapsack instance
     * @return solution
     */
    @Override
    public Solution solve(Instance instance) {
        return solve(instance, 0, true);
    }

    @Override
    public String getName() {
        return "Tabu(s)";
    }

    /**
     * search the most valuable of all unselected items
     * that are not tabu and return it
     * @param x solution vector
     * @param tabuList TabuList
     * @param instance Instance
     * @return result
     */
    private static int add(int[] x, List<Item> tabuList, Instance instance) {
        int bestItem = -1;
        int bestValue = 0;

        for (int i = 0; i < x.length; i++) {
            if (x[i] == 0) {
                if (!isTabu(tabuList, i)) {
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
                        bestItem = i;
                        bestValue = c;
                    }
                }
            }
        }

        return bestItem;
    }

    /**
     * search the least valuable of all selected items
     * that are not tabu and return it
     * @param x solution vector
     * @param tabuList TabuList
     * @param instance Instance
     * @return result
     */
    private static int clear(int[] x, List<Item> tabuList, Instance instance) {
        int worstItem = -1;
        int worstValue = Integer.MAX_VALUE;

        for (int i = 0; i < x.length; i++) {
            if (x[i] == 1) {
                if (!isTabu(tabuList, i)) {

                    for (int j = 0; j < x.length; j++) {
                        if (x[j] == 1) {
                            if (instance.getValue(j) <= worstValue) {
                                worstItem = j;
                                worstValue = instance.getValue(j);
                            }
                        }
                    }
                }
            }
        }

        return worstItem;
    }

    /**
     * updates the TabuList:
     * delete items with duration zero and
     * reduce duration of all items by one
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
     * @param tabuList TabuList
     * @param item Item
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

    /**
     * prints solution vector x to console
     * @param x solution vector
     */
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
                x[i] = random.nextInt(1);
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
     * Item Object with Identity and duration
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
