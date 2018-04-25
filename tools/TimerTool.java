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

package tools;


// Java imports
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


public class TimerTool {
    private Timer timer; // The current timer object.
    final private Runnable[] tasks; // The tasks to be executed.
    private Long delay; // The initial delay
    private long interval; // The timerinterval
    
    // The start timestamp of the timer for the current iteration
    private Long startTime;
    
    // The pause timestamp of the timer. If there was no pause in
    // this iteration then it is equal to the start timestamp
    private Long pauseTime;
    
    // The current state of the timer
    private TimerState timerState = TimerState.CANCELED;
    public enum TimerState {
        RUNNING, PAUSED, CANCELED
    }
    
    /* --------------------------------------------------------------------------------------------------------
     * Constructor
     * --------------------------------------------------------------------------------------------------------
     */
    /*
     * @param r the action that will be executed when the timer ends.
     * @param delay the time in ms before the first exectution of {@code r.run()}.
     * @param interval the time in ms which is between two executions of {@code r.run()}.
     */
    public TimerTool(Long delay, long interval, Runnable... rs) {
        // Update the values to the values in this class
        this.tasks = rs;
        this.delay = delay;
        this.interval = interval;
        
        // Create new timer.
        timer = new Timer(true);
        
        // Checks if the delay is not equal to 'null'.
        // If so, set the initial delay to '0'
        if (delay == null) {
            delay = 0L;
        }
        
        this.delay = delay;
        
        // For the first iteration is the start time modified because then there are no problems
        // with the pause/resume functions if the timer is still in the initial delay.
        startTime = System.currentTimeMillis() + delay - interval;
    }
    
    
    /* --------------------------------------------------------------------------------------------------------
     * Functions
     * --------------------------------------------------------------------------------------------------------
     */
    /* 
     * Create a new timer task from the given runnable.
     * Also updates the start time and the pause time.
     * 
     * @param rs the tasks to be executed. Is allowed to be null, but this is not effective.
     */
    private TimerTask createTimerTask(Runnable... rs) {
        return new TimerTask() {
            @Override
            public void run() {
                // Update the timestamps
                startTime = System.currentTimeMillis();
                pauseTime = System.currentTimeMillis();
                
                // Run the function(s)
                if (rs != null) {
                    for (Runnable r : rs) {
                        r.run();
                    }
                }
            }
        };
    }
    
    /* 
     * (Re)-starts the timer.
     * If the timer is already running, purge the timer and create a new timer.
     */
    public void start() {
        if (timerState == TimerState.RUNNING) {
            timer.cancel();
            timer.purge();
        }
        
        // Update the timestamps
        startTime = System.currentTimeMillis();
        pauseTime = System.currentTimeMillis();
        
        timer = new Timer(true);
        
        timer.scheduleAtFixedRate(createTimerTask(tasks), delay, interval);
        
        // Update the timeState
        timerState = TimerState.RUNNING;
    }
    
    /* 
     * Pauses the timer.
     * If the timer is paused or stopped, no acion is taken.
     */
    public void pause() {
        if (timerState == TimerState.PAUSED || timerState == TimerState.CANCELED) return;
        
        timer.cancel();
        timer.purge();
        
        // Set the pause time stamp
        pauseTime = System.currentTimeMillis();
        
        // Update the timeState
        timerState = TimerState.PAUSED;
    }
    
    /* 
     * Resumes a paused timer
     * If the timer is running or canceled, no action is taken.
     */
    public void resume() {
        if (timerState == TimerState.RUNNING || timerState == TimerState.CANCELED) return;
        
        // The current time
        long curTime = System.currentTimeMillis();
        
        // Calculate the initial delay.
        long timeBeforeRun = interval - (curTime - startTime);
        long startDelay = (timeBeforeRun < 0 ? 0 : timeBeforeRun);
        
        // Update the start time stamp.
        startTime = System.currentTimeMillis() - timeBeforeRun;
        
        timer = new Timer(true);
        
        timer.scheduleAtFixedRate(createTimerTask(tasks), startDelay, interval);
        
        // Update the timeState
        timerState = TimerState.RUNNING;
    }
    
    /* 
     * Cancels a running or canceled timer.
     */
    public void cancel() {
        // Kill the current timer.
        if (timerState == TimerState.RUNNING) {
            timer.cancel();
            timer.purge();
        }
        
        // Update the timeState
        timerState = TimerState.CANCELED;
    }
    
    /* 
     * Sets a new interval for the timer.
     * 
     * @param interval the new interval to be set.
     */
    public void setInterval(long interval) {
        // If the interval is equal, return immediately.
        if (interval == this.interval) return;
        
        // Update the interval
        this.interval = interval;
        
        // The current time
        long curTime = System.currentTimeMillis();
        
        // If the timer is running, kill it.
        if (timerState == TimerState.RUNNING) {
            timer.cancel();
            timer.purge();
            
            // Calculate the initial delay.
            long timeBeforeRun = interval - (curTime - startTime);
            long startDelay = (timeBeforeRun < 0 ? 0 : timeBeforeRun);
            
            // Update the start timestamp if the timer has to start directly.
            if (timeBeforeRun < 0) {
                startTime = curTime + timeBeforeRun;
            }
            
            // Start a new timer
            timer = new Timer(true);
            timer.scheduleAtFixedRate(createTimerTask(tasks), startDelay, interval);
        }
    }
    
    /* 
     * @return the current state of the timer.
     */
    public TimerState getState() {
        return timerState;
    }
    
}