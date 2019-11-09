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
      double ratio = (instance.getWeight(i) / (double)instance.getValue(i));
      itemList.add(new Item(i, ratio));
    }

    Collections.sort(itemList);

    double[] x = new double[instance.getSize()];
    int j = 0;

    while (listSumTo(itemList, j, instance) <= instance.getCapacity()) {
      x[j] = 1.0d;
      j++;
    }

    x[j] = ((double)instance.getCapacity() - listSumTo(itemList, j - 1, instance)) / (double)(instance.getWeight(j));

    for (int i = 0; i < x.length; i++) {
      fractionalSolution.set(itemList.get(i).item, x[i]);
    }

    return fractionalSolution;
  }

  private static int listSumTo(List<Item> itemList, int to, Instance instance) {
    int result = 0;

    for (int i = 0; i <= to; i++) {
      result += instance.getWeight(itemList.get(i).item);
    }

    return result;
  }

  private static String printList(List<Item> list) {
    String result = "";

    for (int i = 0; i < list.size(); i++) {
      result += list.get(i).item + "|" + list.get(i).ratio + "\n";
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
      return (int)(this.ratio - item.ratio);
    }
  }
}
