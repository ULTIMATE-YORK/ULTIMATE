
<img width="616" height="119" alt="ultimate_logo_large" src="https://github.com/user-attachments/assets/8b28dcda-29b9-4ee0-95fc-df28893e8dfc" />


# ULTIMATE: A Multi-Model Stochastic System Modelling, Verification and Synthesis Framework

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

[Python3](https://www.python.org/downloads/)

[Maven](https://maven.apache.org/download.cgi)

The installation checklist is:

- Clone the repo
- Configure config.json
- Install the Python requirements
- Set ULTIMATE_DIR environment variable
- Set MODEL_CHECKING_ENGINE_LIBS_DIRECTORY within evochecker_config.properties
- Run or package using Maven

Detailed instructions can be found below.

#### Configuring config.json

The file config.json (in ULTIMATE_MODEL_MANAGER) supplies ULTIMATE with the locations of various executable dependencies. These must be set prior to running ULTIMATE.

Open the file and set the locations for Storm, PRISM, PRISM-Games, and Python3 for your system.

*Important: You must specify the path to the binaries, not to the installation folders.*
*Tip: It is recommended to use a virtual environment for Python, although this is not required.*

An example of a working config.json is shown below:

```json
{
"stormInstall":"/home/YourUsername/storm/build/bin/storm",
"stormParsInstall":"/home/YourUsername/storm/build/bin/storm-pars",
"prismInstall":"prism/bin/prism",
"prismGamesInstall":"prism-games/bin/prism",
"pythonInstall":"../.venv/bin/python3"
}
```
*Tip: Sometimes it may be beneficial to create a symbolic link in ULTIMATE_MODEL_MANAGER to the binaries. Such a link was used for PRISM and PRISM-games in the example above.*

#### Install the Python requirements

Make sure you have the Python requirements installed. This can be done from the requirements.txt in the main directory with:

`pip install -r requirements.txt`

### Set ULTIMATE_DIR environment variable

Add an environment variable to your system called 'ULTIMATE_DIR' which points to the ultimate/ULTIMATE_MODEL_MANAGER directory. For example, Linux users can add the following to their .bashrc:

```console
export ULTIMATE_DIR=<path to ultimate/ULTIMATE_MODEL_MANAGER>
```

*Important: This variable must point to /ULTIMATE_MODEL_MANAGER (not /ultimate)*

#### Set MODEL_CHECKING_ENGINE_LIBS_DIRECTORY within evochecker_config.properties

ULTIMATE uses [EvoChecker](https://github.com/gerasimou/EvoChecker/) to perform parameter synthesis. EvoChecker's settings are set by the evochecker_config.properties file (in /ULTIMATE_MODEL_MANAGER). Prior to running ULTIMATE, one must set MODEL_CHECKING_ENGINE_LIBS_DIRECTORY within evochecker_config.properties to point to the correct model checking library in libs. This will be:

- `libs/runtime-amd64` if you are using a machine with x86-64 (AMD64) architecture.
- `libs/runtime` otherwise.

#### Run or Package using Maven

To use ULTIMATE in headless mode, you will need to package the jar by running:

`mvn clean package`

Otherwise, you can start the GUI as described below.

### Running the GUI:

Still within the folder 'ULTIMATE_MODEL_MANAGER', the fastest way to start the GUI is via maven. Simply run:

```console
mvn exec:java
```

Alternatively, if the 'exec:java' command does not work, you can run the jar file directly. However, you will need to provide the module path for the JavaFX SDK you have downloaded. Once the project has been built, using `mvn clean install`, to run the following:

```console
java --module-path <path_to_javafx_sdk/libs> --add-modules javafx.controls,javafx.fxml -jar <path_to_jar>
```

### Using the Tool via the GUI

The tool has a simple operation. By default, the tool will launch into a 'blank project' containing no models. The user can either choose to load an exisitng ultimate file using *File -> Load* or a single prism model file by pressing the *+* button next to the *Models* label. This will open a file dialog for the user to choose the appropriate file.

For each model in the current project, there will be a number of *Uncategorised Parameters* that are found by parsing the provided model file. These are any constants in the model file that have no value set. 

For each *Uncategorised Parameter*, the user can choose to define it as an *External Parameter*, a *Dependency Parameter* or an *Internal Parameter*. *External Parameters* will require the user to supply a fixed value, a range  or a data (see note) file in the case of a learned value (Mean or Bayes for example). *Dependency Parameters* will require the user to choose another model in the project on which the *Dependency Parameter* depends on. Additionally, a *Property* must be given which will be verified on the chosen model. Fianlly, an *Internal Parameter* is defined as an interger set, a float set or a boolean. In the case of the first two, a range and interval will be provided. 

Once every *Uncategorised Parameter* has been defined, the user may add *properties* to models by navigating to the *Properties* tab within the tool. Select a model from the left to add a property to and then press the *+* button to add a property. 

To verify a property, select it (it must be highlighted) and click the *Verify* button (still inside the Property tab). If verification is succesful, a result will appear in the textbox below the button. If verification fails, open the *Logs* tab to view the source of the error. These logs can also be saved to a file. Results are displayed in a list view underneath the verify button. The user can toggle between showing all results or only the results of the currently selected Model/Property. When verification is run and there are ranged *External Parameters* in the World Model, there will be multiple results (for each possible configuration). Clicking on such results will display a *'Plot Results'* button which will produce line plots. If there is a single ranged parameter, this will be plotted on the x-axis. When multiple ranged parameters exist, the user will be asked to choose which one to ploit on the x-axis.

The current project can be saved by pressing *File -> Save*.

**NOTE: The project file must be saved in the same directory as all the model files as it uses relative paths to find the files. ALL data files for learned values must be in a folder named 'data' in the same folder as the project file and model files.**

### Use of Headless Mode

ULTIMATE features a headless mode alongside the GUI. 

To run headless mode, first compile it with `mvn clean install` in the ULTIMATE_MODEL_MANAGER folder. Then use:

```console
java -jar target/ultimate-headless.jar -pf <project file> -m <model ID> -p <property> -o <output directory>
```

|Argument|Description|
|-|-|
|-pf| Project file path: path to a .ultimate file which defines the world model. These can be constructed and modified via the GUI.|
|-m| Model ID: the ID of the specific model you wish to investigate within the world model.|
|-p| Property: either the PCTL definition of a property (e.g. Pmin=?[F "done"]) or a file containing one such property per line. If left blank, all the properties of the model specified by the model ID within the .ultimate file will be verified.|
|-o| Output directory path: path to a directory at which ULTIMATE will output its verification results (with a random file name). If left blank, ULIMATE will print its results but not save them to a file.

You will need a valid .ultimate file to use headless mode. This is best obtained by constructing a world model with the GUI.

### Property Synthesis

ULTIMATE can be used to optimise the free parameters of world models. This is accomplished via integration with [EvoChecker](https://github.com/gerasimou/EvoChecker/). Given a world model with free ('internal') parameters and a set of objectives and constraints, ULTIMATE uses EvoChecker to evolve the population of internal parameter configurations and thus generate a Pareto set of parameter values which optimise for the objectives whilst respecting the constraints.

Synthesis presently only works in headless mode. To use it, simply run the headless version as described above on a .ultimate model which contains internal parameters. ULTIMATE will detect this as a synthesis problem, and run invoke EvoChecker. Do not provide a property file or definition; all properties, objectives, and constraints are defined within the .ultimate file. Synthesis may take some time, as ULTIMATE will invoke EvoChecker, which performs evolution and invokes ULTIMATE once per genetic individual in order to find the associated values of the objectives and constraints.

To adjust the behaviour of EvoChecker, one may modify evochecker_config.properties. Important settings include:

|Setting|Description|
|-|-|
|ALGORITHM|The genetic algorithm used for the evolution: NGSAII, MOCELL, SPEA2, RANDOM.|
|POPULATION_SIZE|The size of the initial population for the GA.|
|MAX_EVALUATIONS|The total number of ULTIMATE invocations. Note that EvoChecker invokes ULTIMATE once per individual per generation. Therefore if MAX_EVALUATIONS = 2*POPULATION_SIZE, evolution will last roughly two generations.|
|PLOT_PARETO_FRONT|Whether or not to plot the Pareto front after synthesis.|
|VERBOSE|Sets the verbosity of EvoChecker. If TRUE then the fitness of each individual will be printed.|
|MODEL_CHECKING_ENGINE_LIBS_DIRECTORY | Libs for EvoChecker This should be libs/runtime or libs/runtime-amd64 depending on your architecture.|


### Video Guide

https://github.com/user-attachments/assets/b4111cc0-abfb-4eb9-ac54-3f9ddd7df000

### Publications

Calinescu, Radu, et al. "Verification and External Parameter Inference for Stochastic World Models." [arXiv preprint arXiv:2503.16034 (2025)](https://doi.org/10.48550/arXiv.2503.16034)
