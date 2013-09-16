all: Konkordans Ut A
	
Konkordans: 
	javac Konkordans.java
Ut:
	./tokenizer < korpus.txt | LC_COLLATE=C sort > ut.txt

A:
	java Konkordans
