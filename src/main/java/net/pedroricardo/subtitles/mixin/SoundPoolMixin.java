package net.pedroricardo.subtitles.mixin;

import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundPoolEntry;
import net.minecraft.core.sound.SoundType;
import net.pedroricardo.subtitles.Subtitles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Mixin(value = SoundManager.class, remap = false)
public class SoundPoolMixin {

    @Inject(method = "playSound(Ljava/lang/String;Lnet/minecraft/core/sound/SoundType;FFFFF)V", at = @At(value = "INVOKE", target = "Lpaulscode/sound/SoundSystem;newSource(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void playSound(String soundPath, SoundType soundType, float x, float y, float z, float f3, float f4, CallbackInfo ci,
                          SoundPoolEntry soundpoolentry, String s1, float f5) throws IOException {
        File file = new File(soundpoolentry.soundUrl.getPath());
        if (file.getName().endsWith(".ogg")) {
            double soundDuration = calculateDuration(file);
            Subtitles.soundsPlaying.put(soundPath, 60.0f + (float)soundDuration / 50.0f);
        } else {
            Subtitles.soundsPlaying.put(soundPath, 150.0f);
        }
        Subtitles.soundsPlayingPositions.put(soundPath, new Float[]{x, z});
    }

    @Inject(method = "playSound(Ljava/lang/String;Lnet/minecraft/core/sound/SoundType;FF)V", at = @At(value = "INVOKE", target = "Lpaulscode/sound/SoundSystem;newSource(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void playSound(String soundPath, SoundType soundType, float volume, float pitch, CallbackInfo ci, SoundPoolEntry soundpoolentry, String soundName) throws IOException {
        File file = new File(soundpoolentry.soundUrl.getPath());
        if (file.getName().endsWith(".ogg")) {
            double soundDuration = calculateDuration(file);
            Subtitles.soundsPlaying.put(soundPath, 60.0f + (float)soundDuration / 50.0f);
        } else {
            Subtitles.soundsPlaying.put(soundPath, 150.0f);
        }
        Subtitles.soundsPlayingPositions.put(soundPath, new Float[]{null, null});
    }

    private double calculateDuration(final File oggFile) throws IOException {
        int rate = -1;
        int length = -1;

        int size = (int) oggFile.length();
        byte[] t = new byte[size];

        FileInputStream stream = new FileInputStream(oggFile);
        stream.read(t);

        for (int i = size-1-8-2-4; i>=0 && length<0; i--) { //4 bytes for "OggS", 2 unused bytes, 8 bytes for length
            // Looking for length (value after last "OggS")
            if (
                    t[i]==(byte)'O'
                            && t[i+1]==(byte)'g'
                            && t[i+2]==(byte)'g'
                            && t[i+3]==(byte)'S'
            ) {
                byte[] byteArray = new byte[]{t[i+6],t[i+7],t[i+8],t[i+9],t[i+10],t[i+11],t[i+12],t[i+13]};
                ByteBuffer bb = ByteBuffer.wrap(byteArray);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                length = bb.getInt(0);
            }
        }
        for (int i = 0; i<size-8-2-4 && rate<0; i++) {
            // Looking for rate (first value after "vorbis")
            if (
                    t[i]==(byte)'v'
                            && t[i+1]==(byte)'o'
                            && t[i+2]==(byte)'r'
                            && t[i+3]==(byte)'b'
                            && t[i+4]==(byte)'i'
                            && t[i+5]==(byte)'s'
            ) {
                byte[] byteArray = new byte[]{t[i+11],t[i+12],t[i+13],t[i+14]};
                ByteBuffer bb = ByteBuffer.wrap(byteArray);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                rate = bb.getInt(0);
            }

        }
        stream.close();

        double duration = (double) (length*1000) / (double) rate;
        return duration;
    }
}
