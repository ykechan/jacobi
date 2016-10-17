         ____.                  ___.   .__ 
        |    |____    ____  ____\_ |__ |__|
        |    \__  \ _/ ___\/  _ \| __ \|  |
    /\__|    |/ __ \\  \__(  <_> ) \_\ \  |
    \________(____  /\___  >____/|___  /__|
                  \/     \/          \/    

  Jacobi Java Matrix Library  vers 1.0.0.SNAPSHOT

#Synopsis
Jacobi is a comprehensive library for computations involving matrices for
Java programmers. The goal of this project is to provide easy to use matrix 
library that complex computational logic at the fingertip of the developer.

This library is still under development.

#Code Example
```java
  Matrix matrix = Matrices.of(new double[][]{ {3.0, 2.0, -1.0}, {2.0, -2.0, 4.0}, {-1.0, 0.5, -1.0} });
  Matrix y = Matrices.of(new double[][]{ {1.0}, {-2.0}, {-2.0} });
  Matrix x = matrix.ext(Solver.class).exact(y); // load Solver extension and solve for x
```

#Functionalities

###Algebra
- Addition, Subtraction, Scalar Multiplication, Matrix Multiplication etc

###Properties
- Trace, Rank, Determintant, Inverse, Tranpose, Eigenvalues etc.

###Decomposition
- Cholesky, Gaussian, QR, Hessenberg, Schur, SVD etc.

###System of linear equations
- Solve for exact value, Linear Regression

###Statistics
- Min, Max, Mean, Standard Deviation, Co-variance etc.

###Kernel Trick
- Insert constant column, insert computed values, select certain columns etc.

#License
Jacobi will be licensed under GNU GPL and shall keep on to be a free software in the 
foreseeable future. 

