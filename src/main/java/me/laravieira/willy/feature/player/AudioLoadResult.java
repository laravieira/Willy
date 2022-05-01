package me.laravieira.willy.feature.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.laravieira.willy.Willy;

public class AudioLoadResult implements AudioLoadResultHandler {
	
	private DiscordPlayer player;
	
	public AudioLoadResult(DiscordPlayer player) {
		this.player = player;
	}
	
	@Override
	public void trackLoaded(AudioTrack track) {
		player.getTrackScheduler().queue(track);
		if(player.getPlayer().getPlayingTrack() == null)
			player.getTrackScheduler().playQueue();
		Willy.getLogger().getConsole().info("A track has been add on player queue.");
	}

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
    	int added = 0;
    	for(AudioTrack track : playlist.getTracks()) {
    		if(track != null) {
    			player.getTrackScheduler().queue(track);
    			added++;
    		}
    	}
    	if(player.getPlayer().getPlayingTrack() == null)
    		player.getTrackScheduler().playQueue();
    	Willy.getLogger().getConsole().info(""+added+"/"+playlist.getTracks().size()+" tracks has been on player queue");
    }

	@Override
	public void noMatches() {
		Willy.getLogger().getConsole().info("I can't play that, no matches.");
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		Willy.getLogger().getConsole().info(exception.getMessage());
	}
}
