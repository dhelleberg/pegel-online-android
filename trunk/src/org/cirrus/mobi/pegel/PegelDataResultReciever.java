package org.cirrus.mobi.pegel;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class PegelDataResultReciever extends ResultReceiver
{
	private static final String TAG = "PegelDataResultReciever";
	
	private Receiver mReceiver;
	public PegelDataResultReciever(Handler handler) {
		super(handler);
	}

	public void clearReceiver() {
		mReceiver = null;
	}

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (mReceiver != null) {
			mReceiver.onReceiveResult(resultCode, resultData);
		} else {
			Log.w(TAG, "Dropping result on floor for code " + resultCode + ": "
					+ resultData.toString());
		}
	}
}
