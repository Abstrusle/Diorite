package com.puradox.diorite.screen;

import com.puradox.diorite.DioriteClient;
import com.puradox.diorite.screen.element.TexturedToggleButtonWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ConfigurationScreen extends Screen {
    private final Screen parent;
    public ConfigurationScreen(Screen parent) { //I do not recommend acknowleding anything here as 'best practice.' It just works.
        super(Text.translatable("screen.diorite.configuration"));
        this.parent=parent;
    }

    @Override
    protected void init() {
        if(!(this.parent instanceof LoadoutScreen)) {
            //Need buttons for dump on select, loadout editor, and displaying loadout button
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 90, this.height / 6, 180, 20, Text.translatable("screen.diorite.loadout"), PressAction -> {
                assert client != null;
                client.setScreen(new LoadoutScreen(true));
            }));
        }
        this.addDrawableChild(new TexturedToggleButtonWidget((int)(this.width*0.8), this.height / 6 + 25, 20, 20, 236, 0, 20, -20, new Identifier("diorite", "gui/icons.png"), DioriteClient.getConfig().dumpOnLoadoutSwitch, button -> {
            DioriteClient.getConfig().dumpOnLoadoutSwitch=!DioriteClient.getConfig().dumpOnLoadoutSwitch;
            DioriteClient.saveConfig();
        }));

        this.addDrawableChild(new TexturedToggleButtonWidget((int)(this.width*0.8), this.height / 6 + 50, 20, 20, 236, 0, 20, -20, new Identifier("diorite", "gui/icons.png"), DioriteClient.getConfig().showLoadoutButton, button -> {
            DioriteClient.getConfig().showLoadoutButton=!DioriteClient.getConfig().showLoadoutButton;
            DioriteClient.saveConfig();
        }));

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 40, 200, 20, ScreenTexts.DONE, (PressAction) -> {
            assert this.client != null;
            this.client.setScreen(this.parent);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        textRenderer.draw(matrices, Text.translatable("option.diorite.dump_on_select"), (int)(this.width*0.2), (float)this.height/6 + 30, 0xDDDDDDDD);
        textRenderer.draw(matrices, Text.translatable("option.diorite.show_loadout_button"), (int)(this.width*0.2), (float)this.height/6 + 60, 0xDDDDDDDD);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
