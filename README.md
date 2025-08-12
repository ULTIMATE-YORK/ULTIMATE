# ULTIMATE Multi-model Stochastic System Modelling, Verification and Synthesis Framework

ULTIMATE supports the formal representation, verification and synthesis of multi-model stochastic systems. These are combinations of heterogenous, interdependent stochastic models (discrete and continuous-time Markov chains, Markov decision processes, partially observable Markov decision processes, stochastic games, etc.) required to verify the dependability and performance of modern software-intensive systems.

Through its unique integration of multiple probabilistic and parametric model checking paradigms, and underpinned by a novel verification method for handling model interdependencies, ULTIMATE unifies the modelling of probabilistic and nondeterministic uncertainty, discrete and continuous time, partial observability, and the use of both Bayesian and frequentist inference to exploit domain knowledge and data about the modelled system and its context. 


## Installation

### Prerequisites

**Supported OS: MacOS, Linux**

[Java SE 22](https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html)

[JavaFX SDK](https://gluonhq.com/products/javafx/)

[Storm model checker](https://www.stormchecker.org/index.html)

[PRISM model checker](https://www.prismmodelchecker.org/)

[PRISM Games model checker](https://www.prismmodelchecker.org/games/download.php)

[python](https://www.python.org/downloads/)

[Maven](https://maven.apache.org/download.cgi)

The lastest version of the JavaFX SDK should be downloaded as well as Java 22 or later as the SDK requires this. 
Download and build storm and prism according to their respective installation guides.

Firstly, clone the repo in a convenient location. Navigate to the folder 'ULTIMATE_MODEL_MANAGER' within the cloned repo. Here you will find a file called *config.json*.
This file stores the installation locations of storm and prism. These will need to be set for the tool to access them during verification. The file looks like this:

```json
{"stormInstall":"","stormParsInstall":"","prismInstall":"","prismGamesInstall":"","pythonInstall":""}
```
The full path to each executable for storm, storm-pars and prism will need to be set or the tool will report and error and close. For example:

```json
{"stormInstall":"/Users/user/Desktop/storm","stormParsInstall":"/Users/user/Desktop/storm-pars","prismInstall":"/Users/user/Desktop/prism","prismGamesInstall":"/Users/user/Desktop/prismg","pythonInstall":"/opt/homebrew/bin/python3"}
```

Make sure you have the Python requirements installed. This can be done from the requirements.txt in the main directory.

### Option 1:

Still within the folder 'ULTIMATE_MODEL_MANAGER', the simplest way to run the tool is to use the maven integration in the project. Run the following commands:

```console
mvn clean install
mvn exec:java
```
### Option 2:

Alternatively, if the 'exec:java' command does not work, you can run the jar file directly. However, you will need to provide the module path for the JavaFX SDK you have downloaded.
Firstly, from the 'ULTIMATE_MODEL_MANAGER' folder, run:

```console
mvn clean install
```
Once the project has been built, you will need to run the following:

```console
java --module-path <path_to_javafx_sdk/libs> --add-modules javafx.controls,javafx.fxml -jar <path_to_jar>
```

### Running the tool

The tool has a simple operation. By default, the tool will launch into a 'blank project' containing no models. The user can either choose to load an exisitng ultimate file using *File -> Load* or a single prism model file by pressing the *+* button next to the *Models* label. This will open a file dialog for the user to choose the appropriate file.

For each model in the current project, there will be a number of *Uncategorised Parameters* that are found by parsing the provided model file. These are any constants in the model file that have no value set. 

For each *Uncategorised Parameter*, the user can choose to define it as an *External Parameter*, a *Dependency Parameter* or an *Internal Parameter*. *External Parameters* will require the user to supply a fixed value, a range  or a data (see note) file in the case of a learned value (Mean or Bayes for example). *Dependency Parameters* will require the user to choose another model in the project on which the *Dependency Parameter* depends on. Additionally, a *Property* must be given which will be verified on the chosen model. Fianlly, an *Internal Parameter* is defined as an interger set, a float set or a boolean. In the case of the first two, a range and interval will be provided. 

Once every *Uncategorised Parameter* has been defined, the user may add *properties* to models by navigating to the *Properties* tab within the tool. Select a model from the left to add a property to and then press the *+* button to add a property. 

To verify a property, select it (it must be highlighted) and click the *Verify* button (still inside the Property tab). If verification is succesful, a result will appear in the textbox below the button. If verification fails, open the *Logs* tab to view the source of the error. These logs can also be saved to a file. Results are displayed in a list view underneath the verify button. The user can toggle between showing all results or only the results of the currently selected Model/Property. When verification is run and there are ranged *External Parameters* in the World Model, there will be multiple results (for each possible configuration). Clicking on such results will display a *'Plot Results'* button which will produce line plots. If there is a single ranged parameter, this will be plotted on the x-axis. When multiple ranged parameters exist, the user will be asked to choose which one to ploit on the x-axis.

The current project can be saved by pressing *File -> Save*.

**NOTE: The project file must be saved in the same directory as all the model files as it uses relative paths to find the files. ALL data files for learned values must be in a folder named 'data' in the same folder as the project file and model files.**

### Use of headless mode

ULTIMATE features a headless mode alongside the GUI.

To run headless mode, use:

```
java -jar target/ultimate-headless.jar -pf <project file> -m <model ID> -p <property> -o <output directory>
```
|Argument|Description|
|-|-|
|-pf| Project file path: path to a .ultimate file which defines the world model. These can be constructed and modified via the GUI.|
|-m| Model ID: the ID of the specific model you wish to investigate within the world model.|
|-p| Property: either the PCTL definition of a property (e.g. Pmin=?[F "done"]) or a file containing one such property per line. If left blank, all the properties of the model within the .ultimate file will be verified.|
|-o| Output directory path: path to a directory at which ULTIMATE will output its verification results (with a random file name). If left blank, ULIMATE will print its results but not save them to a file.



### Video Guide

https://github.com/user-attachments/assets/b4111cc0-abfb-4eb9-ac54-3f9ddd7df000

### Publications

Calinescu, Radu, et al. "Verification and External Parameter Inference for Stochastic World Models." [arXiv preprint arXiv:2503.16034 (2025)](https://doi.org/10.48550/arXiv.2503.16034)
