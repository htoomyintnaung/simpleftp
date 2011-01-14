package com.googlecode.simpleftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;

import android.util.Log;

public class ServerPI implements Runnable {
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

	/**
	 * Server data transfer process
	 */
	private ServerDTP dtp;

	@SuppressWarnings("rawtypes")
	private Class[] commandHanlderArgumentTypes = { String.class,
			StringTokenizer.class };

	public ServerPI(Socket incoming) throws IOException {
		this.clientSocket = incoming;
		in = new BufferedReader(new InputStreamReader(
				this.clientSocket.getInputStream()));
		out = new PrintWriter(this.clientSocket.getOutputStream(), true);
		dtp = new ServerDTP(this);
		//baseDir = new File("").getAbsolutePath();
		baseDir = "/mnt/sdcard";
		relativeDir = "/";
		absoluteDir = baseDir + relativeDir;
		fileName = "";
		filePath = absoluteDir + "/" + fileName;
	}

	private void readCommandLoop() throws IOException,
			InvocationTargetException, SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException {
		String line = null;
		reply(220, "Welcome to the SimpleFTP server!");
		while ((line = in.readLine()) != null) {
			int replyCode = executeCommand(line.trim());
			if (replyCode == 221) {
				return;
			}
		}
	}

	private int executeCommand(String userCommand) throws IOException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		StringTokenizer st = new StringTokenizer(userCommand);
		String commandType = st.nextToken().toLowerCase();

		if (commandType.equals("xpwd")) {
			commandType = "pwd";
		}

		String methodName = "handle"
				+ commandType.substring(0, 1).toUpperCase()
				+ commandType.substring(1);
		Method commandHandler = getClass().getMethod(methodName,
				this.commandHanlderArgumentTypes);

