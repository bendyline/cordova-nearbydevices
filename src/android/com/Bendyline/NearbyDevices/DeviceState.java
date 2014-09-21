package com.Bendyline.NearbyDevices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.StringBuilder;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

public class DeviceState 
{
	private String macAddress;
	private DeviceStateStatus status = DeviceStateStatus.Unknown;
	private ListenerBackgroundService lbs;

	public DeviceState(ListenerBackgroundService backgroundService)
	{
		this.lbs = backgroundService;
	}
	
	public DeviceStateStatus getStatus()
	{
		return status;
	}
	
	public String getMacAddress()
	{
		return this.macAddress;
	}
	
	public void setMacAddress(String newMacAddress)
	{
		this.macAddress = newMacAddress;
	}
	
	public void loadStatus()
	{
		try 
		{
			String url = "http://localhost:120/api.svc/Beacons?$filter=BeaconId%20eq%20%27" + this.macAddress + "%27";
		
			HttpClient httpclient = new DefaultHttpClient();
       
 			HttpGet httpget = new HttpGet(url); 

            httpget.addHeader("Accept", "application/json;odata=minimalmetadata");
            httpget.addHeader("Content-Type", "application/json");

			HttpResponse response;
	
			response = httpclient.execute(httpget);
        
			HttpEntity entity = response.getEntity();

			if (entity != null) 
			{ 
				InputStream instream = entity.getContent();
				/*
				JsonReader rdr = Json.createReader(instream);
 
				JsonObject obj = rdr.readObject();
				JsonArray results = obj.getJsonArray("value");
				if (results != null)
				{
					for (JsonObject result : results.getValuesAs(JsonObject.class)) 
					{
						String appTitle = result.getString("AppTitle");

						PostNotification("RES" + appTitle);
						lbs.setBeaconList(appTitle);
					}
				}*/
				String result = convertStreamToString(instream);
				
				JSONObject jsoRoot = new JSONObject(result);
				if (jsoRoot != null)
				{
					JSONArray jsa  = jsoRoot.getJSONArray("value");
					
					if (jsa != null && jsa.length() == 1)
					{
						JSONObject beaconApp = jsa.getJSONObject(0);
								
						String appTitle = beaconApp.getString("AppTitle");

						PostNotification(appTitle + " is nearby", "Touch to open this item.");
						lbs.setBeaconList(appTitle);
						this.status = DeviceStateStatus.IsApp;
					}
				}
				else
				{
				
					this.status = DeviceStateStatus.NotBeacon;
				}
				instream.close();
			}
		} 
		catch (Exception e) 
		{
			lbs.setError(e.toString());
		}
	}
	
    private static String convertStreamToString(InputStream is) 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;	
		try 
		{
			while ((line = reader.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try 
			{
				is.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	private void PostNotification(String title, String description)
	{
		String url = "qualla://nearby/" + this.macAddress;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));

		PendingIntent pIntent = PendingIntent.getActivity(lbs, 0, intent, 0);

		Notification n  = new Notification.Builder(lbs)
			.setContentTitle(title)
			.setContentText(description)
			.setContentIntent(pIntent)
			.setSmallIcon(0x7f020000)
			.setAutoCancel(true)
			.build();


		NotificationManager notificationManager = (NotificationManager)lbs.getSystemService(lbs.NOTIFICATION_SERVICE);

		notificationManager.notify(0, n); 
	}
}