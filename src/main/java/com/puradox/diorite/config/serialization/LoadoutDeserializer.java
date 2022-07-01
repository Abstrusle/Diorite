package com.puradox.diorite.config.serialization;

import com.google.gson.*;
import com.puradox.diorite.config.LoadoutConfiguration;

import java.lang.reflect.Type;

public class LoadoutDeserializer implements JsonDeserializer<LoadoutConfiguration> { //Parses the json.

    @Override
    public LoadoutConfiguration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();
        LoadoutConfiguration loadout = new LoadoutConfiguration();

        loadout.id = obj.get("id").getAsInt();
        loadout.modifiable = obj.get("modifiable").getAsBoolean();
        loadout.name = obj.get("name").getAsString();
        loadout.leaveStacksForBuilding = obj.get("leaveStacksForBuilding").getAsInt();

        if (obj.get("nameFilters") != null) {
            for (JsonElement jsonItem : obj.getAsJsonArray("nameFilters")) {
                loadout.nameFilters.add(jsonItem.getAsString().toLowerCase());
            }
        }
        if (obj.get("itemFilters") != null) {
            for (JsonElement jsonItem : obj.getAsJsonArray("itemFilters")) {
                loadout.itemFilters.add(jsonItem.getAsString().toLowerCase());
            }
        }
        if (obj.get("nbtStringFilters") != null) {
            for (JsonElement jsonItem : obj.getAsJsonArray("nbtStringFilters")) {
                loadout.nbtStringFilters.add(jsonItem.getAsString().toLowerCase());
            }
        }

        return loadout;
    }
}
