package jmetal.operators.crossover;

import arquitetura.exceptions.ClassNotFound;
import arquitetura.exceptions.ConcernNotFoundException;
import arquitetura.exceptions.NotFoundException;
import arquitetura.exceptions.PackageNotFound;
import arquitetura.helpers.UtilResources;
import arquitetura.representation.*;
import arquitetura.representation.Class;
import arquitetura.representation.Package;
import arquitetura.representation.relationship.GeneralizationRelationship;
import arquitetura.representation.relationship.Relationship;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.ArchitectureSolutionType;
import jmetal.problems.OPLA;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.core.Problem;

import java.util.*;
import java.util.logging.Level;

import javax.swing.text.AsyncBoxView.ChildLocator;

import org.mockito.cglib.beans.BeanCopier.Generator;


/**
 * @author Rafael, Janaina e João Messias
 * Proposta de operador PLAModularCrossover
 */

public class PLAModularCrossover extends Crossover {
	
	private static List<java.lang.Class<ArchitectureSolutionType>> VALID_TYPES = Arrays.asList(ArchitectureSolutionType.class);
	private Double crossoverProbability_ = null;
	private static String SCOPE_LEVEL = "allLevels";
   
	public PLAModularCrossover(HashMap<String, Object> parameters) {
		super(parameters);
		// TODO Auto-generated constructor stub
	}
	

	
	public Solution modularCrossover(double probability, Solution parent1, Solution parent2, String scope) throws JMException, CloneNotSupportedException, ClassNotFound, PackageNotFound, NotFoundException
    {
		Solution offspring = new Solution(parent1);
		
		Solution chosenParent = null;
		Solution otherParent = null;
		
		try {
			if (parent1.getDecisionVariables()[0].getVariableType() == java.lang.Class
			        .forName(Architecture.ARCHITECTURE_TYPE)){
				
				Random generator = new Random();
				if (generator.nextInt(2) == 0){
					chosenParent = parent1;
					otherParent = parent2;
				}else{
					chosenParent = parent2;
					otherParent = parent1;				
				}
				
			}		
			
			Variable[] chosenDecisionVariables = chosenParent.getDecisionVariables();
			List<Variable> chosenElements = Arrays.asList(chosenDecisionVariables);
			
			Variable[] otherDecisionVariables = otherParent.getDecisionVariables();
			List<Variable> otherElements = Arrays.asList(otherDecisionVariables);
			
			Random generator = new Random();
			int i = generator.nextInt(chosenElements.size());
			Variable c = chosenElements.get(i);
			
			Architecture ac = ((Architecture) c);
			
			
			//  RelationShip do c (sorteado)
			int nRelationship1 = 0;
			int nVariationPoint1 = 0;
			int nFeature1 = 0;
			
			for (Class clazz: ac.getAllClasses()) {
				nRelationship1 += classRelationshipCounter(clazz);
				nVariationPoint1 += classVariationPointerCounter(clazz);
				nFeature1 += classFeatureCounter(clazz);
			}
			
			for (Interface ai: ac.getInterfaces()) {
				nRelationship1 += interfaceRelationshipCounter(ai);
				nVariationPoint1 += interfaceVariationPointCounter(ai);
				nFeature1 += interfaceFeatureCounter(ai);
			}
			
			for (Package pc: ac.getAllPackages()) {
				nRelationship1 += packageRelationshipCounter(pc);
				nVariationPoint1 += packageVariationPointerCounter(pc);
				nFeature1 += packageFeatureCounter(pc);
			}
			
			// RelationShip do otherParent
			int nRelationship2 = 0;
			int nVariationPoint2 = 0;
			int nFeature2 = 0;
			
			Variable otherVariable = otherParent.getDecisionVariables()[0];
			Architecture otherArchitecture = ((Architecture) otherVariable);
			
			for (Class clazz: otherArchitecture.getAllClasses()) {
				nRelationship2 += classRelationshipCounter(clazz);
				nVariationPoint2 += classVariationPointerCounter(clazz);
				nFeature2 += classFeatureCounter(clazz);
			}
			
			for (Interface ai: otherArchitecture.getInterfaces()) {
				nRelationship2 += interfaceRelationshipCounter(ai);
				nVariationPoint2 += interfaceVariationPointCounter(ai);
				nFeature2 += interfaceFeatureCounter(ai);
			}
			
			for (Package pc: otherArchitecture.getAllPackages()) {
				nRelationship2 += packageRelationshipCounter(pc);
				nVariationPoint2 += packageVariationPointerCounter(pc);
				nFeature2 += packageFeatureCounter(pc);
			}
			
			System.out.println("SIZE: " + offspring.getDecisionVariables() + offspring.getDecisionVariables().length);
			Architecture offspringArchitecture = ((Architecture) offspring.getDecisionVariables()[0]);
			Architecture chosenParentArchitecture = ((Architecture) chosenParent.getDecisionVariables()[0]);
		    if (nRelationship1  > nRelationship2){
		    	addElements(offspringArchitecture, chosenParentArchitecture);
		    	
		    }
		    else{
		        if (nRelationship1 < nRelationship2){
		        	addElements(offspringArchitecture, otherArchitecture);
		        }
		        else{
		        	if (nVariationPoint1 > nVariationPoint2){
		        		addElements(offspringArchitecture, chosenParentArchitecture);
		        	}
		        	else{
		        		if (nVariationPoint1 < nVariationPoint2){
		        			addElements(offspringArchitecture, otherArchitecture);
		        		}
		        		else{
		        			if (nFeature1 <= nFeature2){
		        				addElements(offspringArchitecture, chosenParentArchitecture);
		        			}
		        			else{
		        				addElements(offspringArchitecture, otherArchitecture);
		        			}
		        		}
		        	}
		        }
		    }
		    
		    // Exclui todos os relacionamentos das classes do pai com o filho, para não serem selecionados novamente 
		    for (Class cc: chosenParentArchitecture.getAllClasses()) {
		    	offspringArchitecture.deleteClassRelationships(cc);
		    }
		    
		                

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return offspring;
		
	}
	
	public void addElements(Architecture offspringArchitecture, Architecture chosenParentArchitecture){
		for (Class clazz: chosenParentArchitecture.getAllClasses()) {
    		offspringArchitecture.addExternalClass(clazz);
		}
		
		for (Interface ai: chosenParentArchitecture.getInterfaces()) {
			offspringArchitecture.addExternalInterface(ai);
		}
		
		for (Package pc: chosenParentArchitecture.getAllPackages()) {
			offspringArchitecture.addPackage(pc);
		}
	}

	public Solution doCrossover(double probability, Solution parent1, Solution parent2)throws JMException,
    CloneNotSupportedException, ClassNotFound, PackageNotFound, NotFoundException, ConcernNotFoundException{
		Solution offspring = new Solution(parent1);
		Solution crossModular = this.modularCrossover(crossoverProbability_, parent1, parent2, SCOPE_LEVEL);
		offspring = crossModular;
		
		return offspring;
	}
	
	public Object execute(Object object) throws Exception{
		// TODO Auto-generated method stub
		Solution [] parents = (Solution[]) object;
		if (!(VALID_TYPES.contains(parents[0].getType().getClass()) &&VALID_TYPES.contains(parents[1].getType().getClass()))){
			Configuration.logger_.severe("PLAModularCrossover.execute: the solution"+"are not of the right type.");
		}
		crossoverProbability_ =(Double)getParameter("probability");
		if (parents.length < 2){
			Configuration.logger_.severe("PLAModularCrossover.execute: operator needs two" + "parents");
			java.lang.Class<String> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in" + name + ".execute()");
			
		}
		Solution offspring = doCrossover (crossoverProbability_, parents[0], parents[1]);
		return offspring;
	}
	
	public int packageFeatureCounter (Package pack){
		return pack.getAllConcerns().size();
	}
	
	public int classFeatureCounter (Class klass){
		return klass.getAllConcerns().size();
	}
	
	public int interfaceFeatureCounter (Interface inter){
		return inter.getAllConcerns().size();
	}
	
	public int classRelationshipCounter (Class Klass){
		return Klass.getRelationshipHolder().getAllRelationships().size();
	}
	
	public int interfaceRelationshipCounter (Interface Inter){
		return Inter.getRelationshipHolder().getAllRelationships().size();
	}
	
	public int packageRelationshipCounter (Package pack){
		int count = 0;
		for (Class klass : pack.getAllClasses()){
			count += klass.getRelationshipHolder().getAllRelationships().size();
		}	
		for (Class inter : pack.getAllClasses()){
			count += inter.getRelationshipHolder().getAllRelationships().size();
		}
		return count;
	}
	
	
	public int interfaceVariationPointCounter (Interface inter){
		int count = 0;
		if (inter.isVariationPoint()){
			count ++;
		}
		return count;
	}
	
	public int classVariationPointerCounter (Class klass){
		int count = 0;
		if (klass.isVariationPoint()){
			count ++;
		}
		return count;
	}
	
	public int packageVariationPointerCounter (Package pack){
		int count = 0;
		for (Class klass : pack.getAllClasses()){
			count += classVariationPointerCounter(klass);
		}
		for (Interface inter : pack.getAllInterfaces()){
			count += interfaceFeatureCounter(inter);
		}
		return count;
	}
	
	
	public void addPackageInOffspring (Architecture offspring, Architecture parent, Package parentPackage) {
		offspring.addPackage(parentPackage);
		Package packageInOffspring = offspring.findPackageByName(parentPackage.getName());
		addInterfacesToPackageInOffSpring(parentPackage, packageInOffspring, offspring, parent);
		addClassesToOffspring (null, parentPackage, packageInOffspring, offspring, parent); 
		
	}
	
	//43
	 public static boolean isChild(Class cls) {
	        boolean child = false;

	        for (Relationship relationship : cls.getRelationships()) {
	            if (relationship instanceof GeneralizationRelationship) {
	                GeneralizationRelationship generalization = (GeneralizationRelationship) relationship;
	                if (generalization.getChild().equals(cls)) {
	                    child = true;
	                    return child;
	                }
	            }
	        }
	        return child;
	    }
	
	 //48
	 public static Class getParent(Class cls) {
	        for (Relationship relationship : cls.getRelationships()) {
	            if (relationship instanceof GeneralizationRelationship)
	                if (((GeneralizationRelationship) relationship).getChild().equals(cls))
	                    return (Class) ((GeneralizationRelationship) relationship).getParent();
	        }
	        return null;
	    }
	 
	 //67
	    private static GeneralizationRelationship getGeneralizationForClass(Element cls) {
	        for (Relationship relationship : ((Class) cls).getRelationships()) {
	            if (relationship instanceof GeneralizationRelationship) {
	                if (((GeneralizationRelationship) relationship).getParent().equals(cls))
	                    return (GeneralizationRelationship) relationship;
	            }
	        }

	        return null;
	    }
	 
	 
    /**
     * Adicionar as interfaces que o pacote possuia em parent no pacote em
     * offspring
     *
     * @param parentPackage
     * @param packageInOffspring
     * @param offspring
     * @param parent
     */ //336
    private void addInterfacesToPackageInOffSpring(Package parentPackage, Package packageInOffspring,
                                                   Architecture offspring, Architecture parent) {
        for (Interface inter : parentPackage.getAllInterfaces()) {
            packageInOffspring.addExternalInterface(inter);
            saveAllRelationshiopForElement(inter, parent, offspring);
        }
    }
    
    //401
    private Package findOrCreatePakage(String packageName, Architecture offspring) {
        Package pkg = null;
        pkg = offspring.findPackageByName(packageName);
        if (pkg == null)
            return offspring.createPackage(packageName);
        return pkg;
    }
    
    
    /**
     * Adiciona as classes do pacote em parent no pacote em offspring
     *
     * @param parentPackage
     * @param packageInOffspring
     * @param offspring
     * @param parent
     *///418
    private void addClassesToOffspring(Concern feature, Package parentPackage, Package packageInOffspring,
                                       Architecture offspring, Architecture parent) {
        Iterator<Class> iteratorClasses = parentPackage.getAllClasses().iterator();
        while (iteratorClasses.hasNext()) {
            Class classComp = iteratorClasses.next();
            if (!classComp.belongsToGeneralization()) {
                addClassToOffspring(classComp, packageInOffspring, offspring, parent);
            } else {
                if (this.isHierarchyInASameComponent(classComp, parent)) {
                    moveHierarchyToSameComponent(classComp, packageInOffspring, parentPackage, offspring, parent,
                            feature);
                } else {
                    packageInOffspring.addExternalClass(classComp);
                    moveHierarchyToDifferentPackage(classComp, packageInOffspring, parentPackage, offspring, parent);
                }
                saveAllRelationshiopForElement(classComp, parent, offspring);
            }
            addInterfacesImplementedByClass(classComp, offspring, parent, parentPackage);
            addInterfacesRequiredByClass(classComp, offspring, parent, parentPackage);
        }
    }
    
    
    //487
    private boolean isHierarchyInASameComponent(Class class_, Architecture architecture) {
        boolean sameComponent = true;
        Class parent = class_;
        Package componentOfClass = null;
        componentOfClass = architecture.findPackageOfClass(class_);
        Package componentOfParent = architecture.findPackageOfClass(class_);
        ;
        while (CrossoverOperations.isChild(parent)) {
            parent = CrossoverOperations.getParent(parent);
            componentOfParent = architecture.findPackageOfClass(parent);
            if (!(componentOfClass.equals(componentOfParent))) {
                sameComponent = false;
                return false;
            }
        }
        return sameComponent;
    }
    //505
    private void moveChildrenToSameComponent(Class parent, Package sourceComp, Package targetComp,
            Architecture offspring, Architecture parentArch) {

		final Collection<Element> children = getChildren(parent);
		// move cada subclasse
		for (Element child : children) {
			moveChildrenToSameComponent((Class) child, sourceComp, targetComp, offspring, parentArch);
		}
		// move a super classe
		if (sourceComp.getAllClasses().contains(parent)) {
			addClassToOffspring(parent, targetComp, offspring, parentArch);
		} else {
				try {
					for (Package auxComp : parentArch.getAllPackages()) {
						if (auxComp.getAllClasses().contains(parent)) {
							sourceComp = auxComp;
							if (sourceComp.getName() != targetComp.getName()) {
								targetComp = offspring.findPackageByName(sourceComp.getName());
								if (targetComp == null) {
									targetComp = offspring.createPackage(sourceComp.getName());
									for (Concern feature : sourceComp.getOwnConcerns())
										targetComp.addConcern(feature.getName());
								}
							}
						}
						addClassToOffspring(parent, targetComp, offspring, parentArch);
						break;
					}
				} catch (Exception e) {
					System.err.println(e);
				}
		}
	}

    
    
   //539 
    private void moveChildrenToDifferentComponent(Class root, Package newComp, Architecture offspring,
            Architecture parent) {

		final String rootPackageName = UtilResources.extractPackageName(root.getNamespace());
		Package rootTargetPackage = offspring.findPackageByName(rootPackageName);
		if (rootPackageName == null)
			rootTargetPackage = offspring.createPackage(rootPackageName);
			
			addClassToOffspring(root, rootTargetPackage, offspring, parent);
			
			saveAllRelationshiopForElement(parent.findPackageByName(rootPackageName), parent, offspring);
			for (Element child : getChildren(root)) {
			final String packageName = UtilResources.extractPackageName(child.getNamespace());
			Package targetPackage = parent.findPackageByName(packageName);
			if (targetPackage != null)
			moveChildrenToDifferentComponent((Class) child, targetPackage, offspring, parent);
		}
	}
    
    
    /**
     * Adicionar klass a targetComp em offspring.
     *
     * @param klass
     * @param targetComp
     * @param offspring
     * @param parent
     *///566
    public void addClassToOffspring(Class klass, Package targetComp, Architecture offspring, Architecture parent) {
        targetComp.addExternalClass(klass);
        saveAllRelationshiopForElement(klass, parent, offspring);
    }
    
    
    /**
     * Adiciona as interfaces implementadas por klass em offspring.
     *
     * @param klass
     * @param offspring
     * @param parent
     * @param targetComp
     *///579
    private void addInterfacesImplementedByClass(Class klass, Architecture offspring, Architecture parent,
                                                 Package targetComp) {

        for (Interface itf : klass.getImplementedInterfaces()) {
            if (itf.getNamespace().equalsIgnoreCase("model"))
                offspring.addExternalInterface(itf);
            else
                findOrCreatePakage(UtilResources.extractPackageName(itf.getNamespace()), offspring)
                        .addExternalInterface(itf);
            saveAllRelationshiopForElement(itf, parent, offspring);
        }
    }
    
    /**
     * Adiciona as interfaces requeridas por klass em offspring
     *
     * @param klass
     * @param offspring
     * @param parent
     * @param targetComp
     *///600
    private void addInterfacesRequiredByClass(Class klass, Architecture offspring, Architecture parent,
                                              Package targetComp) {
        for (Interface itf : klass.getRequiredInterfaces()) {
            if (itf.getNamespace().equalsIgnoreCase("model"))
                offspring.addExternalInterface(itf);
            else
                findOrCreatePakage(UtilResources.extractPackageName(itf.getNamespace()), offspring)
                        .addExternalInterface(itf);

            saveAllRelationshiopForElement(itf, parent, offspring);
        }
    }
    
    //613
    private void moveHierarchyToSameComponent(Class classComp, Package targetComp, Package sourceComp,
            Architecture offspring, Architecture parent, Concern concern) {
		Class root = classComp;
			while (isChild(root)) {
			root = getParent(root);
			}
			if (sourceComp.getAllClasses().contains(root)) {
			moveChildrenToSameComponent(root, sourceComp, targetComp, offspring, parent);
		}
	}
    
    
    //624
    private void moveHierarchyToDifferentPackage(Class classComp, Package newComp, Package parentPackage,
            Architecture offspring, Architecture parent) {
			Class root = classComp;
			while (isChild(root)) {
			root = getParent(root);
			}
			moveChildrenToDifferentComponent(root, newComp, offspring, parent);
	}
    
    //670
    private void saveAllRelationshiopForElement(Element element, Architecture parent, Architecture offspring) {

        if (element instanceof Class) {
            for (Relationship r : ((Class) element).getRelationships())
                offspring.getRelationshipHolder().addRelationship(r);
            return;
        }
        if (element instanceof Interface) {
            for (Relationship r : ((Interface) element).getRelationships())
                offspring.getRelationshipHolder().addRelationship(r);
            return;
        }
        if (element instanceof Package) {
            for (Relationship r : ((Package) element).getRelationships())
                offspring.getRelationshipHolder().addRelationship(r);
            return;
        }
    }
	
    
    //689
    private Set<Element> getChildren(Element cls) {
        GeneralizationRelationship g = getGeneralizationForClass(cls);
        if (g != null)
            return g.getAllChildrenForGeneralClass();
        return Collections.emptySet();
    }
    

    
}