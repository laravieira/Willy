package me.laravieira.willy.youtube;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;

import me.laravieira.willy.bitly.Bitly;
import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;

public class Youtube {
	
	private String       youtube  = null;
	private String       video_id = null;
	private VideoInfo    video    = null;
	private Format       format   = null;
	private File         file     = null;
	private String       link     = null;
	
	
	public Youtube(String url) {
		
		try {
			URIBuilder builder = new URIBuilder(url);
			for (NameValuePair set : builder.getQueryParams())
				if(set.getName().equalsIgnoreCase("v"))
					video_id = set.getValue();
			youtube = builder.build().toString();
		} catch (URISyntaxException e) {
			MyLogger.getConsoleLogger().info(e.getMessage());
		}
	}
	
	public String getYouTubeLink() {
		return youtube;
	}
	
	public String getId() {
		return video_id;
	}
	
	public boolean getVideo() {
		RequestVideoInfo request = new RequestVideoInfo(video_id)
	        .callback(new YoutubeCallback<VideoInfo>() {
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
	
	public List<Format> getFormats() {
		return video.formats();
	}
	
	public boolean autoChooseAnyFormat(String quality) {
		YoutubeFormatter formatter = new YoutubeFormatter();
		if(format == null || video.videoWithAudioFormats() != null || !video.videoWithAudioFormats().isEmpty())
			format = formatter.getVideoWithAudioFormat(video.videoWithAudioFormats(), quality);
		if(format == null || video.videoFormats() != null || !video.videoFormats().isEmpty())
			format = formatter.getVideoOnlyFormat(video.videoFormats(), quality);
		if(format == null || video.audioFormats() != null || !video.audioFormats().isEmpty())
			format = formatter.getAudioOnlyFormat(video.audioFormats(), quality);
		MyLogger.getConsoleLogger().info("id: "+format.itag().id()+" quality: "+format.itag().videoQuality().name());
		if(format != null) return true;
		else return false;
	}

	public boolean autoChooseOnlyVideoWithAudioFormat(String quality) {
		video.videoFormats().clear();
		video.audioFormats().clear();
		return autoChooseAnyFormat(quality);
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
		
		if(Config.getYoutubeUseWillyVPN())
			download();
		else link = format.url();
		
		if(Bitly.canUse && Config.getYoutubeUseBitly()) {
			Bitly bitly = new Bitly(link);
			if(bitly.getShorted() != null)
				return bitly.getShorted();
			else return link;
		}else return link;
	}
	
	private void download() {
		File folder = new File(new File(".").getAbsolutePath()+File.pathSeparator+"web"+File.pathSeparator+"videos");
		if(!folder.exists()) folder.mkdirs();
		RequestVideoFileDownload request = new RequestVideoFileDownload(format)
		    .saveTo(folder)
		    .renameTo("video")
		    .overwriteIfExists(true);
		YoutubeDownloader downloader = new YoutubeDownloader();
		Response<File> response = downloader.downloadVideoFile(request);
		file = response.data();

		link = Config.getWebUri()+"videos/"+file.getName();
	}
	
//    private static boolean isReachable(String url) {
//        try {
//            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//            connection.setConnectTimeout(1000);
//            connection.setReadTimeout(1000);
//            connection.setRequestMethod("HEAD");
//            int responseCode = connection.getResponseCode();
//            return (200 <= responseCode && responseCode <= 399);
//        } catch (IOException exception) {
//            return false;
//        }
//    }
}
