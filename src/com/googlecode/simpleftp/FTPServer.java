package com.googlecode.simpleftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class FTPServer extends Activity {
	private static int COMMAND_PORT = 2121;
	static final int DIALOG_ALERT_ID = 0;
	private static ExecutorService executor  = Executors.newCachedThreadPool();
	
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
				FTPServer.this.finish();
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
    	Socket incoming = null;
    	
    	try{
    		s = new ServerSocket(COMMAND_PORT);
    		String ip = (s.getInetAddress()).getHostAddress();
    		Context context = this.getApplicationContext();
    		CharSequence text = ip;
    		int duration = Toast.LENGTH_LONG;
    		
    		Toast toast = Toast.makeText(context, text, duration);
    		Thread.sleep(1000);
    		toast.show();
    		while(true){
    			incoming = s.accept();
    			executor.execute(new ServerPI(incoming));
    		}
    	}
    	catch(Exception e){
    		System.out.println(e.toString());
    		e.printStackTrace();
    	}
    	finally{
    		try
			{
				if(incoming != null)incoming.close();
			}
			catch(IOException ignore)
			{
				//ignore
			}

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
}