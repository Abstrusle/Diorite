package com.puradox.diorite.mixin;

import com.puradox.diorite.SelectedTabAccess;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin implements SelectedTabAccess {
    //Utilized specifically to determine if the loadout button should be rendered.

    @Shadow
    private static int selectedTab;

    public int getSelectedTab() {return selectedTab;}
}
