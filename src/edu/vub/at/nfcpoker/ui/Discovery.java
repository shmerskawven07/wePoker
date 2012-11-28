package edu.vub.at.nfcpoker.ui;

import java.util.Timer;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.comm.DiscoveryAsyncTask;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

public class Discovery extends Activity {

	// Local settings
	private Activity activity;
	private DiscoveryAsyncTask.DiscoveryCompletionListener dcl;
	
	// Discovery
	private volatile DiscoveryAsyncTask discoveryTask;
	private volatile Timer client_startClientServerTimer;
	private volatile Dialog client_startClientServerAsk;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_discovery);
		this.activity = this;
		this.dcl = new DiscoveryAsyncTask.DiscoveryCompletionListener() {
			@Override
			public void onDiscovered(CommLibConnectionInfo result) {
				if (client_startClientServerTimer != null) {
					client_startClientServerTimer.cancel();
					client_startClientServerTimer = null;
					if (client_startClientServerAsk != null) {
						client_startClientServerAsk.dismiss();
						client_startClientServerAsk = null;
					}
				}
				int port = CommLib.SERVER_PORT;
				try {
					Integer.parseInt(result.getPort());
				} catch (Exception e) { }
				ClientActivity.startClient(activity, result.getAddress(), port, result.isDedicated(), false, null, null, null);
			}
		};
		startDiscovery();
	}
	
	@Override
	public void onStop() {
		this.activity = null;
		stopDiscovery();
	}
	
	public void startDiscovery() {
		if (discoveryTask != null)
			discoveryTask.cancel(true);
		
		discoveryTask = new DiscoveryAsyncTask(activity, dcl);
		discoveryTask.execute();
	}
	
	public void stopDiscovery() {
		discoveryTask.cancel(true);
		discoveryTask = null;
	}
}