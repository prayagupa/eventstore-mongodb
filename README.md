on 2000 docs with 4 consumers
-------------------------------

initial cursor start - 0-9ms
--------------------------------

a) handling last cursor index with scala variable 
---------------------------------------------

```
consumer#2 | 17 ms
consumer#0 | 18 ms
consumer#1 | 18 ms
consumer#3 | 17 ms
```

*2 documents

```
consumer#2 took 22 ms
consumer#0 took 23 ms
consumer#3 took 22 ms
consumer#1 took 23 ms
```

*4

```
consumer#1 took 17 ms
consumer#2 took 17 ms
consumer#3 took 17 ms
consumer#0 took 18 ms
```

* 8 
consumer#3 | 19 ms
consumer#0 | 20 ms
consumer#1 | 19 ms
consumer#2 | 19 ms

* 16

consumer#2 | 16 ms
consumer#3 | 16 ms
consumer#0 | 16 ms
consumer#1 | 16 ms

b) with Mongo collection for last index
-------------

```
consumer#0  took 17 ms
consumer#2  took 17 ms
consumer#1  took 17 ms
consumer#3  took 17 ms
```

after failure on consumer - start from the last index/ time to seek the index
-----------------------------

```
consumer#0  took 22 ms
consumer#1  took 22 ms
consumer#3  took 21 ms
consumer#2  took 21 ms
```

```
consumer#0  took 21 ms.
consumer#2  took 21 ms.
consumer#3  took 21 ms.
consumer#1  took 21 ms.
```

with *2 documents

```
consumer#3 took 28 ms
consumer#1 took 29 ms
consumer#2 took 29 ms
consumer#0 took 29 ms
```

with *4 documents

```
consumer#2 took 32 ms
consumer#0 took 32 ms
consumer#1 took 32 ms
consumer#3 took 31 ms
```

*8

consumer#1 | 23 ms
consumer#3 | 23 ms
consumer#2 | 23 ms
consumer#0 | 23 ms

*16

consumer#1 | 20 ms
consumer#2 | 19 ms
consumer#0 | 20 ms
consumer#3 | 19 ms