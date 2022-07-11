CONTIKI_PROJECT = coap-example-server
all: $(CONTIKI_PROJECT)

# Do not try to build on Sky because of code size limitation
PLATFORMS_EXCLUDE = sky z1

# Include CoAP resources
MODULES_REL += ./resources

CONTIKI=../../..
SDCM = ../

# Include the CoAP implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap
MODULES_REL += $(SDCM)/CoAP-network/utils
MODULES_REL += $(SDCM)/CoAP-network/sensor-signs

include $(CONTIKI)/Makefile.include