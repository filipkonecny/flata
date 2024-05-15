package verimag.flata.common;

import java.net.Socket;

import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.log.BasicLogManager;

import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

import com.google.common.collect.ImmutableList;

public class FlataJavaSMT {

    private Configuration config;
    private ShutdownNotifier notifier;
    private LogManager logger;
    private SolverContext mainContext;

    private IntegerFormulaManager ifm;
    private BooleanFormulaManager bfm;
    private QuantifiedFormulaManager qfm;

    private ImmutableList<Model.ValueAssignment> modelAssignments;

    // TODO: remove this (maybe)
    private int solverCalls;

    // TODO: Add options for smt solving
    public FlataJavaSMT() {
        try {
            config = Configuration.defaultConfiguration();
            notifier = ShutdownNotifier.createDummy();
            logger = BasicLogManager.create(config);
            mainContext = SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.YICES2);
            bfm = mainContext.getFormulaManager().getBooleanFormulaManager();

            // Some solvers don't support Integer/Quantified theory
            try {
                ifm = mainContext.getFormulaManager().getIntegerFormulaManager();
            } catch (UnsupportedOperationException uoe) {
                System.out.println("Chosen solver does not support Integer-theory, exiting");
                System.exit(1);
            }

            try { // TODO: remove this?
                qfm = mainContext.getFormulaManager().getQuantifiedFormulaManager();
            } catch (UnsupportedOperationException uoe) {
                System.out.println("Chosen solver does not support Quantifier-theory");
            }

            solverCalls = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IntegerFormulaManager getIfm() {
        return ifm;
    }

    public BooleanFormulaManager getBfm() {
        return bfm;
    }

    public QuantifiedFormulaManager getQfm() {
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
        try (ProverEnvironment prover = generateModel ? mainContext.newProverEnvironment(ProverOptions.GENERATE_MODELS) : mainContext.newProverEnvironment()) {
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
            System.out.println("isSat: " + isSatisfiable);
            return Answer.DONTKNOW;
        } 
    }
}