/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.googlecode.simpleftp;
import java.io.*;

/**
 * This is the extended class for sending ASCII data
 * @author zhewang
 */
public class ASCIIOutputStream extends FilterOutputStream
{
	/**
	 * This is the constructor for ASCIIOutputStream
	 * @param out
	 */

    public ASCIIOutputStream(OutputStream out)
    {
        super(out);
    }

    /**
     * Write a character to output stream, transfer '\n' to '\r\n'
     * @param b The character to be sent
     */
    
    public void write(int b) throws IOException 
    {
        if(b == '\n')
        {
            out.write('\r');
        }
        out.write(b);
    }

    /**
     * Write characters to output stream, transfer '\n' to '\r\n'
     * @param b		The array that stores the character
     * @param off 	The starting index of the characters to be sent
     * @param len	The number of characters to be seng
     */
    
    public void write(byte[] b, int off, int len) throws IOException 
    {
        for(int i = 0; i < len; i++)
        {
            int cha = b[off+i];
            if(cha == '\n')
            {
                out.write('\r');
            }
            out.write(cha);
        }
    }
}
