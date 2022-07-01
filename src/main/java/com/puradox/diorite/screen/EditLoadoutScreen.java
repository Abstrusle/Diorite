package com.puradox.diorite.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.puradox.diorite.DioriteClient;
import com.puradox.diorite.config.LoadoutConfiguration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class EditLoadoutScreen extends Screen { //I do not recommend acknowleding anything here as 'best practice.' It just works.

    public final int backgroundWidth = 237;
    public final int backgroundHeight = 204;
    private final Boolean modifiable;

    private static TexturedButtonWidget editFiltersButton;

    private static TexturedButtonWidget saveButton;
    private static TexturedButtonWidget cancelButton;

    private static TextFieldWidget nameTextField;
    private static TextFieldWidget buildingBlocksTextField;
    private static TextFieldWidget idTextField;

    private LoadoutConfiguration loadout;
    private static int originalId;

    private String currentError = "";

    public EditLoadoutScreen(LoadoutConfiguration providedLoadout, boolean fromSecondary) { //From the 'new loadout' or 'configure loadout' buttons.
        super(new TranslatableText("screen.diorite.edit_loadout"));
        this.modifiable=providedLoadout.modifiable;
        this.loadout = new LoadoutConfiguration(providedLoadout);
        if(!fromSecondary) {
            originalId = providedLoadout.id;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        //Render current error, if present.
        this.textRenderer.draw(matrices, new TranslatableText(currentError), (this.width-textRenderer.getWidth(new TranslatableText(currentError)))/(float)2, this.height-30, 0xFF0000);
    }
    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        //Drawing the primary GUI background.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier("diorite","gui/loadout_gui_editor.png"));
        this.drawTexture(matrices, (this.width - this.backgroundWidth)/2, (this.height - backgroundHeight)/2, 0, 0, 256, 256);

        //Now drawing text.
        this.textRenderer.draw(matrices, new TranslatableText("option.diorite.name"),(this.width-this.backgroundWidth)/(float)2+10, (this.height-this.backgroundWidth)/(float)2+75, 0xFF000000);
        this.textRenderer.draw(matrices, new TranslatableText("option.diorite.building_blocks"), (this.width-this.backgroundWidth)/(float)2+10, (this.height-this.backgroundWidth)/(float)2+111, 0xFF000000);
        this.textRenderer.draw(matrices, new TranslatableText("option.diorite.id"), (this.width-this.backgroundWidth)/(float)2+10, (this.height-this.backgroundWidth)/(float)2+147, 0xFF000000);

        textRenderer.drawWithShadow(matrices, this.title, (float)this.width/2-111, (float)this.height/2-(float)this.backgroundHeight/2+5, 4210752);

    }

    @Override
    protected void init() {
        initWidgets();

        this.addDrawableChild(editFiltersButton);
        this.addDrawableChild(cancelButton);
        if(this.modifiable) {this.addDrawableChild(saveButton);}


        this.addDrawableChild(nameTextField);
        this.addDrawableChild(buildingBlocksTextField);
        this.addDrawableChild(idTextField);
    }

    private void initWidgets() {
        if(editFiltersButton!=null) {
            this.remove(editFiltersButton);
            this.remove(saveButton);
            this.remove(cancelButton);

            this.remove(nameTextField);
            this.remove(buildingBlocksTextField);
            this.remove(idTextField);
        }

        editFiltersButton = new TexturedButtonWidget((this.width-this.backgroundWidth)/2+8, (this.height-this.backgroundWidth)/2+34,
                222, 20, 0, 182, 20, new Identifier("diorite", "gui/icons.png"),
                (PressAction) -> {
                    assert client != null;
                    if (EditLoadoutScreen.catchIntegerNumberFormatException(buildingBlocksTextField.getText()) || EditLoadoutScreen.catchIntegerNumberFormatException(idTextField.getText())) {
                        assert client != null;
                        assert client.player != null;
                        currentError="error.diorite.not_integer";
                    } else {
                        loadout.name=nameTextField.getText();
                        loadout.leaveStacksForBuilding=Integer.parseInt(buildingBlocksTextField.getText());
                        loadout.id=Integer.parseInt(idTextField.getText());
                        client.setScreen(new EditLoadoutFiltersScreen(loadout));
                    }

                }) {
            @Override
            public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                super.renderButton(matrices, mouseX, mouseY, delta);
                TranslatableText text = new TranslatableText("screen.diorite.edit_loadout_filters");
                textRenderer.draw(matrices, text, (this.x + (float)this.width / 2) - (float)textRenderer.getWidth(text) / 2, this.y + (float)(this.height - 8) / 2, 0x000000);
            }
        };

        saveButton = new TexturedButtonWidget(this.width/2-22, (this.height+this.backgroundHeight)/2-30, 20, 20, 216, 0, 20, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            if(saveLoadout()) {
                assert client != null;
                client.setScreen(new LoadoutScreen(false));
            }
        }) {
            @Override
            public void renderTooltip(MatrixStack matrixStack, int i, int j) {
                EditLoadoutScreen.this.renderTooltip(matrixStack, new TranslatableText("tooltip.diorite.save"), i, j);
            }
        };

        cancelButton = new TexturedButtonWidget(this.width/2+2, (this.height+this.backgroundHeight)/2-30, 20, 20, 236, 0, 20, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            loadout=DioriteClient.getConfig().loadoutConfigs.get(originalId);
            assert client != null;
            client.setScreen(new LoadoutScreen(false));
        }) {
            @Override
            public void renderTooltip(MatrixStack matrixStack, int i, int j) {
                EditLoadoutScreen.this.renderTooltip(matrixStack, ScreenTexts.CANCEL, i, j);
            }
        };

        nameTextField = new TextFieldWidget(textRenderer, ((this.width + this.backgroundWidth)/2)-72, ((this.height - this.backgroundHeight)/2)+53, 64, 18, new TranslatableText("widgets.diorite.name"));
        nameTextField.setText(loadout.name);
        if(this.modifiable) {
            nameTextField.setMaxLength(9);
        } else {
            nameTextField.setEditable(false);
            nameTextField.active=false;
        }

        buildingBlocksTextField = new TextFieldWidget(textRenderer, ((this.width + this.backgroundWidth)/2)-72, ((this.height - this.backgroundHeight)/2)+89, 64, 18, new TranslatableText("widgets.diorite.building_blocks"));
        buildingBlocksTextField.setText(String.valueOf(loadout.leaveStacksForBuilding));
        if(!modifiable) {
            buildingBlocksTextField.setEditable(false);
            buildingBlocksTextField.active=false;
        }


        idTextField = new TextFieldWidget(textRenderer, ((this.width + this.backgroundWidth)/2)-72, ((this.height - this.backgroundHeight)/2)+125, 64, 18, new TranslatableText("widgets.diorite.id"));
        idTextField.setText(String.valueOf(loadout.id));
        if(!modifiable) {
            idTextField.setEditable(false);
            idTextField.active=false;
        }

    }

    public static boolean catchIntegerNumberFormatException(String integer) {
        try {
            Integer.parseInt(integer);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public boolean saveLoadout() {
        if(loadout.nameFilters.size()==0 && loadout.itemFilters.size()==0 && loadout.nbtStringFilters.size()==0) {
            assert client != null;
            assert client.player != null;
            currentError="error.diorite.no_filters";
        } else if (catchIntegerNumberFormatException(buildingBlocksTextField.getText()) || catchIntegerNumberFormatException(idTextField.getText())) {
            assert client != null;
            assert client.player != null;
            currentError="error.diorite.not_integer";
        }else {
            this.loadout.name=nameTextField.getText();
            this.loadout.leaveStacksForBuilding=Integer.parseInt(buildingBlocksTextField.getText());
            this.loadout.id=originalId; //Not reverting. The following code saves the data using whatever is in the text field.
            if(!transferEntry(loadout, Integer.parseInt(idTextField.getText()))) {
                assert client != null;
                assert client.player != null;
                currentError="error.diorite.bad_id";
                return false; //Id is less than 1, or greater than current size.
            }

            return true;
        }
        return false;
    }

    public boolean transferEntry(LoadoutConfiguration moved, int replaced) {
        if(replaced<=0 || moved.id<=0 || replaced>DioriteClient.getConfig().loadoutConfigs.size()) {return false;} //Cannot be 0 or less.
        Map<Integer, LoadoutConfiguration> modifiedLoadouts = new HashMap<>();
        int originalMovedId = moved.id;

        modifiedLoadouts.put(replaced, moved);
        modifiedLoadouts.get(replaced).id = replaced; //Wouldn't be a transfer if it didn't transfer.

        if(replaced<originalMovedId) { //Moving backwards
            modifiedLoadouts.put(replaced+1, DioriteClient.getConfig().loadoutConfigs.get(replaced));
            modifiedLoadouts.get(replaced+1).id++;
        } else if(replaced>originalMovedId){ //Moving forwards
            modifiedLoadouts.put(replaced-1, DioriteClient.getConfig().loadoutConfigs.get(replaced));
            modifiedLoadouts.get(replaced-1).id--;
        } //Relocating whatever was replaced.

        DioriteClient.getConfig().loadoutConfigs.forEach((key, value) -> { //Relocating everything else.
            if(key<originalMovedId && key<replaced) { //Before
                modifiedLoadouts.putIfAbsent(key, DioriteClient.getConfig().loadoutConfigs.get(key));
            } else if(key>originalMovedId && key<replaced) { //Between. Called only if moving forwards
                modifiedLoadouts.putIfAbsent(key-1, DioriteClient.getConfig().loadoutConfigs.get(key));
                modifiedLoadouts.get(key-1).id--;
            } else if(key<originalMovedId && key>replaced) { //Between. Called only if moving backwards.
                modifiedLoadouts.putIfAbsent(key+1, DioriteClient.getConfig().loadoutConfigs.get(key));
                modifiedLoadouts.get(key+1).id++;
            } else if (key > originalMovedId && key > replaced){ //After
                if(modifiedLoadouts.containsKey(key)) { //If insertion
                    modifiedLoadouts.putIfAbsent(key+1, DioriteClient.getConfig().loadoutConfigs.get(key));
                    modifiedLoadouts.get(key+1).id++;
                } else {
                    modifiedLoadouts.putIfAbsent(key, DioriteClient.getConfig().loadoutConfigs.get(key));
                }
            }
        });
        DioriteClient.getConfig().loadoutConfigs=modifiedLoadouts;
        DioriteClient.saveConfig();
        return true; //Success
    }
}
