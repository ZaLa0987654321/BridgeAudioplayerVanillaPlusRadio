package org.zala.audiobridge.mixin;

import com.damir00109.RadioAudioEffect;
import com.damir00109.VoiceIntegration;
import com.damir00109.WorldRadioManager;
import com.damir00109.extend.ServerWorldExtend;
import de.maxhenkel.audioplayer.apiimpl.ChannelReferenceImpl;
import de.maxhenkel.audioplayer.audioplayback.PlayerManager;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import com.damir00109.blockentity.RadioBlockEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zala.audiobridge.AudioBridgeStorage;

import java.util.Map;
import java.util.UUID;

@Mixin(value = PlayerManager.class, remap = false)
public class PlayerManagerMixin {

    @Inject(
            method = "playLocational",
            at = @At("RETURN"),
            remap = false
    )
    private void onPlayLocational(ServerWorld level, Vec3d pos, UUID sound, ServerPlayerEntity p, float distance, String category, Float maxLengthSeconds, CallbackInfoReturnable<ChannelReferenceImpl<LocationalAudioChannel>> cir) {
        Object channelReference = cir.getReturnValue();
        if (channelReference == null) return;

        try {
            var getChannelMethod = channelReference.getClass().getMethod("getChannel");
            Object rawChannel = getChannelMethod.invoke(channelReference);

            if (rawChannel instanceof LocationalAudioChannel locationalChannel) {
                BlockPos jukeboxPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z).down();
                BlockPos radioPos = jukeboxPos.up();

                var blockEntity = level.getBlockEntity(radioPos);
                if (blockEntity instanceof RadioBlockEntity radio) {
                    var channelField = radio.getClass().getDeclaredField("lastChannel");
                    channelField.setAccessible(true);
                    int currentChannel = channelField.getInt(radio);
                    Map<?, ?> radioPlayersMap = null;
                    VoicechatServerApi api = null;

                    // Выполняем поиск мапы ОДИН РАЗ при старте пластинки
                    WorldRadioManager radioManager = ((ServerWorldExtend) level).radio_getRadioManager();
                    if (radioManager != null && currentChannel >= 1 && currentChannel <= 15) {
                        var channelsField = radioManager.getClass().getDeclaredField("channels");
                        channelsField.setAccessible(true);
                        Int2ObjectMap<?> channelsMap = (Int2ObjectMap<?>) channelsField.get(radioManager);

                        if (channelsMap != null) {
                            Object radioChannelObj = channelsMap.get(currentChannel);
                            if (radioChannelObj != null) {
                                var radioPlayersField = radioChannelObj.getClass().getDeclaredField("radioPlayers");
                                radioPlayersField.setAccessible(true);
                                radioPlayersMap = (Map<?, ?>) radioPlayersField.get(radioChannelObj);

                                var apiField = radioChannelObj.getClass().getDeclaredField("api");
                                apiField.setAccessible(true);
                                api = (VoicechatServerApi) apiField.get(radioChannelObj);
                            }
                        }
                    }

                    if (api == null) {
                        api = VoiceIntegration.getServerApi();
                    }

                    assert api != null;
                    AudioBridgeStorage.boundRadios.put(
                            locationalChannel.getId(),
                            new AudioBridgeStorage.RadioChannelData(radio, level, api)
                    );
                }
            }
        } catch (Exception ignored) {
        }
    }
}
