# ULTIMATE
Software for creating world models for complex and heterogeneous systems


## Case studies

Click on a case study to learn more.

<table>
  <tr>
    <td>
      <a href="https://github.com/ULTIMATE-YORK/WorldModel/tree/main/case_studies/robot%20assistive%20dressing">
        <img src="https://github.com/user-attachments/assets/9f591933-eb47-4105-8a00-8d449eec4c50" alt="Image 1" width="300">
      </a>
    </td>
    <td>
      <a href="https://github.com/ULTIMATE-YORK/WorldModel/tree/main/case_studies/FX%20with%20dynamic%20power%20management">
        <img src="https://github.com/user-attachments/assets/5c5b817d-97b4-4563-9bfa-9e16dbe0d34e" alt="Image 2" width="300">
      </a>
    </td>
  </tr>
  <tr>
    <td>
      <a href="https://github.com/ULTIMATE-YORK/WorldModel/tree/main/case_studies/smart%20movement%20detection">
        <img src="https://github.com/user-attachments/assets/8345c45c-76e9-49c0-ae13-a5e182094ed7" alt="Image 3" width="300">
      </a>
    </td>
    <td>
      <!>
      <a href="https://github.com/ULTIMATE-YORK/WorldModel/tree/main/case_studies/ROBOTNAV">
        <img src="https://github.com/user-attachments/assets/2dbf2139-0afa-402f-9332-baac0c43fe13" alt="Image 4" width="300">
      </a>
    </td>
  </tr>
</table>


## Installation

### Prerequisites

[Java SE 22](https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html)

[JavaFX SDK](https://gluonhq.com/products/javafx/)

[Storm model checker](https://www.stormchecker.org/index.html)

[PRISM model checker](https://www.prismmodelchecker.org/)

The lastest version of the JavaFX SDK should be downloaded as well as Java 22 or later as the SDK requires this. 
Download and build storm and prism according to their respective installation guides.

### Installation

Firstly, clone the repo in a convenient location. Navigate to the folder 'ULTIMATE_MODEL_MANAGER' within the cloned repo. The simplest way to run the tool is to use the maven integration in the project. Run the following commands:

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

### Loading a project
The ULTIMATE tool is currently supported in XX OS.
To install it...

Press +
![image](https://github.com/user-attachments/assets/6a5de79a-3517-45b0-8ad2-518a15acc580)

Select prism model (.dtmc, .ctmc ...)
![image](https://github.com/user-attachments/assets/cfd0f9df-24c6-4269-8819-a4827ff9bb68)

![image](https://github.com/user-attachments/assets/72f95592-d2b7-469a-9ff4-4c2160b2ca41)

Bring up dependency editor
![image](https://github.com/user-attachments/assets/daf752a1-228e-43b7-a2f6-a465d3860932)

fill out 
![image](https://github.com/user-attachments/assets/1e07ebd7-d528-456c-a267-9c9d20511ccf)

added
![image](https://github.com/user-attachments/assets/ad700a2a-fdb0-4fb5-8666-02409e7cc3a6)


Select tapb property, press +, add prop
![image](https://github.com/user-attachments/assets/2267481d-1b39-45e3-aed6-72b59637ded4)


### Configuration

Configuring path to storm Options>Configure Storm
![image](https://github.com/user-attachments/assets/ec843eb6-12eb-496a-b994-ee5e40af8848)

Configure PMC prism vs storm
![image](https://github.com/user-attachments/assets/5cfc0ae8-501f-44cc-b282-0ff2c0d0fee5)


