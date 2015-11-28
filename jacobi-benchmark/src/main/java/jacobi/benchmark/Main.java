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
import jacobi.benchmark.demo.StringConcat;

/**
 *
 * @author Y.K. Chan
 */
public class Main {
    
    public static void main(String[] args) {
        new Main().run();
        return;
    }

    private void run() {
        Result result = new StringConcat().get();
        new Table(result).writeCsv(System.out);
    }
}
