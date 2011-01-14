package com.googlecode.simpleftp;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.util.Log;

public class ServerDTP {

    private boolean passive;
    /**
     * The default buffer size for reading and sending file
     */
    private static final int BUFFER = 8 * 1024;
    /**
     * The writer writes the message back to the client
     */
    private PrintWriter writer;
    /**
     * The ServerPI that is associated with this ServerDTP
     */
    private ServerPI spi;
    /**
     * The IP address of the remote client
     */
    private String ipAddress;
    /**
     * The port number of the remote client that is waiting for data transfer connection
     */
    private int port;
    /**
     * The transmission type
     */
    private String type;

    /**
     * The constructor for ServerDTP
     * @param spi	The ServerPI that is associated with the ServerDTP
     */
    public ServerDTP(ServerPI spi) {
        this.spi = spi;
        writer = this.spi.getPrintWriter();
        type = "A";
        passive = false;
    }

    /**
     * Set the IP address and port for remote client
     * @param ip	The IP address of the remote client
     * @param port	The port number of the remote client that is waiting for data transfer connection
     */
    public void setDataIPPort(String ip, int port) {
        this.ipAddress = ip;
        this.port = port;
    }

    public void setDataPort(int port) {
        this.port = port;
    }

    public void setPassiveMode(){
        this.passive = true;
    }

    public void setActiveMode(){
        this.passive = false;
    }

    /**
     * Set the transmission type
     * @param type	The transmission type of data transfer, "A" for ASCII or "I" for IMAGE
     */
    public void setType(String type) {
        this.type = type;
    }
    

