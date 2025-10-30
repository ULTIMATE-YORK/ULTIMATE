# ---------- Build Stage ----------
# FROM maven:3.9.6-eclipse-temurin-21 AS build
FROM --platform=linux/amd64 maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app


# Copy Maven project files
COPY ./pom.xml .
COPY ./src ./src
COPY ./configDocker.json .
COPY ./evochecker_config.properties .
COPY ./libs ./libs
COPY ./case_studies ./case_studies
COPY ./ULTIMATE_Numerical_Solver ./ULTIMATE_Numerical_Solver


# Build the application with Linux profile to pull JavaFX with 'linux' classifier
RUN mvn clean package -P linux

# ---------- Runtime Stage ----------
FROM --platform=linux/amd64 eclipse-temurin:21-jdk


ENV PRISM_VERSION=4.8 
ENV STORM_VERSION=1.8.0

# Install required system libraries and Xvfb
RUN apt-get update && apt-get install -y \
    git cmake build-essential  \
    wget unzip xvfb \
    libxrender1 libxtst6 libxi6 libglx-mesa0  libgtk-3-0 \
    libcanberra-gtk-module libcanberra-gtk3-module \
    xvfb x11-xkb-utils xfonts-100dpi xfonts-75dpi xfonts-scalable xfonts-cyrillic \
    libgmp10 \
    libz3-4 \
    && apt-get clean

# Install Prism
RUN wget https://github.com/prismmodelchecker/prism/releases/download/v$PRISM_VERSION/prism-$PRISM_VERSION-linux64-x86.tar.gz -O /tmp/prism.tar.gz && \
    mkdir -p /opt/prism && \
    tar -xzf /tmp/prism.tar.gz -C /opt/prism --strip-components=1 && \
    rm /tmp/prism.tar.gz && \
    cd /opt/prism  && \
    ./install.sh


# Install Prism-Games
RUN wget https://www.prismmodelchecker.org/dl/prism-games-3.2.1-linux64-x86.tar.gz -O /tmp/prism-games.tar.gz && \
    mkdir -p /opt/prism-games && \
    tar -xzf /tmp/prism-games.tar.gz -C /opt/prism-games --strip-components=1 && \
    rm /tmp/prism-games.tar.gz && \
    cd /opt/prism-games  && \
    ./install.sh


# Install Storm

# Storm Dependencies
RUN apt-get update && apt-get install -y  \
    automake build-essential cmake git libboost-dev libginac-dev  \ 
    libglpk-dev libgmp-dev libhwloc-dev libeigen3-dev libxerces-c-dev libz3-dev

RUN git clone -b stable https://github.com/moves-rwth/storm.git  && \
    mv storm /opt/storm && \
    cd /opt/storm && \
    mkdir build && \
    cd build && \
    cmake .. && \
    make -j4 && \
    make install


# wget https://github.com/moves-rwth/storm/releases/download/v$STORM_VERSION/storm-$STORM_VERSION-linux-amd64.tar.gz -O /tmp/storm.tar.gz && \
#     mkdir -p /opt/storm && \
#     tar -xzf /tmp/storm.tar.gz -C /opt/storm --strip-components=1 && \
#     rm /tmp/storm.tar.gz && \ 
#     export STORM_DIR=/opt/storm && \
#     cd $STORM_DIR && \
#     mkdir build && \
#     cd build && \
#     cmake .. && \
#     make -j4 && \
#     make install


# Download and extract JavaFX SDK manually
ENV JAVAFX_VERSION=23.0.1
ENV JAVAFX_HOME=/opt/javafx

# OLD JAVA FX INSTALLATION
# RUN mkdir -p $JAVAFX_HOME && \
#     wget https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip -O /tmp/javafx.zip && \
#     unzip /tmp/javafx.zip -d /opt && \
#     mv /opt/javafx-sdk-${JAVAFX_VERSION}/* $JAVAFX_HOME && \
#     rm -rf /tmp/javafx.zip

# Copy JAR from build stage
WORKDIR /app


#Set environmental variables
ENV ULTIMATE_DIR=/app/
ENV LD_LIBRARY_PATH=/app/libs/runtime
ENV XDG_RUNTIME_DIR=/tmp/runtime

RUN mkdir -p /tmp/runtime && chmod 700 /tmp/runtime

# Copy artifacts from build stage
COPY --from=build /app/target/ultimate_model_manager-1.0.jar .
COPY --from=build /app/configDocker.json .
COPY --from=build /app/case_studies ./case_studies
COPY --from=build /app/libs ./libs
COPY --from=build /app/ULTIMATE_Numerical_Solver ./ULTIMATE_Numerical_Solver
COPY --from=build /app/evochecker_config.properties .

RUN mv configDocker.json config.json


# JavaFX module path
ENV PATH=$JAVA_HOME/bin:$PATH
ENV JAVAFX_MODULES=javafx.controls,javafx.fxml
ENV PRISM_HOME=/opt/prism
ENV PATH=$PRISM_HOME/bin:$PATH
ENV PATH=$STORM_DIR/build/bin:$PATH


# OLD START COMMAND USING JAVA FX MODULES
# # Start Xvfb + run JavaFX with software rendering
# CMD ["sh", "-c", "Xvfb :99 -screen 0 1024x768x16 & export DISPLAY=:99 && \
#      java -Dprism.verbose=true -Djavafx.verbose=true \
#      --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml \
#      -Dprism.order=sw -jar ultimate_model_manager-1.0.jar"]

CMD ["sh", "-c", "Xvfb :99 -screen 0 1024x768x16 & export DISPLAY=:99 && \
     java -Dprism.verbose=true -Djavafx.verbose=true \
     -Dprism.order=sw -jar ultimate_model_manager-1.0.jar"]