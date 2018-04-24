/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2018 by Kaj Wortel - all rights reserved                    *
 * Contact: kaj.wortel@gmail.com                                             *
 *                                                                           *
 * This file is part of the tools project, which can be found on github:     *
 * https://github.com/Kaj0Wortel/tools                                       *
 *                                                                           *
 * It is allowed to use, (partially) copy and modify this file               *
 * in any way for private use only by using this header.                     *
 * It is not allowed to redistribute any (modifed) versions of this file     *
 * without my permission.                                                    *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package tools.log;


// Tools imports
import tools.MultiTool;


// Java imports
import java.io.IOException;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;


/* 
 * Default format for a logger class.
 */
public abstract class DefaultLogger extends Logger {
    final public static String LS = System.getProperty("line.separator");
    
    /* 
     * Default constructor which creates a new fair lock.
     */
    public DefaultLogger() {
        lock = new ReentrantLock(true);
    }
    
    
    /* 
     * Writes the given text to the log.
     * 
     * @param text to be write to the log.
     * 
     * Note: The default exception handeling is printing them to
     * the {@code System.out stream}. Catch The exception here to change the
     * behaviour.
     * 
     * @throws IOException.
     */
    protected abstract void writeText(String text)
            throws IOException;
    
    
    @Override
    protected void writeE(Exception e, Type type, Date timeStamp) {
        if (useFull) {
            String[] text = Arrays.toString(e.getStackTrace()).split(", ");
            String message = e.getClass().getName() + ": " + e.getMessage();
            
            if (lock != null) lock.lock();
            try {
                processText(message, type, timeStamp, useTimeStamp);
                
                for (int i = 0; i < text.length; i++) {
                    processText(text[i], Type.NONE, timeStamp, false);
                }
                
                flush();
                
            } finally {
                if (lock != null) lock.unlock();
            }
            
        } else {
            String message = e.getClass().getName() + ": " + e.getMessage();
            
            if (lock != null) lock.lock();
            try {
                processText(message, Type.ERROR, timeStamp, useTimeStamp);
                flush();
                
            } finally {
                if (lock != null) lock.unlock();
            }
        }
    }
    
    @Override
    protected void writeO(Object obj, Type type, Date timeStamp) {
        if (lock != null) lock.lock();
        try {
            processText(obj.toString(), type, timeStamp, useTimeStamp);
            flush();
            
        } finally {
            if (lock != null) lock.unlock();
        }
    }
    
    protected void processText(String text, Type type, Date timeStamp,
                                  boolean useDate) {
        try {
            String dateLine;
            String infoLine;
            
            // Determine the date line
            dateLine = dateFormat.format(timeStamp) + " ";
            if (!useDate) {
                dateLine = MultiTool.fillSpaceRight("", dateLine.length());
            }
            
            // Determine the info line
            infoLine = MultiTool
                .fillSpaceRight("[" + type.toString() + "]", 10);
            
            writeText(dateLine + infoLine + text + LS);
            
        } catch (IOException e){
            System.err.println(e);
        }
            
    }
    
    /* 
     * Writes the header of the log file.
     * Here is given that {@code writer != null}.
     */
    protected void writeHeader() {
        try {
            if (header == null) return;
            writeText(header.replaceAll("&date&", formatDate(new Date())) + LS);
            
        } catch (IOException e){
            System.err.println(e);
        }
    }
    
}