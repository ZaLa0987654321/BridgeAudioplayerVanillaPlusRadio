package org.zala.audiobridge;

import com.damir00109.RadioAudioEffect;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import net.minecraft.server.world.ServerWorld;
import com.damir00109.blockentity.RadioBlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AudioBridgeStorage {

    // Храним всю нужную информацию о радиоканале для каждого ID трека
    public static final Map<UUID, RadioChannelData> boundRadios = new ConcurrentHashMap<>();

    public static class RadioChannelData {
        public final RadioBlockEntity radio;
        public final ServerWorld world;

        // Переменные для динамического кэширования канала внутри потока
        public int lastCachedChannel = -1;
        public Map<?, ?> cachedRadioPlayersMap = null;

        public final VoicechatServerApi api;
        public final RadioAudioEffect radioAudioEffect;
        public OpusEncoder encoder;
        public OpusDecoder decoder;

        public RadioChannelData(RadioBlockEntity radio, ServerWorld world, VoicechatServerApi api) {
            this.radio = radio;
            this.world = world;

            this.api = api;
            this.radioAudioEffect = new RadioAudioEffect();
            encoder = api.createEncoder();
            decoder = api.createDecoder();
        }
    }
}