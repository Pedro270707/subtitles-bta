package net.pedroricardo.subtitles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class Subtitles implements ClientModInitializer {
    public static Map<String, Float> soundsPlaying = new HashMap<>();
    public static Map<String, Float[]> soundsPlayingPositions = new HashMap<>();
    public static final String MOD_ID = "subtitles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Subtitles initialized.");
    }
}
