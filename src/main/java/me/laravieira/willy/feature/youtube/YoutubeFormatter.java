package me.laravieira.willy.feature.youtube;

import java.util.*;
import java.util.function.Consumer;

import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import org.jetbrains.annotations.Nullable;

public class YoutubeFormatter {
	
	private final Map<Integer, Format> formats = new HashMap<>();
	private Format               format  = null;
	
	private final Queue<Integer> avBestQualities = new LinkedList<>();
	private final Queue<Integer> avGoodQualities = new LinkedList<>();
	private final Queue<Integer> avHalfQualities = new LinkedList<>();
	private final Queue<Integer> avPoorQualities = new LinkedList<>();

	private final Queue<Integer> vBestQualities = new LinkedList<>();
	private final Queue<Integer> vGoodQualities = new LinkedList<>();
	private final Queue<Integer> vHalfQualities = new LinkedList<>();
	private final Queue<Integer> vPoorQualities = new LinkedList<>();
	
	private final Queue<Integer> aBestQualities = new LinkedList<>();
	private final Queue<Integer> aGoodQualities = new LinkedList<>();
	private final Queue<Integer> aPoorQualities = new LinkedList<>();
	
	public YoutubeFormatter() {
		
		// Videos with Audio
		avHalfQualities.add(44); avBestQualities.add(46); avBestQualities.add(38);
		avHalfQualities.add(35); avBestQualities.add(37); avGoodQualities.add(22);
		avHalfQualities.add(18); avGoodQualities.add(45); avHalfQualities.add(34);
		avHalfQualities.add(43); avPoorQualities.add(5);  avPoorQualities.add(6);
		avPoorQualities.add(17); avPoorQualities.add(36); avPoorQualities.add(13);
		
		// VR or 3D Videos with Audio
		avHalfQualities.add(101); avHalfQualities.add(83); avBestQualities.add(85);
		avGoodQualities.add(102); avGoodQualities.add(84); avHalfQualities.add(100);
		avHalfQualities.add(82);
		
		// Apple HTTP Live Streaming
		//avBestQualities.add(96);  avHalfQualities.add(94);  avGoodQualities.add(95);
		//avHalfQualities.add(93);  avGoodQualities.add(132); avPoorQualities.add(92);
		//avPoorQualities.add(151); avPoorQualities.add(91);

	    // DASH mp4 video
		vBestQualities.add(266); vBestQualities.add(138); vBestQualities.add(264);
		vBestQualities.add(299); vBestQualities.add(137); vGoodQualities.add(212);
		vGoodQualities.add(135); vGoodQualities.add(298); vGoodQualities.add(136);
		vHalfQualities.add(134); vPoorQualities.add(133); vPoorQualities.add(160);
		
	    // DASH mp4 video
		vBestQualities.add(266); vBestQualities.add(138); vBestQualities.add(264);
		vBestQualities.add(299); vBestQualities.add(137); vHalfQualities.add(212);
		vHalfQualities.add(135); vGoodQualities.add(298); vGoodQualities.add(136);
		vGoodQualities.add(134); vPoorQualities.add(133); vPoorQualities.add(160);
		
		// Dash webm Video
		vBestQualities.add(272); vBestQualities.add(315); vBestQualities.add(313);
		vBestQualities.add(308); vBestQualities.add(271); vBestQualities.add(303);
		vBestQualities.add(248); vBestQualities.add(170); vHalfQualities.add(246);
		vHalfQualities.add(245); vHalfQualities.add(244); vHalfQualities.add(218);
		vHalfQualities.add(168); vGoodQualities.add(302); vGoodQualities.add(247);
		vGoodQualities.add(169); vHalfQualities.add(167); vHalfQualities.add(243);
		vPoorQualities.add(242); vPoorQualities.add(278); vPoorQualities.add(219);
		
		// Dash webm HDR Video
		vHalfQualities.add(333); vBestQualities.add(335); vBestQualities.add(336);
		vBestQualities.add(337); vHalfQualities.add(332); vGoodQualities.add(334);
		vPoorQualities.add(330); vPoorQualities.add(331);
		
		// AV01 Video
		vPoorQualities.add(394); vPoorQualities.add(395); vHalfQualities.add(396);
	    vGoodQualities.add(398); vHalfQualities.add(397); vBestQualities.add(399);
	    vBestQualities.add(400); vBestQualities.add(401); vBestQualities.add(402);
		
	    // Dash MP4 Audio
	    aBestQualities.add(141); aGoodQualities.add(140); aPoorQualities.add(256);
	    aPoorQualities.add(325); aPoorQualities.add(328); aPoorQualities.add(139);
	    
	    // Dash webm Audio
	    aBestQualities.add(172); aGoodQualities.add(171);
	    
	    // Dash webm Audio with Opus
	    aGoodQualities.add(251); aPoorQualities.add(249); aPoorQualities.add(250);
	}

