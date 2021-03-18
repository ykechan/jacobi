# Introduction to Support Vector Machine

Support Vector Machines (SVMs) are based on a very simple idea: cut a space in half such that it separates the data 
as far as possible. However, training a SVM is much more involved than one would expect. General quadratic programming 
algorithms are notoriously difficult to comprehend, let alone implement. And even with one given the shear size (~1000) 
of common problems it is not efficient to simply pack the whole she-bang into the QP library.

Two algorithms which are specifically designed to train SVMs are to be included in this library: PEGASOS and SMO. 
This document serves as a technical document on the explanation of the implementations of these algorithms.

## Problem formulation

Given a set of data {x<sub>i</sub>, y<sub>i</sub>}, y<sub>i</sub> &isin; {-1, 1}, and an inner product function
&lt;.,.&gt;: R<sup>n</sup> x R<sup>n</sup> -&gt; R, a SVM classifies an instance x if 

>&lt;w, x&gt; &gt; b, where w is the coefficient vector, and b is some bias term. 

Also, w and b are the arguments that minimize the Hinge-Loss function:

>L(w, b) = &sum;max(0, 1 - y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b)).

The max function excludes instances that are easily classified, and focus on those that are difficult.
If the inner product is the simple dot-product, then it is a hyperplane with w being the normal vector. 
The expression

>&lt;w, x<sub>i</sub>&gt; - b

is the distance between x and the hyperplane, and the unit is measured by the norm of the normal vector.
The longer the normal vector, the closer x needs to get to the hyperplane to have the value of the
above expression lesser than one. Not only would we want to correctly classify most instances, but we want 
the margin as wide as possible. Thus a regulation term is included:

>L(w, b) = &lambda;||w||<sup>2</sup> + &sum;max(0, 1 - y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b)).
>where &lambda; is the strength of regulation.

Definitions may vary from text to text, e.g. adding a &frac12; to the ||w|| for canceling out in differentiation,
or putting the strength coefficient to the sum. However they are inter-changable by scaling. For the time being
L refers to the simplest version defined above.

## Sub-Gradient method: Primal Estimated sub-GrAdient SOlver algorithm for SVM (PEGASOS)

Most texts would start by discussing about primal and dual problem, and goes no-where after reaching the dual problem.
This is saved for later. Without going in depth in convex optimization, there is a surprisingly simple algorithm 
to solve this problem: Stochastic gradient descent. 

Stochastic gradient descent, in its core, is a very simple algorithm: Go in opposite direction of the gradient vector.
For each training instance, stride a small step, until the step is so small it stops. However, the Hinge-Loss function
is not differentiable by the max function. This stops everybody from trying for more than a decade, and in 2010, it
was discovered that it doesn't matter: just put a zero as derivative at that position and call it sub-gradient:

> &nabla;L(w, b) = 2&lambda;w - &sum;&delta;<sub>i</sub>y<sub>i</sub>&nabla;&lt;w, x&gt;
> where &delta;<sub>i</sub> = 1 if y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) < 1 and 0 otherwise

In stochastic gradient descent, the coefficients are updated instance by instance, so the sum is gotten rid of. The
PEGASOS papers suggests a learning rate:

> &eta;<sub>t</sub> = 1/t&lambda;, where t is the number of instance done

And the sequence thus becomes

> w* = w - &eta;<sub>t</sub>&nabla;L(w, b) = (1 - 2/t)w + &eta;<sub>t</sub>&delta;<sub>i</sub>y<sub>i</sub>&nabla;&lt;w, x&gt;

PEGASOS iterates a fixed number of times, each time randomly picks an instance and calls it a day. Some sources
iterates all instances in order, an educated guess would be the randomness in order helps avoid some close cycle
in traversing the search space.

In the PEGASOS the 2 is gotten rid of, and here it is reserved for full favour.

Wait, but what about the bias term b? Here is a little bit of trouble. When compute the derivative w.r.t. b similarly,

> b* = b - &eta;<sub>t</sub>L'(w, b) = b - &eta;<sub>t</sub>&delta;<sub>i</sub>y<sub>i</sub>.

It is completely independent of w, and in &eta;<sub>t</sub> in way larger than in standard stochastic gradient descent,
it actually cause b to grow uncontrollably. This symptom is not mentioned in the original paper, only the remedies:
 
1. Treat it as the same as an element in w, just like using the kernel trick.
    - This cause the objective function to slightly change to include b<sup>2</sup> in the norm term.
    - Everything stays the same, seems to work, doesn't seem to matter
    
