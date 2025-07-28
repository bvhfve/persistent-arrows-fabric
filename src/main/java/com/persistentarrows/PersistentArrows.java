package com.persistentarrows;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentArrows implements ModInitializer {
    public static final String MOD_ID = "persistentarrows";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Persistent Arrows mod initialized! Bringing Bedrock Edition arrow behavior to Java Edition.");
    }
}