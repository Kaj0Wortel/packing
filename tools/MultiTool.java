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


// Tools imports
import tools.log.Logger;


// Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/* 
 * This class contains multiple handy methods.
 */
public class MultiTool {
    /* 
     * Converts a decimal integer to a 32 based number String.
     * 
     * @param dHex a decimal number.
     * @return a String containing the 32 based representation of {@code dHex}.
     */
    public static String dHexToString(int dHex) throws NumberFormatException {
        if (dHex < 0) throw new NumberFormatException
            ("Cannot convert a negative number.");
        
        String result = "";
        int heighestDHexNumber = 1;
        
        // Compute the length of the result.
        while (intPow(32, heighestDHexNumber++) - 1 < dHex) { }
        
        // Compute the result.
        for (int counter = 0; counter < heighestDHexNumber; counter++) {
            int part = (int)((double)(dHex) / Math.pow
                                 (32, heighestDHexNumber- (counter+1)));
            dHex -= part * Math.pow(32, heighestDHexNumber - (counter+1));
            result += singleDHexToString(part);
        }
        
        return result;
    }
    
    /* 
     * Converts a 32 based number String to a decimal integer.
     * 
     * @param dHex 32 based number String.
     * @return an integer represented by the 32 based number of {@code dHex}.
     */
    public static int stringToDHex(String dHex) throws NumberFormatException {
        if (dHex == null) throw new NullPointerException("Input is null.");
        
        // Convert to upper case
        dHex = dHex.toUpperCase();
        
        // Compute the result.
        int result = 0;
        if (dHex.length() == 0) throw new NumberFormatException
            ("Invallid length. Expected: length > 0, but found length == 0.");
        
        for (int counter = 0; counter < dHex.length(); counter++) {
            result += intPow(32, dHex.length() - (counter+1))
                * singleStringToDHex(dHex.charAt(counter));
        }
        
        return result;
    }
    
    /* 
     * Converts a single decimal number to a hexadecimal char.
     * 
     * @param dHex a decimal number (0 <= dHex < 32).
     * @return the 32 based representation of {@code dHex}.
     */
    private static char singleDHexToString(int dHex)
            throws NumberFormatException {
        if (dHex >= 0 && dHex <= 9) {
            return (char) (dHex + '0');
            
        } else if (dHex >= 10 && dHex <= 31) {
            return (char) (dHex - 10 + 'A');
            
        } else {
            throw new NumberFormatException
                ("Expected: 0 <= n <= 31, but found: " + dHex);
        }
    }
    
    /* 
     * Converts a single 32 based char to a decimal number.
     * 
     * @param dHex a 32 based number ('0' <= dHex <= '9' || 'A' <= dHex <= 'V').
     * @return the decimal representation of {@code dHex}.
     */
    private static int singleStringToDHex(char dHex)
            throws NumberFormatException {
        if (dHex >= '0' && dHex <= '9') {
            return dHex - '0';
            
        } else if (dHex >= 'A' && dHex <= 'V') {
            return dHex - 'A' + 10;
            
        } else {
            throw new NumberFormatException
                ("Expected: '0' <= n <= '9' or 'A' <= n <= 'V'"
                     + ", but found: " + dHex);
        }
    }
    
    /* 
     * Lists all files in a dir.
     * @param rootDir the root dir from which the files will be listed.
     * @param listDirs whether the directories should be listed or not.
     * @param pathSoFar the path that has been traversed so far.
     * 
     * @return an ArrayList containing an array of File objects of
     *     length 2 for which holds:
     *     - The first element contains the full path of the file.
     *     - The second element contains the path of the file relative to
     *       the given rootDir (excl. the rootdir and the file itself).
     * 
     * @throws IllegalArgumentException if the root dir is not a directory.
     * @throws IllegalStateException if a file found in a certain directory,
     *     is not located in that directory.
     * 
     * Furthermore is guarenteed that:
     * - If {@code listDirs}, then it holds for every directory that all its
     *   children (sub-dirs included) are listed directly below (higher index)
     *   its own entry.
     * - All files X that are part of a directory tree always occur
     *   in consecutive order (so no other directory trees that are not
     *   contained by the former one will be in this part of the array).
     * No other assumptions regarding file-order can be made on the output.
     * 
     * Note: ONLY use the THIRD function when you know what you are doing!
     */
    public static ArrayList<File[]> listFilesAndPathsFromRootDir(File rootDir)
            throws IllegalArgumentException, IllegalStateException {
        return listFilesAndPathsFromRootDir(rootDir, "", true);
    }
    
