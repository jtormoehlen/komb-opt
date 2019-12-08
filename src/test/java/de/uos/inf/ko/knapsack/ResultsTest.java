package de.uos.inf.ko.knapsack;

import de.uos.inf.ko.knapsack.reader.Reader;
import de.uos.inf.ko.knapsack.solver.student.*;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;

public class ResultsTest {

  public final static String KNAPSACK_INSTANCES_PATH = "./resources/knapsack/";

  /**
   * Prints all results n a table.
   *
   * @param matrix matrix of results, first index is the column index, second the row index
   */
  private static void printMatrix(String[][] matrix, String[] header) {
    if (matrix.length != header.length) {
      throw new IllegalArgumentException("Header does not fit to matrix");
    }

    int[] columnWidth = new int[matrix.length];
    for (int i = 0; i < columnWidth.length; i++) {
      columnWidth[i] = header[i].length();
      for (int j = 0; j < matrix[i].length; j++) {
        // determine entry with most characters
        if (columnWidth[i] < matrix[i][j].length()) {
          columnWidth[i] = matrix[i][j].length();
        }
      }
    }
    StringBuilder horizontalLine = new StringBuilder();
    horizontalLine.append("+");
    for (int i = 0; i < header.length; i++) {
      // + 2 for space and the end and beginning
      for (int j = 0; j < columnWidth[i] + 2; j++) {
        horizontalLine.append("-");
      }
      horizontalLine.append("+");
    }

    StringBuilder printMatrix = new StringBuilder();
    // header
    String[] columnFormat = new String[header.length];
    for (int i = 0; i < header.length; i++) {
      columnFormat[i] = "| %" + columnWidth[i] + "s ";
      printMatrix.append(String.format(columnFormat[i], header[i]));
    }
    printMatrix.append("\n");
    printMatrix.append(horizontalLine);
    printMatrix.append("\n");
    for (int j = 0; j < matrix[0].length; j++) {
      for (int i = 0; i < matrix.length; i++) {
        printMatrix.append(String.format(columnFormat[i], matrix[i][j]));
      }
      printMatrix.append("|\n");
      printMatrix.append(horizontalLine);
      printMatrix.append("\n");
    }

    System.out.print(printMatrix.toString());
  }

  private final static int SOLVER_NAME = 0;
  private final static int FILE_NAME = 1;
  private final static int ITEM_NUMBER = 2;
  private final static int CAPACITY = 3;
  private final static int SOLUTION_VALUE = 4;
  private final static int SOLUTION_TIME = 5;

  public static <SolutionType extends GenericSolution<?>> void runSolver(SolverInterface<SolutionType> solver, Instance instance,
      ArrayList<String>[] valueMatrix, boolean binary) {
    // solve it
    long start = System.currentTimeMillis();

    SolutionType solution;
    try {
      solution = solver.solve(instance);
    } catch (UnsupportedOperationException ex) {
      solution = null;
    }

    long end = System.currentTimeMillis();

    // insert all values in the matrix
    valueMatrix[SOLVER_NAME].add(solver.getName());
    valueMatrix[FILE_NAME].add(instance.getFilename());
    valueMatrix[ITEM_NUMBER].add(String.valueOf(instance.getSize()));
    valueMatrix[CAPACITY].add(String.valueOf(instance.getCapacity()));
    valueMatrix[SOLUTION_VALUE].add(solution == null ? "-" : solution.getValue().toString());
    valueMatrix[SOLUTION_TIME].add(String.format("%.3fs", (end - start) / 1000.0));
    final int index = valueMatrix[SOLVER_NAME].size() - 1;

    System.out.println("=== " + valueMatrix[SOLVER_NAME].get(index) + " ===");
    if (instance.getSize() <= 60) {
      System.out.println("solution = " + solution);
    }
    System.out.println("value = " + valueMatrix[SOLUTION_VALUE].get(index));
    System.out.println("time = " + valueMatrix[SOLUTION_TIME].get(index));
    if (solution != null) {
      assert solution.getInstance() == instance : "Solution is for another instance!";
      assert solution.isFeasible() : "Solution is not feasible!";
      if (binary) {
        assert solution.isBinary() : "Solution is not binary!";
      }
    }
  }

  public static <SolutionType extends GenericSolution<?>> void runSolver(SolverInterface<SolutionType> solver, Instance instance,
      ArrayList<String>[] valueMatrix) {
    runSolver(solver, instance, valueMatrix, true);
  }

  @Test
  public void testMain() throws IOException {
    final int FRST_ENTRY = 0;
    final int SECD_ENTRY = 1;
    int[][] instances = new int[][] {{10, 1}, {15, 1}, {20, 2}, {30, 1}, {40, 1}, {50, 1}, {60, 1}, {100, 2}, {500, 1}, {1000, 1}//, {5000,2}, {10000,5}
    };

    int index = 0;
    final int COLUMN_NUMER = 6;
    ArrayList<String>[] valueMatrix = new ArrayList[COLUMN_NUMER];
    for (int i = 0; i < valueMatrix.length; i++) {
      valueMatrix[i] = new ArrayList<>(20);
    }

    for (int i = 0; i < instances.length; i++) {
      for (int j = 1; j <= instances[i][SECD_ENTRY]; j++) {
        final String filename = "rucksack" + String.format("%05d", instances[i][FRST_ENTRY]) + "-" + j + ".txt";
        final Instance instance = Reader.readInstance(KNAPSACK_INSTANCES_PATH + filename);
        instance.setFilename(filename);
        System.out.println("# Instance file: " + filename);
        System.out.println("# Number of items: " + instance.getSize());
        System.out.println("# Capacity of knapsack: " + instance.getCapacity());

        if (instance.getSize() <= 20) {
          //runSolver(new Enumeration(), instance, valueMatrix);
          //runSolver(new ConstraintProgramming(), instance, valueMatrix);
          //runSolver(new ConstraintProgrammingDirect(), instance, valueMatrix);
        }

        if (instance.getSize() <= 40) {
          //runSolver(new BranchAndBound(), instance, valueMatrix);
          //runSolver(new TabuSearch(), instance, valueMatrix);
        }

        if (instance.getSize() <= 50) {
          runSolver(new BranchAndBound(), instance, valueMatrix);
          runSolver(new TabuSearch(), instance, valueMatrix);
        }

        if (instance.getSize() <= 100) {
          //runSolver(new FractionalSolver(), instance, valueMatrix);
          //runSolver(new GreedyHeuristic(), instance, valueMatrix);
          //runSolver(new BranchAndBound(), instance, valueMatrix);
          //runSolver(new SimulatedAnnealing(), instance, valueMatrix);
          //runSolver(new TabuSearch(), instance, valueMatrix);
        }
      }
    }

    String[] header = new String[] {"solver name", "filename", "#items", "capacity", "value", "time"};
    String[][] matrix = new String[valueMatrix.length][valueMatrix[0].size()];
    for (int i = 0; i < valueMatrix.length; i++) {
      matrix[i] = valueMatrix[i].toArray(new String[0]);
    }
    printMatrix(matrix, header);
  }
}
