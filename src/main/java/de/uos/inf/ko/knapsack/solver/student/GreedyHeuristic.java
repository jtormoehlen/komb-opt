package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A sorting-based heuristic for the binary knapsack problem.
 *
 * @author jtormoehlen
 */
public class GreedyHeuristic implements SolverInterface<Solution> {

    @Override
    public Solution solve(Instance instance) {
        List<Item> itemList = new ArrayList<>();
        Solution solution = new Solution(instance);

        /** store every object as item with identifier and ratio in the list **/
        for (int i = 0; i < instance.getSize(); i++) {
            double ratio = (instance.getWeight(i) / (double) instance.getValue(i));
            itemList.add(new Item(i, ratio));
        }

        /** sort the list by ratio starting with lowest **/
        Collections.sort(itemList);

        /** initialize solution vector and index **/
        int[] x = new int[instance.getSize()];
        int j = 0;

        /** as long as capacity not reached bind another item **/
        while (listSumTo(itemList, j, instance) <= instance.getCapacity()) {
            x[j] = 1;
            j++;
        }

        /** check if capacity allows any other objects with worse ratio **/
        while (j != instance.getSize()) {
            if ((instance.getCapacity() - listSumTo(itemList, j, instance)) >= instance.getWeight(itemList.get(j).item)) {
                x[j] = 1;
            }

            j++;
        }

        /** bind solution vector to FractionalSolution Object **/
        for (int i = 0; i < x.length; i++) {
            solution.set(itemList.get(i).item, x[i]);
        }

        return solution;
    }

    /**
     * weight sum of all items in the list
     * @param itemList items
     * @param to upper limit of the sum
     * @param instance initial instance
     * @return result as integer
     */
    private static int listSumTo(List<Item> itemList, int to, Instance instance) {
        int result = 0;

        for (int i = 0; i <= to; i++) {
            result += instance.getWeight(itemList.get(i).item);
        }

        return result;
    }

    @Override
    public String getName() {
        return "Greedy(s)";
    }

    /**
     * Item with identifier and ratio of w_i/c_i
     */
    private class Item implements Comparable<Item> {
        double ratio;
        int item;

        public Item(int item, double ratio) {
            this.item = item;
            this.ratio = ratio;
        }

        @Override
        public int compareTo(Item item) {
            double dif = this.ratio - item.ratio;

            if (dif == 0.0d) return 0;
            else if (dif > 0.0d) return 1;
            else return -1;
        }
    }
}