    public static ArrayList<File[]> listFilesAndPathsFromRootDir
            (File rootDir, boolean listDirs)
            throws IllegalArgumentException, IllegalStateException {
        return listFilesAndPathsFromRootDir(rootDir, "", listDirs);
    }
    
    public static ArrayList<File[]> listFilesAndPathsFromRootDir
            (File rootDir, String pathSoFar, boolean listDirs)
            throws IllegalArgumentException, IllegalStateException {
        
        if (rootDir.isFile()) {
            new IllegalArgumentException
                ("The file \"" + rootDir.getPath() + "\" is no dir.");
        }
        
        pathSoFar = (pathSoFar == null ? "" : pathSoFar);
        
        ArrayList<File[]> output = new ArrayList<File[]>();
        String root = rootDir.getPath();
        File[] listOfFiles = rootDir.listFiles();
        
        if (listDirs && !pathSoFar.equals("")) {
            output.add(new File[] {rootDir, new File(pathSoFar)});
        }
        
        for (int i = 0; i < listOfFiles.length; i++) {
            if (!root.equals(listOfFiles[i].getPath()
                                 .substring(0, root.length()))) {
                throw new IllegalStateException
                    ("File \"" + listOfFiles[i]
                         + "\" could not be found in dir \"" + root + "\"");
            }
            
            if (listOfFiles[i].isFile()) {
                output.add(new File[] {listOfFiles[i],
                    new File(pathSoFar + listOfFiles[i].getParent()
                                 .substring(root.length()))});
                
            } else {
                output.addAll
                    (listFilesAndPathsFromRootDir
                         (listOfFiles[i], pathSoFar
                              + listOfFiles[i].getPath()
                              .substring(root.length()), listDirs));
            }
        }
        
        return output;
    }
    
    
    /* 
     * Checks if a given number is in the array.
     * 
     * @param array array to look in.
     * @param number number to look for.
     * @return true iff the number is in the array.
     */
    public static boolean isInArray(int[] array, int number) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == number) {
                return true;
            }
        }
        
        return false;
    }
    
    /* 
     * Checks if a given generic value is in the array.
     * 
     * @param array the array to look in.
     * @param value the value to look for.
     * @return true iff value is in the array.
     */
    public static <T> boolean isInArray(T[] array, T value) {
        for (T entry : array) {
            if (entry.equals(value)) return true;
        }
        
        return false;
    }
    
    /* 
     * Converts an ArrayList to an array.
     * 
     * @param array the array to be converted.
     * @return an ArrayList containing every element of {@code array} and
     *     in the same order.
     */
    public static <T> ArrayList<T> toArrayList(T[] array) {
        if (array == null) return null;
        
        ArrayList<T> list = new ArrayList<T>(array.length);
        
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        
        return list;
    }
    
    /* 
     * Converts a List to an array.
     * 
     * @param list the input list
     * @param classValue the input/output class type
     * @param start the first element that will be put in the array.
     * @param end the last element that will NOT be put in the array.
     * @return the elements from the output array in the same order as in the
     *     input List, and null iff the given list or class are null.
     * @throws IllegalArgumentException iff start < end.
     */
    @SuppressWarnings("unchecked")
    public static <A, B extends A> A[] listToArray(List<B> list,
                                                   Class<B> classValue) {
        return listToArray(list, classValue, 0);
    }
    
    @SuppressWarnings("unchecked")
    public static <A, B extends A> A[] listToArray
            (List<B> list, Class<B> classValue, int start) {
        return listToArray(list, classValue, start, list.size());
    }
    
    @SuppressWarnings("unchecked")
    public static <A, B extends A> A[] listToArray
            (List<B> list,Class<B> classValue, int start, int end)
           throws IllegalArgumentException {
        if (list == null || classValue == null) return null;
        if (start >= end) throw new IllegalArgumentException
            ("start(" + start + ") > end(" + end + ").");
        
        A[] array = (A[]) Array.newInstance(classValue, end - start);
        
        for (int i = start; i < list.size() && i < end; i++) {
            array[i - start] = list.get(i);
        }
        
        return (A[]) array;
    }
    
    /* 
     * Converts any array to an ArrayList
     * 
     * @param array the input array
     * @param classValue the input/output class type
     * @return the elements from the output ArrayList in the same order as
     *     in the input array.
     *     Returns null iff the given array or class are null.
     * 
     * WARNING! THIS FUNCTION HAS NOT BEEN EXTENSIVLY TESTED!
     * If you get class cast exceptions (e.g. cannot convert/cast
     * Object[] to XXX[]), here's you problem.
     * 
     * Note to me: Easier default function might exist. First look
     * at those before actually trying to fix this.
     */
    @SuppressWarnings("unchecked")
    public static <A, B extends A> ArrayList<A> arrayToArrayList
            (B[] array, Class<B> classValue) {
        if (array == null) return null;
        
        List<A> list = new ArrayList<A>(array.length);
        
        for (int i = 0; i < array.length; i++) {
            list.add((A) array[i]);
        }
        
        return (ArrayList<A>) list;
    }
    
    /* 
     * Makes a copy of an arrayList.
     * 
     * @param list ArrayList to copy
     * WARNING! THIS FUNCTION HAS NOT BEEN EXTENSIVLY TESTED!
     * 
     * Note to me: Easier default function might exist. First look
     * at those before actually trying to fix this.
     */
    public static <T> ArrayList<T> copyArrayList(ArrayList<T> list) {
        if (list == null) return null;
        
        ArrayList<T> newList = new ArrayList<T>(list.size());
        
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i));
        }
        
        return newList;
    }
    
    /* 
     * Makes a copy of an array.
     * 
     * @param array array to copy.
     * WARNING! THIS FUNCTION HAS NOT BEEN EXTENSIVLY TESTED!
     * 
     * Note to me: Easier default function might exist. First look
     * at those before actually trying to fix this.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyArray(T[] array) {
        if (array == null) return null;
        
        T[] newArray = (T[]) Array.newInstance
            (array.getClass().getComponentType(), array.length);
        
        for (int i = 0; i < array.length; i++) {
            newArray[i] = (T) array[i];
        }
        
        return (T[]) newArray;
    }
    
    /*
     * Sleeps the current thread.
     * Mainly used to avoid the annoying catch statement.
     * 
     * @param time time in ms that the thread sleeps.
     * 
     * @see Thead#sleep(long)
     */
    public static void sleepThread(long time) {
        try {
            Thread.sleep(time);
            
        } catch (InterruptedException e) {
            System.err.println(e);
        }   
    }
    
    /* 
     * Fires an ActionEvent for all ActionListeners in the array.
     * Uses as time the current time stamp.
     * 
     * See fireActioneEvents(String, long, int, ActionListener[]) for more info.
     */
    public static void fireActionEvents(Object source, String command,
                                        int modifiers, ActionListener[] als) {
        fireActionEvents(source, command, System.currentTimeMillis(),
                         modifiers, als);
    }
    
    /* 
     * Fires an ActionEvent for all ActionListeners currently listening.
     * Uses another thread for execution.
     * 
     * @param source the source of the event.
     * @param command the command used for the event.
     * @param when the time when the event occured.
     * @param modifiers the modifiers for the event.
     * @param als array containing the ActionListeners that
     *     need to be notified of the event.
     */
    public static void fireActionEvents(final Object source,
                                        final String command, final long when,
                                        final int modifiers,
                                        final ActionListener[] als) {
        if (als == null) return;
        
        new Thread(source.getClass().getName() + " ActionEvent") {
            public void run() {
                ActionEvent e = new ActionEvent(source,
                                                ActionEvent.ACTION_PERFORMED,
                                                command, when, modifiers);
                
                for (int i = 0; i < als.length; i++) {
                    if (als[i] == null) continue;
                    
                    als[i].actionPerformed(e);
                }
            }
        }.start();
    }
    
    /* 
     * Converts a double to a String, having 'decimals' decimals.
     * 
     * @param num the number to be converted.
     * @param the number of decimals.
     * @return String representation of a double, having 'decimals' decimals.
     */
    public static String doubleToStringDecimals(double num, int decimals) {
        if (decimals < 0) throw new IllegalArgumentException
            ("Number of decimals was negative: " + decimals);
        
        String number = Double.toString(num);
        for (int i = 0; i < number.length(); i++) {
            if (number.charAt(i) == '.') {
                if (decimals == 0) {
                    return number.substring(0, i);
                    
                } else if (number.length() > i + decimals) {
                    return number.substring(0, i + decimals + 1);
                    
                } else {
                    while (number.length() < i + decimals + 1) {
                        number += "0";
                    }
                }
                
                return number;
            }
        }
        
        // '.' was not found
        number += ".";
        for (int i = 0; i < decimals; i++) {
            number += "0";
        }
        
        return number;
    }
    
    /* 
     * Converts an Integer to a String, with zero's filled
     * till the n'th position.
     * 
     * @param i number to be converted.
     * @param n the length of the number + number of leading zeroes.
     * 
     * If the length of the number is bigger then n, then the full number
     * is returned.
     */
    public static String fillZero(int i, int n) throws NumberFormatException {
        String number = Integer.toString(i);
        
        while (number.length() < n) {
            number = "0" + number;
        }
        
        return number;
    }
    
    /* 
     * Adds spaces to the left of the given text till the total length
     * of the text is equal to the given size.
     * If the initial length of the text is longer then the given size,
     * no action is taken.
     * 
     * @param text text to process.
     * @param size length of the text.
     */
    public static String fillSpaceLeft(String text, int size) {
        for (int i = text.length(); i < size; i++) {
            text = " " + text;
        }
        
        return text;
    }
    
    /* 
     * Adds spaces to the right of the given text till the total length
     * of the text is equal to the given size.
     * If the initial length of the text is longer then the given size,
     * no action is taken.
     * 
     * @param text text to process.
     * @param size length of the text.
     */
    public static String fillSpaceRight(String text, int size) {
        for (int i = text.length(); i < size; i++) {
            text = text + " ";
        }
        
        return text;
    }
    
    /* 
     * Converts all spaces in the input String to spaces that
     * are visible in html.
     * 
     * @param text text to process.
     */
    public static String toHTMLSpace(String text) {
        return text.replaceAll(" ", "" + ((char) 0x00A0));
    }
    
    /*
     * Calculates the power of a base.
     * Has a much higher accuracy compared to the function
     * java.lang.Math.pow(double, double), but only accepts integer powers.
     * Takes significantly less time calculating powers less then 500, but more
     * when calculating higher powers compared to the same method.
     * 
     * @param base the base used for the power.
     * @param pow the power used for the power.
     * @result the result of base ^ pow.
     */
    public static double intPow(double base, int pow) {
        double result = 1;
        
        for (int i = 0; i < pow; i++) {
            result *= base;
        }
        
        for (int i = -1; i >= pow; i--) {
            result /= base;
        }
        
        return result;
    }
    
    /* 
     * Checks if a given object is an array.
     * Returns true if so, false otherwise.
     * 
     * @param obj the Object to be tested.
     * @return true if {@code obj} is an array. False otherwise.
     */
    public static boolean isArray(Object obj) {
        if (obj == null) return false;
        return obj.getClass().isArray();
    }
    
    /* 
     * Randomly shuffles an array.
     * 
     * @param in the input array.
     * @param rnd the used Random object.
     */
    public static <V> V[] shuffleArray(V[] in) {
        return shuffleArray(in, new Random());
    }
    
    public static <V> V[] shuffleArray(V[] in, Random rnd) {
        for (int i = in.length; i > 1; i--) {
            swap(in, i - 1, rnd.nextInt(i));
        }
        
        return in;
    }
    
    /* 
     * Makes a copy of a Class array, keeping the type variable.
     * 
     * @param array Class array with type to copy.
     * WARNING! THIS FUNCTION HAS NOT BEEN EXTENSIVLY TESTED YET!
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T>[] copyArray(Class<T>[] array) {
        if (array == null) return null;
        
        Class<T>[] newArray = (Class<T>[]) new Class[array.length];
        
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        
        return newArray;
    }
    
    /* 
     * Performs a swap between two elements of an array.
     * 
     * @param arr the array where the swap occurs.
     * @param i the first element of the swap.
     * @param j the second element of the swap.
     * @return arr, but then with the elements i and j swapped.
     * @throws throws ArrayIndexOutOfBoundsException if {@code i} or {@code j}
     *     are invallid indices of {@code arr}.
     *     
     */
    public static <V> V[] swap(V[] arr, int i, int j)
            throws ArrayIndexOutOfBoundsException {
        V tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        return (V[]) arr;
    }
    
    /* 
     * Compares two arrays. Does a deep check.
     * 
     * @param a array to compare.
     * @param b array to compare.
     * @return whether the two given arrays {@code a} and {@code b} are
     *     fully identical.
     */
    public static boolean compareDeepArray(Object[] a, Object[] b) {
        if (a == null || b == null) return a == b;
        if (a.length != b.length) return false;
        
        for (int i = 0; i < a.length; i++) {
            boolean isArrayA = a[i].getClass().isArray();
            boolean isArrayB = b[i].getClass().isArray();
            
            if (isArrayA && isArrayB) {
                // If both are an array, recursivly determine whether
                // they are equal.
                if (!compareDeepArray(safeObjArrCast(a[i]),
                                      safeObjArrCast(b[i]))) {
                    return false;
                }
                
            } else if (isArrayA ^ isArrayB) {
                // If either of them is an array, they can never be equal.
                return false;
                
            } else {
                // If both are plain elements, simply check for equality.
                if (!a[i].equals(b[i])) return false;
            }
        }
        
        return true;
    }
    
    /* 
     * Makes a deep clone of the given value.
     * If the value is an array, recursivly make a clone of each element
     * and put them in a new array.
     * 
     * Supported types:
     * - {@code value} contains tools.Cloneable elements, then each element is
     *   simply cloned by invoking the clone method.
     * - {@code value} contains java.lang.Cloneable elements, then each element
     *   is cloned by bypassing the private access modifier of clone method.
     *   Note that the clone method should be overridden by that class.
     * - {@code value} contains a primitive data type (boolean, char, byte,
     *   short, int, long, float, double) or is a String.
     * 
     * @param value the value to be cloned.
     * @return a clone of the value. This means that {@code value != \return
     *     && value.equals(\return)} will hold afterwards (assuming that the
     *     equals method is implemented correctly).
     * @throws IllegalStateException iff
     *     the clone method could not terminate normally.
     * @throws UnsupportedOperationException iff
     *     the provided value does not contain one of the supported types.
     */
    @SuppressWarnings("unchecked")
    public static <V> V deepClone(V value)
            throws IllegalStateException, UnsupportedOperationException {
        if (value == null) return null;
        
        if (value.getClass().isArray()) {
            return (V) deepArrayClone(safeObjArrCast(value));
            
        } else {
            if (value instanceof tools.Cloneable) {
                return (V) ((Cloneable) value).clone();
                
            } else if (value instanceof java.lang.Cloneable) {
                try {
                    Method clone = value.getClass().getMethod("clone");
                    clone.setAccessible(false);
                    return (V) clone.invoke(value);
                    
                } catch (NoSuchMethodException |
                         SecurityException |
                         IllegalAccessException e) {
                    // When the method was not reacheable.
                    Logger.write(new Object[] {
                        "Unaccessable clone method of object \""
                            + value.toString() + "\"!", e
                    });
                    
                    throw new IllegalStateException
                        ("Could not finish cloning! Last element: "
                             + value.toString());
                    
                } catch (InvocationTargetException e) {
                    // When the clone method threw an exception.
                    Logger.write(new Object[] {
                        "An error occured while cloning the object \""
                            + value.toString() + "\":", e.getCause()
                    });
                    
                    throw new IllegalStateException
                        ("An error occured while cloning the object \""
                            + value.toString() + "\".");
                }
                
            } else {
                if (value instanceof Boolean) {
                    return (V) new Boolean((Boolean) value);
                    
                } else if (value instanceof Character) {
                    return (V) new Character((Character) value);
                    
                } else if (value instanceof Byte) {
                    return (V) new Byte((Byte) value);
                    
                } else if (value instanceof Short) {
                    return (V) new Short(((Short) value).shortValue());
                    
                } else if (value instanceof Integer) {
                    return (V) new Integer(((Integer) value).intValue());
                    
                } else if (value instanceof Long) {
                    return (V) new Long(((Long) value).longValue());
                    
                } else if (value instanceof Float) {
                    return (V) new Float(((Float) value).floatValue());
                    
                } else if (value instanceof Double) {
                    return (V) new Double(((Double) value).doubleValue());
                    
                } else if (value instanceof String) {
                    return (V) new String((String) value);
                    
                }
                
                // For anything else
                throw new UnsupportedOperationException
                    ("Expected a cloneable object, but found: "
                         + value.getClass().toString());
            }
            
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T[] deepArrayClone(T[] objArr)
            throws IllegalStateException, IllegalArgumentException {
        T[] newObjArr = (T[]) Array.newInstance(objArr
                                                    .getClass()
                                                    .getComponentType(),
                                                objArr.length);
        for (int i = 0; i < objArr.length; i++) {
            newObjArr[i] = (T) deepClone(objArr[i]);
        }
        
        return (T[]) newObjArr;
    }
    
    /* 
     * Calculates a hash code for an object with the given dependant objects.
     * 
     * @param objArr immutable dependant variables of a class.
     * @return a hash code for the class, given the dependant objects.
     * 
     * Note: this method will always return the same number if the input
     * objects are equal. This also holds for null objects.
     * If no objects are given (i.e. {@code objArr == null}),
     * then 0 is returned.
     */
    public static int calcHashCode(Object... objArr) {
        if (objArr == null) return 0;
        
        int result = 41;
        
        for (Object obj : objArr) {
            int c;
            
            if (obj == null) {
                c = 0;
                
            } else if (obj instanceof Boolean) {
                c = ((Boolean) obj ? 1 : 0);
                
            } else if (obj instanceof Byte ||
                       obj instanceof Character ||
                       obj instanceof Short ||
                       obj instanceof Integer) {
                c = (int) obj;
                
            } else if (obj instanceof Long) {
                c = (int) Math.pow((Long) obj, (Long) obj >>> 32);
                
            } else if (obj instanceof Float) {
                c = Float.floatToIntBits((Float) obj);
                
            } else if (obj instanceof Double) {
                Long value = Double.doubleToLongBits((Double) obj);
                c = (int) Math.pow(value, value >>> 32);
                
            } else if (isArray(obj)) {
                c = calcHashCode(safeObjArrCast(obj));
                
            } else {
                c = obj.hashCode();
            }
            
            result = 37 * result + c;
        }
        
        return result;
    }
    
    /* 
     * Calculates the dimensions of the array.
     * 
     * @param obj array to calculate the dimensions of.
     * @param isEqual whether each sub-level of the array as the same dimension.
     *     (So new {@code int[5][5]} yields true, but
     *      {@code int[][] {int[4], int[5]}} yields false).
     * @return an array containing the dimensions of the array, where the
     *     lowest index denotes the topmost level. When the array is unequal,
     *     the maximum value for each level is taken.
     *     (so {@code int[][] {int[4], int[5]}} yields {@code int[] {2, 5}}).
     */
    public static int[] calcDimArray(Object obj) {
        return calcDimArray(obj, false);
    }
    
    public static int[] calcDimArray(Object obj, boolean isEqual) {
        return calcDimArray(obj, isEqual, calcDepthArray(obj) - 1);
    }
    
    private static int[] calcDimArray(Object obj, boolean isEqual, int depth) {
        if (obj == null || depth < 0) return new int[0];
        
        if (obj.getClass().isArray()) {
            int[] dim = new int[depth + 1];
            dim[0] = Array.getLength(obj);
            
            if (Array.getLength(obj) == 0) {
                return dim;
            }
            
            if (isEqual) {
                int[] oldDim = calcDimArray(Array.get(obj, 0),
                                            isEqual, depth - 1);
                for (int i = 0; i < oldDim.length; i++) {
                    dim[i + 1] = oldDim[i];
                }
                
                return dim;
                
            } else {
                for (int i = 0; i < Array.getLength(obj); i++) {
                    int[] oldDim = calcDimArray(Array.get(obj, i),
                                                isEqual, depth - 1);
                    for (int j = 0; j < oldDim.length; j++) {
                        if (oldDim[j] > dim[j + 1]) {
                            dim[j + 1] = oldDim[j];
                        }
                    }
                }
                
                return dim;
                
            }
            
        } else {
            return new int[0];
        }
    }
    
    /* 
     * Calculates the depth of the given array.
     * 
     * @param obj the array to calculate the depth of.
     * @return the depth of the given array.
     */
    public static int calcDepthArray(Object obj) {
        if (obj == null) return -1;
        String name = obj.getClass().getName();
        
        int depth = -1;
        while (depth < name.length() && name.charAt(++depth) == '[') {}
        
        return depth;
    }
    
    /* 
     * Safely casts an Object to an Object[].
     * 
     * @param obj object to be casted.
     * @return a safly casted version of obj.
     * @throws IllegalArgumentException iff obj is not an array.
     * 
     * Note: if obj is a 1D array of a primative type (e.g. int[]),
     * then a new Object[] is created that contains the same values.
     */
    public static Object[] safeObjArrCast(Object obj) {
        if (!obj.getClass().isArray()) {
            throw new IllegalArgumentException
                ("Expected an array, but found: " + obj.getClass().getName());
        }
        
        try {
            // Try to cast to Object[]
            return (Object[]) obj;
            
        } catch (ClassCastException e) {
            // If it fails (only in case of a 1D primative array),
            // Create a new Object[] and copy the values of obj.
            int length = Array.getLength(obj);
            Object[] objArr = new Object[length];
            for (int i = 0; i < length; i++) {
                objArr[i] = Array.get(obj, i);
            }
            
            return objArr;
        }
    }
    
}