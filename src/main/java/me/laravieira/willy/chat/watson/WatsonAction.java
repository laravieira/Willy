package me.laravieira.willy.chat.watson;

import com.ibm.watson.assistant.v2.model.DialogNodeAction;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import me.laravieira.willy.chat.discord.DiscordSender;
import me.laravieira.willy.context.Context;
import me.laravieira.willy.feature.bitly.Bitly;
import me.laravieira.willy.feature.player.DiscordPlayer;
import me.laravieira.willy.feature.youtube.Youtube;
import me.laravieira.willy.chat.openai.OpenAiSender;
import me.laravieira.willy.storage.ContextStorage;

import java.util.UUID;

public class WatsonAction {
	private final DialogNodeAction action;
	private final Context context;
	
	public WatsonAction(DialogNodeAction action, UUID context) {
		this.context = ContextStorage.of(context);
		this.action = action;
		execute();
	}

	private void execute() {
		Thread actionTask = new Thread(() -> {
			if(action.getName().equals("openai"))
				openAi();
			else if(action.getName().equals("get_youtube_link"))
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
		actionTask.setDaemon(true);
		actionTask.start();
	}

	private void openAi() {
		OpenAiSender sender = new OpenAiSender(context.getId());
		sender.sendText(context.getLastMessage().getText());
	}

	private void getYoutubeLink() {
		Youtube youtube = new Youtube(action.getParameters().get("id").toString());
		if(youtube.getVideo()) {
			youtube.autoChooseOnlyVideoWithAudioFormat(null);
			context.getWatson().getSender().sendAction("youtube", youtube.getDownloadLink());
		}else {
			context.getWatson().getSender().sendAction("youtube", "null");
		}
	}
	
	private void shortLink() {
		if(Bitly.canUse) {
			String fullLink  = action.getParameters().get("link").toString();
			String shortLink = new Bitly(fullLink).getShort();
			if(shortLink != null && !shortLink.equals(fullLink) && !shortLink.isEmpty())
				context.getWatson().getSender().sendAction("short_link", shortLink);
			else
				context.getWatson().getSender().sendAction("short_link", "null");
		}else
			context.getWatson().getSender().sendAction("short_link", "false");
	}
	
	private void musicAdd() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			if(action.getParameters().containsKey("link"))
				player.add(action.getParameters().get("link").toString());
			else
				player.play();
		}else
			context.getWatson().getSender().sendAction("music_add", "null");
	}
	
	private void musicPlay() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			player.play();
		}else
			context.getWatson().getSender().sendAction("music_play", "null");
	}

	private void musicResume() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			player.resume();
		}else
			context.getWatson().getSender().sendAction("music_resume", "null");
	}

	private void musicPause() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			player.pause();
		}else
			context.getWatson().getSender().sendAction("music_pause", "null");
	}

	private void musicStop() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			player.stop();
		}else
			context.getWatson().getSender().sendAction("music_stop", "null");
	}

	private void musicNext() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			player.next();
		}else
			context.getWatson().getSender().sendAction("music_next", "null");
	}

	private void musicClear() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			player.clear();
		}else
			context.getWatson().getSender().sendAction("music_clear", "null");
	}

	private void musicDestroy() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			player.destroy();
		}else
			context.getWatson().getSender().sendAction("music_destroy", "null");
	}

	private void musicInfo() {
		DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context.getId());
		if(player != null) {
			EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
				embed.color(Color.of(27, 161, 121));

				if(player.getPlayer().getPlayingTrack() == null)
					embed.title("Atualmente Parado.");
				else if(player.getPlayer().isPaused())
					embed.title("Atualmente Pausado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.FINISHED))
					embed.title("Atualmente Parado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.INACTIVE))
					embed.title("Atualmente Parado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.LOADING))
					embed.title("Atualmente Carregando.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.PLAYING))
					embed.title("Atualmente Reproduzindo.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.SEEKING))
					embed.title("Atualmente Parado.");
				else if(player.getPlayer().getPlayingTrack().getState().equals(AudioTrackState.STOPPING))
					embed.title("Atualmente Parado.");
				
				
				if(player.getPlayer().getPlayingTrack() != null) {
					long duration = player.getPlayer().getPlayingTrack().getDuration()/1000;
					long position = player.getPlayer().getPlayingTrack().getPosition()/1000;
					String title  = player.getPlayer().getPlayingTrack().getInfo().title;
					String author = player.getPlayer().getPlayingTrack().getInfo().author;
					title = (title.length() > 35)?title.substring(0, 35).trim()+"...":title.trim();
					author = (author.length() > 35)?author.substring(0, 35).trim()+"...":author.trim();
					String body = title + "\r\n" + author + "\r\n" + position/60+":"+position%60 + (duration>3600?"":" de "+duration/60+":"+duration%60);
					embed.addField("Faixa atual", body, true);
				}
				
				if(player.getTrackScheduler().getNext() != null) {
					long duration = player.getTrackScheduler().getNext().getDuration()/1000;
					String title  = player.getTrackScheduler().getNext().getInfo().title;
					String author = player.getTrackScheduler().getNext().getInfo().author;
					title = (title.length() > 35)?title.substring(0, 35).trim()+"...":title.trim();
					author = (author.length() > 35)?author.substring(0, 35).trim()+"...":author.trim();
					String body = title + "\r\n" + author + "\r\n" + (duration>3600?"> 1h":duration/60+":"+duration%60);
					embed.addField("Próxima", body, true);
				}

				if(player.getTrackScheduler().getQueue().size() == 0)
					embed.addField("Não há músicas na fila.", "\u200B", false);
				else if(player.getTrackScheduler().getQueue().size() == 1)
					embed.addField("Não há mais músicas na fila.", "\u200B", false);
				else if(player.getTrackScheduler().getQueue().size() == 2)
					embed.addField("Tem mais 1 música em espera.", "\u200B", false);
				else {
					int size = player.getTrackScheduler().getQueue().size();
					embed.addField("Tem mais " + (size-1) + " músicas em espera.", "\u200B", false);
				}
				
				embed.footer("On "+player.getChannel().getName(), null);
			((DiscordSender)context.getSender()).sendEmbed(MessageCreateSpec.builder()
					.addEmbed(embed.build())
					.build());
		}else
			context.getWatson().getSender().sendAction("music_info", "null");
	}
}
