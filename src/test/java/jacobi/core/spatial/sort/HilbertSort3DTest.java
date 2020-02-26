package jacobi.core.spatial.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;


/**
 * This class contains unit tests and code generation for Hilbert Sort 3-D.
 * 
 * <p>A Hilbert Curve 3-D in its simpliest order can be obtained by joining 2 Hilbert curve in 2-D
 * in different dimension and moving direction, for example
 * 
 *      4 ---- 5               4 ---- 5               4      5   
 *       \      \               \                     |\     |\  
 *        \      \               \                    | \    | \ 
 *         6      7               6------7            |  6   |  7
 *                |                      |            |  |   |  |
 *      0 ---- 1  |            0 ---- 1  |            0--|---1  |
 *       \      \ |             \      \ |               |      |
 *        \      \|              \      \|               |      |
 *         2      3               2      3               2      3
 * 
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class HilbertSort3DTest {
}
