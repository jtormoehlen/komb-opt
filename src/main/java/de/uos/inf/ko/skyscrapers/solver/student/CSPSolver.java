package de.uos.inf.ko.skyscrapers.solver.student;

import de.uos.inf.ko.skyscrapers.Instance;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * A skyscrapers solver.
 *
 * @author
 */
public class CSPSolver {
  public static Instance solve(Instance instance) {
    if (false) {
      throw new UnsupportedOperationException();
    }

    int n = instance.getGamefieldSize();
    int[] west = instance.getWest();

    // 1. create model
    Model m = new Model("skyscrapers " + n);

    // 2. create varaibles
    IntVar x[][] = m.intVarMatrix("x", n, n, 1, n);
//    IntVar vN[][] = m.boolVarMatrix("vN", n, n);
//    IntVar vO[][] = m.boolVarMatrix("vO", n, n);
//    IntVar vS[][] = m.boolVarMatrix("vS", n, n);
    IntVar vW[][] = m.boolVarMatrix("vW", n, n);
    IntVar[] lines = m.intVarArray("lines", n, 1, n);


    // 3. add constraints
    int tmp = 0;
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

        m.sum(vW[i], "=", lines[i]).post();
      }
    }

    for (int i = 0; i < n; i++) {
      m.arithm(lines[i], "=", instance.getWest()[i]).post();
      m.arithm(vW[i][1], "=", 1).post();
    }

    // 4. get solver and solve model
    Solver s = m.getSolver();
    List<Solution> solutions = new ArrayList<>();
    solutions.add(s.findSolution());
    System.out.println(m.getVar(lines[0].getValue()) + "$");

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
}
