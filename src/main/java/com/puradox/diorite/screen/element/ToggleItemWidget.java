package com.puradox.diorite.screen.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.puradox.diorite.DioriteClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
@Environment(EnvType.CLIENT)
public class ToggleItemWidget extends ToggleButtonWidget {
    private final ItemStack item;
    private final String itemString;
    private List<String> warnings;
    public ToggleItemWidget(int x, int y, boolean toggled, String item) {
        super(x, y, 18, 18, toggled);
        this.itemString=item;
        this.item=new ItemStack(Registry.ITEM.get(new Identifier(item)));
        warnings = new ArrayList<>();
    }

    public ToggleItemWidget(int x, int y, boolean toggled, String item, List<String> warnings) {
        super(x, y, 18, 18, toggled);
        this.itemString=item;
        this.item=new ItemStack(Registry.ITEM.get(new Identifier(item)));
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

    public void confirmWarnings() {
        this.warnings.forEach((value) -> {
            if(value.equals("warning.diorite.item_not_detected")) {
                if(!Registry.ITEM.getDefaultId().equals(Registry.ITEM.getId(item.getItem()))) {
                    removeWarning(value);
                }
            } else {
                DioriteClient.LOGGER.warn("Invalid warning on item: " + value + ". Removing...");
                removeWarning(value);
            }
        });
    }

    public void applyWarnings() {
        if(Registry.ITEM.getDefaultId().equals(Registry.ITEM.getId(item.getItem()))) {
            this.addWarning("warning.diorite.item_not_detected");
        }
        //Add more warnings if necessary.
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

        MinecraftClient client = MinecraftClient.getInstance();
        if(isHovered() || isToggled()) {
            DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x99999999);
        }
        client.getItemRenderer().renderInGui(item, x+1, y+1);
        if(warnings.size()>0) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, new Identifier("diorite", "gui/icons.png"));
            this.drawTexture(matrices, x + 1, y + 1, 64, 0, 16, 16);
        }
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        List<Text> text = new ArrayList<>(List.of(Text.literal(itemString), Text.of("")));
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
    public ItemStack getItem() {
        return new ItemStack(item.getItem());
    }

    public String getItemString() {
        return new String(itemString);
    }

    public List<String> getWarnings() {return new ArrayList<>(warnings);}
}
