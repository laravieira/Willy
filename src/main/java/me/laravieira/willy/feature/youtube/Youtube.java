package me.laravieira.willy.feature.youtube;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import com.github.kiulian.downloader.YoutubeDownloader;
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
        Response<VideoInfo> response = new YoutubeDownloader().getVideoInfo(new RequestVideoInfo(video_id));
        video = response.data();
        return true;
	}

	public String getDownloadLink() {
		if (!video.videoWithAudioFormats().isEmpty())
			link = (format = video.bestVideoWithAudioFormat()).url();
		else if (!video.videoFormats().isEmpty())
			link = (format = video.bestVideoFormat()).url();
		else if(!video.audioFormats().isEmpty())
			link = (format = video.bestAudioFormat()).url();

		if(Bitly.canUse && Config.getBoolean("ytd.use_bitly")) {
			Bitly bitly = new Bitly(link);
			if(bitly.getShort() != null)
				return bitly.getShort();
			else return link;
		}else return link;
	}
	
	public void download() {
		try {
			String VIDEOS_FOLDER = (new File(".").getCanonicalPath())+File.separator+"videos"+File.separator;
			RequestVideoFileDownload request = new RequestVideoFileDownload(format)
					.saveTo(new File(VIDEOS_FOLDER))
					.overwriteIfExists(true)
					.callback(new YoutubeProgressCallback<>() {
						@Override
						public void onDownloading(int progress) {
							Willy.getLogger().info("Youtube video download is " + progress + "% completed.");
						}

						@Override
						public void onFinished(File video) {
							Willy.getLogger().info("Finished yt download of " + video);
							link = video.getAbsolutePath();
						}

						@Override
						public void onError(Throwable throwable) {
							throwable.printStackTrace();
						}
					})
					.async();
			Response<File> response = new YoutubeDownloader().downloadVideoFile(request);
			Thread yt = new Thread(response::data);
			yt.setDaemon(true);
			yt.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
