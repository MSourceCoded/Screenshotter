package sourcecoded.screenshot.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SideOnly(Side.CLIENT)
public class ScreenShotHelper
{
    private static final Logger logger = LogManager.getLogger();
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    /** A buffer to hold pixel values returned by OpenGL. */
    private static IntBuffer pixelBuffer;
    /**
     * The built-up array that contains all the pixel values returned by OpenGL.
     */
    private static int[] pixelValues;
    private static final String __OBFID = "CL_00000656";

    /**
     * Saves a screenshot in the game directory with a time-stamped filename.  Args: gameDirectory,
     * requestedWidthInPixels, requestedHeightInPixels, frameBuffer
     */
    public static IChatComponent saveScreenshot(File gameDir, int xSize, int ySize, Framebuffer framebuffer) {
        return saveScreenshot(gameDir, (String)null, xSize, ySize, framebuffer);
    }

    /**
     * Saves a screenshot in the game directory with the given file name (or null to generate a time-stamped name).
     * Args: gameDirectory, fileName, requestedWidthInPixels, requestedHeightInPixels, frameBuffer
     */
    public static IChatComponent saveScreenshot(File File, String suggestedName, int xSize, int ySize, final Framebuffer framebuffer) {
        try {
            File file2 = new File(File, "screenshots");
            file2.mkdir();

            if (OpenGlHelper.isFramebufferEnabled()) {
                xSize = framebuffer.framebufferTextureWidth;
                ySize = framebuffer.framebufferTextureHeight;
            }

            int k = xSize * ySize;

            if (pixelBuffer == null || pixelBuffer.capacity() < k) {
                pixelBuffer = BufferUtils.createIntBuffer(k);
                pixelValues = new int[k];
            }

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();

            if (OpenGlHelper.isFramebufferEnabled()) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            } else {
                GL11.glReadPixels(0, 0, xSize, ySize, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            }

            pixelBuffer.get(pixelValues);
            TextureUtil.func_147953_a(pixelValues, xSize, ySize);

            final File file3;

            if (suggestedName == null) {
                file3 = getTimestampedPNGFileForDirectory(file2);
            } else {
                file3 = new File(file2, suggestedName);
            }

            final int finalXSize = xSize;
            final int finalYSize = ySize;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedImage bufferedimage = null;

                        if (OpenGlHelper.isFramebufferEnabled()) {
                            bufferedimage = new BufferedImage(framebuffer.framebufferWidth, framebuffer.framebufferHeight, 1);
                            int l = framebuffer.framebufferTextureHeight - framebuffer.framebufferHeight;

                            for (int i1 = l; i1 < framebuffer.framebufferTextureHeight; ++i1) {
                                for (int j1 = 0; j1 < framebuffer.framebufferWidth; ++j1) {
                                    bufferedimage.setRGB(j1, i1 - l, pixelValues[i1 * framebuffer.framebufferTextureWidth + j1]);
                                }
                            }
                        } else {
                            bufferedimage = new BufferedImage(finalXSize, finalYSize, 1);
                            bufferedimage.setRGB(0, 0, finalXSize, finalYSize, pixelValues, 0, finalXSize);
                        }

                        ImageIO.write(bufferedimage, "png", file3);
                    } catch (IOException e) {}
                }
            }).start();

            ChatComponentText chatcomponenttext = new ChatComponentText(file3.getName());
            chatcomponenttext.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath()));
            chatcomponenttext.getChatStyle().setUnderlined(true);
            return new ChatComponentTranslation("screenshot.success", chatcomponenttext);
        } catch (Exception exception) {
            logger.warn("Couldn\'t save screenshot", exception);
            return new ChatComponentTranslation("screenshot.failure", exception.getMessage());
        }
    }

    /**
     * Creates a unique PNG file in the given directory named by a timestamp.  Handles cases where the timestamp alone
     * is not enough to create a uniquely named file, though it still might suffer from an unlikely race condition where
     * the filename was unique when this method was called, but another process or thread created a file at the same
     * path immediately after this method returned.
     */
    private static File getTimestampedPNGFileForDirectory(File par0File)
    {
        String s = dateFormat.format(new Date());
        int i = 1;

        while (true)
        {
            File file2 = new File(par0File, s + (i == 1 ? "" : "_" + i) + "_c.png");

            if (!file2.exists())
            {
                return file2;
            }

            ++i;
        }
    }
}