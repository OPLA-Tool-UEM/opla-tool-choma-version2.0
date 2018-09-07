package jmetal.operators.crossover;

import arquitetura.representation.Architecture;
import arquitetura.representation.Class;
import arquitetura.representation.Interface;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.ArchitectureSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.RandomGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class PLAComplementaryCrossover extends Crossover {

    private static List VALID_TYPES = Arrays.asList(ArchitectureSolutionType.class);
    private Double crossoverProbability = null;
    private CrossoverUtils crossoverutils;


    public PLAComplementaryCrossover(HashMap<String, Object> parameters) {
        super(parameters);
    }

    @Override
    public Object execute(Object object) throws Exception {
        Solution[] parents = (Solution[]) object;
        if (!(VALID_TYPES.contains(parents[0].getType().getClass()) && VALID_TYPES.contains(parents[1].getType().getClass()))) {
            logSevere(".execute: the solutions are not of the right type.");
        }
        crossoverProbability = (Double) getParameter("probability");
        if (parents.length < 2) {
            logSevere(".execute: operator need two parents");
            java.lang.Class<String> cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }
        Solution offspring = doCrossver(crossoverProbability, parents[0], parents[1]);
        return offspring;
    }

    public static void main(String[] args) {
        System.out.println();
    }

    private Solution doCrossver(Double crossoverProbability, Solution father, Solution mother) throws JMException, ClassNotFoundException {
        int n = father.numberOfObjectives();
        Solution offspring = new Solution();

        if (father.getDecisionVariables()[0].getVariableType() == java.lang.Class.forName(Architecture.ARCHITECTURE_TYPE)) {
            if (PseudoRandom.randDouble() < crossoverProbability) {
                Random generator = new Random();
                if (generator.nextInt() == 0) {
                    Solution f = father;
                    father = mother;
                    mother = f;
                }
                applyComplementaryCrossover(father, mother, offspring);
            }
        }


        return offspring;
    }

    private void applyComplementaryCrossover(Solution father, Solution mother, Solution offspring) {
        Architecture[] fatherDecisionVariables = (Architecture[]) father.getDecisionVariables();
        List<Architecture> fatherElements = Arrays.asList(fatherDecisionVariables);

        Architecture[] motherDecisionVariables = (Architecture[]) mother.getDecisionVariables();
        List<Architecture> motherElements = Arrays.asList(motherDecisionVariables);

        int cp = PseudoRandom.randInt(0, fatherElements.size() - 1);

        List<Class> diffClasses = new ArrayList<>();
        fatherElements.forEach(f -> diffClasses.addAll(f.getAllClasses()));

        List<Interface> diffInterfaces = new ArrayList<>();
        fatherElements.forEach(f -> diffInterfaces.addAll(f.getAllInterfaces()));

        List<Architecture> architectures = fatherElements.subList(0, cp);
        offspring.setDecisionVariables((Variable[]) architectures.toArray());

        motherElements = motherElements.stream()
                .filter(me -> !((Architecture) offspring.getDecisionVariables()[0])
                        .getAllClasses().contains(me)).collect(Collectors.toList());


        diffClasses.forEach(cl -> {
            ((Architecture) offspring.getDecisionVariables()[0]).addExternalClass(cl);
        });


        motherElements.forEach(cl -> cl.getAllClasses()
                .forEach(cla -> ((Architecture) offspring
                        .getDecisionVariables()[0]).addExternalClass(cla)));
    }


    private void logSevere(String s) {
        Configuration.logger_.severe(this.getClass().getCanonicalName() + s);
    }
}