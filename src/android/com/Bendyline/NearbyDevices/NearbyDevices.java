package com.Bendyline.NearbyDevices;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;

import java.util.Collections;
import java.util.List;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class NearbyDevices extends CordovaPlugin 
{
	private BeaconManager beaconManager;
	private CallbackContext context;
	private static final String ESTIMOTE_PHONEVIRTUALBEACON_PROXIMITY_UUID = "8492e75f-4fd6-469d-b132-043fe94921d8";
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", null, null, null);
 //private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, 3724, 9614);

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
	{

        if (action.equals("startVirtualBeacon")) 
		{
            String uuid = args.getString(0); 
			int majorNumber = args.getInt(1);
			int minorNumber = args.getInt(2);
			
			this.context = callbackContext;
			
            this.startVirtualBeacon(uuid, majorNumber, minorNumber, callbackContext);
			
            return true;
        }
        else if (action.equals("listenForBeacons")) 
		{
			this.context = callbackContext;
			
			this.listenForBeacons(callbackContext);
			
            return true;
        }
		
        return false;
    }
	
    private void startVirtualBeacon(String uuid, int majorNumber, int minorNumber, CallbackContext callbackContext) 
	{		

	}
	
    private void listenForBeacons(CallbackContext callbackContext) 
	{		
		try
		{
			beaconManager = new BeaconManager(this.cordova.getActivity().getApplicationContext());

			if (!beaconManager.hasBluetooth())
			{
				callbackContext.error("Bluetooth is not available on the device.");
				return;
			}
		
			//beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
		//	beaconManager.setRangingListener(new BeaconRangingListener(this.context, this.cordova.getActivity()));
		
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
							context.error("Exception when starting a beacon." + e.getMessage());
						}

						if (results.length() > 2)
						{
							results += ",";
						}
						
						results += obj.toString();
					}
					
					results += "]";
					
					PluginResult result = new PluginResult(PluginResult.Status.OK, results);
					result.setKeepCallback(true);
					context.sendPluginResult(result);
				}			
				
				@Override
				public void onExitedRegion(Region region) 
				{
					PluginResult result = new PluginResult(PluginResult.Status.OK, "EXIT");
					result.setKeepCallback(true);
					context.sendPluginResult(result);     
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
			callbackContext.error("Can't listen for beacons. Details: " + e.getMessage());
		}
	}
}