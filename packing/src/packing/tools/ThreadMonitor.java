
package packing.tools;


//##########
// Java imports
import java.util.ArrayList;
import java.util.List;


/**
 * Keeps track of the threads currently active in the application.
 * Use this class to start and terminate a thread.
 */
public class ThreadMonitor {
    // List containing all currently active threads.
    final private static List<Thread> threads = new ArrayList<Thread>();
    // Whether the threads are being stopped.
    private static boolean stoppingThreads = false;
    
    /**
     * Creates and starts a new thread that runs the given runnable.
     * 
     * @param r the function to be executed.
     * @return the created thread. Returns {@code null} if the threads are
     *     being stopped.
     */
    public static Thread startThread(Runnable r) {
        if (stoppingThreads) return null;
        
        Thread thread = new Thread() {
            @Override
                public void run() {
                r.run();
                
                synchronized(threads) {
                    threads.remove(this);
                }
            }
        };
        
        synchronized(threads) {
            if (stoppingThreads) return null;
            threads.add(thread);
            thread.start();
        }
        
        return thread;
    }
    
    /**
     * @return a list containing all threads.
     */
    public static List<Thread> getThreads() {
        synchronized(threads) {
            return threads;
        }
    }
    
    /**
     * Kills all threads.
     * 
     * IMPORTANT NOTE:
     * This way of stopping threads should only be used when you are
     * CERTAIN that the threads aren't keeping important monitors occupied
     * as these resources are being released when the threads are killed.
     * When it is not nesseccary to directly kill a thread you should
     * use {@link interruptAllThreads()}.
     */
    @SuppressWarnings("deprecation")
    public static void killAll() {
        synchronized(threads) {
            stoppingThreads = true;
            for (Thread thread : threads) {
                thread.stop();
            }
            stoppingThreads = false;
        }
    }
    
    /**
     * Interrupts all threads.
     * 
     * Note that this does NOT guarantee that the threads immediately stop.
     * They only stop when methods that check for the interrupt flag are
     * invoked, for example {@link Thread#sleep(long)}.
     */
    public static void interruptAll() {
        synchronized(threads) {
            stoppingThreads = true;
            for (Thread thread : threads) {
                thread.interrupt();
            }
            stoppingThreads = false;
        }
    }
    
    /**
     * @return whether all assigned threads are finished.
     */
    public static boolean areFinished() {
        synchronized(threads) {
            return threads.isEmpty();
        }
    }
        
}
