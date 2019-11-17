package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.*;

/**
 * A branch-and-bound algorithm for the binary knapsack problem.
 *
 * @author jtormoehlen
 */
public class BranchAndBound implements SolverInterface<Solution> {

  List<Item> items;
  int capacity;

  @Override
  public Solution solve(Instance instance) {

    Solution solution = new Solution(instance);

    items = new ArrayList<>();
    capacity = instance.getCapacity();
    int n = instance.getSize();

    for (int i = 0; i < n; i++) {
      items.add(new Item(i, instance.getWeight(i), instance.getValue(i)));
    }

    Collections.sort(items);

    Node best = new Node();
    Node root = new Node();
    root.computeBound();

    Queue<Node> q = new PriorityQueue<>();
    q.offer(root);

    while (!q.isEmpty()) {
      Node node = q.poll();

      if (node.bound > best.value && node.h < items.size() - 1) {

        Node with = new Node(node);
        Item item = items.get(node.h);
        with.weight += item.weight;

        if (with.weight <= instance.getCapacity()) {

          with.taken.add(items.get(node.h));
          with.value += item.value;
          with.computeBound();

          if (with.value > best.value) {
            best = with;
          }
          if (with.bound > best.value) {
            q.offer(with);
          }
        }

        Node without = new Node(node);
        without.computeBound();

        if (without.bound > best.value) {
          q.offer(without);
        }
      }
    }

    //System.out.println(items);

    for (int i = 0; i < best.taken.size(); i++) {
      solution.set(best.taken.get(i).label, 1);
    }

    return solution;
  }

  @Override
  public String getName() {
    return "BB(s)";
  }

  private class Node implements Comparable<Node> {

    int h;
    List<Item> taken;
    double bound, value, weight;

    public Node() {
      taken = new ArrayList<>();
    }

    public Node(Node parent) {
      h = parent.h + 1;
      taken = new ArrayList<>(parent.taken);
      bound = parent.bound;
      value = parent.value;
      weight = parent.weight;
    }

    public int compareTo(Node other) {
      return (int) (other.bound - bound);
    }

    public void computeBound() {
      int i = h;
      double w = weight;
      bound = value;
      Item item;
      do {
        item = items.get(i);
        if (w + item.weight > capacity) break;
        w += item.weight;
        bound += item.value;
        i++;
      } while (i < items.size());
      bound += (capacity - w) * (item.value / item.weight);
    }
  }

  private class Item implements Comparable<Item> {
    int label, weight, value;
    double ratio;

    public Item(int label, int weight, int value) {
      this.label = label;
      this.weight = weight;
      this.value = value;
      ratio = (double)value / (double)weight;
    }

    @Override
    public int compareTo(Item o) {
      double res = this.ratio - o.ratio;

      if (res == 0) {
        return 0;
      }
      else if (res < 0) {
        return 1;
      }
      else {
        return -1;
      }
    }

    @Override
    public String toString() {
      return label + "|w=" + weight + "|v=" + value + "|r=" + ratio;
    }
  }
}
