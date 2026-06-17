package org.zala.audiobridge.mixin;

import com.damir00109.RadioState;
import com.damir00109.WorldRadioManager;
import com.damir00109.extend.ServerWorldExtend;
import com.llamalad7.mixinextras.sugar.Local;
import de.maxhenkel.audioplayer.audioplayback.PlayerThread;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import com.damir00109.RadioPlayer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zala.audiobridge.AudioBridgeStorage;

import java.util.Map;

@Mixin(value = PlayerThread.class, remap = false)
public class PlayerThreadMixin {

    @Final
    @Shadow
    private AudioChannel audioChannel;

    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/api/audiochannel/AudioChannel;send([B)V"
            ),
            remap = false
    )
    private void onFrameSend(CallbackInfo ci, @Local byte[] frame) {
        if (audioChannel instanceof LocationalAudioChannel locationalChannel && frame != null) {

            AudioBridgeStorage.RadioChannelData data = AudioBridgeStorage.boundRadios.get(locationalChannel.getId());

            if (data != null && data.radio != null && data.world != null) {
                int currentChannel = 0;

                try {
                    var stateField = data.radio.getClass().getDeclaredField("lastEnabledState");
                    stateField.setAccessible(true);
                    var state = (RadioState) stateField.get(data.radio);

                    if (!state.equals(RadioState.BROADCAST)) return;
                } catch (Exception e) {
                    return;
                }

                try {
                    var channelField = data.radio.getClass().getDeclaredField("lastChannel");
                    channelField.setAccessible(true);
                    currentChannel = channelField.getInt(data.radio);
                } catch (Exception ignored) {
                }

                if (currentChannel != data.lastCachedChannel) {
                    data.lastCachedChannel = currentChannel;
                    data.cachedRadioPlayersMap = null;

                    if (currentChannel >= 1 && currentChannel <= 15) {
                        try {
                            WorldRadioManager radioManager = ((ServerWorldExtend) data.world).radio_getRadioManager();
                            if (radioManager != null) {
                                var channelsField = radioManager.getClass().getDeclaredField("channels");
                                channelsField.setAccessible(true);
                                Int2ObjectMap<?> channelsMap = (Int2ObjectMap<?>) channelsField.get(radioManager);

                                if (channelsMap != null) {
                                    Object radioChannelObj = channelsMap.get(currentChannel);
                                    if (radioChannelObj != null) {
                                        var radioPlayersField = radioChannelObj.getClass().getDeclaredField("radioPlayers");
                                        radioPlayersField.setAccessible(true);
                                        data.cachedRadioPlayersMap = (Map<?, ?>) radioPlayersField.get(radioChannelObj);
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }

                if (currentChannel < 1 || currentChannel > 15) return;

                if (data.cachedRadioPlayersMap != null && !data.cachedRadioPlayersMap.isEmpty()) {
                    short[] audio = data.decoder.decode(frame);
                    data.radioAudioEffect.apply(audio);
                    byte[] newEncoded = data.encoder.encode(audio);

                    for (Object value : data.cachedRadioPlayersMap.values()) {
                        if (value instanceof RadioPlayer radioPlayer) {
                            radioPlayer.packet(locationalChannel.getId(), newEncoded);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "run", at = @At("TAIL"), remap = false)
    private void onThreadTail(CallbackInfo ci) {
        if (audioChannel instanceof LocationalAudioChannel locationalChannel) {
            AudioBridgeStorage.boundRadios.remove(locationalChannel.getId());
        }
    }
}