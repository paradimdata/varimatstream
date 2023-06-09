## Installation Application:
1. [Java 11](https://docs.oracle.com/en/java/javase/11/install/overview-jdk-installation.html#GUID-8677A77F-231A-40F7-98B9-1FD0B48C346A)
2. [Maven](https://maven.apache.org/install.html)

   [For Mac users] https://www.digitalocean.com/community/tutorials/install-maven-mac-os

## Kafka Environment Variable
Make sure that you have your environment contains an appropriate Kafka username and password . From [empad.config](https://github.com/paradimdata/pyempadcalibratescript/blob/main/stream/empad.config), you may need to modify **KAFKA_ENV_USERNAME** and **KAFKA_ENV_PASSWORD**.

## EMPAD Environment Variable
1. Create an environment variable for empad path: `export EMPAD_HOME=/from/to/empad/` on your local computer.
2. Create an "output" directory under your _EMPAD_HOME_. All your results will be place to the output folder.
3. Copy [EMPAD2-calib_oct2020](https://github.com/paradimdata/pyempadcalibratescript/tree/main/related_data/EMPAD2-calib_oct2020) directory under the empad folder and make sure it contains all those eight filters:
* [G1A_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G1A_prelim.r32)
* [G1B_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G1B_prelim.r32)
* [G2A_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G2A_prelim.r32)
* [G2B_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G2B_prelim.r32)
* [FFA_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/FFA_prelim.r32)
* [FFB_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/FFB_prelim.r32)
* [B2A_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/B2A_prelim.r32)
* [B2B_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/B2B_prelim.r32)

## RocksDBState Checkpint Configuration
From the config file you will need to specify the path for RocksDBStateBackend checkpoint. From [empad.config](https://github.com/paradimdata/pyempadcalibratescript/blob/main/stream/empad.config) assign a folder name to your **CHECKPOINT_STORAGE** and create a folder with the same name under your EMPAD_HOME.

* For more information please read Flink State Backends documenation: (https://nightlies.apache.org/flink/flink-docs-master/docs/ops/state/state_backends/)

* Stateful Stream Processing  https://nightlies.apache.org/flink/flink-docs-master/docs/concepts/stateful-stream-processing/

* Working with State https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/fault-tolerance/state/

## Build from MAVEN
1. In your terminal, from the root of the project, run `mvn package`
2.  `cd target/`

## Setup Configurations
Open [empad.config](https://github.com/paradimdata/pyempadcalibratescript/blob/main/stream/empad.config) and fill out the blank properties.
You will need to modify IMAGE_TOPIC, NOISE_TOPIC, GROUP_ID, and CHECKPOINT_STORAGE. **IMAGE_TOPIC** and **NOISE_TOPIC** are topics for producer, and you may need to change them as well.

## Running the application
1. Before running the app, you will need to make sure that the configuration file (empad.cfg) exists.
2. There are several options that you will need them to run the application properly.
* i: System Information
* c: Config File (**required**)
3. From the target folder, `java -Xms15g -Xmx24g -jar varimat-stream-processing-1.3.jar --config empad.config`
