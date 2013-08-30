all:
	mkdir -p bin
	javac src/chatclient/*.java -d bin
	javac src/chatserver/*.java -d bin

client:
	javac src/chatclient/*.java -d bin

server:
	javac src/chatserver/*.java -d bin

clean:
	rm -rfv bin
