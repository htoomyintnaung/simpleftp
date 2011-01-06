package com.example.helloandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class HelloAndroid extends Activity {
	static final int DIALOG_ALERT_ID = 0;
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
				HelloAndroid.this.finish();
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
}