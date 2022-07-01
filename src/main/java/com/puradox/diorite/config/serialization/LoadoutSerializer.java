package com.puradox.diorite.config.serialization;

import com.google.gson.*;
import com.puradox.diorite.config.LoadoutConfiguration;

import java.lang.reflect.Type;

public class LoadoutSerializer implements JsonSerializer<LoadoutConfiguration>{ //Json formatting for export.

    @Override
    public JsonElement serialize(LoadoutConfiguration loadout, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray nameFilters = new JsonArray();
        JsonArray itemFilters = new JsonArray();
        JsonArray nbtStringFilters = new JsonArray();

        loadout.nameFilters.forEach(nameFilters::add);
        loadout.itemFilters.forEach(itemFilters::add);
        loadout.nbtStringFilters.forEach(nbtStringFilters::add);

        JsonObject obj = new JsonObject();
        obj.addProperty("id", loadout.id);
        obj.addProperty("modifiable", loadout.modifiable);
        obj.addProperty("name", loadout.name);
        obj.addProperty("leaveStacksForBuilding", loadout.leaveStacksForBuilding);
        obj.add("nameFilters", nameFilters);
        obj.add("itemFilters", itemFilters);
        obj.add("nbtStringFilters", nbtStringFilters);

        return obj;
    }
}
