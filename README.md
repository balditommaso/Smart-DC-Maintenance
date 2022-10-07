# Smart-DC-Maintenance

Project developed for the Internet Of Things course of the Master of Artificial Intelligence and Data Engineering at the University of Pisa.

This project consists in the design and implementation of a Smart network of microcontrollers which monitor the status of the datacenter's racks and takes care of the workers' health by recording biometric signals.

More informetion about the project are available in the documentation.

## Repository

The repository is organized as follows:
- *CoAP-network/* contains the implementation of the CoAP network carried out by microcontrollers
- *Grafana/* contains the implementation of the web based dashboard to visualize the telemetry signals
- *MQTT-network/* contains the implementation of the MQTT network carried out by microcontrollers
- *collector/* contains the Java program in execution on the master node to coordinate all the communication and the access to the DB
- *rpl-border-router* contains the code that allow the microcontrollers to be accessible from the web

## Contributors
- Tommaso Baldi [@balditommaso](https://github.com/balditommaso)
- Edoardo Ruffoli [@edoardoruffoli](https://github.com/edoardoruffoli)
