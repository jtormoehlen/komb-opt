package de.uos.inf.ko.tsp.solver.student;

import java.text.DecimalFormat;
import java.util.*;

import de.uos.inf.ko.tsp.Instance;

/**
 * Solver for the TSP based on the Ants algorithm
 *
 * @author jtormoehlen
 */
public class Ants {
    /**
     * Solves a given TSP instance with the Ants algorithm.
     *
     * @param instance TSP instance
     * @return TSP tour described as a list of cities
     */
    public List<Integer> solve(Instance instance) {
        final double evaporation = 0.2d;
        final double alpha = 0.1;
        final double beta = 0.4;
        final int m = 1000;
        int n = instance.getNumCities();
        double[][] pheromones = new double[n][n];

        int loopCount = 0;
        List<Integer> optimal = new ArrayList<>();
        double optimalLength = Double.POSITIVE_INFINITY;

        while (loopCount <= 10) {
            Ant[] ants = new Ant[m];
            List<Double> tourLenghts = new ArrayList<>();

            for (int k = 0; k < m; k++) {
                ants[k] = new Ant();
                Random random = new Random();
                int start = random.nextInt(n);

                List<Integer> notVisitedV = generateSetV(n, start);
                ants[k].tour.add(new Integer(start));

                for (int lambda = 1; lambda < n; lambda++) {
                    double prob = 0;
                    int j = -1;

                    for (int i = 0; i < notVisitedV.size(); i++) {
                        double p_ij = calcProbability(pheromones, instance, alpha, beta,
                                                        ants[k].getLast(), notVisitedV.get(i), notVisitedV);

                        if (p_ij >= prob) {
                            prob = p_ij;
                            j = notVisitedV.get(i);
                        }
                    }

                    if (j != -1) {
                        ants[k].tour.add(new Integer(j));
                        notVisitedV.remove(new Integer(j));
                    }
                }
            }

            for (int k = 0; k < m; k++) {
                ants[k].calcTourLength(instance);
                tourLenghts.add(new Double(ants[k].tourLength));

                if (ants[k].tourLength < optimalLength) {
                    optimalLength = ants[k].tourLength;
                    optimal = new ArrayList<>(ants[k].tour);
                }

                ants[k].calcDelta(instance);
            }

            pheromones = calcPheromones(pheromones, evaporation, ants, instance);
            loopCount++;

            System.out.println(Collections.min(tourLenghts) + "$");
        }

        System.out.println(computeCost(instance, optimal) + "!");

        return optimal;
    }

    /**
     *
     * @param pheromones
     * @param instance
     * @param alpha
     * @param beta
     * @param i
     * @param j
     * @param notVisitedV
     * @return
     */
    private double calcProbability(double[][] pheromones, Instance instance,
                                   double alpha, double beta, int i, int j, List<Integer> notVisitedV) {
        double eta_ij = 1 / instance.getDistance(i, j);

        double counter = Math.pow(pheromones[i][j], alpha) * Math.pow(eta_ij, beta);
        double denominator = 0;

        for (Integer l : notVisitedV) {
            double eta_il = 1 / instance.getDistance(i, l.intValue());
            denominator += Math.pow(pheromones[i][l.intValue()], alpha) * Math.pow(eta_il, beta);
        }

        if (denominator != 0) {
            return counter / denominator;
        } else {
            return 0;
        }
    }

    /**
     *
     * @param pheromones
     * @param evaporation
     * @param ants
     * @param instance
     * @return
     */
    private double[][] calcPheromones(double[][] pheromones, double evaporation, Ant[] ants, Instance instance) {
        for (int i = 0; i < instance.getNumCities(); i++) {
            for (int j = 0; j < instance.getNumCities(); j++) {
                double delta = 0;

                for (int k = 0; k < ants.length; k++) {
                    delta += ants[k].delta[i][j];
                }

                pheromones[i][j] = ((1 - evaporation) * pheromones[i][j]) + delta;
            }
        }

        return pheromones;
    }

    /**
     *
     * @param size
     * @param start
     * @return
     */
    private List<Integer> generateSetV(int size, int start) {
        List<Integer> setV = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            if (index != start) {
                setV.add(new Integer(index));
            }
        }

        return setV;
    }

    /**
     * Computes the length of a given tour. Note that this method does not check whether every city is
     * visited.
     *
     * @param instance TSP instance
     * @param tour     List of cities
     * @return total distance of the tour
     */
    private double computeCost(Instance instance, List<Integer> tour) {
        double result = 0;

        for (int v : tour) {
            int w = tour.get((tour.indexOf(v) + 1) % tour.size());
            result += instance.getDistance(v, w);
        }

        return result;
    }

    /**
     *
     * @param array2D
     */
    private void print2DArray(double[][] array2D) {
        String res = "";

        for (int i = 0; i < array2D.length; i++) {
            for (int j = 0; j < array2D.length; j++) {
                DecimalFormat decimalFormat = new DecimalFormat("#.####");
                res += decimalFormat.format(array2D[i][j]) + "|";
            }

            res += "\n";
        }

        System.out.println(res);
    }

    /**
     *
     */
    private class Ant {
        List<Integer> tour;
        double tourLength;
        double[][] delta;

        public Ant() {
            tour = new ArrayList<>();
        }

        /**
         *
         * @return
         */
        public int getLast() {
            return tour.get(tour.size() - 1).intValue();
        }

        /**
         *
         * @param instance
         */
        public void calcTourLength(Instance instance) {
            tourLength = computeCost(instance, tour);
        }

        /**
         *
         * @param instance
         */
        public void calcDelta(Instance instance) {
            delta = new double[instance.getNumCities()][instance.getNumCities()];

            for (int i = 0; i < tour.size(); i++) {
                if (i != tour.size() - 1) {
                    delta[tour.get(i).intValue()][tour.get(i + 1).intValue()] = 1 / tourLength;
                    delta[tour.get(i + 1).intValue()][tour.get(i).intValue()] = 1 / tourLength;
                } else {
                    delta[tour.get(tour.size() - 1)][tour.get(0)] = 1 / tourLength;
                    delta[tour.get(0)][tour.get(tour.size() - 1)] = 1 / tourLength;
                }
            }
        }
    }
}