		Object args[] = { userCommand, st };
		return ((Integer) commandHandler.invoke(this, args)).intValue();

	}

	public PrintWriter getPrintWriter() {
		return out;
	}

	public int handleAppe(String userCommand, StringTokenizer st){
		Log.d(MainActivity.TAG, "Enter handleAppe command");
		fileName = st.nextToken();
		filePath = this.absoluteDir + "/" + fileName;
		
		dtp.receiveFile(filePath, true);
		Log.d(MainActivity.TAG, "Leave handleAppe command");
		return 0;
	}
	
	/**
	 * Command handler for CDUP command
	 * 
	 * @param userCommand
	 *            The user command read from client socket
	 * @param st
	 *            String tokenizer for user command
	 * @return The FTP status code
	 */
	public int handleCdup(String userCommand, StringTokenizer st) {
		//System.out.println("Enter handleCdup!\n");

		if (!relativeDir.equals("/")) {
			String tempDir = relativeDir.substring(0, relativeDir.length() - 1);
			relativeDir = tempDir.substring(0, tempDir.lastIndexOf('/') + 1);
			absoluteDir = baseDir + relativeDir;
			return reply(250, "CWD command successful." + this.relativeDir
					+ " is the current directory.");
		} else {
			return reply(250, this.relativeDir
					+ " is already the top directory.");
		}
	}

	/**
	 * Command handler for CWD command
	 * 
	 * @param userCommand
	 *            The user command read from client socket
	 * @param st
	 *            String tokenizer for user command
	 * @return The FTP status code
	 */
	public int handleCwd(String userCommand, StringTokenizer st) {
		String nextPathSegment = st.nextToken();
		if (nextPathSegment.equals("..")) {
			return handleCdup(userCommand, st);
		}

		if (nextPathSegment.charAt(0) == '/') {
			relativeDir = nextPathSegment;
		} else {
			relativeDir = relativeDir + nextPathSegment + "/";
		}

		absoluteDir = baseDir + relativeDir;
		return reply(250, "CWD command successful." + this.relativeDir
				+ " is the current directory.");
	}

	public int handleEpsv(String userCommand, StringTokenizer st) {
		Random gen = new Random();
		int data_port = gen.nextInt(63535) + 1500;
		dtp.setDataPort(data_port);
		dtp.setPassiveMode();
		return reply(229, "The server is entering extened passive mode (|||"
				+ data_port + "|)");
	}

	/**
	 * Command handler for FEAT command
	 * 
	 * @param userCommand
	 *            The user command read from client socket
	 * @param st
	 *            String tokenizer for user command
	 * @return The FTP status code
	 */
	public int handleFeat(String userCommand, StringTokenizer st) {
		System.out.println("Enter handleFeat!\n");
		return reply(500, "Unrecognized command");
	}

	/**
	 * Command handler for RETR command
	 * 
	 * @param userCommand
	 *            The user command read from client socket
	 * @param st
	 *            String tokenizer for user command
	 * @return The FTP status code
	 */
	public int handleRetr(String userCommand, StringTokenizer st) {
		System.out.println("Enter handleRetr!\n");

		fileName = st.nextToken();
		filePath = this.absoluteDir + "/" + fileName;
		// System.out.println("sending file ...");
		return dtp.sendFile(filePath);
	}

	/**
	 * Command handler for STOR command
	 * 
	 * @param userCommand
	 *            The user command read from client socket
	 * @param st
	 *            String tokenizer for user command
	 * @return The FTP status code
	 */
	public int handleStor(String userCommand, StringTokenizer st) {
		System.out.println("Enter handleStor!\n");

		fileName = st.nextToken();
		filePath = this.absoluteDir + "/" + fileName;
		return dtp.receiveFile(filePath, false);
	}

	/**
	 * Command handler for DELE command
	 * 
	 * @param userCommand
	 *            The user command read from client socket
	 * @param st
	 *            String tokenizer for user command
	 * @return The FTP status code
	 */
	public int handleDele(String userCommand, StringTokenizer st) {
		System.out.println("Enter handleDele!\n");

		fileName = st.nextToken();
		filePath = this.absoluteDir + "/" + fileName;
		File targetFile = new File(filePath);
		if (!targetFile.exists()) {
			return reply(550, "File " + targetFile.getName()
					+ " does not exist in the directory");
		}

		//System.out.println("Deleting file...");
		boolean deleteStatus = targetFile.delete();
		if (deleteStatus == true) {
			//System.out.println("Finish deleting file...");
			return reply(250, "DELE command successful");
		} else {
			return reply(550, "DELE command fails");
		}
	}
	
	/**
     * Command handler for LIST command
     * @param userCommand 	The user command read from client socket
     * @param st 			String tokenizer for user command
     * @return 				The FTP status code
     */
    public int handleList(String userCommand, StringTokenizer st) {
    	Log.d(MainActivity.TAG, "Enter handList func");
        //fileName = st.nextToken();
        //filePath = this.absoluteDir + fileName;
    	
        return dtp.sendList(this.absoluteDir);
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

	/**
	 * Command handler for EPRT command
	 * 
	 * @param userCommand
	 *            The user command read from client socket
	 * @param st
	 *            String tokenizer for user command
	 * @return The FTP status code
	 * @throws Exception
	 */
	public int handleEprt(String userCommand, StringTokenizer st)
			throws Exception {
		String portArgu = st.nextToken();
		String temp = portArgu.substring(1, portArgu.length() - 1);
		String[] portArguList = temp.split("\\|");

		String ipAddress = portArguList[1];
		System.out.println(ipAddress);
		int port = Integer.parseInt(portArguList[2]);
		System.out.println(port);

		dtp.setDataIPPort(ipAddress, port);

		return reply(200, "The data port has been set");
	}

	public int handleRest(String userCommand, StringTokenizer st)
			throws Exception {
		return reply(500, "Unrecognized command");
	}
	
	/**
     * Command handler for PORT command
     * @param userCommand 	The user command read from client socket
     * @param st 			String tokenizer for user command
     * @return 				The FTP status code
     * @throws Exception
     */
    public int handlePort(String userCommand, StringTokenizer st) throws Exception {
    	Log.d(MainActivity.TAG, "Enter handPort func");
        String portArgu = st.nextToken();
        System.out.println(portArgu);
        String[] portArguList = portArgu.split(",");
        String ipAddress = "";
        for (int i = 0; i < 3; i++) {
            ipAddress += portArguList[i] + ".";
        }
        ipAddress += portArguList[3];

        int p1 = Integer.parseInt(portArguList[4]);
        int p2 = Integer.parseInt(portArguList[5]);
        int port = p1 * 256 + p2;
        if(ipAddress.equals("127.0.0.1"))
        	ipAddress = "10.0.2.2";
        Log.d(MainActivity.TAG, "Set Ip address: " + ipAddress + ":" + port);
        dtp.setDataIPPort(ipAddress, port);
        return reply(200, "The data port has been set");
    }

    
    public int handlePasv(String userCommand, StringTokenizer st){
    	Log.d(MainActivity.TAG, "Enter handlePasv func");
        dtp.setPassiveMode();
        return reply(227, "Enter passive mode");
    }
	/**
     * Command handler for SYST command
     * @param userCommand 	The user command read from client socket
     * @param st 			String tokenizer for user command
     * @return 				The FTP status code
     */
    public int handleSyst(String userCommand, StringTokenizer st) {
        //System.out.println("Enter handleSyst!\n");

        String replyMessage = System.getProperty("os.name");

        return reply(215, replyMessage);
    }

	public int handleUser(String userCommand, StringTokenizer st)
			throws IOException {
		this.username = st.nextToken();
		return reply(331, "User name (" + username + ") okay, need password.");
	}
	
	/**
     * Command handler for QUIT command
     * @param userCommand 	The user command read from client socket
     * @param st 			String tokenizer for user command
     * @return 				The FTP status code
     */
    public int handleQuit(String userCommand, StringTokenizer st) {
        System.out.println("Enter handleQuit!\n");


        username = null;
        password = null;

        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return reply(221, "Client:" + clientSocket.getInetAddress() + " at port " + clientSocket.getPort() + " quits!");
    }

	/**
     * Command handler for PWD command
     * @param userCommand 	The user command read from client socket
     * @param st 			String tokenizer for user command
     * @return 				The FTP status code
     */
    public int handlePwd(String userCommand, StringTokenizer st) {
        //System.out.println("Enter handlePwd!\n");

        return reply(257, "\"" + this.relativeDir + "\"" + "is current directory");
    }
    
    /**
     * Command handler for TYPE command
     * @param userCommand 	The user command read from client socket
     * @param st 			String tokenizer for user command
     * @return 				The FTP status code
     */
    public int handleType(String userCommand, StringTokenizer st) {
        String typeArgu = st.nextToken();
        String typeName = null;
        if (typeArgu.equals("A")) {
            dtp.setType("A");
            typeName = "ASCII";
        } else if (typeArgu.equals("I")) {
            dtp.setType("I");
            typeName = "BINARY";
        }

        return reply(200, "Use " + typeName + " type to transfer on data connection");
    }
    
    /**
     * Check the user login status before any subsequent operation
     * @return The FTP status code
     */
    private int checkLoginStatus() {
        if (username == null || username.equals("")) {
            return reply(530, "Not logged in");
        }

        if (password == null || password.equals("")) {
            return reply(331, "User name okay, need password");
        }

        return reply(230, "User (" + username + ") logged in, proceed");

    }
    
    /**
     * Reply to the remote cient the FTP status code and status message
     * @param statusCode		The FTP status code
     * @param statusMessage		The FTP status message
     * @return					The FTP status code
     */
	public int reply(int statusCode, String statusMessage) {
		out.println(statusCode + " " + statusMessage);
		return statusCode;
	}
	

	@Override
	public void run() {
		try {
			this.readCommandLoop();
		} catch (IOException e) {
			Log.e(MainActivity.TAG, e.toString());
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			Log.e(MainActivity.TAG, e.toString());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Log.e(MainActivity.TAG, e.toString());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Log.e(MainActivity.TAG, e.toString());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			Log.e(MainActivity.TAG, e.toString());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			Log.e(MainActivity.TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
				if (out != null) {
					out.close();
					out = null;
				}
				if (clientSocket != null) {
					clientSocket.close();
					clientSocket = null;
				}
			} catch (IOException e) {
				Log.e(MainActivity.TAG, e.toString());
				e.printStackTrace();
			}
		}
	}
}
