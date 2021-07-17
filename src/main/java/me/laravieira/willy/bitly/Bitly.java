package me.laravieira.willy.bitly;

import me.laravieira.willy.config.Config;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;

public class Bitly {
	public static boolean canUse = Config.getBitlyUse();
	public String      full_link = null;
	public String     short_link = null;
	
	public Bitly(String link) {
		full_link = link;
		BitlyClient bitly = new BitlyClient(Config.getBitlyToken());
		Response<ShortenResponse> response = bitly.shorten().setLongUrl(full_link).call();
		if(response.status_code == 200 && response.data != null) {
			short_link = response.data.url;
		}
	}
	
	public String getShorted() {
		return short_link;
	}
}
