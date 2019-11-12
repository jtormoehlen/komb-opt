package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.FractionalSolution;
import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An optimal greedy solver for the fractional knapsack problem.
 *
 * @author jtormoehlen
 */
public class FractionalSolver implements SolverInterface<FractionalSolution> {

    @Override
    public FractionalSolution solve(Instance instance) {
        List<Item> itemList = new ArrayList<>();
        FractionalSolution fractionalSolution = new FractionalSolution(instance);

        for (int i = 0; i < instance.getSize(); i++) {
            double ratio = (instance.getWeight(i) / (double) instance.getValue(i));
            itemList.add(new Item(i, ratio));
        }

        Collections.sort(itemList);
        //System.out.println(printList(itemList));

        double[] x = new double[instance.getSize()];
        int j = 0;

        while (listSumTo(itemList, j, instance) <= instance.getCapacity()) {
            x[j] = 1.0d;
            j++;
        }

        x[j] = ((double) instance.getCapacity() - listSumTo(itemList, j - 1, instance)) / (double) (instance.getWeight(itemList.get(j).item));

        for (int i = 0; i < x.length; i++) {
            fractionalSolution.set(itemList.get(i).item, x[i]);
        }

        //System.out.println(listToString(itemList, instance));

        return fractionalSolution;
    }

    private static int listSumTo(List<Item> itemList, int to, Instance instance) {
        int result = 0;

        for (int i = 0; i <= to; i++) {
            result += instance.getWeight(itemList.get(i).item);
        }

        return result;
    }

    private static String listToString(List<Item> list, Instance instance) {
        String result = "";

        for (int i = 0; i < list.size(); i++) {
            result += list.get(i).item + "|r=" + list.get(i).ratio + "|w=" + instance.getWeight(list.get(i).item) + "|v=" + instance.getValue(list.get(i).item) + "\n";
        }

        return result;
    }

    @Override
    public String getName() {
        return "Fractional(s)";
    }

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
