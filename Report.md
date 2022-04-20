# CS422 Project 1 - Report

## Task 1: Implement a volcano-style tuple-at-a-time engine
**Feedback: In this task, we aim to deal with the input one by one(pipeline). Yet, in *Sort*, *Join*, and *Aggregate*, we need the whole input set to fulfill the functionality. It is a bit tricky.**  

- Scan: In order to scan through all the tuples, we need a counter to record the rows that we have already seen. 
- Filter: Need to examine whether the tuples pass the predicate or not. 
- Project: Map the tuples on evaluator. It's similar to *Filter*. 
- Sort: Need to read in all the tuples first. All the calculations are done in the **open()** stage. In **next()** stage, just need to output the tuples store in **table_sorted**.
- Join: Need to read in either left tuples or right tuples first and create a HashMap on them. In my case, I create a HashMap on the left tuples in the **open()** stage. In the **next()** stage, I just read the right tuples one at a time and see if it matches the key in the left HashMap. 
- Aggregate: Need to read in all the tuples first and group the aggregate functions by the key indices(from groupSet) in the **open()** stage. Output the result of aggregate in the **next()** stage (tuple by tuple).

## Task 2: Late Materialization (naive)
**Feedback: In this task, the latetuples we are dealing with are similar to the one of Task 1. The only difference is that there is another column for VirtuleID. Thus, most of the implementation is similar to Task one**  

- Stitch: It's doing almost the same thing as **Join** (and it's much easier to implement since there is only one tuple for each VID).   
- Drop: Just need to return the value part (a Tuple structure) of the LateTuple.  
- LateFilter: Doing the same thing as Filter (by only operating on the value part of LateTuple).  
- LateProject: Doing the same thing as Project (by only operating on the value part of LateTuple).  
- LateJoin: Doing the same thing as Join (by only operating on the value part of LateTuple). Yet in LateJoin, we also need to assign a new VID for the returned LateTuples. In this case, I use a counter to record how many LateTuples I have already produced.  
- Fetch: Need to deal with two cases, one with project the other without. Pattern matching is so useful!

## Task 3: Query Optimization Rules
**Feedback: This is the part I found the most confusing. It took me SO MUCH TIME to understand how to use the API. EXTREME TRICKY!**

- LazyFetchRules: Need to change a Stitch to a LogicalFetch. Must be careful of the fetchType (it should be the same type as LateColumnScan)
- LazyFetchProjectRule: Need to change a Stich with Project to a LogicalFetch with Project. Must be careful of the fetchType (it should be the same type as LateColumnScan)
- LazyFetchFilterRule: Need to change a Stitch with Filter with a LogicalFetch plus LogicalFilter. Most be careful about the column indices of Filter, since, after the Fetch, the indices are going to change.

## Task 4: Execution Models
**Feedback: The implementation of this part is mostly similar to Part 1. Just need to understand how to map column indices with tuple values. Also, in this part, we have access to all the input Tuples in the very beginning, which makes it easier when implementing Sort, Join, and Aggregate.**

**Note: The only difference between Operationatatime and Columnatatime is that the former deals with *Column* and the latter deals with *HomogeneousColumn*. Thus, the implementation for the two parts is mostly the same.**

- Scan: Already implemented.
- Filter: After retrieving all the tuples, the implementing logic is the same as Filter in Part1.
- Project: After retrieving all the tuples, the implementing logic is the same as Project in Part1.
- Sort: After retrieving all the tuples, the implementing logic is the same as Sort in Part1. Yet, we don't need to output tuples by tuples. Instead, we can return the Column of the whole **table_sorted** at once.
- Join: After retrieving all the tuples, the implementing logic is the same as Join in Part1. Yet, in this part, using a flatMap to map everything at once and output it, in the end, is way easier than what I've done in Part
- Aggregate: after retrieving all the tuples, the implementing logic is the same as Aggregate in Part1.

---
**Feedback In Total: It is a very complicated project for people who is not familiar with Scala. I spent a lot of time understanding the Scala expression and notation. Also, it is hard to understand the API. Some part of the documentation is not straightforward. It would be nice if the spec and documentation can be made clearer. In total, I think it's this project quite difficult(at least for me...).**

