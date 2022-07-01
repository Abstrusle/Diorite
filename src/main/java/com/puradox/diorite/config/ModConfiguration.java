package com.puradox.diorite.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.puradox.diorite.DioriteClient;
import com.puradox.diorite.config.serialization.LoadoutDeserializer;
import com.puradox.diorite.config.serialization.LoadoutSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ModConfiguration { //Where all configuration settings and loadouts are stored. Also the primary class for serialization/deserialization of the json.
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(LoadoutConfiguration.class, new LoadoutSerializer())
            .registerTypeAdapter(LoadoutConfiguration.class, new LoadoutDeserializer())
            .create();

    public boolean showLoadoutButton = true;
    public int loadoutButtonX = 60;
    public int loadoutButtonY = -30;
    public int loadoutButtonCreativeX = 50;
    public int loadoutButtonCreativeY = -40;
    public boolean dumpOnLoadoutSwitch = false;

    public Map<Integer, LoadoutConfiguration> loadoutConfigs = Map.of(0, new LoadoutConfiguration(
            Arrays.asList("crude", "trash", "broken"),
            Arrays.asList("minecraft:gravel", "minecraft:wheat_seeds", "minecraft:diorite", "minecraft:andesite", "minecraft:granite"),
            Arrays.asList("trash", "id:\"minecraft:binding_curse\""),
            0,
            "Example",
            false
    ));

    public static ModConfiguration loadConfig(File file) {
        ModConfiguration config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {
                config = GSON.fromJson(bufferedReader, ModConfiguration.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            config = new ModConfiguration();
        }
        if(config==null) {
            config = new ModConfiguration();
        }
        config.saveConfig(file);

        return config;
    }

    public void saveConfig(File config) {
        try (
                FileOutputStream stream = new FileOutputStream(config);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            DioriteClient.LOGGER.error("Failed to save config");
        }
    }
}
