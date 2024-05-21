package verimag.flata.common;

import java.util.LinkedList;

import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.log.BasicLogManager;

import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

import com.google.common.collect.ImmutableList;

public class FlataJavaSMT {

    private Configuration config;
    private ShutdownNotifier notifier;
    private LogManager logger;
    private SolverContext context;

    private IntegerFormulaManager ifm;
    private BooleanFormulaManager bfm;
    private QuantifiedFormulaManager qfm;

    private ImmutableList<Model.ValueAssignment> modelAssignments;

    Solvers chosenSolver;

    private static String ifmSupport = "[MATHSAT5, SMTINTERPOL, Z3, PRINCESS, CVC4, YICES2]";
    private static String qfmSupport = "[Z3, PRINCESS]";

    private int solverCalls;

    private Solvers chooseSolver(String solver) {
        String s = solver.toUpperCase();
        Solvers chosenSolver = Solvers.valueOf(s);

        if (chosenSolver == null) {
            System.out.println("Invalid solver, exiting");
            System.exit(1);
        }

        return chosenSolver;
    }

    public FlataJavaSMT() {
        this(null);
    }
    public FlataJavaSMT(String solver) {
        try {
            if (solver == null) {
                chosenSolver = Solvers.YICES2; // Default solver
            } else {
                chosenSolver = chooseSolver(solver);
            }

            
            config = Configuration.defaultConfiguration();
            notifier = ShutdownNotifier.createDummy();
            logger = BasicLogManager.create(config);
            context = SolverContextFactory.createSolverContext(config, logger, notifier, chosenSolver);
            bfm = context.getFormulaManager().getBooleanFormulaManager();
            
            // Some solvers don't support Integer/Quantified theory
            // Integer-theory is required for the current implementation
            try {
                ifm = context.getFormulaManager().getIntegerFormulaManager();
            } catch (UnsupportedOperationException uoe) {
                System.out.println("WARNING: Chosen solver (" + chosenSolver + ") does not support Integer-theory, exiting");
                System.out.println("Solvers supporting Integer-theory: " + ifmSupport);
                System.exit(1);
            }

            System.out.println("Using solver: " + chosenSolver);
            
            // Quantifier-theory is optional, may result in some features not working
            try {
                qfm = context.getFormulaManager().getQuantifiedFormulaManager();
            } catch (UnsupportedOperationException uoe) {
                System.out.println("WARNING: Chosen solver does not support Quantifier-theory");
            }

            solverCalls = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public BooleanFormulaManager getBfm() {
        return bfm;
    }

    public IntegerFormulaManager getIfm() {
        return ifm;
    }

    public QuantifiedFormulaManager getQfm() {
        if (qfm == null) {
            System.out.println("QuantifiedFormulaManager not supported by chosen solver " + chosenSolver);
            System.out.println("Solvers supporting QuantifiedFormulaManager: " + qfmSupport);
            System.exit(1);
        }
        return qfm;
    }

    public ImmutableList<ValueAssignment> getModelAssignments() {
        return modelAssignments;
    }

    public int getSolverCalls() {
        return solverCalls;
    }

    public Answer isSatisfiable (BooleanFormula formula) {
        return isSatisfiable(formula, false, false);
    }

    public Answer isSatisfiable (BooleanFormula formula, Boolean invert) {
        return isSatisfiable(formula, invert, false);
    }

    public Answer isSatisfiable (BooleanFormula formula, Boolean invert, Boolean generateModel) {
        solverCalls++;
        Boolean isSatisfiable = false;
        try (ProverEnvironment prover = generateModel ? context.newProverEnvironment(ProverOptions.GENERATE_MODELS) : context.newProverEnvironment()) {
            prover.addConstraint(formula);
            isSatisfiable = !prover.isUnsat();
            // No model exists when unsat, will throw exception if asked for
            if (generateModel && isSatisfiable) {
                modelAssignments = prover.getModelAssignments();
            }
            if (invert) {
                return Answer.createAnswer(isSatisfiable).negate();
            }
            prover.close();
            return Answer.createAnswer(isSatisfiable);
        } catch (Exception e) {
            System.out.println("Error in isSatisfiable: " + e.getMessage());
            return Answer.DONTKNOW;
        } 
    }
}