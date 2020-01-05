package de.uos.inf.ko.tsp.solver.student;

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
        /** initiate parameters: evaporation factor, alpha, beta and number of ants **/
        final double evaporation = 0.5d;
        final double alpha = 0.5;
        final double beta = 1.0;
        final int m = 1000;
        /** tsp instance size and pheromone "field" (zero at start) **/
        int n = instance.getNumCities();
        double[][] pheromones = new double[n][n];

        int loopCount = 0;
        List<Integer> optimal = new ArrayList<>();
        double optimalLength = Double.POSITIVE_INFINITY;

        while (loopCount <= 10) {
            /** initiate all ants **/
            Ant[] ants = new Ant[m];
            List<Double> tourLenghts = new ArrayList<>();

            for (int k = 0; k < m; k++) {
                /** set ant on random start location **/
                ants[k] = new Ant();
                Random random = new Random();
                int start = random.nextInt(n);

                /** generate list of unvisited cities for ant k **/
                List<Integer> notVisitedV = generateSetV(n, start);
                ants[k].tour.add(new Integer(start));

                /** for all (random) unvisited cities... **/
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

                    /** ...visit next city with highest probability for ant k **/
                    if (j != -1) {
                        ants[k].tour.add(new Integer(j));
                        notVisitedV.remove(new Integer(j));
                    }
                }
            }

            /** for all ants calculate tourlength and delta values;
             * update optimal solution if possible **/
            for (int k = 0; k < m; k++) {
                ants[k].calcTourLength(instance);
                tourLenghts.add(new Double(ants[k].tourLength));

                if (ants[k].tourLength < optimalLength) {
                    optimalLength = ants[k].tourLength;
                    optimal = new ArrayList<>(ants[k].tour);
                }

                ants[k].calcDelta(instance);
            }

            /** calculate new pheromone values **/
            pheromones = calcPheromones(pheromones, evaporation, ants, instance);
            loopCount++;

            //System.out.println(Collections.min(tourLenghts) + "$");
        }

        /** print best result **/
        if (instance.getNumCities() != 1) {
            System.out.println("tsp " + instance.getNumCities() + " Tourlength: " + computeCost(instance, optimal));
        }

        /** return best result **/
        return optimal;
    }

    /**
     * calculate probability for one ant
     * to choose a path from i to j using
     * formula (3.1)
     *
     * @param pheromones current pheromones on all paths
     * @param instance tsp instance
     * @param alpha influence of pheromones
     * @param beta influence of greedy solution
     * @param i from city
     * @param j to city
     * @param notVisitedV list of unvisited cities
     * @return probability
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
     * overwrite pheromone values for all path between all cities
     *
     * @param pheromones current pheromones on all paths
     * @param evaporation pheromones' disappearing factor
     * @param ants array of ants
     * @param instance tsp instance
     * @return 2d array of pheromone values (for each path between all cities)
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
     * generate list of unvisited cities (excluding start location)
     *
     * @param size tsp instance size
     * @param start city to start
     * @return list of unvisited cities
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
     * Ant with tsp tour including length and 2d array of delta values
     */
    private class Ant {
        List<Integer> tour;
        double tourLength;
        double[][] delta;

        public Ant() {
            tour = new ArrayList<>();
        }

        /**
         * get last visited city
         *
         * @return last visited city
         */
        public int getLast() {
            return tour.get(tour.size() - 1).intValue();
        }

        /**
         * calculate the current tour length and store to tourlength
         * @param instance tsp instance
         */
        public void calcTourLength(Instance instance) {
            tourLength = computeCost(instance, tour);
        }

        /**
         * calculate 2d array of delta values (required for pheromones)
         * @param instance tsp instance
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
