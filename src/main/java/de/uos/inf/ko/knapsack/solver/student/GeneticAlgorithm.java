package de.uos.inf.ko.knapsack.solver.student;

import de.uos.inf.ko.knapsack.Instance;
import de.uos.inf.ko.knapsack.Solution;
import de.uos.inf.ko.knapsack.SolverInterface;

import java.util.*;

/**
 * Solver for the binary knapsack problem based on a genetic algorithm.
 *
 * @author jtormoehlen
 */
public class GeneticAlgorithm implements SolverInterface<Solution> {

    /**
     * solve knapsack problem using genetic algorithm:
     * creating a starting population and generate new
     * individuals by choosing two random parent
     * solutions and crossing their genes;
     * mutate the child and add it to the population;
     * regularly reduce the population by natural
     * selection; repeat until time limit is reached
     *
     * @param instance The given knapsack instance
     * @return approximated solution
     */
    @Override
    public Solution solve(Instance instance) {
        if (false) {
            throw new UnsupportedOperationException();
        }

        /** best solution **/
        Solution sStar = new Solution(instance);

        /** maximum possible weight of instance **/
        Solution tmp = new Solution(instance);
        for (int i = 0; i < instance.getSize(); i++) {
            tmp.set(i, 1);
        }
        final int w = tmp.getWeight();

        /** initial population and compute fitness of each individual **/
        final int popStartSize = 100;
        Set<String> populationSet = generateInitialPopulation(instance, popStartSize);
        Map<String, Integer> fitnessMap = new HashMap<>();
        for (String individual : populationSet) {
            Solution individualSolution = stringToSolution(individual, instance);
            fitnessMap.put(individual, computeFitness(individualSolution, w));
        }
        final int popSizeMax = 1000;

        /** mutation probability **/
        final double probability = 0.75d;

        /** loop until time limit reached **/
        long t0 = System.currentTimeMillis();
        final long tMax = t0 + 1000;
        while (t0 <= tMax) {

            /** get random mother and remove from population **/
            Iterator<String> populationIterator = populationSet.iterator();
            Random random = new Random();
            int r = random.nextInt(populationSet.size());
            for (int i = 0; i < r - 1; i++) {
                populationIterator.next();
            }
            String sM = populationIterator.next();
            populationIterator.remove();
            /** get random father **/
            populationIterator = populationSet.iterator();
            random.nextInt(populationSet.size());
            for (int i = 0; i < r - 1; i++) {
                populationIterator.next();
            }
            String sF = populationIterator.next();
            /** add mother back to population **/
            populationSet.add(sM);

            /** generate child from mother and father **/
            String sC = onePointCrossover(sM, sF);

            /** mutate child with probability 0.0,...,1.0 **/
            sC = mutate(sC, probability);

            /** compute child fitness **/
            fitnessMap.put(sC, computeFitness(stringToSolution(sC, instance), w));
            populationSet.add(sC);

            /** natural selection on population **/
            if (populationSet.size() >= popSizeMax) {
                populationSet = naturalSelection(populationSet, fitnessMap, popStartSize);
            }

            t0 = System.currentTimeMillis();
        }

        /** update best solution when possible **/
        Iterator<String> populationIterator = populationSet.iterator();
        while (populationIterator.hasNext()) {
            Solution individualSolution = stringToSolution(populationIterator.next(), instance);
            if (individualSolution.isFeasible() && individualSolution.getValue() > sStar.getValue()) {
                sStar = new Solution(individualSolution);
            }
        }

        return sStar;
    }

    /**
     * compute the fitness of a single solution
     * by subtracting a penalty from the solution's
     * total value (knapsack problem may be defined
     * with weights w, profits c, solution x,
     * instance size n and capacity W):
     * f <- sum(x_i*c_i) for all i elem {1,...,n}
     * penalty <- sum(w_i)*|sum(x_i*w_i)-W| for all i elem {1,...,n}
     * f <- f-penalty
     *
     * @param solution solution
     * @param w        weight of all items
     * @return fitness value
     */
    private static int computeFitness(Solution solution, int w) {
        int fitness = solution.getValue();
        int penalty = w * Math.abs(solution.getWeight() - solution.getInstance().getCapacity());

        return fitness - penalty;
    }

