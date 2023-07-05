package net.pedroricardo.subtitles;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class Subtitles implements ModInitializer {
    public static Map<String, Float> soundsPlaying = new HashMap<>();
    public static Map<String, Float[]> soundsPlayingPositions = new HashMap<>();
    public static final String MOD_ID = "subtitles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Subtitles initialized.");
    }
}
