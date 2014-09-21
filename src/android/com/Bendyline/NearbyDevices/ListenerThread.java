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
import android.content.Intent;

import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;
import android.os.RemoteException;

import android.util.Log;
import android.net.Uri;

import java.util.Collections;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class ListenerThread extends HandlerThread implements Handler.Callback
{
	private boolean isListenerInitialized = false;
	private Handler handler, callback;
	public static final int MSG_FINISHED = 100;
	public static final int MSG_CHECK_FOR_DEVICES = 101;
	public static final int MSG_COUNT_DOWN = 102;

	private ListenerBackgroundService lbs;
	private BeaconManager beaconManager;
	private static final String ESTIMOTE_PHONEVIRTUALBEACON_PROXIMITY_UUID = "8492e75f-4fd6-469d-b132-043fe94921d8";
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", null, null, null);

	private String beaconList = "";
	private String error = "";
	
	private Map<String, DeviceState> deviceStatesById;
     
	public ListenerThread(ListenerBackgroundService service, String name) 
	{
		super(name);
		this.deviceStatesById = new HashMap();
		this.lbs = service;
		this.lbs.setBeaconList("INIT");
	}
   
	@Override
	protected void onLooperPrepared() 
	{
		handler = new Handler(getLooper(), this);
		this.lbs.setBeaconList("ONLOOPERPREPARED");
		this.InitializeDeviceListener();
	}
	
	public void setCallback(Handler cb)
	{
		callback = cb;
	}
	
	 
	public void requestCheckForDevices()
	{	
		if (handler != null)
		{
			handler.sendMessage(Message.obtain(null, ListenerThread.MSG_CHECK_FOR_DEVICES, 0));
		}
	}
 
	public boolean handleMessage(Message msg) 
	{    
		switch(msg.what)
		{
			case MSG_CHECK_FOR_DEVICES:
				this.checkForDevices();
			break;
		}
		
		return true;
	}
	
	private void checkForDevices()
	{
		try
		{
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		String now = df.format(new Date(System.currentTimeMillis())); 
			
						lbs.setBeaconList("CH" + now + "|" + deviceStatesById.size());

			for (DeviceState ds : deviceStatesById.values())
			{					
				if (ds != null)
				{
					if (ds.getStatus() != DeviceStateStatus.IsApp)
					{
						lbs.setBeaconList("Q" + ds.getMacAddress());
						ds.loadStatus();
					}
				}
				//System.out.println(entry.getKey() + "/" + entry.getValue());
			}
		}	
		catch (Exception e)
		{
			String error = "generr" + e;
			
			lbs.setError(error);
		}	
	}
	
	protected void InitializeDeviceListener()
	{
		this.isListenerInitialized = true;
		
		try
		{
			beaconManager = new BeaconManager(this.lbs);

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
						String id = beacon.getMacAddress();
						
						if (!deviceStatesById.containsKey(id))
						{
							DeviceState ds = new DeviceState(lbs);
							
							deviceStatesById.put(id, ds);
							
							ds.setMacAddress(beacon.getMacAddress());
							
							ds.loadStatus();
						}
						/*
						if thi
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
						
						results += obj.toString();*/
					}
					
				/*	results += "]";
					
					beaconList = results;
					
					lbs.setBeaconList(beaconList);*/
				//	callback.sendMessage(Message.obtain(null, MSG_FINISHED, beaconList + "..." + error));
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
			
			lbs.setError(error);
		}
	}
}