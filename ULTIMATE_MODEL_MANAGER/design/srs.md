# Software Requirements Specification

### ULTIMATE Multi-model Stochastic System Modelling, Verification and Synthesis Framework  

---
<p align="center">
  <img src="resources/uoy.png" alt="University of York Logo">
</p>
---

---
**Revision History**

| Date         | Version      | Description  | Author       |
|:------------:|:------------:|:------------:|:------------:|
|16/04/2025    | 0.1          | Initial draft| Micah Bassett|
|24/04/2025    | 0.1          | Minor changes| Micah Bassett|
---

## Table of Contents

- [1. Introduction](#1-introduction)
	- [1.1 Purpose](#11-purpose)
	- [1.2 Scope](#12-scope)
	- [1.3 Terms and Definitions](#13-terms-and-definitions)

- [2. Overall Description](#2-overall-description)
	- [2.1 Product Perspective](#21-product-perspective)
	- [2.2 User Classes](#22-user-classes)
	- [2.3 Operating Environment](#23-operating-environment)
	- [2.4 Assumptions and Dependencies](#24-assumptions-and-dependencies)
	
- [3. Functional Requirements](#3-functional-requirements)
	- [3.1 Project Management](#31-project-management)
	- [3.2 Model Management](#32-model-management)
	- [3.3 Parameter Management](#33-parameter-management)
	- [3.4 Property Management](#34-property-management)
	- [3.5 Learning](#35-learning)
	- [3.6 Verification](#36-verification)
	- [3.7 Logging](#37-logging)
	- [3.8 CLI](#38-cli)

- [4. Non-Functional Requirements](#4-non-functional-requirements)
	- [4.1 Usability](#41-usability)
	- [4.2 Performace](#42-performance)
	- [4.3 Compatibility](#43-compatibillity)
	- [4.4 Reliability](#44-reliability)
	- [4.5 Maintainability and Extensibility](#45-maintainability-and-extensibility)

## 1. Introduction

### 1.1 Purpose

This software requirements specification outlines both the functional and non-functional for release 1.0 of the *ULTIMATE Multi-model Stochastic System Modelling, Verification and Synthesis Framework Tool*. This document is primarily intended for future developers and maintainers of the tool. However, it also serves as a useful resource for researchers and users seeking to understand the system's capabilities, internal structure, and underlying methodologies.

### 1.2 Scope

Existing probabilistic model checkers (PMCs), such as PRISM and Storm, restrict users to verifying properties (various probabilistic temporal logics) on a single probabilistic model at a time. For complex systems, which may require many such models, perhaps with dependencies between them, these tools are not sufficient. ULTIMATE aims to address this by providing users of the tool with the ability to create *World Models* which are systems in which many probabilistic models may exists. 

Furthermore, ULTIMATE extends simple probabilistic model representation with the addition of special parameter types. Users can customise models by adding *External Parameters (EPs)*, *Dependency Parameters (DPs)* and *Internal Parameters (IPs)* to the model. These allow for the creation of complex systems of models. Each parameter type fulfils a unique role. EPs allow for the model to be interfaced to data or machine learning components. DPs, crucially, provide a way for the user to define dependencies between models which is a common feature of real-world systems. Finally, IPs allow for internal values which can be optimised through GAs.

Below is a short description of ULTIMATE from the GitHub page: 

> ULTIMATE supports the formal representation, verification and synthesis of multi-model stochastic systems. These are combinations of heterogenous, interdependent stochastic models (discrete and continuous-time Markov chains, Markov decision processes, partially observable Markov decision processes, stochastic games, etc.) required to verify the dependability and performance of modern software-intensive systems.

> Through its unique integration of multiple probabilistic and parametric model checking paradigms, and underpinned by a novel verification method for handling model interdependencies, ULTIMATE unifies the modelling of probabilistic and nondeterministic uncertainty, discrete and continuous time, partial observability, and the use of both Bayesian and frequentist inference to exploit domain knowledge and data about the modelled system and its context.

### 1.3 Terms and Definitions

| Term | Definition |
|:----|:----------|
|PMC|Probabilistic Model Checker|
|PRISM| PRISM is a PMC tool|
|Storm| Storm is a PMC tool|
|World Model| An ULTIMATE representation of multi-model systems|
|GAs| Genetic Algorithms|
|PRISM model file| This is a text file written in the PRISM language - it describes a stochastic model |
|UP| Uncategorsied Parameter|
|EP| External Parameter|
|DP| Dependency Parameter|
|IP| Internal Parameter|

## 2. Overall Description

### 2.1 Product Perspective

**Positioning**

The ULTIMATE tool is a specialized tool designed for managing and verifying multi-model stochastic systems, built as an extension to existing probabilistic model checkers (PMCs) like PRISM and Storm. Unlike these traditional PMCs, which focus primarily on individual models, ULTIMATE allows users to work with systems of models, enabling complex interdependencies and verification tasks across multiple models.

This tool bridges the gap between simple model checking and sophisticated multi-model system synthesis by introducing Dependency Parameters, External Parameters, and Internal Parameters—which are unique to ULTIMATE and are not part of the base PRISM/Storm functionality. By managing these parameters and integrating different models, the tool offers a unified framework for modeling, defining, and verifying stochastic systems with interrelated components.

**System Boundaries**

The ULTIMATE tool provides a self-contained graphical environment for managing probabilistic models, with a primary focus on model creation, parameter definition, and verification. While the graphical components of the tool are fully self-contained, the verification engine requires integration with external tools (PRISM and Storm) to perform model checking tasks.

Within the system:

* Users can create, manage, and define models and parameters.

* Users can initiate and view verification results from selected models.

* The tool supports project-based workflows, saving and loading complete system configurations for reuse.

Outside the system:

* The PRISM and Storm model checkers are required for verification. These tools must be installed on the user's system.

* The tool interacts with a configuration file that ensures PRISM and Storm binaries are correctly located for verification to occur.

* Users must also provide model files in PRISM language format (typically .pm files) that are loaded into the tool for parameterization and verification.

**Major Interfaces**

The ULTIMATE tool features two primary interfaces: the Graphical User Interface (GUI) and the Command-Line Interface (CLI).

Graphical User Interface (GUI):

* Users can load, edit, and manage projects.

* Models are added by selecting PRISM model files, and parameters (Environment Parameters, Dependency Parameters, and Internal Parameters) are defined and edited via dialog boxes.

* The interface allows for visual feedback on model structures, parameters, and verification results.

Command-Line Interface (CLI):

* Users can pass project files and verification parameters directly to the tool through the command line.

* The CLI mode enables batch operations, making it ideal for automation and integration into larger workflows.

* Users specify verification tasks, input models, and parameters using a set of command-line arguments.

**Dependencies**

The ULTIMATE tool has several key dependencies:

* PRISM: The tool uses PRISM as the default Probabilistic Model Checker for verification. PRISM must be installed on the system, and its binaries are referenced through the tool’s configuration file.

* Storm: Storm is an alternative PMC for model checking, requiring separate installation. The tool prompts users to provide the installation path for Storm if they wish to use it.

* Configuration File: A configuration file is used to check the installation status of PRISM and Storm, ensuring the binaries are available for verification tasks.

* Model Files: Models must be in the PRISM language format (typically .pm files). These files are loaded into the tool to define parameters and perform verification. 

### 2.2 User Classes

**Researchers (Primary User Class)**

The primary users of the ULTIMATE tool are researchers engaged in the study of stochastic systems. This includes:

* Students: Graduate or undergraduate students using the tool for coursework or academic projects.

* Professors: Faculty members utilizing the tool to support research activities, collaboration, and publication.

* Postdoctoral Researchers: Researchers conducting advanced investigations into the modeling and verification of complex stochastic systems, typically as part of a research group or independent study.

**Goals and Tasks of Researchers**

* Verification of Stochastic Systems: Researchers use ULTIMATE to create systems of probabilistic models, define parameters, and verify their correctness or performance. This is done to validate hypotheses or to demonstrate the reliability of stochastic systems in academic papers or research studies.

* Parameterization of Models: Researchers utilize the tool to define dependency parameters, external parameters, and internal parameters that customize existing models (usually PRISM models) for their research needs.

* Interactive Model Management: They need a straightforward way to load models, define and manage parameters, and run verification tasks on different configurations of models, both interactively (via the GUI) and programmatically (via the CLI).

**Future Tool Maintainers and Developers (Secondary User Class)**

While not the primary users of the tool in its current form, maintainers and developers may become important stakeholders in the future as the tool evolves. This user class would be responsible for maintaining, extending, and improving the ULTIMATE tool. These roles may include:

* Software Maintainers: Individuals who ensure that the tool remains up-to-date, fixing bugs and ensuring compatibility with newer versions of PRISM and Storm, and other dependencies. They may also work on enhancing the tool’s features based on user feedback or the changing needs of the academic community.

* Tool Developers: Academic software engineers or researchers who contribute to the development of new features, integrations with additional PMCs, or improvements to the tool’s architecture. This class could also include those building additional interfaces or extensions for more advanced use cases.

**Goals and Tasks of Maintainers and Developers**

* Ensure Tool Compatibility: Maintainers must ensure that the tool integrates smoothly with new versions of PRISM, Storm, and other dependencies. They must also maintain the configuration file and update installation requirements as necessary.

* Improve Functionality: Developers focus on adding new features such as additional verification methods, user interface enhancements, or extended model support.

* Bug Fixing and Documentation: Tool maintainers ensure the tool runs smoothly for all users, handling bug reports, code maintenance, and user documentation.

### 2.3 Operating Environment

The ULTIMATE tool is designed to run on Unix-based operating systems, specifically Linux and macOS environments. The tool has been developed and tested exclusively on these platforms. At present, Windows is not officially supported, and compatibility has not been verified. Future updates may consider Windows support if demand or contributions from the community necessitate it.

**Software Requirements:**

* Operating System: Linux (Ubuntu 20.04+), macOS (Monterey+)

* Python 3.8+ 

* Java 22

* PRISM model checker

* Storm model checker

* Maven

### 2.4 Assumptions and Dependencies

ULTIMATE relies on several external and internal software components to perform its core functions. The following dependencies must be satisfied for the tool to operate correctly:

**External Dependencies**

* PRISM Model Checker

Must be installed manually by the user.
The tool requires the path to the PRISM binary to be provided in the configuration file for Storm integration.

* Storm Model Checker

Must be installed manually by the user.
The tool requires the path to the Storm binary to be provided in the configuration file for Storm integration.

* Java Runtime Environment (JRE)

* Python 3.8+

**Configuration**

ULTIMATE uses a configuration file to specify paths to installed binaries (such as Storm), tool preferences, and other system-level settings.
On first launch, the tool checks the configuration file to ensure that all required binaries are available and prompts the user to configure any missing dependencies.

**Model Files**

ULTIMATE operates on PRISM model files (.prism or .sm formats).
Each model used in a project must be written in the PRISM language and must contain clearly defined constants that can be parameterized by the tool.
Model files are not modified by the tool; however, users may define parameters (environment, dependency, internal) based on constants found in these files.

## 3. Functional Requirements

Below is a list of functional requirements for the ULTIMATE tool. These requirements outline the expected capabilities of the tool, both from a user-facing and system perspective.

### 3.1 Project Management

- **FR1**: The user shall be able to create a new blank project.
- **FR2**: The user shall be able to load an existing project into the tool from an ultimate file.
- **FR3**: The user shall be able to save the current project as an ultimate file.
- **FR4**: The user shall be able to use "Save As" to save the project under a new name or location.
- **FR5**: The tool should generate ultimate files (json file) that fully describe all the models and thier defined parameters/properties.

### 3.2 Model Management

- **FR6**: The user shall be able to add a model to the project by selecting a PRISM model file.
- **FR7**: The user shall be able to view the PRISM file of a selected model within the tool.
- **FR8**: The user shall be able to delete a model from the project.

### 3.3 Parameter Management

- **FR9**: The tool shall detect undefined constants in a PRISM model file and present them as uncategorized parameters (UPs).
- **FR10**: The tool should detect defined parameters in the ultimate file and add them to the correct model. 
- **FR11**: The user shall be able to define each UP as:
  - External Parameters (EPs)
  - Dependency Parameters (DPs)
  - Internal Parameters (IPs)
- **FR12**: The user shall be able to:
  - Edit an existing parameter’s definition.
  - Remove a parameter and return it to the list of UPs.

### 3.4 Property Management

- **FR13**: The user shall be able to define a property for a given model.
- **FR14**: The user should be able to delete properties for a given model.
- **FR15**: The user should be able to edit a propery definition.
- **FR16**: The user shall be able to import multiple properties via a property file.

### 3.5 Learning

- **FR17**: The tool should be able to learn values for EPs from the data file provided when they were defined. 

### 3.6 Verification

- **FR18**: The user shall be able to initiate verification of a model and selected property.
- **FR19**: The user shall be able to choose between available PMCs (PRISM or Storm) for verification.
- **FR20**: The system shall ensure all dependencies are defined before running verification on dependent models.
- **FR21**: The system shall display a progress indicator during verification.
- **FR22**: The system shall present results including:
  - Status of verification
  - Result values

### 3.7 Logging

- **FR20**: The system shall maintain a log of user actions and system processes.
- **FR21**: The user shall be able to:
  - View logs after verification
  - Save logs to a file

### 3.8 CLI

- **FR22**: The user shall be able to execute verification from the command line using an existing project file.
- **FR23**: The CLI shall accept arguments for:
  - Model selection
  - Property selection
  - PMC choice
  - Output format and destination

## 4. Non-Functional Requirements

### 4.1 Usability

- **NFR1**: The graphical user interface shall be intuitive and easy to navigate for users.
- **NFR2**: The tool shall support keyboard shortcuts for common actions to improve workflow efficiency.
- **NFR3**: The tool shall provide clear visual cues and labels for all interactive components, such as models, parameters, and properties.
- **NFR4**: Error messages and system prompts shall be clear and informative, aiding in user troubleshooting.
- **NFR5**: The command-line interface shall provide concise help messages and usage instructions via a --help flag.

### 4.2 Performance

- **NFR6**: If verification is going to take longer than 2 minutes, the user should be informed.

### 4.3 Compatibility

- **NFR7**: The tool should be compatible with MacOS and Linux operating systems.
- **NFR8**: The tool should be compatible with JRE 22 and Python3.8.

### 4.4 Reliability

- **NFR9**: The tool should never crash without displaying an error message to the user.

### 4.5 Maintainability and Extensibility

- **NFR10**: Code should be clearly documented and well structured.
- **NFR11**: Logs should be saved to a crash report in the event the tool crashes.  