package com.googlecode.simpleftp;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

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
    public int receiveFile(String path) {
        int reply = 0;

        OutputStream out = null;
        Object data = null;
        InputStream in = null;
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
                out = new BufferedOutputStream(new FileOutputStream(file));
            } else if (type.equals("A")) {
                out = new ASCIIOutputStream(new FileOutputStream(file));
            }

            writer.println(150 + " " + "File status okay; about to open data connection.");

            if(!passive)
                in = ((Socket)data).getInputStream();
            else{
                in = client.getInputStream();
            }

            byte bufferRead[] = new byte[BUFFER];
            int nread;
            while ((nread = in.read(bufferRead, 0, BUFFER)) > 0) {
                out.write(bufferRead, 0, nread);
            }
            out.flush();
            in.close();

            writer.println(226 + " " + "Finish Transfer.");
            reply = 226;
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
                System.out.println(e.getMessage());
            }
        }
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
            System.out.println(e.getMessage());
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
                System.out.println(e.getMessage());
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
        File directory = new File(path);
        String fList[] = directory.list();
        Object data = null;
        PrintWriter out = null;
        Socket client = null;

        try {
            if(!passive)
                data = new Socket(ipAddress, port);
            else{
                data = new ServerSocket(port);
                client = ((ServerSocket)data).accept();
            }
           
            writer.println(150 + " " + "File status okay; about to open data connection.");

            if(!passive)
                out = new PrintWriter(new ASCIIOutputStream(((Socket)data).getOutputStream()));
            else{
                out = new PrintWriter(new ASCIIOutputStream(client.getOutputStream()));
            }

            for (int i = 0; i < fList.length; i++) {
                String fileName = fList[i];

                File tempfile = new File(directory, fileName);

                Date date = null;
                SimpleDateFormat dateFormat = null;
                String dateStr = null;
                if (!tempfile.isDirectory()) {
                    date = new Date(tempfile.lastModified());
                    dateFormat = new SimpleDateFormat("MMM dd hh:mm");
                    dateStr = dateFormat.format(date);

                } else {
                    date = new Date(tempfile.lastModified());
                    dateFormat = new SimpleDateFormat("MMM dd  yyyy");
                    dateStr = dateFormat.format(date);
                }

                int padLength = 15 - dateStr.length();
                for (int j = 0; j < padLength; j++) {
                    dateStr += " ";
                }
                long size = tempfile.length();
                String sizeStr = Long.toString(size);
                int sizePadLength = Math.max(12 - sizeStr.length(), 0);
                for (int j = 0; j < sizePadLength; j++) {
                    sizeStr += ' ';
                }
                String sizeField = sizeStr;


                String permission = "";
                if (tempfile.canRead()) {
                    permission += "r";
                } else {
                    permission += "-";
                }
                if (tempfile.canWrite()) {
                    permission += "w";
                } else {
                    permission += "-";
                }
                if (tempfile.canExecute()) {
                    permission += "x";
                } else {
                    permission += "-";
                }
                //since windows lack the permission for user, group and others, we just set them as same.
                String privi = permission + permission + permission;
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
            }

            out.flush();
            writer.println(226 + " " + "Finish Transfer");

        } catch (Exception e) {
            System.out.println(e.getMessage());
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
                System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
        } finally {
            try {
                if (data != null) {
                    data.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return reply;
    }
}
