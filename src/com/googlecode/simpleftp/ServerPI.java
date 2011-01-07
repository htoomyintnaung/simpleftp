package com.googlecode.simpleftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerPI implements Runnable{
	private Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	
	private String baseDir;
	private String relativeDir;
	private String absoluteDir;
	private String fileName;
	private String filePath;
	private String username;
	private String password;
	
	@SuppressWarnings("rawtypes")
	private Class[] commandHanlderArgumentTypes = {String.class, StringTokenizer.class};
	
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
	
	private void readCommandLoop() throws IOException, InvocationTargetException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException {
		String line = null;
		reply(220, "Welcome to the SimpleFTP server!");
		while((line = in.readLine()) != null){
			int replyCode = executeCommand(line.trim());
			if(replyCode == 221){
				return;
			} 
		}
	}
	
	private int executeCommand(String userCommand) throws IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StringTokenizer st = new StringTokenizer(userCommand);
        String commandType = st.nextToken().toLowerCase();

        if (commandType.equals("xpwd")) {
            commandType = "pwd";
        }

        String methodName = "handle" + commandType.substring(0, 1).toUpperCase() + commandType.substring(1);
        Method commandHandler = getClass().getMethod(methodName, this.commandHanlderArgumentTypes);

        Object args[] = {userCommand, st};
        return ((Integer) commandHandler.invoke(this, args)).intValue();

    }
	
	public int handlePass(String userCommand, StringTokenizer st) {
        if (username == null) {
            return reply(503, "Enter username fist.");
        }

        String password;
        if (st.hasMoreTokens()) {
            password = st.nextToken();
        } else {
            return reply(501, "Missing password argument.");
        }
        this.password = password;

        return reply(230, "User (" + username + ") logged in, proceed");
    }
	
	public int handleRest(String userCommand, StringTokenizer st) throws Exception {
        return reply(500, "Unrecognized command");
    }
	
	public int handleSyst(String userCommand, StringTokenizer st) {
        String replyMessage = System.getProperty("os.name");
        return reply(215, replyMessage);
    }

	public int handleUser(String userCommand, StringTokenizer st) throws IOException {
        this.username = st.nextToken();
        return reply(331, "User name (" + username + ") okay, need password.");
    }
	
	
	public int handlePwd(String userCommand, StringTokenizer st) {
        return reply(257, "\"" + this.relativeDir + "\"" + "is current directory");
    }
	
	private int reply(int statusCode, String statusMessage){
		out.println(statusCode + " " + statusMessage);
		return statusCode;
	}
	
	@Override
	public void run(){
		try{
			this.readCommandLoop();
		} catch (IOException e){
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
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
