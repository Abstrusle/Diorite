package com.puradox.diorite;

import com.puradox.diorite.config.LoadoutConfiguration;
import com.puradox.diorite.config.ModConfiguration;
import com.puradox.diorite.screen.LoadoutScreen;
import com.puradox.diorite.screen.element.LoadoutEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class DioriteClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Diorite");

    public static ModConfiguration config;

    public static LoadoutConfiguration currentLoadout;
    public static LoadoutEntry currentEntry;
    public static int skipOverValue = 0;

    private static ClientPlayerInteractionManager interactionManager;
    private static ClientPlayerEntity player;
    private static AbstractInventoryScreen<?> invScreen;

    private static KeyBinding discardKey;
    private static KeyBinding loadoutKey;

    private static ButtonWidget loadoutButton;

    private static boolean inInventory = false;


    @Override
    public void onInitializeClient() {

        config = ModConfiguration.loadConfig(new File(FabricLoader.getInstance().getConfigDir().toFile(), "diorite.json"));
        discardKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.diorite.discard",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_END,
                "category.diorite.keys"
        )); //Key to discard according to loadout.
        loadoutKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.diorite.loadout",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.diorite.keys"
        )); //Key to open loadout menu.

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(loadoutKey.wasPressed()) {client.setScreen(new LoadoutScreen(false));}
            if(discardKey.wasPressed()) {
                if(currentLoadout==null) {
                    player.sendMessage(new TranslatableText("error.diorite.no_selected_loadout"), false);
                } else {
                    startFiltering();
                    assert client.currentScreen != null;
                    client.currentScreen.close();
                }}
        }); //Keybinding function for when outside a screen.

        WorldRenderEvents.LAST.register((last) -> {
            interactionManager = MinecraftClient.getInstance().interactionManager;
            player = MinecraftClient.getInstance().player;
        }); //Set variables. Probably not the most efficient means, but better than crashing.

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen || screen instanceof MultiplayerScreen) {
                currentLoadout = null;
                currentEntry = null;
                inInventory = false;
                loadoutButton = null;
                return;
            } //Persisting loadouts through worlds is possibly a bad move.

            ScreenEvents.afterTick(screen).register((AfterTick) -> {
                if (screen instanceof InventoryScreen) {
                    invScreen = (InventoryScreen) screen;
                } else if (screen instanceof CreativeInventoryScreen) {
                    invScreen = (CreativeInventoryScreen) screen;
                    if (((SelectedTabAccess) invScreen).getSelectedTab() != ItemGroup.INVENTORY.getIndex()) {
                        inInventory = false;
                        if(loadoutButton!=null) {
                            Screens.getButtons(screen).remove(loadoutButton);
                            loadoutButton=null;
                        }
                        return;
                    }
                } else {
                    inInventory = false;
                    Screens.getButtons(screen).remove(loadoutButton);
                    loadoutButton=null;
                    return;
                }
                inInventory = true;

                if(config.showLoadoutButton) {
                    if(loadoutButton!=null) {
                        Screens.getButtons(screen).remove(loadoutButton);
                    }
                    assert interactionManager != null;
                    if(interactionManager.getCurrentGameMode().isCreative()) {
                        loadoutButton = new ButtonWidget(screen.width/2+config.loadoutButtonCreativeX, screen.height/2+config.loadoutButtonCreativeY, 20, 20, Text.of("TL"),
                                (PressAction) -> client.setScreen(new LoadoutScreen(false)));
                        Screens.getButtons(screen).add(loadoutButton);
                    } else {
                        loadoutButton = new ButtonWidget(screen.width/2+config.loadoutButtonX, screen.height/2+config.loadoutButtonY, 20, 20, Text.of("TL"),
                                (PressAction) -> client.setScreen(new LoadoutScreen(false)));
                        Screens.getButtons(screen).add(loadoutButton);
                    }
                } //Display the loadout button in the player's inventory.
            });



            ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                if(discardKey.matchesKey(key, scancode) && inInventory) {
                    if(currentLoadout==null) {
                        player.sendMessage(new TranslatableText("error.diorite.no_selected_loadout"), false);
                    } else {startFiltering();}
                } else if (loadoutKey.matchesKey(key, scancode) && inInventory) {
                    inInventory=false;
                    client.setScreen(new LoadoutScreen(false));
                }
            });
            ScreenEvents.remove(screen).register((Remove) -> inInventory = false);
        }); //Added keybinds again, so they will be available while in the inventory.
    }

    public static void startFiltering() {
        MinecraftClient client = MinecraftClient.getInstance();
        int shouldSkipOverResult;

        if(!(client.currentScreen instanceof InventoryScreen || client.currentScreen instanceof CreativeInventoryScreen)) {
            assert client.player != null;
            client.setScreen(new InventoryScreen(client.player));
        }
        if(invScreen == null) {
            if(interactionManager.getCurrentGameMode().isCreative()) {
                assert client.currentScreen != null;
                //This is where I'd set the selected tab to the player's inventory... but CreativeInventoryScreen wanted to be weird.
                //Therefore, have fun 'dumping' (item doesn't actually spawn in the world) items from the creative inventory screen.
                invScreen = (CreativeInventoryScreen) client.currentScreen;
            }else{
                assert client.currentScreen instanceof InventoryScreen;
                invScreen = (InventoryScreen) client.currentScreen;
            }
        }

        PlayerScreenHandler playerScreen = player.playerScreenHandler;

        if(currentLoadout.leaveStacksForBuilding>0) { //Checking if any blocks can be used for building, other than those to be trash.
            int buildingBlocks = 0;

            for(Slot slot:playerScreen.slots) {
                if(slot.getIndex()>=5 && slot.getIndex()<=8)  {continue;} //Don't include armour.
                if(slot.getIndex()>=36) {continue;} //Ignore hotbar.
                if(!(slot.hasStack()) || (checkFilters(slot))) {
                    continue;
                } //If no stack, why bother? If from the filters, it doesn't count.
                if (slot.getStack().getItem() instanceof BlockItem
                        && Block.getBlockFromItem(slot.getStack().getItem()).getStateManager().getDefaultState().getMaterial().blocksMovement()) {
                    buildingBlocks++;
                    if(buildingBlocks>=currentLoadout.leaveStacksForBuilding) {break;}
                }

            }
                skipOverValue=currentLoadout.leaveStacksForBuilding-buildingBlocks;
        }

        for(Slot slot:playerScreen.slots) {
            if(slot.getIndex()>=5 && slot.getIndex()<=8)  { //Don't remove armour.
                continue;
            }
            if(slot.getIndex()>=36) {continue;} //Ignore hotbar.
            shouldSkipOverResult=shouldSkipOver(slot);
            if (shouldSkipOverResult==2) {continue;}
            if(shouldSkipOverResult==1) {
                assert interactionManager != null;
                interactionManager.clickSlot(
                        invScreen.getScreenHandler().syncId,
                        slot.getIndex(),
                        1,
                        SlotActionType.THROW, player);
            }
        } //Iterate over slots, and throw.
        skipOverValue=0; //For the worst-case scenario.
    }

    private static boolean checkFilters(Slot slot) {
        if(!(slot.hasStack())) {return false;} //Can't discard air. Or maybe you can. No reason to, though.

        for(String itemName : currentLoadout.nameFilters) {
            if (slot.getStack().getName().getString().toLowerCase().contains(itemName)) {
                return true;
            }
        }

        for(String itemId : currentLoadout.itemFilters) {
            if (slot.getStack().getItem().equals(Registry.ITEM.get(new Identifier(itemId)))) {
                return true;
            }
        }
        for(String itemNbtString : currentLoadout.nbtStringFilters) {
            if(slot.getStack().getNbt()!=null) {
                if(Objects.requireNonNull(slot.getStack().getNbt()).asString().toLowerCase().contains(itemNbtString)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int shouldSkipOver(Slot slot) {
        if(checkFilters(slot)) {
            if(skipOverValue>0 && Block.getBlockFromItem(slot.getStack().getItem()).getStateManager().getDefaultState().getMaterial().blocksMovement()) {
                skipOverValue--;
                return 2; //A proper block.
            }
            return 1; //In filters, but not a proper block.
        }
        return 0; //Not in filters. Skip.
    }

    public static void saveConfig () {
        config.saveConfig(new File(FabricLoader.getInstance().getConfigDir().toFile(), "diorite.json"));
    }

    public static ModConfiguration getConfig() {
        return config;
    }
}
