package sourcecoded.screenshot.tick;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import sourcecoded.screenshot.Screenshot;
import sourcecoded.screenshot.utils.ScreenShotHelper;

public class TickHandler {

    @SubscribeEvent
    public void keyEvent(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (Screenshot.key.isPressed()) {
            IChatComponent data = ScreenShotHelper.saveScreenshot(mc.mcDataDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            if (Screenshot.displayMessage) mc.ingameGUI.getChatGUI().printChatMessage(data);
        }
    }

}