    /**
     * natural selection by (my + lambda) strategy:
     * remove all (my + lambda) but the my (initial pop size)
     * fittest solutions
     *
     * @param populationSet population
     * @param fitnessMap    fitness of each individual
     */
    private static Set<String> naturalSelection(Set<String> populationSet, Map<String, Integer> fitnessMap, int number) {
        List<Individual> fitnessList = new ArrayList<>();
        for (String s : fitnessMap.keySet()) {
            int fitness = fitnessMap.get(s);
            fitnessList.add(new Individual(s, fitness));
        }

        Collections.sort(fitnessList);
        for (int i = number - 1; i < fitnessList.size(); i++) {
            String individual = fitnessList.get(i).s;
            populationSet.remove(individual);
        }

        return populationSet;
    }

    /**
     * mutate child solution by flipping random
     * gen with probability p elem {0.0,...,1.0}
     *
     * @param s child solution
     * @param p probability
     * @return (mutated) child solution
     */
    private static String mutate(String s, double p) {
        Random random = new Random();
        double q = random.nextDouble();

        if (q <= p) {
            int r = random.nextInt(s.length());
            StringBuilder chromosome = new StringBuilder(s);

            int gen = Character.getNumericValue(s.charAt(r));
            if (gen == 0) {
                chromosome.setCharAt(r, '1');
            } else {
                chromosome.setCharAt(r, '0');
            }

            return chromosome.toString();
        } else {
            return s;
        }
    }

    /**
     * generate child solution from mother and father
     * using one-point-crossover operator: child inherits
     * first part (1,...,r) from mother and second
     * (r+1,...,n) from father with random integer
     * r elem {1,...,n-1}
     *
     * @param sM mother
     * @param sF father
     * @return child
     */
    private static String onePointCrossover(String sM, String sF) {
        Random random = new Random();
        int r = random.nextInt(sM.length() - 1);

        String sC = "";

        for (int i = 0; i < sM.length(); i++) {

            if (i <= r) {
                sC += sM.charAt(i);
            } else {
                sC += sF.charAt(i);
            }
        }

        return sC;
    }

    /**
     * generate the initial population
     *
     * @param instance instance
     * @param popSize  population size
     * @return set of string solutions
     */
    private static Set<String> generateInitialPopulation(Instance instance, int popSize) {
        Set<String> populationSet = new HashSet<>();

        for (int i = 0; i < popSize; i++) {
            Solution randomSolution = generateRandomStartSolution(instance);
            populationSet.add(solutionToString(randomSolution));
        }

        return populationSet;
    }

    /**
     * generate a random (in-)feasible start solution
     *
     * @param instance instance
     * @return solution
     */
    private static Solution generateRandomStartSolution(Instance instance) {
        int bound = 2;
        int n = instance.getSize();
        Solution solution = new Solution(instance);
        Random random = new Random();

        for (int i = 0; i < n; i++) {
            solution.set(i, random.nextInt(bound));
        }

        return solution;
    }

    /**
     * converts a string solution to a solution
     *
     * @param s        string solution
     * @param instance instance
     * @return solution
     */
    private static Solution stringToSolution(String s, Instance instance) {
        Solution solution = new Solution(instance);

        for (int i = 0; i < instance.getSize(); i++) {
            solution.set(i, Character.getNumericValue(s.charAt(i)));
        }

        return solution;
    }

    /**
     * converts a solution to a string solution
     *
     * @param solution solution
     * @return string solution
     */
    private static String solutionToString(Solution solution) {
        String s = "";

        for (int i = 0; i < solution.getInstance().getSize(); i++) {
            s += solution.get(i);
        }

        return s;
    }

    /**
     * individual of the population with solution s
     * and fitness value
     */
    private static class Individual implements Comparable<Individual> {
        String s;
        int fitness;

        public Individual(String s, int fitness) {
            this.s = s;
            this.fitness = fitness;
        }

        @Override
        public int compareTo(Individual individual) {
            return -(this.fitness - individual.fitness);
        }
    }

    @Override
    public String getName() {
        return "GA(s)";
    }
}
