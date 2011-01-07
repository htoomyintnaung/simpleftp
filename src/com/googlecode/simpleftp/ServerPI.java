package com.googlecode.simpleftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerPI implements Runnable{
	private Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	
	private String baseDir;
	private String relativeDir;
	private String absoluteDir;
	private String fileName;
	private String filePath;
	
	public ServerPI(Socket incoming) throws IOException{
		this.clientSocket = incoming;
		in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		out = new PrintWriter(this.clientSocket.getOutputStream(), true);
		
		baseDir = new File("").getAbsolutePath();
		
		relativeDir = "/";
		absoluteDir = baseDir + relativeDir;
		fileName = "";
		filePath = absoluteDir + "/" + fileName;
	}
	
	private void readCommandLoop() throws IOException {
		String line = null;
		reply(220, "Welcome to the SimpleFTP server!");
		while((line = in.readLine()) != null){
			int replyCode = executeCommand(line.trim());
			if(replyCode == 221){
				return;
			}
		}
	}
	
	private int executeCommand(String trim) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int reply(int statusCode, String statusMessage){
		out.println(statusCode + " " + statusMessage);
		return statusCode;
	}
	
	@Override
	public void run(){
		try{
			this.readCommandLoop();
		} catch (IOException e){
			e.printStackTrace();
		}
		finally {
			try { 
				if(in != null){
					in.close();
					in = null;
				}
				if(out != null){
					out.close();
					out = null;
				}
				if (clientSocket != null){
					clientSocket.close();
					clientSocket = null;
				}
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}
