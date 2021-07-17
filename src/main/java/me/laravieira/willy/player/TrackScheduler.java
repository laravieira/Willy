package me.laravieira.willy.player;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.discord.Discord;

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
		MyLogger.getConsoleLogger().info("Playing "+title+" of "+track.getInfo().author+".");
		if(Config.getPlayerChangeActivity())
			try {
				Thread.sleep(500);
				Discord.getBotGateway().updatePresence(Presence.online(Activity.listening(track.getInfo().title))).block();
			} catch (InterruptedException e) {}
	}
	
	@Override
	public void onPlayerResume(AudioPlayer player) {
		AudioTrack track = player.getPlayingTrack();
		String title = (track.getInfo().title.length() > 25)?track.getInfo().title.substring(0, 25).trim()+"...":track.getInfo().title;
		MyLogger.getConsoleLogger().info("Playing "+title+" of "+track.getInfo().author+".");
		if(Config.getPlayerChangeActivity())
			try {
				Thread.sleep(500);
				Discord.getBotGateway().updatePresence(Presence.online(Activity.listening(track.getInfo().title))).block();
			} catch (InterruptedException e) {}
	}
	
	@Override
	public void onPlayerPause(AudioPlayer player) {
		if(Config.getPlayerChangeActivity())
			Discord.getBotGateway().updatePresence(Presence.online()).block();
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if(endReason.mayStartNext)
			nextTrack();
		else if(Config.getPlayerChangeActivity())
				Discord.getBotGateway().updatePresence(Presence.online()).block();
	}
	
	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		MyLogger.getConsoleLogger().info("Track error:"+exception.getMessage());
		if(Config.getPlayerChangeActivity())
			Discord.getBotGateway().updatePresence(Presence.online()).block();
	}
	
	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		MyLogger.getConsoleLogger().info("Track stuck.");
		if(Config.getPlayerChangeActivity())
			Discord.getBotGateway().updatePresence(Presence.online()).block();
	}
}