on 2000 docs
-------------------------------
with 
events-stream size : 1 * 1024 * 1024 * 1024
consumers : 3

initial offset start - 21-24ms
--------------------------------

```

------------------------------------------------------------------------
|                                  |         |   *2  |  *4
| consumer#PRODUCT_RELEASED        | 22 ms   |   24  |  27
| consumer#PRODUCT_DOWNLOAD_EVENT  | 22 ms   |   24  |  27
| consumer#PRODUCT_NOT_FOUND_EVENT | 23 ms   |   21  |  26
-------------------------------------------------------------------------

```

after failure on consumer - start from the last index/ time to seek the offset
-----------------------------

```

-----------------------------------
           | *1    | *2 | *4 |
consumer#0 | 39 ms | 39 | 51 |
consumer#1 | 39 ms | 40 | 50 |
consumer#2 | 39 ms | 41 | 56 |
----------------------------------

```