2. It is vague in the original paper, but I think it refers to times a 1/n factor when updating b. 
    - It seems to work.
    - Provides little explanation mathematically: why update w needs no such factor?
    
3. Execute in mini-batch, and update b to the optimal value in the batch.
    - Complicate things

4. Fix b. Find w that has minimize the objective function. Loop over.
    - Doesn't seems to have a clever way to search for b.
    - Complicate things
    
All things considered, I would select remedy 1: it is simple and it works. Afterall, the main advantage of PEGASOS is that
it is simple.

However the problem with PEGASOS is it is non-deterministic, and doesn't have a clear-cut on how many iterations 
should be enough.

*Sidenote: The acronym should be sent to AAAAA (American Association Against Acronym Abuse).*

## Primal and Dual problem 

For an estimation, PEGASOS works fine. But if a static, global optimal solution is needed then there is a whole lot
of trouble to go through.

Let's go back to the original problem, formulated slightly differently:

> min &frac12;||w||<sup>2</sup> subject to y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) = 1 &forall; i

This forces all instances to be identified correctly, which in most cases there is no solution. A tolerance
error term is thus added to such instance, and include the error terms in the minimization, yielding:

> min &frac12;||w||<sup>2</sup> + C&sum;&xi;<sub>i</sub> 
> subject to y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) + &xi;<sub>i</sub> >= 1, &xi;<sub>i</sub> >= 0

If x is considered too easy, i.e. it scores higher than 1, &xi; = 0. As &xi; increases x tends to have errors, just
like using the max function, and when &xi; > 1 x is wrongly classified.

Combining the objective function with constraints to Lagrangian using Lagrange multipliers:

> L = &frac12;||w||<sup>2</sup> + C&sum;&xi;<sub>i</sub> - &sum;&alpha;<sub>i</sub>(&delta;<sub>i</sub> + &xi;<sub>i</sub>) - &sum;&beta;<sub>i</sub>&xi;<sub>i</sub>
> where &delta;<sub>i</sub> = y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) - 1

A solution to the problem must satisfies the KKT conditions:

- Stationarity
> &nabla;L = 0 w.r.t. w, b, and &xi;

- Primal feasibility
> &delta;<sub>i</sub> + &xi;<sub>i</sub> >= 0, &xi;<sub>i</sub> >= 0

- Complementarity
> &alpha;<sub>i</sub>[&delta;<sub>i</sub> + &xi;<sub>i</sub>] = 0, 
> &beta;<sub>i</sub>&xi;<sub>i</sub> = 0

By Stationarity,
> &nabla;L<sup>(w)</sup> = w - &sum;&alpha;<sub>i</sub>y<sub>i</sub>&nabla;&lt;w, x&gt; = 0

> w = &sum;&alpha;<sub>i</sub>y<sub>i</sub>&nabla;&lt;w, x&gt;

> &nabla;L<sup>(b)</sup> = &sum;&alpha;<sub>i</sub>y<sub>i</sub> = 0

> &nabla;L<sup>(&xi;)</sup> = C - &alpha;<sub>i</sub> - &beta;<sub>i</sub> = 0, 
> &alpha;<sub>i</sub> + &beta;<sub>i</sub> = C

The dual problem can be obtained by substitution:

> ||w||<sup>2</sup> = w &middot; w 
 
> = (&sum;&alpha;<sub>i</sub>y<sub>i</sub>&nabla;&lt;w, x&gt;)<sup>2</sup>
> = (&sum;&alpha;<sub>i</sub>&alpha;<sub>j</sub>
y<sub>i</sub>y<sub>j</sub>
&nabla;&lt;w, x<sub>i</sub>&gt;&nabla;&lt;w, x<sub>j</sub>&gt;)

> &sum;&alpha;<sub>i</sub>(&delta;<sub>i</sub> + &xi;<sub>i</sub>) + &sum;&beta;<sub>i</sub>&xi;<sub>i</sub>  
> = &sum;[&alpha;<sub>i</sub>&delta;<sub>i</sub> + (&alpha; + &beta;)&xi;<sub>i</sub>]
> = &sum;&alpha;<sub>i</sub>&delta;<sub>i</sub> + C&sum;&xi;<sub>i</sub>

