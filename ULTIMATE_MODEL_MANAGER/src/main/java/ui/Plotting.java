package ui;

import java.io.IOException;
import java.nio.file.Paths;

import data.SynthesisRun;
import data.VerificationRun;
import sharedContext.SharedContext;

public class Plotting {

    private static final String synthesisScriptFile = "/scripts/plotSynthesis.py";
    private static final String rangedVerificationScriptFile = "/scripts/plotRangedVerification.py";

    @SuppressWarnings("unused")
    public static void plotParetoFront(SynthesisRun run) throws IOException {

        System.out.println("Plotting Pareto front from " + run.getParetoFrontFilePath());

        String python3Dir = SharedContext.getProject().getPythonInstall();
        ProcessBuilder processBuilder = new ProcessBuilder(python3Dir,
                Paths.get(System.getenv("ULTIMATE_DIR") + synthesisScriptFile).toString(),
                run.getParetoFrontFilePath());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

    }

    @SuppressWarnings("unused")
    public static void plotParetoSet(SynthesisRun run) throws IOException {

        int numInternalParameters = run.getAllInternalParameterNames().size();
        System.out.println("Plotting Pareto set from " + run.getParetoSetFilePath());

        String python3Dir = SharedContext.getProject().getPythonInstall();
        ProcessBuilder processBuilder = new ProcessBuilder(python3Dir,
                Paths.get(System.getenv("ULTIMATE_DIR") + synthesisScriptFile).toString(),
                run.getParetoSetFilePath());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

    }

    @SuppressWarnings("unused")
    public static void plotRangedVerification(VerificationRun run, String xVariable, String yVariable, String zVariable)
            throws IOException {

        String python3Dir = SharedContext.getProject().getPythonInstall();
        ProcessBuilder processBuilder = new ProcessBuilder(python3Dir,
                Paths.get(System.getenv("ULTIMATE_DIR") + rangedVerificationScriptFile).toString(),
                run.getDataFile(),
                xVariable,
                yVariable,
                zVariable != null ? zVariable : "");

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

    }

}
