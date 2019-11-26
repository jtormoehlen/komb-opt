package de.uos.inf.ko.skyscrapers.solver.student;

import de.uos.inf.ko.skyscrapers.Instance;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * A skyscrapers solver.
 *
 * @author jtormoehlen
 */
public class CSPSolver {
  public static Instance solve(Instance instance) {
    if (false) {
      throw new UnsupportedOperationException();
    }

    int n = instance.getGamefieldSize();

    // 1. create model
    Model m = new Model("skyscrapers " + n);

    // 2. create variables
    IntVar[][] x = m.intVarMatrix("x", n, n, 1, n);
    IntVar[][] xT = m.intVarMatrix("xT", n, n, 1, n);
    BoolVar[][] vN = m.boolVarMatrix("vN", n, n);
    BoolVar[][] vE = m.boolVarMatrix("vE", n, n);
    BoolVar[][] vS = m.boolVarMatrix("vS", n, n);
    BoolVar[][] vW = m.boolVarMatrix("vW", n, n);


    // 3. add constraints
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n - 1; j++) {
        for (int k = 0; k < n; k++) {
          if (j != k) {
            m.arithm(x[i][j], "!=", x[i][k]).post();
            m.arithm(x[j][i], "!=", x[k][i]).post();
          }
        }

        if (instance.getGamefield()[i][j] > 0) {
          m.arithm(x[i][j],"=", instance.getGamefield()[i][j]).post();
        }

        m.arithm(xT[j][i], "=", x[i][j]).post();
      }
    }

    for (int i = 0; i < n; i++) {
      m.sum(vW[i], "=", instance.getWest()[i]).post();
      m.sum(vE[i], "=", instance.getEast()[i]).post();
      m.sum(vN[i], "=", instance.getNorth()[i]).post();
      m.sum(vS[i], "=", instance.getSouth()[i]).post();
      m.arithm(vW[i][0], "=", 1).post();
      m.arithm(vE[i][n - 1], "=", 1).post();
      m.arithm(vN[i][0], "=", 1).post();
      m.arithm(vS[i][n - 1], "=", 1).post();
    }

    for (int i = 0; i < n; i++) {
      for (int j = 1; j < n; j++) {
        Constraint[] westConstraints = new Constraint[j];
        Constraint[] northConstraints = new Constraint[j];

        for (int h = 0; h < j; h++) {
          westConstraints[h] = m.arithm(x[i][h], "<", x[i][j]);
          northConstraints[h] = m.arithm(xT[i][h], "<", xT[i][j]);
        }

        m.ifOnlyIf(m.arithm(vW[i][j], "=", 1), m.and(westConstraints));
        m.ifOnlyIf(m.arithm(vN[i][j], "=", 1), m.and(northConstraints));
      }
    }

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n - 1; j++) {
        Constraint[] eastConstraints = new Constraint[n - (j + 1)];
        Constraint[] southConstraints = new Constraint[n - (j + 1)];

        for (int h = j + 1; h < n; h++) {
          eastConstraints[h - (j + 1)] = m.arithm(x[i][h], "<", x[i][j]);
          southConstraints[h - (j + 1)] = m.arithm(xT[i][h], "<", xT[i][j]);
        }

        m.ifOnlyIf(m.arithm(vE[i][j], "=", 1), m.and(eastConstraints));
        m.ifOnlyIf(m.arithm(vS[i][j], "=", 1), m.and(southConstraints));
      }
    }

    // 4. get solver and solve model
    Solver s = m.getSolver();
    List<Solution> solutions = new ArrayList<>();
    solutions.add(s.findSolution());

    // 5. print solutions
    int size = instance.getGamefieldSize();
    System.out.println("Number of solutions: " + solutions.size());
    int cnt = 1;
    for (Solution solution : solutions) {
      int[][] solutionArray = new int[size][size];
      for (int i = 0; i < size; ++i) {
        for (int j = 0; j < size; ++j) {
          solutionArray[i][j] = solution.getIntVal(x[i][j]);
        }
      }
      if (cnt == 1) {
        instance.setSolution(solutionArray);
      }
      System.out.println("------- solution number " + cnt + "-------");
      instance.printSolution();

      ++cnt;
    }

    return instance;
  }

  private static void checkArray(Constraint[] array) {
    String res = "";

    for (int index = 0; index < array.length; index++) {
      try {
        res += array[index].toString() + "\n";
      } catch (NullPointerException e) {
        System.out.println(index + "/" + array.length);
      }
    }

    System.out.println(res);
  }

  /**
   * for test purpose
   * @param list of constraints
   * @return array of constraints
   */
  private static Constraint[] listToArray(List<Constraint> list) {
    Constraint[] array = new Constraint[list.size()];

    int index = 0;
    for (Constraint constraint : list) {
      array[index] = constraint;
    }

    return array;
  }

  private static void printVarMat(IntVar[][] varMat, Solution s) {
    String res = "";

    for (int i = 0; i < varMat.length; i++) {
      for (int j = 0; j < varMat.length; j++) {
        res += s.getIntVal(varMat[i][j]) + " $ ";
      }

      res += "\n";
    }

    System.out.println(res);
  }
}
