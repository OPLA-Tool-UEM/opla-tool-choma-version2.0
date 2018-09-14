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
        Solution offspring = new Solution(father);

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
     * Para testar crossover deve-se configurar os seguintes valores na ferramenta
     * Number of Runs: 2 Max Evaluations: 4 Population Size: 3
     * Devido ao fato de que na etapa de aplicação do operador, é verificado se populationSize < maxEvaluations
     * @param father Pai
     * @param mother Mãe
     * @param offspring Solução descendente
     */
    private void applyComplementaryCrossover(Solution father, Solution mother, Solution offspring) {
        System.out.println("applyComplementaryCrossover" + father + mother + offspring);
        Variable[] fatherDecisionVariables = father.getDecisionVariables();
        List<Variable> fatherElements = Arrays.asList(fatherDecisionVariables);

        Variable[] motherDecisionVariables = (Variable[]) mother.getDecisionVariables();
        List<Variable> motherElements = Arrays.asList(motherDecisionVariables);

        // Lista de classes presente somente no pai
        List<Class> diffClasses = new ArrayList<>();
        for (Variable fatherElement : fatherElements) {
            diffClasses.addAll(((Architecture) fatherElement).getAllClasses());
        }

        // Lista de interfaces presentes somento no pai
        List<Interface> diffInterfaces = new ArrayList<>();
        for (Variable f : fatherElements) {
            diffInterfaces.addAll(((Architecture) f).getAllInterfaces());
        }

        // Elementos presentes no pai
        Variable[] vs = new Variable[fatherElements.size()];
        for (int i = 0; i < fatherElements.size(); i++) {
            vs[i] = fatherElements.get(i);
        }
        offspring.setDecisionVariables(vs);

        // remove os elementos da mão que são iguais ao do filho
        List<Variable> list = new ArrayList<>();
        for (Variable me : motherElements) {
            if (!((Architecture) offspring.getDecisionVariables()[0])
                    .getAllClasses().contains(me)) {
                list.add(me);
            }
        }
        motherElements = list;

        // Adiciona todos elementos das diferentes listas
        for (Class diffClass : diffClasses) {
            ((Architecture) offspring.getDecisionVariables()[0]).addExternalClass(diffClass);
        }

        // Adiciona todos elementos remanescentes da mãe
        for (Variable cl : motherElements) {
            for (Class cla : ((Architecture) cl).getAllClasses()) {
                ((Architecture) offspring
                        .getDecisionVariables()[0]).addExternalClass(cla);
            }
        }
    }

    private void logSevere(String s) {
        Configuration.logger_.severe(this.getClass().getCanonicalName() + s);
    }
}