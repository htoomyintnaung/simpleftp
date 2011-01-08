package com.googlecode.simpleftp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	static final int DIALOG_ALERT_ID = 0;
	public static final String TAG = "simpleFTP";
	private TextView ipAddress;
	private String ip;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.my_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {    
		// Handle item selection    
		switch (item.getItemId()) {
			case R.id.new_game:    
				System.out.println("New game button is pressed!");
				//newGame();        
				return true;    
			case R.id.quit:        
				System.out.println("Quit button is pressed!");
				showDialog(DIALOG_ALERT_ID);        
				return true;    
			default:        
				return super.onOptionsItemSelected(item);    }
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to exit?")
		.setCancelable(false).setPositiveButton("yes", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int id){
				MainActivity.this.finish();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}
    
    @Override
    protected void onStart(){
    	super.onStart();
    	ServerSocket s = null;
    	
    	
    	try{
    		
    		ipAddress = (TextView)findViewById(R.id.ipAddress);
    		ip = this.getLocalIpAddress();
    		
    		runOnUiThread(new Runnable(){
    			public void run(){
    				ipAddress.setText(ip);
    			}
    		});
    		
    		new Thread(new FTPServerThread()).start();
    		
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	finally{

			try
			{
				if (s!= null) 
				{
					s.close();
				}
			}
			catch(IOException ignore)
			{
				//ignore
			}
    	}
    }
    
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }
}