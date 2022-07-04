package com.puradox.diorite.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.puradox.diorite.DioriteClient;
import com.puradox.diorite.config.LoadoutConfiguration;
import com.puradox.diorite.config.ModMenuIntegration;
import com.puradox.diorite.screen.element.LoadoutEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class LoadoutScreen extends Screen { //I do not recommend acknowleding anything here as 'best practice.' It just works.

    public static final int columns = 3;
    public static final int rows = 5;
    private int scrollLevel = 0;
    private static int maxScroll=0;

    public final int backgroundWidth = 237;
    public final int backgroundHeight = 204;

    private static TexturedButtonWidget newLoadoutButton;
    private static ButtonWidget configButton;
    private static ButtonWidget doneButton;

    private static Map<Integer, LoadoutEntry> loadoutEntries;
    private final boolean configureOnly;

    public LoadoutScreen(boolean configureOnly) {
        super(Text.translatable("screen.diorite.loadout"));
        this.configureOnly = configureOnly;
    }

    @Override
    public void init() {
        initiateEntries();
        initiateButtons();
        renderEntries();
        renderButtons();
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier("diorite","gui/loadout_gui.png"));
        this.drawTexture(matrices, (this.width/2)-backgroundWidth/2, (this.height/2)-backgroundHeight/2, 0, 0, 256, 256);
        RenderSystem.setShaderTexture(0, new Identifier("diorite","gui/loadout_gui_extra.png"));
        this.drawTexture(matrices, ((this.width + this.backgroundWidth)/2), ((this.height - this.backgroundHeight)/2)+3, 0, 0, 21, 22); //Draw 'new loadout' tab.

        textRenderer.drawWithShadow(matrices, this.title, (float)this.width/2-((float)textRenderer.getWidth(this.title)/2), (float)this.height/2-(float)this.backgroundHeight/2+5, 4210752); //Render screen title.
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        loadoutEntries.forEach((key, value) -> value.render(matrices, mouseX, mouseY, delta)); //Render entry segments which aren't buttons.


        //Draw scrollbar
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier("diorite","gui/loadout_gui_extra.png"));
        if(isScrollBarHovered(mouseX, mouseY)) {
            this.drawTexture(matrices, ((this.width + this.backgroundWidth)/2)-20, (int)(((this.height - this.backgroundHeight)/2)+18+((double)scrollLevel/maxScroll*163)), 33, 0, 12, 15);
        } else{
            this.drawTexture(matrices, ((this.width + this.backgroundWidth)/2)-20, (int)(((this.height - this.backgroundHeight)/2)+18+((double)scrollLevel/maxScroll*163)), 21, 0, 12, 15);
        }


    }

    public void initiateEntries() {
        if(loadoutEntries!=null) {
            loadoutEntries.forEach((key, value) -> value.setVisible(false));
            loadoutEntries = null;
        }

        loadoutEntries = new HashMap<>();
        if (DioriteClient.getConfig().loadoutConfigs.size()==1) {
            loadoutEntries.put(0, new LoadoutEntry(DioriteClient.getConfig().loadoutConfigs.get(0), this, 1));
        }
        DioriteClient.getConfig().loadoutConfigs.forEach((key, value) ->
                loadoutEntries.put(key, new LoadoutEntry(value, this, key-scrollLevel)));
        maxScroll = loadoutEntries.size()/(rows*columns)*columns+columns;
    }
    private void initiateButtons() {
        if(newLoadoutButton != null || configButton != null || doneButton != null) {
            this.remove(newLoadoutButton);
            this.remove(configButton);
            this.remove(doneButton);
        }
        newLoadoutButton = new TexturedButtonWidget(this.width / (2)+121, this.height / (2)-96, 16, 16, 0, 0, 16, new Identifier("diorite", "gui/icons.png"),
                (PressAction) -> {
                    assert client != null;
                    client.setScreen(new EditLoadoutScreen(new LoadoutConfiguration(DioriteClient.getConfig().loadoutConfigs.size()), false));
                }) {
            @Override public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
                LoadoutScreen.this.renderTooltip(matrices, Text.translatable("button.diorite.new_loadout"), mouseX, mouseY);
            }
        };

        if(configureOnly) {
            doneButton = new ButtonWidget(this.width / 2 - 50, this.height / (2) + backgroundHeight / 2, 100, 20, ScreenTexts.DONE,
                    (PressAction) -> {
                        assert client != null;
                        client.setScreen(new ConfigurationScreen(ModMenuIntegration.menuReturn()));
                    });
        } else {
            configButton = new ButtonWidget(this.width / 2-60, this.height / (2)+backgroundHeight/2, 120, 20, Text.translatable("screen.diorite.configuration"),
                    (PressAction) -> {
                        assert client != null;
                        client.setScreen(new ConfigurationScreen(this));
                    });
        }
    }


    public void renderEntries() {
        if(loadoutEntries.size()==1) {
            loadoutEntries.get(0).setVisible(true);
        } else {
            loadoutEntries.forEach((key, value) -> {
                value.setVisible(key > scrollLevel && key <= (columns * rows)+scrollLevel);
                if(configureOnly) {
                    value.selectButton.active=false;
                }
            });

        }

    }

    private void renderButtons() {
        this.addDrawableChild(newLoadoutButton);
        if(configureOnly) {
            this.addDrawableChild(doneButton);
        } else {this.addDrawableChild(configButton);}

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        amount = amount*-1;
        int oldScroll = scrollLevel;
        scrollLevel = (int) (amount*columns+scrollLevel);
        if (scrollLevel > maxScroll) {
            scrollLevel = maxScroll;
        } else if (scrollLevel < 0) {scrollLevel = 0;}
        if (oldScroll != scrollLevel) {
            initiateEntries();
            renderEntries();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(isScrollBarHovered((int)mouseX, (int)mouseY)) {
            scrollLevel = (int)(Math.abs(((((double)178/(mouseY - ((this.height - this.backgroundHeight) / (double)2) + 18))))-(maxScroll-1)));
            initiateEntries();
            renderEntries();
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.8F));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isScrollBarHovered(int mouseX, int mouseY) {
        return mouseX >= (this.width + this.backgroundWidth) / 2 - 20 && mouseX <= (this.width + this.backgroundWidth) / 2 - 8
                && mouseY >= (this.height - this.backgroundHeight) / 2 + 18 && mouseY <= ((this.height - this.backgroundHeight) / 2)+196;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static Map<Integer, LoadoutEntry> getLoadoutEntries() { return loadoutEntries;}
}
