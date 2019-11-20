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

    /** initialize list of items from instance **/
    for (int i = 0; i < n; i++) {
      items.add(new Item(i, instance.getWeight(i), instance.getValue(i)));
    }

    /** sort items by ratio **/
    Collections.sort(items);

    /** initialize root and best node and compute upperBound of root node **/
    Node best = new Node();
    Node root = new Node();
    root.computeBound();

    /** strategy: breadth-first-search iterative **/
    Queue<Node> q = new PriorityQueue<>();
    /** start with root/most valuable item **/
    q.offer(root);

    /** visit every "interesting" node **/
    while (!q.isEmpty()) {
      /** poll next node **/
      Node node = q.poll();

      /** check for more profit and max level not reached **/
      if (node.bound > best.value && node.h < items.size() - 1) {

        /** build with from parent node and add item weight from next level node **/
        Node with = new Node(node);
        Item item = items.get(node.h);
        with.weight += item.weight;

        /** check if any capacity in knapsack left **/
        if (with.weight <= instance.getCapacity()) {

          /** update with node and compute upperBound **/
          with.taken.add(items.get(node.h));
          with.value += item.value;
          with.computeBound();

          if (with.value > best.value) {
            best = with;
          }
          /** put with in queue for further investigation **/
          if (with.bound > best.value) {
            q.offer(with);
          }
        }

        /** initialize without node and compute upperBound **/
        Node without = new Node(node);
        without.computeBound();

        /** put without in queue for further investigation **/
        if (without.bound > best.value) {
          q.offer(without);
        }
      }
    }

    /** put best result in solution **/
    for (int i = 0; i < best.taken.size(); i++) {
      solution.set(best.taken.get(i).label, 1);
    }

    return solution;
  }

  @Override
  public String getName() {
    return "BB(s)";
  }

  /**
   * Node with level h, list of taken items, upperBound and value/weight of current node
   */
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

    /**
     * compute upper Bound
     */
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

  /**
   * Item with label, weight, value and ratio=value/weight
   */
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
