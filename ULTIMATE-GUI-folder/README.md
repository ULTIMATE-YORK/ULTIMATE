# Setting up the JavaFX project in eclipse

## Ensure all libraries, dependencies, and extensions are correctly installed
  
  - Open the eclipse marketplace and install the e(fx)clipse 3.8.0 extension. Restart Eclipse.
  - Download and unzip the appropriate [JavaFX SDK](https://openjfx.io/) for your system.
  - Navigate to **Preferences -> Java -> Build Path -> User Libraries**. Remove JavaFX if it appears here.
  - Click new, name the library JavaFX, click add.
  - Click on the new library and choose 'add external JARs'.
  - Navigate to the 'lib' folder where you unzipped JavaFX and choose all the JARs inside this directory. Apply and close.
  - Right-click on the project root. **Build Path -> Configure Build Path -> Libraries -> Classpath -> Add Library -> User Library -> JavaFX.** Click Add.
  - Download the [JSON JAR](https://central.sonatype.com/artifact/org.json/json/versions). Save this in a convenient location.
  - Right-click on the project root. **Build Path -> Add external archives**. Select the json jar file and add.

## Set up run configurations

  - Go to **Run -> Run Configurations**.
  - In the 'VM arguments' section add the following: --module-path "/Path_to/javafx-sdk-23.0.1" --add-modules javafx.controls,javafx.fxml
  - Click on the dependencies tab, remove JavaFX from classpath here. 
  - Click on Modulepath Entries, Advanced, Add Library, User Library, choose JavaFx and apply
  - On the **Dependencies** tab, click on **Classpath entries -> Add external JARs**, choose the json jar and add.

## If the project is not running yet (on macOS)

  - Go to run configurations and disable the option: Use the -XstartOnFirstThread argument when launching with SWT
	
## HELP

  - [A useful youtube guide](https://www.youtube.com/watch?v=nz8P528uGjk)