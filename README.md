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

#Code Example
```java
  Matrix matrix = Matrices.of({ {3.0, 2.0, -1.0}, {2.0, -2.0, 4.0}, {-1.0, 0.5, -1.0} });
  Matrix y = Matrices.of({ {1.0}, {-2.0}, {-2.0} });
  Matrix x = matrix.ext(Solver.class).exact(y); // load Solver extension and solve for x
```

#Motivation
Currently popular matrix library like JAMA is a bit dated if not legacy. I 
want to fill in the gap to provide a library that employs more modern practices
and style, like test-driven development and borderline functional approach.
The goal is to provide Java developers a matrix library that is easy-of-use,
tested, efficient, and extensible. This project introduces a little built-in
Facade framework (matrix.ext(...) in the above code) to separate functionality
and implementation. Switching implementation would be easy and backward 
compatibility archived by switching to new facade altogether if change is great.

#License
Jacobi will be licensed under GNU GPL and shall keep on to be a free software in the 
foreseeable future. 

