package me.laravieira.willy.feature.player;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.Discord;

public class TrackScheduler extends AudioEventAdapter {
	private BlockingQueue<AudioTrack> queue;
	private DiscordPlayer player;

	public TrackScheduler(DiscordPlayer player) {
		queue = new LinkedBlockingQueue<>();
		this.player = player;
	}

	public void queue(AudioTrack track) {
		queue.add(track);
	}

	public void playQueue() {
		if(player.getPlayer().isPaused())
			player.getPlayer().setPaused(false);
		player.getPlayer().startTrack(queue.poll(), true);
	}
	
	public void nextTrack() {
		if(player.getPlayer().isPaused())
			player.getPlayer().setPaused(false);
		player.getPlayer().startTrack(queue.poll(), false);
	}

	public void clearQueue() {
		queue.clear();
	}

	public AudioTrack getNext() {
		return queue.peek();
	}
	
	public BlockingQueue<AudioTrack> getQueue() {
		return queue;
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		String title = (track.getInfo().title.length() > 25)?track.getInfo().title.substring(0, 25).trim()+"...":track.getInfo().title;
		Willy.getLogger().getConsole().info("Playing "+title+" of "+track.getInfo().author+".");
		if(Willy.getConfig().asBoolean("audio-player.change-activity"))
			try {
				Thread.sleep(500);
				Discord.getBotGateway()
					.updatePresence(ClientPresence.online(ClientActivity.listening(track.getInfo().title)))
					.block();
			} catch (InterruptedException e) {}
	}
	
	@Override
	public void onPlayerResume(AudioPlayer player) {
		AudioTrack track = player.getPlayingTrack();
		String title = (track.getInfo().title.length() > 25)?track.getInfo().title.substring(0, 25).trim()+"...":track.getInfo().title;
		Willy.getLogger().getConsole().info("Playing "+title+" of "+track.getInfo().author+".");
		if(Willy.getConfig().asBoolean("audio-player.change-activity"))
			try {
				Thread.sleep(500);
				Discord.getBotGateway()
					.updatePresence(ClientPresence.online(ClientActivity.listening(track.getInfo().title)))
					.block();
			} catch (InterruptedException e) {}
	}
	
	@Override
	public void onPlayerPause(AudioPlayer player) {
		if(Willy.getConfig().asBoolean("audio-player.change-activity"))
			Discord.getBotGateway().updatePresence(ClientPresence.online()).block();
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if(endReason.mayStartNext)
			nextTrack();
		else if(Willy.getConfig().asBoolean("audio-player.change-activity"))
				Discord.getBotGateway().updatePresence(ClientPresence.online()).block();
	}
	
	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		Willy.getLogger().getConsole().info("Track error:"+exception.getMessage());
		if(Willy.getConfig().asBoolean("audio-player.change-activity"))
			Discord.getBotGateway().updatePresence(ClientPresence.online()).block();
	}
	
	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		Willy.getLogger().getConsole().info("Track stuck.");
		if(Willy.getConfig().asBoolean("audio-player.change-activity"))
			Discord.getBotGateway().updatePresence(ClientPresence.online()).block();
	}
}