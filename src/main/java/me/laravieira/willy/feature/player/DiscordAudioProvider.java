package me.laravieira.willy.feature.player;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import discord4j.voice.AudioProvider;


public final class DiscordAudioProvider extends AudioProvider {

    private final MutableAudioFrame frame = new MutableAudioFrame();
    private final DiscordPlayer player;

    public DiscordAudioProvider(DiscordPlayer player) {
        super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        frame.setBuffer(getBuffer());
        this.player = player;
    }

    @Override
    public boolean provide() {
        boolean didProvide = player.getPlayer().provide(frame);
        if (didProvide)
            getBuffer().flip();
        return didProvide;
    }
}
