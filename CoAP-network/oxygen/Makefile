CONTIKI_PROJECT = coap-server
all: $(CONTIKI_PROJECT)
PLATFORMS_ONLY = cc26x0-cc13x0

# Do not try to build on Sky because of code size limitation
PLATFORMS_EXCLUDE = sky z1 nrf52dk

CONTIKI=../../../..
SDCM = ../../

# Include the CoAP implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

CFALGS += -DPROJECT_CONF_H=\"project-conf.h\"

-include $(CONTIKI)/Makefile.identify-target
MODULES_REL += arch/platform/$(TARGET)
# Include CoAP resources

MODULES_REL += $(SDCM)/CoAP-network/utils
MODULES_REL += $(SDCM)/CoAP-network/sensor-signs
MODULES_REL += ./resources

MAKE_NET_IPV6 = 1
MAKE_MAC ?= MAKE_MAC_CSMA
MAKE_NET = MAKE_NET_IPV6

include $(CONTIKI)/Makefile.include