> &sum;&alpha;<sub>i</sub>&delta;<sub>i</sub>  
> = &sum;&alpha;<sub>i</sub>[y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) - 1]  
> = &sum;&alpha;<sub>i</sub>y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) - &sum;&alpha;<sub>i</sub>  
> = &sum;&alpha;<sub>i</sub>y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; - b&sum;&alpha;<sub>i</sub>y<sub>i</sub> -
&sum;&alpha;<sub>i</sub>  
> = &sum;&alpha;<sub>i</sub>y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; - &sum;&alpha;<sub>i</sub>,
&because; L<sup>(b)</sup> = &sum;&alpha;<sub>i</sub>y<sub>i</sub> = 0  
> = &sum;&alpha;<sub>i</sub>y<sub>i</sub>&lt; &sum;&alpha;<sub>i</sub>y<sub>i</sub>
&nabla;&lt;w, x<sub>j</sub>&gt;, x<sub>i</sub>&gt; -
&sum;&alpha;<sub>i</sub>  
> = &sum;&alpha;<sub>i</sub>&alpha;<sub>j</sub>y<sub>i</sub>y<sub>j</sub>&lt;&nabla;&lt;w, x<sub>j</sub>&gt;
, x<sub>i</sub>&gt; - &sum;&alpha;<sub>i</sub>  

If &lt;., .&gt; is simple dot product, 

> &lt;&nabla;&lt;w, x<sub>j</sub>&gt;, x<sub>i</sub>&gt; = x<sub>i</sub>&middot;x<sub>j</sub> = 
&nabla;&lt;w, x<sub>i</sub>&gt;&nabla;&lt;w, x<sub>j</sub>&gt;

For other inner product, it actually needs some more justifications, which is omitted here.

>&therefore; &sum;&alpha;<sub>i</sub>&delta;<sub>i</sub> 
> = &frac12;(&sum;&alpha;<sub>i</sub>&alpha;<sub>j</sub>
y<sub>i</sub>y<sub>j</sub>
&nabla;&lt;w, x<sub>i</sub>&gt;&nabla;&lt;w, x<sub>j</sub>&gt;) - &sum;&alpha;

> &therefore; L = &frac12;(&sum;&alpha;<sub>i</sub>&alpha;<sub>j</sub>
y<sub>i</sub>y<sub>j</sub>
&nabla;&lt;w, x<sub>i</sub>&gt;&nabla;&lt;w, x<sub>j</sub>&gt;) + 
C&sum;&xi;<sub>i</sub> - &sum;&alpha;<sub>i</sub>&delta;<sub>i</sub> - C&sum;&xi;<sub>i</sub>  
> = &frac12;(&sum;&alpha;<sub>i</sub>&alpha;<sub>j</sub>
y<sub>i</sub>y<sub>j</sub>
&nabla;&lt;w, x<sub>i</sub>&gt;&nabla;&lt;w, x<sub>j</sub>&gt;) - 
&sum;&alpha;<sub>i</sub>y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; + &sum;&alpha;<sub>i</sub>  
> = &sum;&alpha;<sub>i</sub> - &frac12;(&sum;&alpha;<sub>i</sub>&alpha;<sub>j</sub>
y<sub>i</sub>y<sub>j</sub>
&nabla;&lt;w, x<sub>i</sub>&gt;&nabla;&lt;w, x<sub>j</sub>&gt;).

Finally, the dual problem is derived:

> max L(&alpha;) = &sum;&alpha;<sub>i</sub> - &frac12;&sum;&alpha;<sub>i</sub>&alpha;<sub>j</sub>
y<sub>i</sub>y<sub>j</sub>
&nabla;&lt;w, x<sub>i</sub>&gt;&nabla;&lt;w, x<sub>j</sub>&gt;  
> subject to &alpha;<sub>i</sub> &isin; [0, C], &sum;&alpha;<sub>i</sub>y<sub>i</sub> = 0.

