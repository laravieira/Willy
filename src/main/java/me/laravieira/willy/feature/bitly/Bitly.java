package me.laravieira.willy.feature.bitly;

import me.laravieira.willy.internal.Config;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;

public class Bitly {
	public static boolean canUse = Config.getBoolean("bitly.enable");
	public String short_link = null;
	
	public Bitly(String link) {
		BitlyClient bitly = new BitlyClient(Config.getString("bitly.token"));
		Response<ShortenResponse> response = bitly.shorten().setLongUrl(link).call();
		if(response.status_code == 200 && response.data != null) {
			short_link = response.data.url;
		}
	}
	
	public String getShort() {
		return short_link;
	}
}
