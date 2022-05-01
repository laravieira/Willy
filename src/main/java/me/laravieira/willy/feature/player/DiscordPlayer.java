package me.laravieira.willy.feature.player;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.DiscordContext;
import me.laravieira.willy.internal.Config;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Flux;

public class DiscordPlayer {

	private static AudioPlayerManager manager = new DefaultAudioPlayerManager();
	private static Map<Snowflake, DiscordPlayer> players = new HashMap<Snowflake, DiscordPlayer>();
	
	private TrackScheduler trackScheduler = null;
	private AudioProvider provider = null;
	private VoiceChannel channel = null;
	private AudioPlayer player = null;
	
	public static void load() {
		if (!Config.getBoolean("ap.enable"))
			return;
		//manager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		manager.setHttpRequestConfigurator((config) -> RequestConfig.copy(config).setConnectTimeout(10000).build());
		AudioSourceManagers.registerRemoteSources(manager);
	}
	
	public static DiscordPlayer createDiscordPlayer(VoiceChannel channel) {
		DiscordPlayer player;
		if(!players.containsKey(channel.getGuildId())) {
			players.put(channel.getGuildId(), new DiscordPlayer(channel));
			player = players.get(channel.getGuildId());
		}else
			player = players.get(channel.getGuildId());
		
		if(!player.getChannel().isMemberConnected(Snowflake.of(Config.getString("discord.client_id"))).block()) {
			player.getChannel().join(spec -> spec.setProvider(player.getProvider())).block();
			Willy.getLogger().getConsole().info("Joined on "+player.getChannel().getName()+" ("+player.getChannel().getId().asString()+").");
		}return player;
	}
	
	public DiscordPlayer(VoiceChannel channel) {
		player = manager.createPlayer();
		trackScheduler = new TrackScheduler(this);
		player.addListener(trackScheduler);
		provider = new DiscordAudioProvider(this);
		this.channel = channel;
	}
	
	public void add(String uri) {
		try {
			URIBuilder builder = new URIBuilder(uri);
			URIBuilder link = new URIBuilder(builder.build());
			link.clearParameters();
			link.setScheme("https");
			
			for(NameValuePair set : builder.getQueryParams()) {
				if(set.getName().equalsIgnoreCase("v"))
					link.addParameter(set.getName(), set.getValue());
				if(set.getName().equalsIgnoreCase("list"))
					link.addParameter(set.getName(), set.getValue());
				if(set.getName().equalsIgnoreCase("playlist"))
					link.addParameter(set.getName(), set.getValue());
			}
			
	//		// Work around to solve YouTube music
	//		if(link.contains("youtu") && !link.contains("list")) {
	//			Youtube youtube = new Youtube(link);
	//			if(youtube.getVideo()) {
	//				youtube.autoChooseOnlyAudioFormat(null);
	//				link = youtube.getDownloadLink();
	//			}
	//		}
			
			manager.loadItem(link.build().toString(), new AudioLoadResult(this));
		} catch (URISyntaxException e) {
			Willy.getLogger().getConsole().info(e.getMessage());
		}
	}
	
	public void search(String data) {
		manager.loadItem(data, new AudioLoadResult(this));
	}
	
	public void clear() {
		player.stopTrack();
		trackScheduler.clearQueue();
	}

	public void play() {
		trackScheduler.playQueue();
	}

	public void pause() {
		player.setPaused(true);
	}

	public void resume() {
		player.setPaused(false);
	}

	public void stop() {
		player.stopTrack();
	}

	public void next() {
		trackScheduler.nextTrack();
	}

	public void destroy() {
		trackScheduler.clearQueue();
		player.destroy();
		players.remove(channel.getGuildId());
		if(channel.isMemberConnected(Snowflake.of(Config.getString("discord.client_id"))).block())
			channel.sendDisconnectVoiceState().block();
	}
	
	public static AudioPlayerManager getManager() {
		return manager;
	}
	
	public static Map<Snowflake, DiscordPlayer> getPlayers() {
		return players;
	}
	
	public TrackScheduler getTrackScheduler() {
		return trackScheduler;
	}
	
	public AudioProvider getProvider() {
		return provider;
	}
	
	public VoiceChannel getChannel() {
		return channel;
	}
	
	public AudioPlayer getPlayer() {
		return player;
	}

	public static void onVoiceChannelUpdate(VoiceStateUpdateEvent event) {
		Snowflake guild = null;
		if(event.getOld().isPresent())
			guild = event.getOld().get().getGuildId();
		if(players.containsKey(guild)) {
			Flux<VoiceState> voiceStates = event.getOld().get().getChannel().block().getVoiceStates();
			if(voiceStates.count().block() == 1) {
				if(voiceStates.blockFirst().getUserId().equals(Snowflake.of(Config.getString("discord.client_id")))) {
					players.get(guild).destroy();
					Willy.getLogger().getConsole().info("Willy was alone on a voice channel, player instance destroed.");
				}
			}
		}
	}
	
	public static DiscordPlayer getDiscordPlayerFromContext(DiscordContext context) {
		if(context.getMessage() == null)
			return null;
		Member member = context.getMessage().getAuthorAsMember().block();
		if(member != null) {
			VoiceState voiceState = member.getVoiceState().block();
			if(voiceState != null) {
				VoiceChannel channel = (VoiceChannel)voiceState.getChannel().block();
				if(channel != null) {
					DiscordPlayer player = createDiscordPlayer(channel);
					if(channel.getId().equals(player.getChannel().getId()))
						return player;
				}
			}
		}
		return null;
	}
}
