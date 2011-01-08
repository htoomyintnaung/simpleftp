package com.googlecode.simpleftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

public class FTPServerThread implements Runnable{
	private static int COMMAND_PORT = 2121;
	private ServerSocket s;
	private static ExecutorService executor  = Executors.newCachedThreadPool();
	private boolean done = false;
	public FTPServerThread() throws IOException{
		s = new ServerSocket(COMMAND_PORT);
	}
	
	
	public void run(){
		Socket incoming = null;
		while(!done){
			try {
				incoming = s.accept();
				executor.execute(new ServerPI(incoming));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(MainActivity.TAG, e.toString());
			}
		}
	}
}
