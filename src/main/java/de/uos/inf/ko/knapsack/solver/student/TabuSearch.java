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
 * @author
 */
public class TabuSearch implements SolverInterface<Solution> {

    @Override
    public Solution solve(Instance instance) {
        if (false) {
            throw new UnsupportedOperationException();
        }

        int count = 10000;
        int duration = 3;
        int[] s = generateRandomStartSolution(instance.getSize(), instance);
        int[] sStar = Arrays.copyOf(s, s.length);
        int cStar = new Solution(arrayToSolution(s, instance)).getValue();
        List<Item> tabuList = new ArrayList<>();

        while (count > 0) {
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
            count--;
        }

//        printSolution(s);
//        System.out.println(add(s, tabuList, instance) + " add");
//        System.out.println(clear(s, tabuList, instance) + " clear");

        Solution solution = new Solution(arrayToSolution(sStar, instance));
        return solution;
    }

    @Override
    public String getName() {
        return "Tabu(s)";
    }

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

    private static int clear(int[] x, List<Item> tabuList, Instance instance) {
        int worstItem = -1;
        int worstValue = Integer.MAX_VALUE;
        int worstWeight = 0;

        for (int i = 0; i < x.length; i++) {
            if (x[i] == 1) {
                if (!isTabu(tabuList, i)) {

                    for (int j = 0; j < x.length; j++) {
                        if (x[j] == 1) {
                            if (instance.getValue(j) <= worstValue) { // && instance.getWeight(j) >= worstWeight
                                worstItem = j;
                                worstValue = instance.getValue(j);
                                worstWeight = instance.getWeight(j);
                            }
                        }
                    }
                }
            }
        }

        return worstItem;
    }

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

    private static boolean isTabu(List<Item> tabuList, int item) {
        for (Item iterator : tabuList) {
            if (iterator.indentity == item) {
                return true;
            }
        }

        return false;
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

    private class Item {
        int indentity;
        int duration;

        public Item(int indentity, int duration) {
            this.indentity = indentity;
            this.duration = duration;
        }
    }
}