Whew. That is the whole gory details of arriving in the dual lagrangian. The above is based on [this reference](http://fourier.eng.hmc.edu/e176/lectures/ch9/node8.html), though the full substitution is missing. 

Some observations: 

If &alpha;<sub>i</sub> = 0, &beta;<sub>i</sub> = C, &xi;<sub>i</sub> = 0,
&therefore; &delta;<sub>i</sub> &gt;= 0, by Primal feasibility.

If &alpha;<sub>i</sub> = C, &beta;<sub>i</sub> = 0,
&therefore; &delta;<sub>i</sub> = -&xi;<sub>i</sub> &lt;= 0 by Complementarity

If &alpha;<sub>i</sub> &isin; (0, C), &delta;<sub>i</sub> = -&xi;<sub>i</sub>.
&because; &beta;<sub>i</sub> = C - &alpha;<sub>i</sub> &gt; 0, &xi;<sub>i</sub> = 0.
&therefore; &delta;<sub>i</sub> = 0.

## Sequential Minimal Optimization Algorithm

The SMO algorithm is an efficient algorithm of solving the above optimization problem without
using QP algorithms such as interior point methods. If all &alpha;<sub>i</sub> can be found that
satisfy the KKT conditions:

> &alpha;<sub>i</sub> = 0 -&gt; y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) >= 1  
> &alpha;<sub>i</sub> = C -&gt; y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) <= 1  
> &alpha;<sub>i</sub> &isin; (0, C) -&gt; y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b) = 1  
> &sum;&alpha;<sub>i</sub>y<sub>i</sub> = 0  

Then a solution is found:
> w = &sum;&alpha;<sub>i</sub>y<sub>i</sub>x<sub>i</sub>  
> b = E({ y<sub>i</sub> - &lt;w, x<sub>i</sub>&gt;}), where E(.) is the expected value

In each step, SMO optimizes &alpha;<sub>i</sub> and &alpha;<sub>j</sub> and fixes all other &alpha;s.

&because; &sum;&alpha;<sub>n</sub>y<sub>n</sub> = 0,
&therefore; &alpha;<sub>i</sub>y<sub>i</sub> + &alpha;<sub>j</sub>y<sub>j</sub> =
-&sum;<sub>n != i, j</sub>&alpha;<sub>n</sub>y<sub>n</sub>

> &alpha;<sub>i</sub> + &alpha;<sub>j</sub>y<sub>ij</sub>=-&sum;<sub>n != i, j</sub>&alpha;<sub>n</sub>y<sub>ni</sub>  
> where double subscript refers to multiplicative of the two terms with subscript

Since the RHS is independent of &alpha;<sub>i</sub> and &alpha;<sub>j</sub>, it stays constant after the update.

If y<sub>ij</sub> = 1, &alpha;<sub>i</sub> + &alpha;<sub>j</sub> = some constant A

Therefore if &alpha;^<sub>i</sub> = 0, &alpha;^<sub>j</sub> = A  
If &alpha;^<sub>i</sub> = C, &alpha;^<sub>j</sub> = A - C  
WLOG, &alpha;^<sub>i</sub> &isin; [max(0, A - C), min(C, A)]  

If y<sub>ij</sub> = 1, &alpha;<sub>i</sub> - &alpha;<sub>j</sub> = some constant B

Therefore if &alpha;^<sub>i</sub> = 0, &alpha;^<sub>j</sub> = -B  
If &alpha;^<sub>i</sub> = C, &alpha;^<sub>j</sub> = C - B  
WLOG, &alpha;^<sub>i</sub> &isin; [max(0, -B), min(C, C - B)]  

If the updates goes out of these bounds, it is truncated to the closest bound.

Consider the Lagrangian,

> L = &alpha;<sub>i</sub> + &alpha;<sub>j</sub> - 
&frac12;&sum;&alpha;<sub>nn</sub>y<sub>nn</sub>x<sub>nn</sub> -
&sum;<sub>u != v</sub>&alpha;<sub>uv</sub>y<sub>uv</sub>x<sub>uv</sub>  
> = &sum;&alpha;<sub>i</sub> - 
&frac12;(&alpha;<sub>ii</sub>x<sub>ii</sub> + &alpha;<sub>jj</sub>x<sub>jj</sub>) -
&alpha;<sub>ij</sub>y<sub>ij</sub>x<sub>ij</sub> -
&sum;<sub>n != i, j</sub>(&alpha;<sub>ni</sub>y<sub>ni</sub>x<sub>ni</sub> + 
&alpha;<sub>nj</sub>y<sub>nj</sub>x<sub>nj</sub>)  

> &because; w = &sum;&alpha;<sub>i</sub>y<sub>i</sub>x<sub>i</sub>,
> &sum;<sub>n != i, j</sub>&alpha;<sub>nk</sub>y<sub>nk</sub>x<sub>nk</sub>=
&alpha;<sub>k</sub>y<sub>k</sub>&lt;w, x<sub>k</sub>&gt; - &alpha;<sub>ik</sub>y<sub>ik</sub>x<sub>ik</sub>-
&alpha;<sub>jk</sub>y<sub>jk</sub>x<sub>jk</sub> 