	private final Consumer<Integer> qualityFinder = quality -> {
		if(!formats.containsKey(quality))
			format = formats.get(quality);
	};

	@Nullable
	private Format getSomeFormat() {
		if(!formats.isEmpty()) {
			Queue<Queue<Integer>> queue = new LinkedList<>();
			queue.add(avBestQualities);
			queue.add(avGoodQualities);
			queue.add(avHalfQualities);
			queue.add(avPoorQualities);

			queue.add(vBestQualities);
			queue.add(vGoodQualities);
			queue.add(vHalfQualities);
			queue.add(vPoorQualities);

			queue.add(aBestQualities);
			queue.add(aGoodQualities);
			queue.add(aPoorQualities);

			queue.forEach(formatList -> formatList.forEach(qualityFinder));
			return format;

		}else return null;
	}

	private Format getFormatByQuality(List<Format> formats, Queue<Integer> best, Queue<Integer> good, Queue<Integer> medium, Queue<Integer> poor, String quality) {
		if(quality == null) quality = "any";
		if(quality.equalsIgnoreCase("best")) {
			formats.forEach((format) -> {
				if(best.contains(format.itag().id()))
					this.formats.put(format.itag().id(), format);
			}); return getSomeFormat();
		}else if(quality.equalsIgnoreCase("good")) {
			formats.forEach((format) -> {
				if(good.contains(format.itag().id()))
					this.formats.put(format.itag().id(), format);
			}); return getSomeFormat();
		}else if(quality.equalsIgnoreCase("medium")) {
			formats.forEach((format) -> {
				if(medium.contains(format.itag().id()))
					this.formats.put(format.itag().id(), format);
			}); return getSomeFormat();
		}else if(quality.equalsIgnoreCase("poor")) {
			formats.forEach((format) -> {
				if(poor.contains(format.itag().id()))
					this.formats.put(format.itag().id(), format);
			}); return getSomeFormat();
		}else {
			formats.forEach((format) -> this.formats.put(format.itag().id(), format));
			return getSomeFormat();
		}
	}

	public Format getVideoWithAudioFormat(List<VideoWithAudioFormat> formats, String quality) {
		List<Format> rawFormats = new ArrayList<>(formats);
		return getFormatByQuality(rawFormats, avBestQualities, avGoodQualities, avHalfQualities, avPoorQualities, quality);
	}

	public Format getVideoOnlyFormat(List<VideoFormat> formats, String quality) {
		List<Format> rawFormats = new ArrayList<>(formats);
		return getFormatByQuality(rawFormats, vBestQualities, vGoodQualities, vHalfQualities, vPoorQualities, quality);
	}

	public Format getAudioOnlyFormat(List<AudioFormat> formats, String quality) {
		List<Format> rawFormats = new ArrayList<>(formats);
		return getFormatByQuality(rawFormats, aBestQualities, aGoodQualities, aGoodQualities, aPoorQualities, quality);
	}
}
