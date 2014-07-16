package sourcecoded.screenshot;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.java.games.input.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import sourcecoded.screenshot.tick.TickHandler;

@Mod(modid = Constants.MODID, version = Constants.VERSION)
public class Screenshot {

    Configuration config;

    public static KeyBinding key = new KeyBinding("Take Screenshot", 60, "Screenshotter");

    public static boolean displayMessage;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            FMLLog.log(Level.WARN, "Screenshotter is a Client-Side Only Mod! Please remove it from the server!");
            return;
        }

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        displayMessage = config.getBoolean("DisplayMessage", Configuration.CATEGORY_GENERAL, true, "Display a message after a screenshot is taken?");
        config.save();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

        FMLCommonHandler.instance().bus().register(new TickHandler());

        Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.setOptionKeyBinding(mc.gameSettings.keyBindScreenshot, -1);

        ClientRegistry.registerKeyBinding(key);

    }


}
