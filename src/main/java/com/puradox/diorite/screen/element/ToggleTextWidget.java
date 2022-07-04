package com.puradox.diorite.screen.element;

import com.puradox.diorite.DioriteClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
@Environment(EnvType.CLIENT)
public class ToggleTextWidget extends ToggleButtonWidget {
    private final TextRenderer textRenderer;
    private final int type;

    private List<String> warnings;

    public ToggleTextWidget(int x, int y, boolean toggled, Text text, TextRenderer textRenderer, int type) {
        super(x, y, textRenderer.getWidth(text), 10, toggled);
        this.type=type; //Names widget, nbt widget, etc.

        this.setMessage(text);
        this.textRenderer=textRenderer;

        this.warnings=new ArrayList<>();
    }

    public ToggleTextWidget(int x, int y, boolean toggled, Text text, TextRenderer textRenderer, int type, List<String> warnings) {
        super(x, y, textRenderer.getWidth(text), 10, toggled);
        this.type=type; //Names widget, nbt widget, etc.

        this.setMessage(text);
        this.textRenderer=textRenderer;

        this.warnings=warnings;
    }

    public void addWarning(String warning) {
        if(!this.warnings.contains(warning)) {this.warnings.add(warning);}
    }

    public void addWarning(List<String> warnings) {
        warnings.forEach((value) -> {
            if(!this.warnings.contains(value)) {
                this.warnings.add(value);
            }
        });
    }

    public void removeWarning(String warning) {
        this.warnings.remove(warning);
    }

    public void removeWarning(List<String> warnings) {
        this.warnings.removeAll(warnings);
    }

    private void confirmWarnings() {
        this.warnings.forEach((value) -> {
                DioriteClient.LOGGER.warn("Invalid warning on item: " + value + ". Removing...");
                removeWarning(value);
        });
    }

    private void applyWarnings() {
        //Add any possible warnings here.
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        confirmWarnings(); //Check if warnings still apply, and if not, remove them.
        applyWarnings(); //Apply any new warnings.
        super.render(matrices, mouseX, mouseY, delta);
        if(this.isHovered() && this.visible) {renderTooltip(matrices, mouseX, mouseY);}
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(isHovered() || isToggled()) {
            DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x99999999);
        }
        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, warnings.size()>0 ? 0xFFCCCC00 : 0xFFCCCCCC);
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;

        List<Text> text = new ArrayList<>();
        this.warnings.forEach((value) -> text.add(Text.translatable(value).setStyle(Style.EMPTY.withItalic(true).withColor(0xAAAA00))));

        assert screen != null;
        screen.renderTooltip(matrices, text, mouseX, mouseY);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible) {
            if (keyCode != 257 && keyCode != 32 && keyCode != 335) {
                return false;
            } else {
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                this.onPress();
                return true;
            }
        } else {
            return false;
        }
    }

    public void onPress() {
        this.setToggled(!isToggled());
    }
}