    /**
     * Receive the file sent from the client at specified path
     * @param path	The specified directory
     * @return		The FTP status code
     */
    public int receiveFile(String path, boolean append) {
    	Log.d(MainActivity.TAG, "Enter ServerDTP.receiveFile");
    	Log.d(MainActivity.TAG, "Receive file " + path);
        int reply = 0;

        OutputStream out = null;
        Object data = null;
        InputStream in = null;
        Socket client = null;
        try {
            if(!passive){
            	Log.d(MainActivity.TAG, "Enter active mode");
                data = new Socket(ipAddress, port);
            }
            else{
            	Log.d(MainActivity.TAG, "Enter passive mode");
                data = new ServerSocket(port);
                client = ((ServerSocket)data).accept();
            }

            File file = new File(path);
            if (type.equals("I")) {
                out = new BufferedOutputStream(new FileOutputStream(file, append));
            } else if (type.equals("A")) {
                out = new ASCIIOutputStream(new FileOutputStream(file, append));
            }

            writer.println(150 + " " + "File status okay; about to open data connection.");
            Log.d(MainActivity.TAG, "File status okay; about to open data connection.");
            if(!passive)
                in = ((Socket)data).getInputStream();
            else{
                in = client.getInputStream();
            }

            byte bufferRead[] = new byte[BUFFER];
            int nread;
            Log.d(MainActivity.TAG, "Start transfer file");
            while ((nread = in.read(bufferRead, 0, BUFFER)) > 0) {
                out.write(bufferRead, 0, nread);
            }
            out.flush();
            in.close();
            Log.d(MainActivity.TAG, "Finish transfer");

            writer.println(226 + " " + "Finish Transfer.");
            reply = 226;
        } catch (Exception e) {
        	Log.e(MainActivity.TAG, e.toString());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (data != null) {
                    if(!passive)
                        ((Socket)data).close();
                    else
                        ((ServerSocket)data).close();
                }
            } catch (Exception e) {
            	Log.e(MainActivity.TAG, e.toString());
            }
        }
        Log.d(MainActivity.TAG, "Leave ServerDTP.receiveFile");
        return reply;
    }

    /**
     * Send the file specified by the path to the client
     * @param path	The specified directory
     * @return		The FTP status code
     */
    public int sendFile(String path) {
        int reply = 0;

        InputStream in = null;
        Object data = null;
        OutputStream out = null;
        Socket client = null;

        try {

            if(!passive)
                data = new Socket(ipAddress, port);
            else{
                data = new ServerSocket(port);
                client = ((ServerSocket)data).accept();
            }

            File file = new File(path);

            if (type.equals("I")) {
                in = new BufferedInputStream(new FileInputStream(file));
            } else if (type.equals("A")) {
                in = new ASCIIInputStream(new FileInputStream(file));
            }

            writer.println(150 + " " + "File status okay; about to open data connection.");

            if(!passive)
                out = ((Socket)data).getOutputStream();
            else{
                out = client.getOutputStream();
            }               

            byte bufferRead[] = new byte[BUFFER];
            int nread;
            while ((nread = in.read(bufferRead)) > 0) {
                out.write(bufferRead, 0, nread);

            }
            out.flush();
            out.close();

            writer.println(226 + " " + "Finish Transfer");
            reply = 226;

        } catch (Exception e) {
        	Log.e(MainActivity.TAG, e.toString());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (data != null) {
                    if(!passive)
                        ((Socket)data).close();
                    else{
                        ((ServerSocket)data).close();
                    }
                }
            } catch (Exception e) {
            	Log.e(MainActivity.TAG, e.toString());
            }
        }
        return reply;
    }

    /**
     * Send the file attributes of all the sub directories and files in the specified path to the client
     * @param path	The specified directory
     * @return		The FTP status code
     */
    public int sendList(String path) {
    	Log.d(MainActivity.TAG, "Enter dtp.sendList, show list in Dir " + path);
        File directory = new File(path);
        if(!directory.isDirectory()){
        	return spi.reply(550, "It is not a directory");
        }
        else if(!directory.canRead()){
        	return spi.reply(550, "Permission denialed");
        }
        String fList[] = directory.list();
        Object data = null;
        PrintWriter out = null;
        Socket client = null;

        try {
            if(!passive){
            	Log.d(MainActivity.TAG, "Enter active mode");
                data = new Socket(ipAddress, port);
            }
            else{
            	Log.d(MainActivity.TAG, "Enter passive mode");
                data = new ServerSocket(port);
                client = ((ServerSocket)data).accept();
            }
           
            writer.println(150 + " " + "File status okay; about to open data connection.");

            Log.d(MainActivity.TAG, "Set up link");
            if(!passive){
            	Log.d(MainActivity.TAG, "set out");
                out = new PrintWriter(new ASCIIOutputStream(((Socket)data).getOutputStream()));
            }
            else{
                out = new PrintWriter(new ASCIIOutputStream(client.getOutputStream()));
            }
            
            Log.d(MainActivity.TAG, "Start send list");
            for (int i = 0; i < fList.length; i++) {
                String fileName = fList[i];
                Log.d(MainActivity.TAG, "Create temp file");
                File tempfile = new File(directory, fileName);
                
                Date date = null;
                SimpleDateFormat dateFormat = null;
                String dateStr = null;
                
                Log.d(MainActivity.TAG, "Check whether the temp file is directory or not");
                if (!tempfile.isDirectory()) {
                	Log.d(MainActivity.TAG, "temp file is a file");
                    date = new Date(tempfile.lastModified());
                    dateFormat = new SimpleDateFormat("MMM dd hh:mm");
                    dateStr = dateFormat.format(date);

                } else {
                	Log.d(MainActivity.TAG, "temp file is a directory");
                    date = new Date(tempfile.lastModified());
                    dateFormat = new SimpleDateFormat("MMM dd  yyyy");
                    dateStr = dateFormat.format(date);
                }

                Log.d(MainActivity.TAG, "Pad length to file's length number");
                int padLength = 15 - dateStr.length();
                for (int j = 0; j < padLength; j++) {
                    dateStr += " ";
                }
                
                Log.d(MainActivity.TAG, "Get the length of temp file");
                long size = tempfile.length();
                String sizeStr = Long.toString(size);
                int sizePadLength = Math.max(12 - sizeStr.length(), 0);
                for (int j = 0; j < sizePadLength; j++) {
                    sizeStr += ' ';
                }
                String sizeField = sizeStr;


                Log.d(MainActivity.TAG, "Get the Permission of the file");
                String permission = "";
                Log.d(MainActivity.TAG, "Get the Permission of the file");
                if (tempfile.canRead()) {
                	Log.d(MainActivity.TAG, "can read");
                    permission += "r";
                } else {
                	Log.d(MainActivity.TAG, "can not read");
                    permission += "-";
                }
                if (tempfile.canWrite()) {
                	Log.d(MainActivity.TAG, "can write");
                    permission += "w";
                } else {
                	Log.d(MainActivity.TAG, "can not write");
                    permission += "-";
                }
//                if (tempfile.canExecute()) {
//                	Log.d(MainActivity.TAG, "can execute");
//                    permission += "x";
//                } else {
//                	Log.d(MainActivity.TAG, "can not execute");
//                    permission += "-";
//                }
                
                permission += "x";
                
                Log.d(MainActivity.TAG, "Send the info of the temp file");
                //since windows lack the permission for user, group and others, we just set them as same.
                String privi = permission + permission + permission;
                if(out == null){
                	Log.d(MainActivity.TAG, "out is null");
                }
                out.print(tempfile.isDirectory() ? 'd' : '-');
                out.print(privi);
                out.print(" ");
                out.print("1");
                out.print(" ");
                out.print("ftp");
                out.print(" ");
                out.print("ftp");
                out.print("       ");
                out.print(sizeField);
                out.print(" ");

                out.print(dateStr);
                out.print(" ");
                out.print(tempfile.getName());

                out.print('\n');
                Log.d(MainActivity.TAG, "Finish send one file");
            }
            Log.d(MainActivity.TAG, "Finish send list");

            out.flush();
            Log.d(MainActivity.TAG, "Flush PrinterWriter");
            writer.println(226 + " " + "Finish Transfer");

        } catch (Exception e) {
        	Log.e(MainActivity.TAG, e.toString());
        } finally {
            try {
                if (data != null) {
                    if(!passive)
                        ((Socket)data).close();
                    else
                        ((ServerSocket)data).close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            	Log.e(MainActivity.TAG, e.toString());
            }
        }
        return 226;
    }

    /**
     * Send the name of all the sub directories and files in the specified path to the client
     * @param path	The specified directory
     * @return		The FTP status code
     */
    public int sendFileList(String path) {
        int reply = 0;
        Socket data = null;
        PrintWriter out = null;
        try {
            data = new Socket(ipAddress, port);


            File file = new File(path);
            String fList[] = file.list();

            writer.println(150 + " " + "File status okay; about to open data connection.");

            out = new PrintWriter(data.getOutputStream());

            for (int i = 0; i < fList.length; i++) {
                String temp = fList[i];
                out.write(temp + '\n');
            }
            out.flush();
            writer.println(226 + " " + "Finish Transfer");
            reply = 226;
        } catch (Exception e) {
        	Log.e(MainActivity.TAG, e.toString());
        } finally {
            try {
                if (data != null) {
                    data.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            	Log.e(MainActivity.TAG, e.toString());
            }
        }
        return reply;
    }
}
