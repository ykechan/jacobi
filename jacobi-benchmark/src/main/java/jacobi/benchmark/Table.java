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

package jacobi.benchmark;

import jacobi.benchmark.core.Result;
import jacobi.benchmark.core.Result.Entry;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Y.K. Chan
 */
public class Table {
    
    public Table(Result result) {
        this.headers = this.getHeaders(result);
        this.rows = this.getData(result);
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<double[]> getRows() {
        return rows;
    }
    
    public void writeCsv(PrintStream out) {
        for(int i = 0; i < this.getHeaders().size(); i++){
            if(i > 0){
                out.print(',');
            }
            out.print(this.getHeaders().get(i));
        }
        out.println();
        
        for(double[] row : this.getRows()){
            for(int i = 0; i < row.length; i++){
                if(i > 0){
                    out.print(',');
                }
                out.print(row[i]);
            }
            out.println();
        }
    }
    
    private List<String> getHeaders(Result result) {
        Set<String> set = new TreeSet<>();
        for(int key : result.keySet()){
            set.addAll(result.getEntries(key).keySet());
        }
        String[] row = new String[set.size() + 1];
        row[0] = "#";
        int k = 1;
        for(String header : set){
            row[k++] = header;
        }
        return Collections.unmodifiableList(Arrays.asList(row));
    }
    
    private List<double[]> getData(Result result) {
        double[][] data = new double[result.keySet().size()][this.headers.size()];
        int k = 0;
        for(int key : result.keySet()){
            Map<String, Entry> entries = result.getEntries(key);
            double[] row = data[k++];
            row[0] = key;            
            for(int i = 1; i < this.headers.size(); i++){
                row[i] = Double.NaN;
                if(entries.containsKey(this.headers.get(i))){
                    row[i] = entries.get(this.headers.get(i)).getMean();
                }
            }
        }
        return Collections.unmodifiableList(Arrays.asList(data));
    }

    private List<String> headers;
    private List<double[]> rows;
}
