package me.crossle.vaircast;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class MainActivity extends ListActivity implements OnItemClickListener {

	private static final String AIRPLAY_DNS = "_airplay._tcp.local.";
	private static final int SERVER_PORT = 8765;
	public List<AirItem> mAirList;
	public AirServerListAdapter mAirServerListAdapter;

	private AirPlayAPI mAirVideoAPI;

	int time = 0;

	static {
		System.loadLibrary("vcast");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mAirList = new ArrayList<AirItem>();
		mAirServerListAdapter = new AirServerListAdapter(this, mAirList);
		setListAdapter(mAirServerListAdapter);
		getListView().setOnItemClickListener(this);
		detectAirServer();
		String mineType = getMimeType("/sdcard/Movies/13411210002.wav");
		Log.e("hello=========", mineType);
	}
	
	private static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (!TextUtils.isEmpty(extension)) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}

	private void detectAirServer() {
		new AsyncTask<Void, AirItem, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				JmDNS jmdns = null;
				try {
					jmdns = JmDNS.create();
				} catch (IOException e) {
					e.printStackTrace();
				}
				jmdns.addServiceListener(AIRPLAY_DNS, new DNSListener());
				return null;
			}
		}.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.stop:

			new AsyncTask<AirItem, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(AirItem... params) {
					return mAirVideoAPI.stop();
				}

				protected void onPostExecute(Boolean result) {
					Toast.makeText(MainActivity.this, "=======result====" + result, Toast.LENGTH_SHORT)
					    .show();
				};
			}.execute();

			break;
		case R.id.seek:
			new AsyncTask<AirItem, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(AirItem... params) {
					time += 10;
					return mAirVideoAPI.seekTo(time + "");
				}

				protected void onPostExecute(Boolean result) {
					Toast.makeText(MainActivity.this, "=======result====" + result, Toast.LENGTH_SHORT)
					    .show();
				};
			}.execute();

			break;
		case R.id.start_server:
			// int hello = startServer(SERVER_PORT);
			new Nano(SERVER_PORT).start();
			// Log.e("--songxiaoming-", hello + "========" + SERVER_PORT);
			break;
		case R.id.stop_server:
			stopServer();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private native int startServer(int port);

	private native int stopServer();

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				mAirList.add((AirItem) msg.obj);
				mAirServerListAdapter.notifyDataSetChanged();
				break;
			case 1:
				mAirList.remove((AirItem) msg.obj);
				mAirServerListAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};

	class DNSListener implements ServiceListener {

		@Override
		public void serviceAdded(ServiceEvent event) {
			ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), event.getName());
			InetAddress[] addresses = info.getInet4Addresses();
			String address = null;
			int port = info.getPort();
			if (addresses.length > 0) {
				for (InetAddress inetAddress : addresses) {
					address = inetAddress.getHostAddress();
					AirItem airItem = new AirItem(info.getName() + "=" + address, address,
					    String.valueOf(port));
					Message message = Message.obtain();
					message.what = 0;
					message.obj = airItem;
					mHandler.sendMessage(message);
					break;
				}
			}
		}

		@Override
		public void serviceRemoved(ServiceEvent serviceEvent) {
			AirItem airItem = new AirItem(serviceEvent.getName(), serviceEvent.getType(), serviceEvent
			    .getInfo().getPort() + "");
			Message message = Message.obtain();
			message.what = 1;
			message.obj = airItem;
			mHandler.sendMessage(message);

		}

		@Override
		public void serviceResolved(ServiceEvent serviceEvent) {
		}
	}

	class AirServerListAdapter extends BaseAdapter {
		private List<AirItem> mAirlist;

		public AirServerListAdapter(Context context, List<AirItem> airList) {
			mContext = context;
			mAirlist = airList;
		}

		public int getCount() {
			return mAirList.size();
		}

		public AirItem getItem(int position) {
			return mAirlist.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			HolderView holderView;
			if (convertView instanceof HolderView) {
				holderView = (HolderView) convertView;
			} else {
				holderView = new HolderView(mContext);
			}
			holderView.bind(mAirList.get(position));

			return holderView;
		}

		/**
		 * Remember our context so we can use it when constructing views.
		 */
		private Context mContext;
	}

	public class AirItem {
		public String name;
		public String address;
		public String port;

		public AirItem(String name, String address, String port) {
			this.name = name;
			this.address = address;
			this.port = port;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			return name != null ? name.equals(((AirItem) obj).name) : false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AirItem airItem = mAirServerListAdapter.getItem(position);
		new AsyncTask<AirItem, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(AirItem... params) {
				mAirVideoAPI = new AirPlayAPI(params[0].address, params[0].port);
				String info = mAirVideoAPI.getServerInfo();
				String playback = mAirVideoAPI.getPlaybackInfo();
				Log.e("songxiaoming", "info=" + info + "=============----" + playback);

				String url = "http://" + getLocalIpAddress() + ":" + SERVER_PORT
				    + "/sdcard/big_buck_bunny.mp4";
				url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
				boolean isOK = mAirVideoAPI.play(url, "0.0");
				Log.e("songxiaoming", "===============" + isOK + url);
				return isOK;
			}

			protected void onPostExecute(Boolean result) {
				Toast.makeText(MainActivity.this, "=======result====" + result, Toast.LENGTH_SHORT).show();
			};
		}.execute(airItem);
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
			    .hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
				    .hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
					    && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("IP", ex.toString());
		}
		return null;
	}

}
