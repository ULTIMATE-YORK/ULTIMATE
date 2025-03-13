# ULTIMATE
We introduce the **UniversaL stochasTIc Modelling, verificAtion and synThEsis** (ULTIMATE) tool that supports the representation, verification and synthesis of heterogeneous multi-model stochastic systems (Markov decision processes, discrete time Markov chains, stochastic games, etc.) with complex model interdependencies. Through its unique integration of multiple Parametric Model Checking PMC paradigms, and underpinned by a novel verification method for handling model interdependencies, ULTIMATE unifies&mdash;for the first time&mdash;the modelling of probabilistic and nondeterministic uncertainty, discrete and continuous time, partial observability, and the use of both Bayesian and frequentist inference to exploit domain knowledge and data about the modelled system and its context. We provide a comprehensive set of case studies to demonstrate the versatility and effectiveness of our novel verification framework.




## Installation

### Prerequisites

**Supported OS: MacOS, Linux**

[Java SE 22](https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html)

[JavaFX SDK](https://gluonhq.com/products/javafx/)

[Storm model checker](https://www.stormchecker.org/index.html)

[PRISM model checker](https://www.prismmodelchecker.org/)

The lastest version of the JavaFX SDK should be downloaded as well as Java 22 or later as the SDK requires this. 
Download and build storm and prism according to their respective installation guides.

### Installation

Firstly, clone the repo in a convenient location. Navigate to the folder 'ULTIMATE_MODEL_MANAGER' within the cloned repo. Here you will find a file called *config.json*.
This file stores the installation locations of storm and prism. These will need to be set for the tool to access them during verification. The file looks like this:

```json
{"stormInstall":"","stormParsInstall":"","prismInstall":""}
```
The full path to each executable for storm, storm-pars and prism will need to be set or the tool will report and error and close. For example:

```json
{"stormInstall":"/Users/user/Desktop/storm","stormParsInstall":"/Users/user/Desktop/storm-pars","prismInstall":"/Users/user/Desktop/prism"}
```

Still within the folder 'ULTIMATE_MODEL_MANAGER', the simplest way to run the tool is to use the maven integration in the project. Run the following commands:

```console
mvn clean install
mvn exec:java
```

Alternatively, you can run the jar file directly but you will need to provide the module path for the JavaFX SDK you have downloaded.
Firstly, from the 'ULTIMATE_MODEL_MANAGER' folder, run:

```console
mvn clean install
```
Once the project has been built, you will need to run the following:

```console
java --module-path <path_to_javafx_sdk/libs> --add-modules javafx.controls,javafx.fxml -jar target/ultimate_model_manager-1.0.jar
```

### Running the tool

The tool has a simple operation. By default, the tool will launch into a 'blank project' containing no models. The user can either choose to load an exisitng ultimate file using *File -> Load* or a single prism model file by pressing the *+* button next to the *Models* label. This will open a file dialog for the user to choose the appropriate file.

For each model in the current project, there will be a number of *Uncategorised Parameters* that are found by parsing the provided model file. These are any constants in the model file that have no value set. 

For each *Uncategorised Parameter*, the user can choose to define it as either an *External Parameter* or a *Dependency Parameter*. The former will require the user to supply either a fixed value or a data (see note) file in the case of a learned value (Mean or Bayes for example). The latter will require the user to choose another model in the project on which the *Dependency Parameter* depends on. Additionally, a *property* must be given which will be verified on the chosen model.

Once every *Uncategorised Parameter* has been defined, the user may add *properties* to models by navigating to the *Properties* tab within the tool. Select a model from the left to add a property to and then press the *+* button to add a property. 

To verify a property, select it (it must be highlighted) and click the *Verify* button (still inside the Property tab). If verification is succesful, a result will appear in the textbox below the button. If verification fails, open the *Logs* tab to view the source of the error. These logs can also be saved to a file. 

The current project can be saved by pressing *File -> Save*.

**NOTE: The project file must be saved in the same directory as all the model files as it uses relative paths to find the files
        ALL data files for learned values must be in a folder named 'data' in the same folder as the project file and model files.**

### Video Guide

https://github.com/user-attachments/assets/0ce0242e-65cc-411d-90f4-7d47758a2401
