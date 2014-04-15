package me.crossle.vaircast;

public class AirPlayAPI {

	private static final String USER_AGENT = "VPlayer video player";

	private static final String PLAY = "play";
	private static final String STOP = "stop";
	private static final String SEEK = "scrub?position=";
	private String mHost;
	private String mPort;

	public AirPlayAPI(String host, String port) {
		this.mHost = host;
		this.mPort = port;
	}

	private HttpRequest createPostRequest(String cmd) {
		String command = "http://" + mHost + ":" + mPort + "/" + cmd;
		return HttpRequest.post(command).userAgent(USER_AGENT);
	}

	// only for test
	public String getServerInfo() {
		String url = "http://" + mHost + ":" + mPort + "/server-info";
		String body = HttpRequest.get(url).body();
		return body;
	}
	
	public String getPlaybackInfo() {
		String url = "http://" + mHost + ":" + mPort + "/playback-info";
		String body = HttpRequest.get(url).body();
		return body;
	}
	
	public boolean play(String playUrl, String position) {
		String body = "Content-Location: " + playUrl + "\n" + "Start-Position:  " + position + " \n";
		HttpRequest http = createPostRequest(PLAY);
		http.header("Content-Type", "text/parameters");
		http.header("Content-Length", body.length());
		return http.send(body).ok();
	}

	public boolean seekTo(String position) {
		HttpRequest http = createPostRequest(SEEK + position);
		http.header("Content-Length", 0);
		return http.ok();
	}

	public boolean stop() {
		HttpRequest http = createPostRequest(STOP);
		http.header("Content-Length", 0);
		return http.ok();
	}

}
