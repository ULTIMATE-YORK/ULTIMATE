package ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.SynthesisRun;
import data.VerificationRun;
import javafx.concurrent.Task;
import sharedContext.SharedContext;
import utils.Alerter;

public class Plotting {

	private static final String synthesisScriptFile = "/scripts/plotSynthesis.py";
	private static final String rangedVerificationScriptFile = "/scripts/plotRangedVerification.py";

	private static void runPlottingScript(String scriptRelativePath, String[] arguments) {
		
		Task<Void> task = new Task<Void>() {

			@Override
			public Void call() throws Exception {

				List<String> command = new ArrayList<String>();
				command.add(SharedContext.getProject().getPythonInstall());
				command.add(System.getenv("ULTIMATE_DIR")+scriptRelativePath);
				command.addAll(Arrays.asList(arguments));
				
				ProcessBuilder processBuilder = new ProcessBuilder(command);

				Process process = processBuilder.start();

				int exitCode = 0;
				try {
					exitCode = process.waitFor();
				} catch (InterruptedException e) {
					System.out.println("Plotting interrupted by user.");
				}

				if (exitCode != 0) {
					throw new Exception(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
				}

				return null;
			}
		};
		
		task.setOnFailed(event-> {
			Throwable e = task.getException();
			Alerter.showErrorAlert("Plotting Error",
					"An error occurred whilst running the Python-based plotting script:\n\n"
							+ e.getMessage());
		});

		new Thread(task).start();
		
	}
	
	
	@SuppressWarnings("unused")
	public static void plotParetoFront(SynthesisRun run) throws IOException {
		
		System.out.println("Plotting Pareto front from " + run.getParetoSetFilePath());
		runPlottingScript(synthesisScriptFile, new String[]{run.getParetoFrontFilePath()});
		
	}

	@SuppressWarnings("unused")
	public static void plotParetoSet(SynthesisRun run) throws IOException {

		System.out.println("Plotting Pareto set from " + run.getParetoSetFilePath());
		runPlottingScript(synthesisScriptFile, new String[] {run.getParetoSetFilePath()});

	}

	@SuppressWarnings("unused")
	public static void plotRangedVerification(VerificationRun run, String xVariable, String yVariable, String zVariable)
			throws IOException {

		System.out.println("Plotting ranged verification results from " + run.getDataFile());
		runPlottingScript(rangedVerificationScriptFile, new String[] {run.getDataFile(), xVariable, yVariable, zVariable != null ? zVariable : ""});
		String python3Dir = SharedContext.getProject().getPythonInstall();
		
	}

}
