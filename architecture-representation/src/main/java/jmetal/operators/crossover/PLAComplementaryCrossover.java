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

import java.util.*;

/**
 * @author Willian, Thiago, Diego, Mamoru
 * Proposta de operador de cruzamento baseado no Simple complementary Crossover
 */
public class PLAComplementaryCrossover extends Crossover {

    private static List VALID_TYPES = Arrays.asList(ArchitectureSolutionType.class);
    private Double crossoverProbability = null;
    private CrossoverUtils crossoverutils;


    public PLAComplementaryCrossover(HashMap<String, Object> parameters) {
        super(parameters);
    }

    @Override
    public Object execute(Object object) throws Exception {
        System.out.println("EXECUTE---->");
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

    /**
     *
     * @param crossoverProbability Probabilidade do Crossover
     * @param father Pai
     * @param mother Mãe
     * @return Solução
     * @throws JMException JMETAL Exception
     * @throws ClassNotFoundException Classe não encontrada
     */
    private Solution doCrossver(Double crossoverProbability, Solution father, Solution mother) throws JMException, ClassNotFoundException {
        System.out.println("DOCROSSOVER---->");
        System.out.println(crossoverProbability + father.toString() + mother.toString());
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

    /**
     * Altera solução utilizada no método doCrossver
     * @param father Pai
     * @param mother Mãe
     * @param offspring Solução descendente
     */
    private void applyComplementaryCrossover(Solution father, Solution mother, Solution offspring) {
        System.out.println("applyComplementaryCrossover" + father + mother + offspring);
        Architecture[] fatherDecisionVariables = (Architecture[]) father.getDecisionVariables();
        List<Architecture> fatherElements = Arrays.asList(fatherDecisionVariables);

        Architecture[] motherDecisionVariables = (Architecture[]) mother.getDecisionVariables();
        List<Architecture> motherElements = Arrays.asList(motherDecisionVariables);

        int cp = PseudoRandom.randInt(0, fatherElements.size() - 1);

        List<Class> diffClasses = new ArrayList<>();
        for (Architecture fatherElement : fatherElements) {
            diffClasses.addAll(fatherElement.getAllClasses());
        }

        List<Interface> diffInterfaces = new ArrayList<>();
        for (Architecture f : fatherElements) {
            diffInterfaces.addAll(f.getAllInterfaces());
        }

        List<Architecture> architectures = fatherElements.subList(0, cp);
        offspring.setDecisionVariables((Variable[]) architectures.toArray());

        List<Architecture> list = new ArrayList<>();
        for (Architecture me : motherElements) {
            if (!((Architecture) offspring.getDecisionVariables()[0])
                    .getAllClasses().contains(me)) {
                list.add(me);
            }
        }
        motherElements = list;


        for (Class diffClass : diffClasses) {
            ((Architecture) offspring.getDecisionVariables()[0]).addExternalClass(diffClass);
        }


        for (Architecture cl : motherElements) {
            for (Class cla : cl.getAllClasses()) {
                ((Architecture) offspring
                        .getDecisionVariables()[0]).addExternalClass(cla);
            }
        }
    }


    private void logSevere(String s) {
        Configuration.logger_.severe(this.getClass().getCanonicalName() + s);
    }
}