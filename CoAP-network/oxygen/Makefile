CONTIKI_PROJECT = coap-server
all: $(CONTIKI_PROJECT)

# Do not try to build on Sky because of code size limitation
PLATFORMS_EXCLUDE = sky z1

CONTIKI=../../../..
SDCM = ../../

# Include the CoAP implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

-include $(CONTIKI)/Makefile.identify-target
MODULES_REL += arch/platform/$(TARGET)
# Include CoAP resources
#MODULES_REL += ./resources
MODULES_REL += $(SDCM)/CoAP-network/utils
MODULES_REL += $(SDCM)/CoAP-network/sensor-signs
MODULES_REL += ./resources

include $(CONTIKI)/Makefile.include
