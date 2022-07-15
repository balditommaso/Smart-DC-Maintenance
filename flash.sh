#!/bin/bash

# compile br
cd rpl-border-router
make distclean
make TARGET=cc26x0-cc13x0 BOARD=/launchpad/cc2650 border-router
cd ..

cd MQTT-network
make distclean
make TARGET=cc26x0-cc13x0 BOARD=/launchpad/cc2650 mqtt-client
cd ..

cd CoAP-network/temperature
make distclean
make TARGET=cc26x0-cc13x0 BOARD=/launchpad/cc2650 coap-server
cd ..

cd humidity
make distclean
make TARGET=cc26x0-cc13x0 BOARD=/launchpad/cc2650 coap-server
cd ..

cd oxygen
make distclean
make TARGET=cc26x0-cc13x0 BOARD=/launchpad/cc2650 coap-server

