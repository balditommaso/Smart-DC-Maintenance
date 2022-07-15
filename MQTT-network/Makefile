CONTIKI_PROJECT = mqtt-network
all: $(CONTIKI_PROJECT)
PLATFORMS_ONLY = cc26x0-cc13x0

CONTIKI = ../../..
SDCM = ../

PLATFORMS_EXCLUDE = nrf52dk

include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/mqtt

-include $(CONTIKI)/Makefile.identify-target

MODULES_REL += arch/platform/$(TARGET)
MODULES_REL += $(SDCM)/MQTT-network/utils
MODULES_REL += $(SDCM)/MQTT-network/band-samples

MAKE_NET_IPV6 = 1
MAKE_MAC ?= MAKE_MAC_CSMA
MAKE_NET = MAKE_NET_IPV6

include $(CONTIKI)/Makefile.include
