package com.puradox.diorite.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.puradox.diorite.config.LoadoutConfiguration;
import com.puradox.diorite.screen.element.ToggleItemWidget;
import com.puradox.diorite.screen.element.ToggleTextWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class EditLoadoutFiltersScreen extends Screen { //I do not recommend acknowleding anything here as 'best practice.' It just works.
    public static final int backgroundWidth = 237;
    public static final int backgroundHeight = 204;
    private int scrollLevel = 0;
    private static int maxScroll = 1;
    private boolean showItems = false;
    private boolean showNames = false;
    private boolean showNbtStrings = false;
    private boolean areEntriesRendered = false;


    private static TexturedButtonWidget saveButton;
    private static TexturedButtonWidget cancelButton;

    private static TexturedButtonWidget deleteEntryButton;

    private static ToggleButtonWidget itemsButton;
    private static ToggleButtonWidget namesButton;
    private static ToggleButtonWidget nbtStringsButton;

    private static TextFieldWidget entryTextField;

    private static TexturedButtonWidget leftPageButton;
    private static TexturedButtonWidget rightPageButton;
    private static TexturedButtonWidget confirmSelectionButton;
    private int currentPage;

    private static List<ToggleTextWidget> nameEntries;
    private static List<ToggleTextWidget> nbtStringEntries;
    private static List<ToggleItemWidget> itemEntries;

    private LoadoutConfiguration loadout;
    private final LoadoutConfiguration originalLoadout;
    private final boolean modifiable;

    public List<String> nameFilters;
    public List<String> itemFilters;
    public List<String> nbtStringFilters;

    private String currentError = "";
    private int ticks = 0;

    List<ToggleItemWidget> currentItemsPreview = new ArrayList<>();

    public EditLoadoutFiltersScreen (LoadoutConfiguration loadout) {
        super(Text.translatable("screen.diorite.edit_loadout_filters"));

        this.loadout=loadout;
        this.originalLoadout = loadout;

        this.modifiable=loadout.modifiable;

        this.nameFilters = Objects.requireNonNullElseGet(new ArrayList<>(loadout.nameFilters), ArrayList::new);
        this.itemFilters = Objects.requireNonNullElseGet(new ArrayList<>(loadout.itemFilters), ArrayList::new);
        this.nbtStringFilters = Objects.requireNonNullElseGet(new ArrayList<>(loadout.nbtStringFilters), ArrayList::new);
    }

    @Override
    public void init() {
        initEntries();
        initWidgets();

        this.addDrawableChild(cancelButton);
        if(this.modifiable) {
            this.addDrawableChild(saveButton);
            this.addDrawableChild(entryTextField);
        }

        this.addDrawableChild(deleteEntryButton);
        if((itemsSelected().size()<1 && namesSelected().size()<1 && nbtStringsSelected().size()<1) || !this.modifiable) {
            deleteEntryButton.active=false;
            deleteEntryButton.visible=false;
        } else {
            deleteEntryButton.active=true;
            deleteEntryButton.visible=true;
        }

        this.addDrawableChild(itemsButton);
        this.addDrawableChild(namesButton);
        this.addDrawableChild(nbtStringsButton);

        //The buttons and entries for the item list are registered in render().
        currentPage=0;
    }

    private void initEntries() {
        if(nameEntries != null && itemEntries != null && nbtStringEntries != null) {
            itemEntries.forEach(this::remove);
            nameEntries.forEach(this::remove);
            nbtStringEntries.forEach(this::remove);
        }

        var i = new Object() {int value = 0;};
        var f = new Object() {double value = -0.01;}; //Used for Y in item list. Scrollbar will impact these positions.
        nameEntries = new ArrayList<>();
        nameFilters.forEach((value) ->
                nameEntries.add(new ToggleTextWidget(((this.width - backgroundWidth)/2)+10, ((this.height - backgroundHeight)/2)+44+(i.value++*10)+(scrollLevel*10), false, Text.literal(value), this.textRenderer, 0)));
        i.value=0;
        nbtStringEntries = new ArrayList<>();
        nbtStringFilters.forEach((value) ->
                nbtStringEntries.add(new ToggleTextWidget(((this.width - backgroundWidth)/2)+10, ((this.height - backgroundHeight)/2)+44+(i.value++*10)+(scrollLevel*10), false, Text.literal(value), this.textRenderer, 1)));
        i.value=0;
        itemEntries = new ArrayList<>();
        itemFilters.forEach((value) -> {
            if(i.value>10) {i.value=0;}
            itemEntries.add(new ToggleItemWidget(((this.width - backgroundWidth)/2)+10+(18*(i.value++)), ((this.height - backgroundHeight)/2)+44+(((int)(f.value+=(double)1/11))*18)+(scrollLevel*18), false, value));
        });
        areEntriesRendered=false;
    }

    private void initWidgets() {
        if(cancelButton!=null) {
            this.remove(saveButton);
            this.remove(cancelButton);

            this.remove(deleteEntryButton);
            this.remove(entryTextField);

            this.remove(itemsButton);
            this.remove(namesButton);
            this.remove(nbtStringsButton);

            initEntries();
        }

        saveButton = new TexturedButtonWidget(this.width/2-22, (this.height+backgroundHeight)/2-30, 20, 20, 216, 0, 20, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            this.loadout.itemFilters=itemFilters;
            this.loadout.nameFilters=nameFilters;
            this.loadout.nbtStringFilters=nbtStringFilters;
            //Saving all filters, if they haven't been already.

            assert client != null;
            client.setScreen(new EditLoadoutScreen(loadout, true));
        }) {
            @Override public void renderTooltip(MatrixStack matrixStack, int i, int j) {
                EditLoadoutFiltersScreen.this.renderTooltip(matrixStack, Text.translatable("tooltip.diorite.save"), i, j);
            }
        };

        cancelButton = new TexturedButtonWidget(this.width/2+2, (this.height+backgroundHeight)/2-30, 20, 20, 236, 0, 20, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            loadout=originalLoadout;
            assert client != null;
            client.setScreen(new EditLoadoutScreen(loadout, true));
        }) {
            @Override public void renderTooltip(MatrixStack matrixStack, int i, int j) {
                EditLoadoutFiltersScreen.this.renderTooltip(matrixStack, ScreenTexts.CANCEL, i, j);
            }
        };

        deleteEntryButton = new TexturedButtonWidget(((this.width + backgroundWidth)/2)-22, ((this.height - backgroundHeight)/2)+20, 16, 16, 48, 0, 16, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            if(itemsButton.isToggled()) {
                itemsSelected().forEach((value) -> {
                    value.visible=false;
                    itemEntries.remove(value);
                    itemFilters.remove(value.getItemString());
                });
            } else if(namesButton.isToggled()) {
                namesSelected().forEach((value) -> {
                    value.visible=false;
                    nameEntries.remove(value);
                    nameFilters.remove(value.getMessage().getString());
                });
            } else if(nbtStringsButton.isToggled()) {
                nbtStringsSelected().forEach((value) -> {
                    value.visible=false;
                    nbtStringEntries.remove(value);
                    nbtStringFilters.remove(value.getMessage().getString());
                });
            }
            initEntries();
        });

        //For ToggleButtonWidgets: Unselect all. Untoggle other tabs. Render will call to render related widgets. Rendering text must be manual.
        itemsButton = new ToggleButtonWidget((this.width/2)-110, ((this.height - backgroundHeight)/2)+17, 68, 25, showItems) {
            @Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                if (this.visible) {
                    this.hovered = this.active && ((mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + 12) && mouseY < (double)(this.y + this.height))
                            || (mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height));
                    if (isHovered()) {renderTooltip(matrices, mouseX, mouseY);}
                    this.renderButton(matrices, mouseX, mouseY, delta);
                }
            }
            @Override public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                super.renderButton(matrices, mouseX, mouseY, delta);
                textRenderer.draw(matrices, getMessage(), (this.x + (float)this.width / 2) - (float)textRenderer.getWidth(getMessage()) / 2, this.y + 4 + (float)(this.height - 8) / 2, 0x000000);
            }

            @Override public void onClick(double mouseX, double mouseY) {
                onPress();
            }
            public void onPress() {
                if(isToggled()) {
                    itemsSelected().forEach((value) -> setToggled(false));
                    this.setToggled(false);
                    showItems=false;
                    initEntries();
                    unrenderRegistryItems();
                } else{
                    if(namesButton.isToggled()) {
                        namesSelected().forEach((value) -> setToggled(false));
                        namesButton.setToggled(false);
                        showNames=false;
                    }
                    if(nbtStringsButton.isToggled()) {
                        nbtStringsSelected().forEach((value) -> setToggled(false));
                        nbtStringsButton.setToggled(false);
                        showNbtStrings=false;
                    }
                    scrollLevel=0;
                    initEntries();
                    this.setToggled(true);
                    showItems=true;
                }
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

            @Override protected boolean clicked(double mouseX, double mouseY) {
                return this.active && this.visible && ((mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + 12) && mouseY < (double)(this.y + this.height))
                        || (mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height));
            }

            @Override public boolean isMouseOver(double mouseX, double mouseY) {
                return this.active && this.visible && ((mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + 12) && mouseY < (double)(this.y + this.height))
                        || (mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height));
            }
        };
        itemsButton.setTextureUV(0, 32, 68, 25, new Identifier("diorite", "gui/icons.png"));
        itemsButton.setMessage(Text.translatable("tooltip.diorite.items"));



        namesButton = new ToggleButtonWidget((this.width/2)-42, ((this.height - backgroundHeight)/2)+17, 68, 25, showNames) {

            @Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                if (this.visible) {
                    this.hovered = this.active && mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height;
                    if (isHovered()) {renderTooltip(matrices, mouseX, mouseY);}
                    this.renderButton(matrices, mouseX, mouseY, delta);
                }
            }
            @Override public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                super.renderButton(matrices, mouseX, mouseY, delta);
                textRenderer.draw(matrices, getMessage(), (this.x + (float)this.width / 2) - (float)textRenderer.getWidth(getMessage()) / 2, this.y + 4 + (float)(this.height - 8) / 2, 0x000000);
            }

            @Override public void onClick(double mouseX, double mouseY) {
                onPress();
            }

            public void onPress() {
                if(isToggled()) {
                    namesSelected().forEach((value) -> setToggled(false));
                    this.setToggled(false);
                    showNames=false;
                    initEntries();
                } else{
                    if(itemsButton.isToggled()) {
                        itemsSelected().forEach((value) -> setToggled(false));
                        itemsButton.setToggled(false);
                        showItems=false;
                    }
                    if(nbtStringsButton.isToggled()) {
                        nbtStringsSelected().forEach((value) -> setToggled(false));
                        nbtStringsButton.setToggled(false);
                        showNbtStrings=false;
                    }
                    scrollLevel=0;
                    initEntries();
                    this.setToggled(true);
                    showNames=true;
                }
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
            @Override protected boolean clicked(double mouseX, double mouseY) {
                return this.active && this.visible && mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height;
            }

            @Override public boolean isMouseOver(double mouseX, double mouseY) {
                return this.active && this.visible && mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height;
            }
        };
        namesButton.setTextureUV(0, 82, 68, 25, new Identifier("diorite", "gui/icons.png"));
        namesButton.setMessage(Text.translatable("tooltip.diorite.names"));



        nbtStringsButton = new ToggleButtonWidget((this.width/2)+26, ((this.height - backgroundHeight)/2)+17, 68, 25, showNbtStrings) {

            @Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                if (this.visible) {
                    this.hovered = this.active && ((mouseX >= (double)this.x+56 && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height))
                            || (mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height));
                    if (isHovered()) {renderTooltip(matrices, mouseX, mouseY);}
                    this.renderButton(matrices, mouseX, mouseY, delta);
                }
            }
            @Override public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                super.renderButton(matrices, mouseX, mouseY, delta);
                textRenderer.draw(matrices, getMessage(), (this.x + (float)this.width / 2) - (float)textRenderer.getWidth(getMessage()) / 2, this.y + 4 + (float)(this.height - 8) / 2, 0x000000);
            }

            @Override public void onClick(double mouseX, double mouseY) {
                this.onPress();
            }

            public void onPress() {
                if(isToggled()) {
                    nbtStringsSelected().forEach((value) -> setToggled(false));
                    initEntries();
                    this.setToggled(false);
                    showNbtStrings=false;
                } else{
                    if(itemsButton.isToggled()) {
                        itemsSelected().forEach((value) -> setToggled(false));
                        itemsButton.setToggled(false);
                        showItems=false;
                    }
                    if(namesButton.isToggled()) {
                        namesSelected().forEach((value) -> setToggled(false));
                        namesButton.setToggled(false);
                        showNames=false;
                    }
                    scrollLevel=0;
                    initEntries();
                    this.setToggled(true);
                    showNbtStrings=true;
                }
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
            @Override protected boolean clicked(double mouseX, double mouseY) {
                return this.active && this.visible && ((mouseX >= (double)this.x+56 && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height))
                        || (mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height));
            }

            @Override public boolean isMouseOver(double mouseX, double mouseY) {
                return this.active && this.visible && ((mouseX >= (double)this.x+56 && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height))
                        || (mouseX>=this.x && mouseX<=this.x+this.width && mouseY>=this.y+8 && mouseY<=this.y+height));
            }
        };
        nbtStringsButton.setTextureUV(0, 132, 68, 25, new Identifier("diorite", "gui/icons.png"));
        nbtStringsButton.setMessage(Text.translatable("tooltip.diorite.nbt_strings"));

        //Use suggestions.
        entryTextField = new TextFieldWidget(textRenderer, (this.width/2)-95, ((this.height - backgroundHeight)/2)+14, 175, 14, Text.translatable("widgets.diorite.add_entry")) {
            @Override
            public boolean charTyped(char chr, int modifiers) {
                boolean superValue = super.charTyped(chr, modifiers);
                if(entryTextField.isFocused() && showItems) {
                    var suggestion = new Object() {String value;};
                    Registry.ITEM.stream().takeWhile(n -> suggestion.value==null).forEach(value -> {
                        if(Registry.ITEM.getId(value).toString().contains(entryTextField.getText())) {
                            suggestion.value = Registry.ITEM.getId(value).toString();
                        }
                    });
                    entryTextField.setSuggestion(suggestion.value);
                }
                return superValue;
            }

            @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                boolean superValue = super.keyPressed(keyCode, scanCode, modifiers);
                if(keyCode == GLFW.GLFW_KEY_ENTER && entryTextField.getText().length()>0 && areEntriesRendered) {
                    String text = entryTextField.getText().toLowerCase().trim(); //Remove leading and trailing whitespace, and uncap.
                    var b = new Object() {boolean value = true;}; //For breaking loops.

                    if(showItems) {
                        if (!text.contains(":")) {
                            text = "minecraft:"+text; //Add identifier if lacking.
                            setCurrentError("warning.diorite.no_namespace");
                        }
                        String finalText = text;

                        itemFilters.stream().takeWhile(n -> b.value).forEach(entry -> {
                            if(finalText.equals(entry)) {
                                setCurrentError("error.diorite.duplicate_filters");
                                b.value=false;
                            }
                        });
                        if(!b.value) {
                            entryTextField.setText("");
                            return false;
                        } //If a duplicate, no reason to add it.

                        if(Registry.ITEM.getDefaultId().equals(new Identifier(text))) {
                            setCurrentError("warning.diorite.item_not_detected");
                        }

                        itemFilters.add(0, text); //Add the entry.


                    } else if(showNames) {
                        String finalText1 = text;
                        List<String>newNameFilters= new ArrayList<>(nameFilters);
                        nameFilters.stream().takeWhile(n -> b.value).forEach(entry -> {
                            if(entry.contains(finalText1)) {
                                setCurrentError("warning.diorite.contained_filter");
                                newNameFilters.remove(entry);
                            } else if(finalText1.contains(entry)) {
                                setCurrentError("error.diorite.contains_filter");
                                b.value=false;
                            }
                        });
                        if(!b.value) {
                            setText("");
                            return false;
                        } //A contained filter will always be superior, so why add it?
                        nameFilters=newNameFilters;

                        nameFilters.add(0, text);


                    } else if(showNbtStrings) {
                        String finalText1 = text;
                        List<String>newNbtStringFilters= new ArrayList<>(nbtStringFilters);
                        nbtStringFilters.stream().takeWhile(n -> b.value).forEach(entry -> {
                            if(entry.contains(finalText1)) {
                                setCurrentError("warning.diorite.contained_filter");
                                newNbtStringFilters.remove(entry);
                            } else if(finalText1.contains(entry)) {
                                setCurrentError("error.diorite.contains_filter");
                                b.value=false;
                            }
                        });
                        if(!b.value) {
                            entryTextField.setText("");
                            return false;
                        } //A contained filter will always be superior, so why add it?
                        nbtStringFilters=newNbtStringFilters;

                        nbtStringFilters.add(0, text);

                    }


                    assert client != null;
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.8F));
                    initEntries();

                    entryTextField.setText(""); //Clear field after submission.
                }
                return superValue;
            }
        };
        entryTextField.setDrawsBackground(false);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        if(!areEntriesRendered) {
            if (itemsButton.isToggled()) {
                maxScroll=(-itemEntries.size()/88)-88;
                renderItemEntries();
                if(modifiable) {
                    listAllRegistryItems();
                }

            } else if (namesButton.isToggled()) {
                maxScroll=(-nameEntries.size()/15)-15;
                renderNameEntries();
                unrenderRegistryItems();
            } else if (nbtStringsButton.isToggled()) {
                maxScroll=(-nbtStringEntries.size()/15)-15;
                renderNbtStringEntries();
                unrenderRegistryItems();
            }
        }


        //Render delete-entry button if a selection exists.
        if((itemsSelected().size()<1 && namesSelected().size()<1 && nbtStringsSelected().size()<1) || !this.modifiable) {
            deleteEntryButton.active=false;
            deleteEntryButton.visible=false;
        } else {
            deleteEntryButton.active=true;
            deleteEntryButton.visible=true;
        }


        //Draw scrollbar.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier("diorite","gui/loadout_gui_extra.png"));
        if(isScrollBarHovered(mouseX, mouseY)) {
            this.drawTexture(matrices, ((this.width + backgroundWidth)/2)-20, (int)(((this.height - backgroundHeight)/2)+43+((double)scrollLevel/maxScroll*138)), 33, 0, 12, 15);
        } else{
            this.drawTexture(matrices, ((this.width + backgroundWidth)/2)-20, (int)(((this.height - backgroundHeight)/2)+43+((double)scrollLevel/maxScroll*138)), 21, 0, 12, 15);
        }

        //If text field isn't focused, inform the user.
        if(modifiable && !entryTextField.isFocused()) {
            entryTextField.setSuggestion("");
            if(entryTextField.getText().length()<1) {
                textRenderer.draw(matrices, Text.translatable("widgets.diorite.add_entry"), (this.width - backgroundWidth)/(float)2+23, ((this.height - backgroundHeight)/(float)2)+14, 0xFFFFFFFF);
            }

        }

        //Render the current error, if present.
        this.textRenderer.draw(matrices, Text.translatable(currentError), (this.width-textRenderer.getWidth(Text.translatable(currentError)))/(float)2, this.height-30, 0xFF0000);
        if(ticks++>99) {
            currentError="";
            ticks = 0;
        }
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier("diorite","gui/loadout_gui_filters.png"));
        this.drawTexture(matrices, (this.width - backgroundWidth)/2, (this.height - backgroundHeight)/2, 0, 0, 256, 256);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int oldScroll = scrollLevel;
        scrollLevel+=amount;
        if (scrollLevel < maxScroll) {
            scrollLevel = maxScroll;
        } else if (scrollLevel > 0) {scrollLevel = 0;}
        if (oldScroll != scrollLevel) {
            initEntries();
            //May need to set visibility of all entries, if something breaks.
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(isScrollBarHovered((int)mouseX, (int)mouseY)) {
            scrollLevel -= deltaY;
            if (scrollLevel < maxScroll) {
                scrollLevel = maxScroll;
            } else if (scrollLevel > 0) {scrollLevel = 0;}
            initEntries();
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean isScrollBarHovered(int mouseX, int mouseY) {
        return mouseX >= (this.width + backgroundWidth) / 2 - 20 && mouseX <= (this.width + backgroundWidth) / 2 - 8
                && mouseY >= (this.height - backgroundHeight) / 2 + 43 && mouseY <= ((this.height - backgroundHeight) / 2)+196;
    }

    private void listAllRegistryItems() { //Create a selectable widget for each registry item.

        unrenderRegistryItems(); //No stacking.

        var xPos = new Object() {int value;
            {
                assert MinecraftClient.getInstance().currentScreen != null;
                value = (MinecraftClient.getInstance().currentScreen.width + EditLoadoutFiltersScreen.backgroundWidth)/2-18;
            }
        };
        var yPos = new Object() {int value = 0;};
        var idPos = new Object() {int value = 0;};

        Registry.ITEM.stream().takeWhile(n -> yPos.value<height-48).forEach((value) -> {
                if (xPos.value > width - 35) {
                    xPos.value = (this.width + backgroundWidth) / 2;
                    yPos.value += 18;
                }
                xPos.value+=18;
                idPos.value++;
        }); //Determining how many items fit. Definitely not the efficient means to do this, but for some reason I'm unable to math this.
        int itemsFit = idPos.value;

        assert MinecraftClient.getInstance().currentScreen != null;
        xPos.value=(MinecraftClient.getInstance().currentScreen.width + EditLoadoutFiltersScreen.backgroundWidth)/2;
        yPos.value=0;
        idPos.value=0;



        Registry.ITEM.stream().takeWhile(n -> yPos.value<height-48).forEach((value) -> {
            if(idPos.value >= currentPage*itemsFit-(2*currentPage)) {
                if (xPos.value > width - 35) {
                    xPos.value = (this.width + backgroundWidth) / 2;
                    yPos.value += 18;
                }
                if (yPos.value < height - 48) {
                    currentItemsPreview.add(new ToggleItemWidget(xPos.value += 18, yPos.value, false, Registry.ITEM.getId(value).toString()));
                }
            }
            idPos.value++;
        }); //Adding the toggleable item widgets.

        assert MinecraftClient.getInstance().currentScreen != null;
        leftPageButton = new TexturedButtonWidget((int)(width*0.8), this.height-25, 20, 20, 216, 40, 20, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            if (currentPage == 0) {
                currentPage=(int)((double)Registry.ITEM.size()/itemsFit);
                listAllRegistryItems();
            } else {
                currentPage--;
                listAllRegistryItems();
            }
        });
        rightPageButton = new TexturedButtonWidget((int)(width*0.9), this.height-25,20, 20, 236, 40, 20, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            if (currentPage == (int)((double)Registry.ITEM.size()/itemsFit)) {
                currentPage=0;
                listAllRegistryItems();
            } else {
                currentPage++;
                listAllRegistryItems();
            }
        });
        confirmSelectionButton = new TexturedButtonWidget((int)(width*0.85), this.height-25, 20, 20, 216, 0, 20, new Identifier("diorite", "gui/icons.png"), (PressAction) -> {
            var b = new Object() {
                boolean value = true;
            }; //For breaking loops.

            newItemsSelected().forEach((value) -> {
                itemFilters.stream().takeWhile(n -> b.value).forEach(entry -> {
                    if (value.getItemString().equals(entry)) {
                        setCurrentError("error.diorite.duplicate_filters");
                        b.value = false;
                    }
                });
                if (b.value) {
                    itemFilters.add(0, value.getItemString()); //Add the entry.
                }
                value.setToggled(false);
                b.value=true;

            });
            initEntries();

        });


        currentItemsPreview.forEach(this::addDrawableChild);

        this.addDrawableChild(leftPageButton);
        this.addDrawableChild(rightPageButton);
        this.addDrawableChild(confirmSelectionButton);
    }

    private void unrenderRegistryItems() {
        if(!itemsButton.isToggled()) {currentPage=0;} //May be prone to errors. Bugtest here.
        this.currentItemsPreview.forEach(this::remove);
        this.currentItemsPreview = new ArrayList<>();

        this.remove(leftPageButton);
        this.remove(rightPageButton);
        this.remove(confirmSelectionButton);

        leftPageButton = null;
        rightPageButton = null;
        confirmSelectionButton = null;
    }

    private void renderItemEntries() {
        itemEntries.forEach((value) -> {
            if(value.y > ((this.height - backgroundHeight)/2)+40 && value.y < this.height / (2)+backgroundHeight/2 - 10) {
                this.addDrawableChild(value);
                value.visible=true;
            } //The scrollbar effect will automatically place invalid items outside bounds. This can be used to determine if they're displayed or not.
        });
        areEntriesRendered=true;
    }

    private void renderNameEntries() {
        nameEntries.forEach((value) -> {
            if(value.y > ((this.height - backgroundHeight)/2)+40 && value.y < this.height / (2)+backgroundHeight/2 - 10) {
                this.addDrawableChild(value);
                value.visible=true;
            } //The scrollbar effect will automatically place invalid items outside bounds. This can be used to determine if they're displayed or not.
        });
        areEntriesRendered=true;
    }

    private void renderNbtStringEntries() {
        nbtStringEntries.forEach((value) -> {
            if(value.y > ((this.height - backgroundHeight)/2)+40 && value.y < this.height / (2)+backgroundHeight/2 - 10) {
                this.addDrawableChild(value);
                value.visible=true;
            } //The scrollbar effect will automatically place invalid items outside bounds. This can be used to determine if they're displayed or not.
        });
        areEntriesRendered=true;
    }

    private List<ToggleItemWidget> itemsSelected() {
        List<ToggleItemWidget> widgets = new ArrayList<>();
        itemEntries.forEach((value) -> {
            if(value.isToggled()) {widgets.add(value);}
        });
        return widgets;
    }

    private List<ToggleItemWidget> newItemsSelected() {
        List<ToggleItemWidget> widgets = new ArrayList<>();
        currentItemsPreview.forEach((value) -> {
            if(value.isToggled()) {widgets.add(value);}
        });
        return widgets;
    }

    private List<ToggleTextWidget> namesSelected() {
        List<ToggleTextWidget> widgets = new ArrayList<>();
        nameEntries.forEach((value) -> {
            if(value.isToggled()) {widgets.add(value);}
        });
        return widgets;
    }

    private List<ToggleTextWidget> nbtStringsSelected() {
        List<ToggleTextWidget> widgets = new ArrayList<>();
        nbtStringEntries.forEach((value) -> {
            if(value.isToggled()) {widgets.add(value);}
        });
        return widgets;
    }

    private void setCurrentError(String error) {
        ticks=0;
        currentError=error;
    }

    public static List<ToggleItemWidget> getItemEntries() {
        return itemEntries;
    }

    public static List<ToggleTextWidget> getNameEntries() {
        return nameEntries;
    }

    public static List<ToggleTextWidget> getNbtStringEntries() {
        return nbtStringEntries;
    }
}