>&therefore; &sum;<sub>n != i, j</sub>(&alpha;<sub>ni</sub>y<sub>ni</sub>x<sub>ni</sub> + 
&alpha;<sub>nj</sub>y<sub>nj</sub>x<sub>nj</sub>)  
>= &alpha;<sub>i</sub>y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; - &alpha;<sub>ii</sub>y<sub>ii</sub>x<sub>ii</sub>-
&alpha;<sub>ij</sub>y<sub>ij</sub>x<sub>ij</sub> +
&alpha;<sub>j</sub>y<sub>j</sub>&lt;w, x<sub>j</sub>&gt; - &alpha;<sub>ij</sub>y<sub>ij</sub>x<sub>ij</sub>-
&alpha;<sub>jj</sub>y<sub>jj</sub>x<sub>jj</sub>  
>= &alpha;<sub>i</sub>y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; +
&alpha;<sub>j</sub>y<sub>j</sub>&lt;w, x<sub>j</sub>&gt;-
&alpha;<sub>ii</sub>x<sub>ii</sub>-
&alpha;<sub>jj</sub>x<sub>jj</sub>-
2&alpha;<sub>ij</sub>y<sub>ij</sub>x<sub>ij</sub>

Put a = &alpha;<sub>j</sub>, y = y<sub>ij</sub>, &alpha;<sub>i</sub>=A - ya for some constant A
>&sum;<sub>n != i, j</sub>(&alpha;<sub>ni</sub>y<sub>ni</sub>x<sub>ni</sub> + 
&alpha;<sub>nj</sub>y<sub>nj</sub>x<sub>nj</sub>)  
>= (A - ya)y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; + ay<sub>j</sub>&lt;w, x<sub>j</sub>&gt;-
(A - ya)<sup>2</sup>x<sub>ii</sub>-
a<sup>2</sup>x<sub>jj</sub>-
2a(A - ya)yx<sub>ij</sub>  
>= (A - ya)y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; + ay<sub>j</sub>&lt;w, x<sub>j</sub>&gt;-
(A<sup>2</sup> -2Aya + y<sup>2</sup>a<sup>2</sup>)x<sub>ii</sub>-
a<sup>2</sup>x<sub>jj</sub>-
(2Aa - 2ya<sup>2</sup>)yx<sub>ij</sub>  
>= (A - ya)y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; + ay<sub>j</sub>&lt;w, x<sub>j</sub>&gt;- [
(A<sup>2</sup> -2Aya + y<sup>2</sup>a<sup>2</sup>)x<sub>ii</sub> +
a<sup>2</sup>x<sub>jj</sub> +
(2Aa - 2ya<sup>2</sup>)yx<sub>ij</sub>  ]  
>= (A - ya)y<sub>i</sub>&lt;w, x<sub>i</sub>&gt; + ay<sub>j</sub>&lt;w, x<sub>j</sub>&gt;- [
(x<sub>ii</sub> + x<sub>jj</sub> - 2x<sub>ij</sub>)a<sup>2</sup>
-2Ay(x<sub>ii</sub> - x<sub>ij</sub>)a
+A<sup>2</sup>x<sub>ii</sub>]  
>= y<sub>j</sub>(&lt;w, x<sub>j</sub>&gt; - &lt;w, x<sub>i</sub>&gt;)a - [
(x<sub>ii</sub> + x<sub>jj</sub> - 2x<sub>ij</sub>)a<sup>2</sup> -
2Ay(x<sub>ii</sub> - x<sub>ij</sub>)a] + constants

