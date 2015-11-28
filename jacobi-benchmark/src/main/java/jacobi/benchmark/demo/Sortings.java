/*
 * Copyright (C) 2015 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jacobi.benchmark.demo;

import java.util.Arrays;

/**
 * Benchmark to compare different sorting algorithm with Java's.
 * This is an example of how to build a simple benchmark.
 * 
 * Each algorithm would sort an array of n Strings. String comparison would be
 * done without shortcut as to prevent some always doing easy problem.
 * 
 * @author Y.K. Chan
 */
public class Sortings {
    
    private static String[] generate(int n, int len) {
        String[] array = new String[n];
        StringBuilder buf = new StringBuilder();
        buf.ensureCapacity(len);
        int max = n * len;
        int k = 0;
        for(int i = 0; i < max; i++){
            int j = (int) Math.floor(Math.random() * BASE64.length());
            buf.append(BASE64.charAt(j));
            if(i + 1 % len == 0){
                array[k++] = buf.toString();
                buf.delete(0, buf.length());
            }
        }
        return array;
    }
    
    private static int compare(String a, String b) {
        int cmp = 0;
        int n = Math.min(a.length(), b.length());
        for(int i = 0; i < n; i++){
            char chA = a.charAt(i);
            char chB = b.charAt(i);
            int charCmp = (chA < chB)? -1 : (chA > chB)? 1 : 0;
            if(cmp == 0){
                cmp = charCmp;
            }
        }
        return cmp;
    }
    
    private static String[] bubbleSort(String[] array) {
        for(int i = 0; i < array.length; i++){
            int n = i - 1;
            for(int j = 0; j < n; j++){
                if(compare(array[j], array[j + 1]) < 0){
                    swap(array, j, j + 1);
                }
            }
        }
        return array;
    }
    
    private static String[] mergeSort(String[] array) {
        mergeSort(array, new String[array.length], 0, array.length);
        return array;
    }
    
    private static void mergeSort(String[] array, String[] buf, int begin, int end) {
        if(end - begin < 2){
            return;
        }
        int mid = (begin + end) / 2;
        mergeSort(array, buf, begin, mid);
        mergeSort(array, buf, mid, end);
        System.arraycopy(array, begin, buf, begin, end);
        int i = begin;
        int j = mid;
        int k = begin;
        while(i < mid && j < end){
            int cmp = compare(buf[i], buf[j]);
            switch(cmp){
                case -1:
                    array[k++] = buf[i++];
                    break;
                case 0 :
                    array[k++] = buf[i++];
                    break;
                case 1 :
                    array[k++] = buf[j++];
                    break;
                default :
                    throw new IllegalStateException();
            }
        }
        for(int p = i; p < mid; p++){
            array[k++] = buf[p];
        }
        for(int p = j; p < end; p++){
            array[k++] = buf[p];
        }
        return;
    }
    
    private static String[] javaSort(String[] array) {
        Arrays.sort(array, Sortings::compare);
        return array;
    }
    
    private static void swap(String[] array, int i, int j) {
        if(i != j){
            String temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Base-64 characters.
     */
    private static final String BASE64 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
}
