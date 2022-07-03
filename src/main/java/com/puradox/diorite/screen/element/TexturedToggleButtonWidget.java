package com.puradox.diorite.screen.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TexturedToggleButtonWidget extends TexturedButtonWidget {
    protected int checkedUOffset;
    protected int hoverVOffset;
    protected boolean checked;
    protected Identifier texture;
    protected int u;
    protected int v;

    public TexturedToggleButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, int toggledUOffset, Identifier texture, boolean checked, PressAction pressAction) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, pressAction);
        this.checkedUOffset = toggledUOffset;
        this.hoverVOffset = hoveredVOffset;
        this.checked=checked;
        this.u=u;
        this.v=v;
        this.texture=texture;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.disableDepthTest();
        int i = this.u;
        int j = this.v;
        if (this.checked) {
            i += this.checkedUOffset;
        }

        if (this.isHovered()) {
            j += this.hoverVOffset;
        }

        this.drawTexture(matrices, this.x, this.y, i, j, this.width, this.height);
        RenderSystem.enableDepthTest();
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
    @Override
    public void onPress() {
        this.toggle();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setToggle(boolean b) {
        checked=b;
    }
    public void toggle() {
        checked=!checked;
    }

}
