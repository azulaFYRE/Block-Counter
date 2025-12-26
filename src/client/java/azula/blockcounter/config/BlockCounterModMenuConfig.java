package azula.blockcounter.config;

import azula.blockcounter.ActivationMethod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "blockcounter")
public class BlockCounterModMenuConfig implements ConfigData {

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 3)
    public ActivationMethod activationMethod = ActivationMethod.STANDING;

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 3)
    public MessageDisplay msgDisplayLocation = MessageDisplay.CHAT;

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Tooltip
    public boolean showPosMessages = true;

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Tooltip
    public boolean simplifiedMessages = false;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
    @ConfigEntry.Gui.Tooltip
    public ChatColor chatColor = ChatColor.YELLOW;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 4)
    public RenderType renderType = RenderType.SOLID;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.Gui.Tooltip
    public boolean builderMode = false;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.ColorPicker
    @ConfigEntry.Gui.Tooltip
    public int renderColor = 0xFF0000;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.ColorPicker
    @ConfigEntry.Gui.Tooltip
    public int edgeColor = 0x990000;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.BoundedDiscrete(min=0, max=255)
    @ConfigEntry.Gui.Tooltip
    public int alpha = 200;
}