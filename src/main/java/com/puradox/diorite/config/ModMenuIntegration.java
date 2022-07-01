package com.puradox.diorite.config;

import com.puradox.diorite.screen.ConfigurationScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;

public class ModMenuIntegration implements ModMenuApi {
    //ModMenu integration. For the actual screen, see 'ConfigurationScreen' in the 'screen' package.
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigurationScreen::new;
    }

    public static Screen menuReturn () {
        if(MinecraftClient.getInstance().world==null) { //If from the title.
            return new ModsScreen(new TitleScreen());
        } else {
            return new ModsScreen(new OptionsScreen(null, MinecraftClient.getInstance().options));
        } //If from a world.
    }
}
