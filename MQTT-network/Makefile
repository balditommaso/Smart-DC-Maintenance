CONTIKI_PROJECT = mqtt-network
all: $(CONTIKI_PROJECT)

CONTIKI = ../../..
SDCM = ../

include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/mqtt

-include $(CONTIKI)/Makefile.identify-target

MODULES_REL += arch/platform/$(TARGET)
MODULES_REL += $(SDCM)/MQTT-network/utils
MODULES_REL += $(SDCM)/MQTT-network/band-samples

include $(CONTIKI)/Makefile.include
