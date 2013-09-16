all: Konkordans Ut
	
Konkordans: 
	javac Konkordans.java
Ut:
	./tokenizer < korpus.txt | LC_COLLATE=C sort > ut.txt
