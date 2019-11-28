package de.uos.inf.ko.skyscrapers.solver.student;

import de.uos.inf.ko.skyscrapers.Instance;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;

/**
 * A skyscrapers solver.
 *
 * @author jtormoehlen
 */
public class CSPSolver {
  public static Instance solve(Instance instance) {
    if (true) {
      throw new UnsupportedOperationException();
    }

    int n = instance.getGamefieldSize();

    // 1. create model
    Model m = new Model("skyscrapers " + n);

    // 2. create variables
    /** gamefield matrix **/
    IntVar[][] x = m.intVarMatrix("x", n, n, 1, n);
    /** gamefield matrix transposed x^T **/
    IntVar[][] xT = m.intVarMatrix("xT", n, n, 1, n);
    /** "sight" variables for each field x(i,j) and direction **/
    BoolVar[][] vN = m.boolVarMatrix("vN", n, n);
    BoolVar[][] vE = m.boolVarMatrix("vE", n, n);
    BoolVar[][] vS = m.boolVarMatrix("vS", n, n);
    BoolVar[][] vW = m.boolVarMatrix("vW", n, n);

    // 3. add constraints
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n - 1; j++) {
        for (int k = 0; k < n; k++) {
          if (j != k) {
            /** every number x(i,j) in one line is unique **/
            m.arithm(x[i][j], "!=", x[i][k]).post();
            /** ..and in adjacent column **/
            m.arithm(x[j][i], "!=", x[k][i]).post();
          }
        }

        if (instance.getGamefield()[i][j] > 0) {
          /** bind fixated numbers f(i,j) **/
          m.arithm(x[i][j],"=", instance.getGamefield()[i][j]).post();
        }

        /** x^T is transposed matrix of x **/
        m.arithm(xT[j][i], "=", x[i][j]).post();
      }
    }

    for (int i = 0; i < n; i++) {
      /** sum of skyscrapers "in sight" from west to east equals a_W(i) with line i  **/
      if (instance.getWest()[i] > 0) {
        m.sum(vW[i], "=", instance.getWest()[i]).post();
      }

      /** ...east to west equals a_E(i) with line i  **/
      if (instance.getEast()[i] > 0) {
        m.sum(vE[i], "=", instance.getEast()[i]).post();
      }

      /** ...north to south equals a_N(j) with column j  **/
      if (instance.getNorth()[i] > 0) {
        m.sum(vN[i], "=", instance.getNorth()[i]).post();
      }

      /** ...south to north equals a_S(j) with column j  **/
      if (instance.getSouth()[i] > 0) {
        m.sum(vS[i], "=", instance.getSouth()[i]).post();
      }

      /** skyscrapers on the edge of the field always visible **/
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
          /** skyscraper  in direction v before x(i,j) must be smaller **/
          westConstraints[h] = m.arithm(x[i][h], "<", x[i][j]);
          /** from west to east view is transposed from north to south view and vise versa **/
          northConstraints[h] = m.arithm(xT[i][h], "<", xT[i][j]);
        }

        /** all skyscrapers  in direction v before x(i,j) must be smaller **/
        m.ifOnlyIf(m.arithm(vW[i][j], "=", 1), m.and(westConstraints));
        m.ifOnlyIf(m.arithm(vN[i][j], "=", 1), m.and(northConstraints));
      }
    }

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n - 1; j++) {
        Constraint[] eastConstraints = new Constraint[n - (j + 1)];
        Constraint[] southConstraints = new Constraint[n - (j + 1)];

        for (int h = j + 1; h < n; h++) {
          /** skyscraper  in direction v before x(i,j) must be smaller **/
          eastConstraints[h - (j + 1)] = m.arithm(x[i][h], "<", x[i][j]);
          /** from east to west view is transposed from south to north view and vise versa **/
          southConstraints[h - (j + 1)] = m.arithm(xT[i][h], "<", xT[i][j]);
        }

        /** all skyscrapers  in direction v before x(i,j) must be smaller **/
        m.ifOnlyIf(m.arithm(vE[i][j], "=", 1), m.and(eastConstraints));
        m.ifOnlyIf(m.arithm(vS[i][j], "=", 1), m.and(southConstraints));
      }
    }

    // 4. get solver and solve model
    Solver s = m.getSolver();
    List<Solution> solutions = s.findAllSolutions();

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

  /**
   * for test purpose
   * @param array of constraints
   */
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

  /**
   * for test purpose
   * @param varMat gamefield
   * @param s solution as string
   */
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
