package com.Bendyline.NearbyDevices;

import org.json.JSONException;
import org.json.JSONObject;
import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Handler;
import android.os.Message;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;
import android.os.RemoteException;

import android.content.Intent;
import android.util.Log;
import android.net.Uri;

import java.util.Collections;
import java.util.List;

public class ListenerBackgroundService extends BackgroundService implements Handler.Callback 
{
	private boolean isListenerInitialized = false;
	
	private ListenerThread listenerThread;
	private BeaconManager beaconManager;
	private static final String ESTIMOTE_PHONEVIRTUALBEACON_PROXIMITY_UUID = "8492e75f-4fd6-469d-b132-043fe94921d8";
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", null, null, null);

	private String beaconList = "";
	private String error = "";
	
	private Handler thisHandler;

	
	@Override
	protected JSONObject doWork() 
	{
		JSONObject result = new JSONObject();

		try 
		{	
			if (!isListenerInitialized)
			{
				this.setMilliseconds(5000);
				isListenerInitialized = true;
				listenerThread = new ListenerThread(this, "test");

				listenerThread.start();
			}
			
			
			listenerThread.requestCheckForDevices();
			
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
			String now = df.format(new Date(System.currentTimeMillis())); 
			
			String msg = "Error:|" + error + "|Status: " + beaconList + "|" + "FHello World - its currently " + now;	

			Log.d("ListenerService", msg);

			result.put("Message", msg);		
		} 
		catch (Exception e) 
		{
			try
			{
				result.put("Message", "ERROR" + e);
			}
			catch (JSONException je)
			{
				;
			}
		}
	
		return result;   
	}
	
	public void setBeaconList(String newBeacons)
	{
		this.beaconList = newBeacons;
	}
	
	public void setError(String newError)
	{
		this.error  = newError;
	}
	/*
	protected void InitializeDeviceListener(JSONObject result)
	{
		this.isListenerInitialized = true;
		
		try
		{
			beaconManager = new BeaconManager(this);

			if (!beaconManager.hasBluetooth())
			{
				error += "Bluetooth is not available on the device.";
				
				return;
			}
	
	
		    beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() 
			{
				@Override
				public void onEnteredRegion(Region region, List<Beacon> beacons) 
				{
					String results = "[";
					
					for (Beacon beacon : beacons)
					{
						JSONObject obj = new JSONObject();
						
						try
						{
							obj.put("macAddress", beacon.getMacAddress());
							obj.put("uuid", beacon.getProximityUUID());
							obj.put("name", beacon.getName());
							obj.put("major", beacon.getMajor());
							obj.put("minor", beacon.getMinor());
							obj.put("rssi", beacon.getRssi());
							obj.put("measuredPower", beacon.getMeasuredPower());
						}
						catch (JSONException e)
						{

						}

						if (results.length() > 2)
						{
							results += ",";
						}
						
						results += obj.toString();
					}
					
					results += "]";
					
					beaconList = results;
				}			
				
				@Override
				public void onExitedRegion(Region region) 
				{ 
				}
			});

			
			beaconManager.connect(new BeaconManager.ServiceReadyCallback() 
			{
				@Override public void onServiceReady() 
				{
					try 
					{
						beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS);
					} 
					catch (RemoteException e) 
					{
		
					}				
				}
			});
		}
		catch (Exception e)
		{
				error += "generr" + e;
		}
	}
	
		
			String url = "plugintest://foofoo";
Intent intent = new Intent(Intent.ACTION_VIEW);
intent.setData(Uri.parse(url));

//Intent intent = new Intent(this, NotificationReceiver.class);
PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
Notification n  = new Notification.Builder(this)
        .setContentTitle("test message")
        .setContentText(msg)
        .setContentIntent(pIntent)
		.setSmallIcon(0x7f020000)
        .setAutoCancel(true)
        .build();


NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

notificationManager.notify(0, n); 

	*/
	
	 @Override
	public boolean handleMessage(Message arg0) 
	{
		beaconList = (String)arg0.obj;

		return false;
	}

	@Override
	protected JSONObject getConfig() {
		return null;
	}

	@Override
	protected void setConfig(JSONObject config) {
		return;
	}     

	@Override
	protected JSONObject initialiseLatestResult() {
		return null;
	}

}