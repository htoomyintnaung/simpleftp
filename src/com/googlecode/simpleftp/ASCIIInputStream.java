/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.googlecode.simpleftp;
import java.io.*;

/**
 * This is the extended class for reading ASCII data,
 * @author zhewang
 */
public class ASCIIInputStream extends FilterInputStream
{
	/**
	 * The constructor for ASCIIInputStream
	 * @param in	The input stream that reads ASCII data
	 */
    public ASCIIInputStream(InputStream in)
    {
        super(in);
    }

    /**
     * Read a character from input stream, transfer '\r\n' to '\n'
     * @return The character after processing
     */
    public int read() throws IOException
    {
        int cha;
        cha = in.read();
        if(cha == -1)
        {
            return -1;
        }
        else 
        {
            if(cha == '\r')
            {
                cha = in.read();
            }
            return cha;
        }
    }

    /**
     * Read "len" characters into an array, starting at off, transfer '\r\n' to '\n'
     * @param data	The array that stores the data
     * @param off	The starting index to store the data
     * @param len	The number of characters to be read
     * @return		The number of characters that has been read
     */
    public int read(byte[] data, int off, int len) throws IOException
    {
        if(len <= 0)
        {
            return -1;
        }

        int cha;
        int count = 0;
        while(count < len)
        {
            cha = read();
            if(cha == -1)
            {
                break;
            }
            data[off + count] = (byte) cha;
            count++;
        }
        return count;
    }

}
