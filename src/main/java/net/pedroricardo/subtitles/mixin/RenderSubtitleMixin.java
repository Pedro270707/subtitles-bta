package net.pedroricardo.subtitles.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.render.FontRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.Color;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.subtitles.Subtitles;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = GuiIngame.class, remap = false)
public class RenderSubtitleMixin {
    @Mixin(value = GuiIngame.class, remap = false)
    private interface GuiIngameAccessor {
        @Accessor("mc")
        Minecraft mc();
    }

    @Mixin(value = Gui.class, remap = false)
    private interface GuiAccessor {
        @Invoker("drawRectWidthHeight")
        void drawRectInvoker(int x, int y, int width, int height, int color);
    }

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupScaledResolution()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderGameOverlay(float partialTicks, boolean flag, int mouseX, int mouseY, CallbackInfo ci,
                                  I18n stringtranslate, int width, int height, int sp,
                                  FontRenderer fontrenderer) {
        Minecraft minecraft = ((GuiIngameAccessor)((GuiIngame)(Object)this)).mc();
        int subtitleLines = 0;
        int lineHeight = 10;
        Map<String, Float> temporarySoundsPlaying = new HashMap<>(Subtitles.soundsPlaying);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        int longestStringWidth = 0;
        for (String string : temporarySoundsPlaying.keySet()) {
            if (fontrenderer.getStringWidth(stringtranslate.translateKey("subtitles." + string)) > longestStringWidth) {
                longestStringWidth = fontrenderer.getStringWidth(stringtranslate.translateKey("subtitles." + string));
            }
        }
        for (String string : temporarySoundsPlaying.keySet()) {
            if (Subtitles.soundsPlaying.get(string) > 0) {
                subtitleLines++;
                Subtitles.soundsPlaying.put(string, Subtitles.soundsPlaying.get(string) - partialTicks);
                double angleDegrees = 0.0f;
                if (Subtitles.soundsPlayingPositions.get(string)[0] != null && Subtitles.soundsPlayingPositions.get(string)[1] != null) {
                    Vec3d playerVector = minecraft.thePlayer.getLookAngle().normalize();
                    Vec3d soundVector = Vec3d.createVector(Subtitles.soundsPlayingPositions.get(string)[0] - minecraft.thePlayer.x, 0.0f, Subtitles.soundsPlayingPositions.get(string)[1] - minecraft.thePlayer.z).normalize();

                    double crossProduct = soundVector.xCoord * playerVector.zCoord - soundVector.zCoord * playerVector.xCoord;
                    double angleRadians = Math.atan2(crossProduct, dotProduct(playerVector, soundVector));
                    angleDegrees = Math.toDegrees(angleRadians);
                }

                // Normalize the angle to be within the range of 0 to 360
                angleDegrees = (angleDegrees + 360) % 360;

                int arrowWidth = fontrenderer.getStringWidth("< > ");

                String stringToDraw = stringtranslate.translateKey("subtitles." + string);

                ((GuiAccessor) ((GuiIngame) (Object) this)).drawRectInvoker(
                        width - longestStringWidth - 1 - sp - arrowWidth,
                        height - 35 - (subtitleLines * lineHeight) - sp,
                        longestStringWidth + 1 + arrowWidth, lineHeight, -872415232);
                GL11.glEnable(3553);

                fontrenderer.drawString(stringToDraw,
                        width - (longestStringWidth / 2) - (fontrenderer.getStringWidth(stringToDraw) / 2) - sp - arrowWidth / 2,
                        height - 34 - (subtitleLines * lineHeight) - sp,
                        Color.intToIntARGB(
                                (int) (Math.min(70.0f, 20.0f + Subtitles.soundsPlaying.get(string)) * 255.0f / 70.0f), 255,
                                255, 255));

                if (angleDegrees <= 325 && 215 <= angleDegrees) {
                    fontrenderer.drawString(">",
                            width - 6 - sp,
                            height - 34 - (subtitleLines * lineHeight) - sp,
                            Color.intToIntARGB(
                                    (int) (Math.min(70.0f, 10.0f + Subtitles.soundsPlaying.get(string)) * 255.0f / 70.0f), 255,
                                    255, 255));
                } else if (angleDegrees >= 35 && 145 >= angleDegrees) {
                    fontrenderer.drawString("<",
                            width - longestStringWidth - sp - arrowWidth,
                            height - 34 - (subtitleLines * lineHeight) - sp,
                            Color.intToIntARGB(
                                    (int) (Math.min(70.0f, 20.0f + Subtitles.soundsPlaying.get(string)) * 255.0f / 70.0f), 255,
                                    255, 255));
                }
            } else {
                subtitleLines -= 1;
                Subtitles.soundsPlaying.remove(string);
                Subtitles.soundsPlayingPositions.remove(string);
            }
        }
    }

    private static double dotProduct(Vec3d vec1, Vec3d vec2) {
        return vec1.xCoord * vec2.xCoord + vec1.yCoord * vec2.yCoord + vec1.zCoord * vec2.zCoord;
    }
}
