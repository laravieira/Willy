package me.laravieira.willy.feature.youtube;

import java.io.File;
import java.net.URISyntaxException;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;

import me.laravieira.willy.feature.bitly.Bitly;

@SuppressWarnings("unused")
public class Youtube {

	private String       video_id = null;
	private VideoInfo    video    = null;
	private Format       format   = null;
	private String       link     = null;
	
	
	public Youtube(String url) {
		
		try {
			URIBuilder builder = new URIBuilder(url);
			for (NameValuePair set : builder.getQueryParams())
				if(set.getName().equalsIgnoreCase("v"))
					video_id = set.getValue();
		} catch (URISyntaxException e) {
			Willy.getLogger().getConsole().info(e.getMessage());
		}
	}
	
	public String getId() {
		return video_id;
	}
	
	public boolean getVideo() {
		RequestVideoInfo request = new RequestVideoInfo(video_id)
	        .callback(new YoutubeCallback<>() {
				@Override
				public void onFinished(VideoInfo videoInfo) {
					System.out.println("Finished parsing");
				}

				@Override
				public void onError(Throwable throwable) {
					System.out.println("Error: " + throwable.getMessage());
				}
			})
	        .async();
        YoutubeDownloader downloader = new YoutubeDownloader();
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        video = response.data();
        return true;
	}
	
	public boolean autoChooseAnyFormat(String quality) {
		YoutubeFormatter formatter = new YoutubeFormatter();
		if(format == null || video.videoWithAudioFormats() != null || !video.videoWithAudioFormats().isEmpty())
			format = formatter.getVideoWithAudioFormat(video.videoWithAudioFormats(), quality);
		if(format == null || video.videoFormats() != null || !video.videoFormats().isEmpty())
			format = formatter.getVideoOnlyFormat(video.videoFormats(), quality);
		if(format == null || video.audioFormats() != null || !video.audioFormats().isEmpty())
			format = formatter.getAudioOnlyFormat(video.audioFormats(), quality);
		Willy.getLogger().getConsole().info("id: "+format.itag().id()+" quality: "+format.itag().videoQuality().name());
		return format != null;
	}

	public void autoChooseOnlyVideoWithAudioFormat(String quality) {
		video.videoFormats().clear();
		video.audioFormats().clear();
		autoChooseAnyFormat(quality);
	}

	public boolean autoChooseOnlyVideoFormat(String quality) {
		video.videoWithAudioFormats().clear();
		video.audioFormats().clear();
		return autoChooseAnyFormat(quality);
	}

	public boolean autoChooseOnlyAudioFormat(String quality) {
		video.videoWithAudioFormats().clear();
		video.videoFormats().clear();
		return autoChooseAnyFormat(quality);
	}
	
	public boolean setVideoFormat(int quality) {
		format = video.findFormatByItag(quality);
		if(format.url() != null) {
			link = format.url();
        	return true;
		}else return false;
	}
	
	public String getDownloadLink() {
		if(format == null) return null;
		
		if(Config.getBoolean("ytd.willy_vpn"))
			download();
		else link = format.url();
		
		if(Bitly.canUse && Config.getBoolean("ytd.use_bitly")) {
			Bitly bitly = new Bitly(link);
			if(bitly.getShort() != null)
				return bitly.getShort();
			else return link;
		}else return link;
	}
	
	private void download() {
		File folder = new File(new File(".").getAbsolutePath()+File.pathSeparator+"web"+File.pathSeparator+"videos");
		if(!folder.exists() && !folder.mkdirs())
			return;
		RequestVideoFileDownload request = new RequestVideoFileDownload(format)
		    .saveTo(folder)
		    .renameTo("video")
		    .overwriteIfExists(true);
		YoutubeDownloader downloader = new YoutubeDownloader();
		Response<File> response = downloader.downloadVideoFile(request);
		File file = response.data();

		link = Config.getString("web.uri")+"videos/"+ file.getName();
	}
}