Isolating terms related to a in L, yields
>L(a) = (A - ya) + a - &frac12;[
(A - ya)<sup>2</sup>x<sub>ii</sub> + a<sup>2</sup>x<sub>jj</sub> + 2a(A - ya)yx<sub>ij</sub>
] - &sum;<sub>n != i,j</sub>...  
> = (1 - y)a - &frac12;[
(A<sup>2</sup> - 2yAa + a<sup>2</sup>)x<sub>ii</sub> +
a<sup>2</sup>x<sub>jj</sub> +
(2Aa - 2ya<sup>2</sup>)yx<sub>ij</sub>
] - &sum;<sub>n != i,j</sub>... + constants  
> = (1 - y)a - &frac12;[
(x<sub>ii</sub> + x<sub>jj</sub> - 2x<sub>ij</sub>)a<sup>2</sup> -
2Ay(x<sub>ii</sub> - x<sub>ij</sub>)a
] - &sum;<sub>n != i,j</sub>... + constants  
> = (1 - y)a - &frac12;
(x<sub>ii</sub> + x<sub>jj</sub> - 2x<sub>ij</sub>)a<sup>2</sup> +
Ay(x<sub>ii</sub> - x<sub>ij</sub>)a -
&sum;<sub>n != i,j</sub>... + constants  
> = (1 - y)a + &frac12;
(x<sub>ii</sub> + x<sub>jj</sub> - 2x<sub>ij</sub>)a<sup>2</sup> +
Ay(x<sub>ii</sub> - x<sub>ij</sub> - 2x<sub>ii</sub> + 2x<sub>ij</sub>)a -
y<sub>j</sub>(&lt;w, x<sub>j</sub>&gt; - &lt;w, x<sub>i</sub>&gt;)a + constants  
> = (1 - y)a + &frac12;
(x<sub>ii</sub> + x<sub>jj</sub> - 2x<sub>ij</sub>)a<sup>2</sup> -
Ay(x<sub>ii</sub> - x<sub>ij</sub>)a -
y<sub>j</sub>(&lt;w, x<sub>j</sub>&gt; - &lt;w, x<sub>i</sub>&gt;)a + constants  

Consider 1 - y - y<sub>j</sub>(&lt;w, x<sub>j</sub>&gt; - &lt;w, x<sub>i</sub>&gt;)
> 1 - y - y<sub>j</sub>(&lt;w, x<sub>j</sub>&gt; - &lt;w, x<sub>i</sub>&gt;)  
> = y<sub>j</sub>(y<sub>j</sub> - y<sub>i</sub> - &lt;w, x<sub>j</sub>&gt; + &lt;w, x<sub>i</sub>&gt;)  
> = y<sub>j</sub>[(&lt;w, x<sub>i</sub>&gt; - y<sub>i</sub>) - (&lt;w, x<sub>j</sub>&gt; - y<sub>j</sub>)]  
> = y<sub>j</sub>(&Delta;<sub>i</sub> - &Delta;<sub>j</sub>),  
> where &Delta;<sub>i</sub> = &lt;w, x<sub>i</sub>&gt; - b - y<sub>i</sub>, the bias term cancelled out in the above expression

Differentiate L w.r.t a, setting L' = 0 yields

> &alpha;<sub>j</sub>\* = Ay(x<sub>ij</sub> - x<sub>ii</sub>))/&eta; + y<sub>j</sub>(&Delta;<sub>j</sub> - &Delta;<sub>i</sub>)/&eta;  
> where &eta; = 2x<sub>ij</sub> - x<sub>ii</sub> - x<sub>jj</sub>

WLOG,
> &alpha;<sub>i</sub>\* = Ay(x<sub>ij</sub> - x<sub>jj</sub>))/&eta; + y<sub>i</sub>(&Delta;<sub>i</sub> - &Delta;<sub>j</sub>)/&eta;   
> = Ay(x<sub>jj</sub> - x<sub>ij</sub>))/&eta; - y<sub>i</sub>(&Delta;<sub>j</sub> - &Delta;<sub>i</sub>)/&eta;   

All sources I could find including the [original paper](https://www.microsoft.com/en-us/research/uploads/prod/1998/04/sequential-minimal-optimization.pdf), [here](http://fourier.eng.hmc.edu/e176/lectures/ch9/node9.html) or [here](http://cs229.stanford.edu/materials/smo.pdf) uses 

> &alpha;<sub>j</sub>\* = &alpha;<sub>j</sub> - y<sub>j</sub>(&Delta;<sub>i</sub> - &Delta;<sub>j</sub>)/&eta;  

Making  Ay(x<sub>ij</sub> - x<sub>ii</sub>))/&eta; = &alpha;<sub>j</sub>. I have no justification of this. For now I would
follow the sources and move up, and maybe come back to check what went wrong in the derivation above later.

The normal vector can be updated incrementally:

> w\* = &sum;&alpha;\*<sub>i</sub>y<sub>i</sub>x<sub>i</sub>  
> =  &sum;&alpha;<sub>i</sub>y<sub>i</sub>x<sub>i</sub> + &Delta;&alpha;<sub>i</sub>y<sub>i</sub>x<sub>i</sub> +
&Delta;&alpha;<sub>j</sub>y<sub>j</sub>x<sub>j</sub>  
> = w + &Delta;&alpha;<sub>i</sub>y<sub>i</sub>x<sub>i</sub> +
&Delta;&alpha;<sub>j</sub>y<sub>j</sub>x<sub>j</sub>
