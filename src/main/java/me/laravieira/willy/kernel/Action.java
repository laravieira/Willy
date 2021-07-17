package me.laravieira.willy.kernel;

import com.ibm.watson.assistant.v2.model.DialogNodeAction;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;

import discord4j.rest.util.Color;
import me.laravieira.willy.bitly.Bitly;
import me.laravieira.willy.player.DiscordPlayer;
import me.laravieira.willy.youtube.Youtube;

public class Action {
	private DialogNodeAction action = null;
	private Context context = null;
	
	public Action(String context) {
		this.context = Context.getContext(context);
		execute();
	}
	
	public Action(Context context) {
		this.context = context;
		execute();
	}

	private void execute() {
		Thread actionTask = new Thread(() -> {
			context.getWatsonMessager().getActions().forEach((action) -> {
				this.action = action;
				if(action.getName().equals("get_youtube_link"))
					getYoutubeLink();
				else if(action.getName().equals("short"))
					shortLink();
				else if(action.getName().equals("music_add"))
					musicAdd();
				else if(action.getName().equals("music_play"))
					musicPlay();
				else if(action.getName().equals("music_resume"))
					musicResume();
				else if(action.getName().equals("music_pause"))
					musicPause();
				else if(action.getName().equals("music_stop"))
					musicStop();
				else if(action.getName().equals("music_next"))
					musicNext();
				else if(action.getName().equals("music_clear"))
					musicClear();
				else if(action.getName().equals("music_destroy"))
					musicDestroy();
				else if(action.getName().equals("music_info"))
					musicInfo();
			});
		});
		actionTask.setDaemon(true);
		actionTask.start();
	}
	
	private void getYoutubeLink() {
		Youtube youtube = new Youtube(action.getParameters().get("id").toString());
		if(youtube.getVideo()) {
			youtube.autoChooseOnlyVideoWithAudioFormat(null);
			context.getWatsonMessager().sendActionMessage("youtube", youtube.getDownloadLink());
		}else {
			context.getWatsonMessager().sendActionMessage("youtube", "null");
		}
	}
	
	private void shortLink() {
		if(Bitly.canUse) {
			String fullLink  = action.getParameters().get("link").toString();
			String shortLink = new Bitly(fullLink).getShorted();
			if(shortLink != null && shortLink != fullLink && !shortLink.isEmpty())
				context.getWatsonMessager().sendActionMessage("short_link", shortLink);
			else
				context.getWatsonMessager().sendActionMessage("short_link", "null");
		}else
			context.getWatsonMessager().sendActionMessage("short_link", "false");
	}
	
	private void musicAdd() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			if(action.getParameters().containsKey("link"))
				player.add(action.getParameters().get("link").toString());
			else
				player.play();
		}else
			context.getWatsonMessager().sendActionMessage("music_add", "null");
	}
	
	private void musicPlay() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			player.play();
		}else
			context.getWatsonMessager().sendActionMessage("music_play", "null");
	}

	private void musicResume() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			player.resume();
		}else
			context.getWatsonMessager().sendActionMessage("music_resume", "null");
	}

	private void musicPause() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			player.pause();
		}else
			context.getWatsonMessager().sendActionMessage("music_pause", "null");
	}

	private void musicStop() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			player.stop();
		}else
			context.getWatsonMessager().sendActionMessage("music_stop", "null");
	}

	private void musicNext() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			player.next();
		}else
			context.getWatsonMessager().sendActionMessage("music_next", "null");
	}

	private void musicClear() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			player.clear();
		}else
			context.getWatsonMessager().sendActionMessage("music_clear", "null");
	}

	private void musicDestroy() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			player.destroy();
		}else
			context.getWatsonMessager().sendActionMessage("music_destroy", "null");
	}

	private void musicInfo() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
		if(player != null) {
			Messager messager = new Messager(context);
			messager.sendDiscordEmbedMessage(spec -> {
				spec.setColor(Color.of(27, 161, 121));

				if(player.getPlayer().getPlayingTrack() == null)
					spec.setTitle("Atualmente Parado.");
				else if(player.getPlayer().isPaused())
					spec.setTitle("Atualmente Pausado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.FINISHED))
					spec.setTitle("Atualmente Parado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.INACTIVE))
					spec.setTitle("Atualmente Parado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.LOADING))
					spec.setTitle("Atualmente Carregando.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.PLAYING))
					spec.setTitle("Atualmente Reproduzindo.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.SEEKING))
					spec.setTitle("Atualmente Parado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.STOPPING))
					spec.setTitle("Atualmente Parado.");
				
				
				if(player.getPlayer().getPlayingTrack() != null) {
					long duration = player.getPlayer().getPlayingTrack().getDuration()/1000;
					long position = player.getPlayer().getPlayingTrack().getPosition()/1000;
					String title  = player.getPlayer().getPlayingTrack().getInfo().title;
					String author = player.getPlayer().getPlayingTrack().getInfo().author;
					title = (title.length() > 35)?title.substring(0, 35).trim()+"...":title.trim();
					author = (author.length() > 35)?author.substring(0, 35).trim()+"...":author.trim();
					String body = title + "\r\n" + author + "\r\n" + position/60+":"+position%60 + (duration>3600?"":" de "+duration/60+":"+duration%60);
					spec.addField("Faixa atual", body, true);
				}
				
				if(player.getTrackScheduler().getNext() != null) {
					long duration = player.getTrackScheduler().getNext().getDuration()/1000;
					String title  = player.getTrackScheduler().getNext().getInfo().title;
					String author = player.getTrackScheduler().getNext().getInfo().author;
					title = (title.length() > 35)?title.substring(0, 35).trim()+"...":title.trim();
					author = (author.length() > 35)?author.substring(0, 35).trim()+"...":author.trim();
					String body = title + "\r\n" + author + "\r\n" + (duration>3600?"> 1h":duration/60+":"+duration%60);
					spec.addField("Próxima", body, true);
				}

				if(player.getTrackScheduler().getQueue().size() == 0)
					spec.addField("Não há músicas na fila.", "\u200B", false);
				else if(player.getTrackScheduler().getQueue().size() == 1)
					spec.addField("Não há mais músicas na fila.", "\u200B", false);
				else if(player.getTrackScheduler().getQueue().size() == 2)
					spec.addField("Tem mais 1 música em espera.", "\u200B", false);
				else {
					int size = player.getTrackScheduler().getQueue().size();
					spec.addField("Tem mais " + (size-1) + " músicas em espera.", "\u200B", false);
				}
				
				spec.setFooter("On "+player.getChannel().getName(), null);
			});
		}else
			context.getWatsonMessager().sendActionMessage("music_info", "null");
	}
}
