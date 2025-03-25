package azula.blockcounter.config;

import azula.blockcounter.ActivationMethod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "blockcounter")
public class BlockCounterModMenuConfig implements ConfigData {

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public ActivationMethod activationMethod = ActivationMethod.STANDING;

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public MessageDisplay msgDisplayLocation = MessageDisplay.CHAT;

    @ConfigEntry.Category("General")
    public boolean showPosMessages = true;

    @ConfigEntry.Category("General")
    public boolean simplifiedMessages = false;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
    public ChatColor chatColor = ChatColor.YELLOW;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public RenderType renderType = RenderType.SOLID;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.ColorPicker
    public int renderColor = 0xFF0000;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.ColorPicker
    public int edgeColor = 0x990000;

    @ConfigEntry.Category("Customization")
    @ConfigEntry.BoundedDiscrete(min=0, max=255)
    public int alpha = 200;
}
