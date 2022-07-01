package com.puradox.diorite.screen.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.puradox.diorite.DioriteClient;
import com.puradox.diorite.config.LoadoutConfiguration;
import com.puradox.diorite.screen.EditLoadoutScreen;
import com.puradox.diorite.screen.LoadoutScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class LoadoutEntry extends LoadoutConfiguration implements Drawable {
    //I do not recommend acknowleding anything here as 'best practice.' It just works.
    private boolean visible = false;

    public static int width;
    public static int height;

    public final int x;
    public final int y;
    public final int centreX;
    public final int centreY;

    public int pos;
    private final LoadoutScreen screen;
    private static MinecraftClient client;
    private final TextRenderer textRenderer;

    private TexturedButtonWidget editButton;
    private TexturedButtonWidget deleteButton;
    public ClickableWidget selectButton;

    public LoadoutConfiguration heldLoadout;


    public LoadoutEntry(LoadoutConfiguration loadout, LoadoutScreen screen, int pos) {
        this.heldLoadout = loadout; //The held loadout.
        this.screen = screen;

        client = MinecraftClient.getInstance();

        this.itemFilters = loadout.itemFilters;
        this.nameFilters = loadout.nameFilters;
        this.nbtStringFilters = loadout.nbtStringFilters;
        this.modifiable = loadout.modifiable;

        this.id = loadout.id;
        this.name = heldLoadout.name; //Displayname

        this.pos=loadout.id; //Position is separate from id, but defaults to the id.

        width = 68;
        height = 36;

        if(id==0) {
            this.x=(int)(((double)screen.width/2)-110);
            this.y=(int)(((double)screen.height/2)-85);
        } else {
            this.x = (int)(((double)screen.width/2)-110+(68*((pos-1)%LoadoutScreen.columns)));
            this.y = (int)(((double)screen.height/2)-85+(36*((pos-1)/LoadoutScreen.columns)));
        }



        this.centreX = x+width/2;
        this.centreY = y+height/2;

        this.textRenderer = MinecraftClient.getInstance().textRenderer;

        initButtons();
    }

    public void initButtons() {
        if(id==0) { //Example entry cannot be deleted, only hidden.
            this.deleteButton = new TexturedButtonWidget(this.centreX+16, this.centreY, 16, 16, 48, 0, 16, new Identifier("diorite", "gui/icons.png"),
                    (PressAction) -> {}) {
                @Override
                public void renderTooltip(MatrixStack matrixStack, int i, int j) {
                    screen.renderTooltip(matrixStack, new TranslatableText("tooltip.diorite.delete_example"), i, j);
                }
            };
        } else {
            this.deleteButton = new TexturedButtonWidget(this.centreX+16, this.centreY, 16, 16, 48, 0, 16, new Identifier("diorite", "gui/icons.png"),
                    (PressAction) -> {
                        Map<Integer, LoadoutConfiguration> modifiedLoadouts = new HashMap<>();
                        DioriteClient.getConfig().loadoutConfigs.forEach((key, value) -> {
                            if(id < key){
                                modifiedLoadouts.put(key - 1, DioriteClient.getConfig().loadoutConfigs.get(key));
                                modifiedLoadouts.get(key - 1).id--;
                            } else if(!(key==this.id)) {
                                modifiedLoadouts.put(key, DioriteClient.getConfig().loadoutConfigs.get(key));
                            }
                        });
                        this.setVisible(false);
                        DioriteClient.getConfig().loadoutConfigs=modifiedLoadouts;
                        DioriteClient.saveConfig();
                        screen.initiateEntries();
                        screen.renderEntries();
                    }) {
                @Override
                public void renderTooltip(MatrixStack matrixStack, int i, int j) {
                    screen.renderTooltip(matrixStack, new TranslatableText("tooltip.diorite.delete"), i, j);
                }
            };
        }

        if(modifiable) {
            this.editButton = new TexturedButtonWidget(this.centreX+16, this.centreY-16, 16, 16, 16, 0, 16, new Identifier("diorite", "gui/icons.png"),
                    (PressAction) -> client.setScreen(new EditLoadoutScreen(heldLoadout, false)));
        } else {
            this.editButton = new TexturedButtonWidget(this.centreX+16, this.centreY-16, 16, 16, 32, 0, 16, new Identifier("diorite", "gui/icons.png"),
                    (PressAction) -> client.setScreen(new EditLoadoutScreen(heldLoadout, false)));
        } //Example or obscure custom entries use their own texture for the edit button.

        this.selectButton = new PressableWidget(this.x, this.y, width, height, new TranslatableText("button.diorite.select_loadout")) {
            @Override public void appendNarrations(NarrationMessageBuilder builder) {
                this.appendDefaultNarrations(builder);
            }

            @Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                if (isVisible() && (pos>0 || id==0)) {
                    this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width-18 && mouseY < this.y + this.height;
                    if (isHovered()) {renderTooltip(matrices, mouseX, mouseY);}
                    this.renderButton(matrices, mouseX, mouseY, delta);
                }
            }

            @Override public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
                List<Text> includedItems = new ArrayList<>();
                if(itemFilters.size()>0) {
                    includedItems.add(new TranslatableText("tooltip.diorite.items").setStyle(
                            Style.EMPTY
                                    .withColor(0x0000FF)
                                    .withBold(true)
                    ));
                    itemFilters.forEach((value) -> includedItems.add(new LiteralText(value).setStyle(
                            Style.EMPTY.withItalic(true).withColor(0x998888)
                    )));
                }


                if(nameFilters.size()>0) {
                    includedItems.add(new TranslatableText("tooltip.diorite.names").setStyle(
                            Style.EMPTY
                                    .withColor(0x00FF00)
                                    .withBold(true)
                    ));
                    nameFilters.forEach((value) -> includedItems.add(new LiteralText(value).setStyle(
                            Style.EMPTY.withItalic(true).withColor(0x998888)
                    )));
                }

                if(nbtStringFilters.size()>0) {
                    includedItems.add(new TranslatableText("tooltip.diorite.nbt_strings").setStyle(
                            Style.EMPTY
                                    .withColor(0xFFFF00)
                                    .withBold(true)
                    ));
                    nbtStringFilters.forEach((value) -> includedItems.add(new LiteralText(value).setStyle(
                            Style.EMPTY.withItalic(true).withColor(0x998888)
                    )));
                }


                screen.renderTooltip(matrices, includedItems, mouseX, mouseY);
            }

            @Override protected boolean clicked(double mouseX, double mouseY) {
                return this.active && this.visible && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width-18) && mouseY < (double)(this.y + this.height);
            } //Overriden to set bounds. Otherwise would overlap other buttons.

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return this.active && this.visible && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width-18) && mouseY < (double)(this.y + this.height);
            } //Overriden to set bounds. Otherwise would overlap other buttons.

            @Override public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, new Identifier("diorite","gui/icons.png"));
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                int i = this.getYImage(this.isHovered());
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                this.drawTexture(matrices, this.x, this.y, 148, i * LoadoutEntry.height, this.width, this.height);

            }

            @Override public void onPress() {
                DioriteClient.currentLoadout=heldLoadout;
                if(DioriteClient.getConfig().dumpOnLoadoutSwitch) {
                    assert client.player != null;
                    client.setScreen(new InventoryScreen(client.player));
                    DioriteClient.startFiltering();
                    screen.close();
                } else {
                    screen.close();
                }

            }
        };
    }


    public void setVisible(Boolean b) {
        if(Screens.getButtons(screen).contains(selectButton)) {
            List<ClickableWidget> buttons = List.of(editButton, deleteButton, selectButton);
            Screens.getButtons(screen).removeAll(buttons);
        }
        if(b && !(this.id==0 && LoadoutScreen.getLoadoutEntries().size()!=1)) {
            Screens.getButtons(screen).add(selectButton);
            Screens.getButtons(screen).add(editButton);
            Screens.getButtons(screen).add(deleteButton);
        } else {
            List<ClickableWidget> buttons = List.of(editButton, deleteButton, selectButton);
            Screens.getButtons(screen).removeAll(buttons);
        }
        this.visible = b;
    }

    public boolean isVisible() {
        return visible;
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (isVisible()) {
            if(LoadoutScreen.getLoadoutEntries().size()>1&&this.id==0) {return;}
            textRenderer.draw(matrices, Text.of(heldLoadout.name), (float) x+5, (float) y+5, 0xFFFFFFFF);
            for(int i = 0; i<Math.min(3, heldLoadout.itemFilters.size()); i++) {
                client.getItemRenderer().renderInGui(new ItemStack(Registry.ITEM.get(new Identifier(heldLoadout.itemFilters.get(i)))), centreX-(32-(i*16)), centreY);
            }
        }
    }
}
