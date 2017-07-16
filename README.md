
# Jacobi Java Matrix Library
  
![Travis Latest Build](https://travis-ci.org/ykechan/jacobi.svg?branch=master)
[![codecov](https://codecov.io/gh/ykechan/jacobi/branch/master/graph/badge.svg)](https://codecov.io/gh/ykechan/jacobi)
[![codebeat badge](https://codebeat.co/badges/d0e7fd78-849d-47ae-aced-e24e52cd6961)](https://codebeat.co/projects/github-com-ykechan-jacobi-master)
![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ykechan/jacobi/badge.svg)
[![Javadocs](https://www.javadoc.io/badge/com.github.ykechan/jacobi.svg)](https://www.javadoc.io/doc/com.github.ykechan/jacobi)

## Synopsis
Jacobi is a comprehensive library for computations involving matrices for
Java programmers. The goal of this project is to provide easy to use matrix 
library with complex computational logic at the fingertip of Java developers.

## Code Example
```java
  Matrix matrix = Matrices.of(new double[][]{ {3.0, 2.0, -1.0}, {2.0, -2.0, 4.0}, {-1.0, 0.5, -1.0} });
  Matrix y = Matrices.of(new double[][]{ {1.0}, {-2.0}, {-2.0} });
  Matrix x = matrix.ext(Solver.class).exact(y); // load Solver extension and solve for x
```

## Functionalities

### Algebra
- [x] Addition
- [x] Subtraction
- [x] Scalar Multiplication
- [x] Matrix Multiplication
- [x] Hadamard Product

### Properties
- [x] Trace
- [x] Rank
- [x] Determintant
- [x] Inverse
- [x] Tranpose
- [x] Eigenvalues
- [x] Singular Values

### Decomposition
- [x] Cholesky
- [x] Gaussian (PLU)
- [x] QR
- [x] Hessenberg
- [x] Schur
- [ ] SVD

### System of linear equations
- [x] Exact solution
- [x] Linear Regression
- [x] Linear Programming

### Statistics
- [x] Min
- [x] Max
- [x] Mean
- [ ] Median
- [x] Variance
- [x] Standard Deviation
- [x] Co-variance

### Kernel Trick
- [x] Insert computed values
- [x] Projection (Select)

## License
Jacobi will be licensed under MIT and shall keep on to be a free software in the 
foreseeable future. 


