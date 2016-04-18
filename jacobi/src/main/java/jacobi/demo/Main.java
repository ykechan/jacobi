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
package jacobi.demo;

import jacobi.api.ext.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 *
 * @author Y.K. Chan
 */
public final class Main { 
    
    private Main() {
        
    }
    
    public static void main(String[] args) {
        new Main().run(System.out);
        return;
    }
    
    private void run(PrintStream out) {
        this.printFile(out, README);
    }
    
    private void printFile(PrintStream out, String path) {
        try(InputStream in = this.getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in))){ 
            String line = null;
            while((line = reader.readLine()) != null){
                out.println(line);
            }
        }catch(IOException ex){
           throw new IllegalStateException("Unable to find classpath:" + path, ex);
        }
    }
    
    private static final String README = "/jacobi/demo/readme.txt";
}
