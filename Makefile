all: Konkordans Ut A
	
Konkordans: 
	javac Konkordans.java
Ut:
	/info/adk13/labb1/tokenizer < /info/adk13/labb1/korpus | LC_COLLATE=C sort > /var/tmp/ut.txt

A:
	java Konkordans
