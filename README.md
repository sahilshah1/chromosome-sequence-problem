# chromosome-sequence-problem

## Problem Spec
See the original [problem spec](problem_spec.md) for details.

## Required dependencies
* Java JDK: 1.8.0_45 or later
* Gradle: 2.8 or later

## Approach

We are guaranteed this: for every string
`x` in the list of fragments, there exists 1 other string `y` such that
some suffix of `x` (longer than `len(x) / 2`) is equal to some prefix of `y`
(longer than `len(y) / 2`). This is true for all `x`, except for the very
end of the sequence.

In example, here are the unique overlaps in an unordered fragments:
 fragments = [ATTAGACCTG, CCTGCCGGAA, AGACCTGCCG, GCCGGAATAC]
 ATT*AGACCTG AGACCTG*CCG
 AGA*CCTGCCG CCTGCCG*GAA
 CCT*GCCGGAA GCCGGAA*TAC

```
//n == number of fragments  
//m == length of each fragment
List<String> fragments = readFromFastaFile()

//find the beginning of the sequence (O(n^2 * time to match each string))
beginningString = ""
for (String s : fragments) {

    boolean isBeginning = true
    for (String j : fragments) {
        if (j has valid suffix for s) {
            isBeginning = false
        }
        if (isBeginning = true) {
            beginningString = s
        }
    }
}

currentString = isBeginning
List<String> sequenced;

repeat until all n fragments have been added to sequence:(O(n^2 * time to match each string))
    for (String s : fragments) {
        if (s has valid prefix for currentString)
            sequenced.add(currentString)
            currentString = s
    }


return combineSequenceAccordingToOverlaps(sequenced)
```
## Space-Time Complexity

There are 2 expensive operations here:
- the time to find each sequence, which is `O(n^2)`. There maybe a more advanced solution
to improve the asymptotic performance, but for now, I just used threads to divide up the work.
- the time it takes to compare each string for overlaps.
the naive way to do this is `O(m^2)`, but there are faster algorithms
such as `O(m + p)`.

Since `m` is likely to be a lot bigger than `n` (the problem spec says `m` is ~1000 and `n` is ~50),
I spent more time optimizing the time to compare each string.

The submitted solution in the main method is `O(m * n^2)`. The space cost is `O(n)` for the sequenced fragments,
and `O(n*m)` for the LSP efficient KMP string overlap impl, and `O(n^2 + n)` for the parallel implementation. 
So in total `O(n + n*m + n^2)`.

## Code Structure

There are 2 main parts of the program. They're built into
 interfaces so I could easily swap out algorithms and quickly
 see how they performed in my unit and integration tests.

- `StringOverlapAlgorithm.java`: this interface has 1 simple
method that calculates the overlap between 2 Strings. I first
implemented a naive `O(m^2)` solution to get it working, and then
drastically improved it by implementing Knuth-Morris-Pratt, making
the time cost `O(m)` (technically `O(m + p)`, but both strings are more or less
equivalent in length in this problem).

- `Sequencer.java`: this interface takes a `List<String>` of unsequenced FASTA fragments
and returns the sequenced and combined fragment. There is only a naive `O(n^2)` implementation,
for the reasons described above.

The ParallelSequencer divides the work up, causing a big improvement on performance.
As data sets get bigger or smaller, the number of threads should be throttled accordingly,
but I've used fixed thread numbers for now.

Unit and integration tests can be run to verify correctness and eyeball performance.

There are some obvious places to improve performance on the machine, but out of interest of time, I
haven't explored many of them. The ones I have are commented in the code.
