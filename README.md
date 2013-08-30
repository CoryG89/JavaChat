JavaChat
========

A simple Java chat server and client with swing GUI with a MySQL database
back-end.


Getting the Source
------------------
In order to get the source code either download the compressed package or make
sure you have [Git][git] installed and execute the following:

    git clone https://github.com/CoryG89/JavaChat
    cd JavaChat


Opening in Eclipse
------------------
JavaChat was developed in Eclipse, NetBeans was also using to create the swing
GUI for the client. You should be able to open the project in Eclipse to run
both the server and the client. 


Building JavaChat
-----------------
If you'd rather build JavaChat on the command line you may do so by using make

    make

You can also build the server and client seperately and clean the project as
well.

    make server
    make client
    make clean

You can also use the javac compiler:

    mkdir -p bin
    javac src/chatserver/*.java -d bin
    javac src/chatclient/*.java -d bin


MySQL Dependency
----------------
JavaChat depends on a MySQL back-end, you need to set up the database by
executing the `chatdb.sql` script on your mysql server. The DBManager class
reads the database authentication details from a data file. A sample data file
is included as `dbauth.dat.sample` simply remove the sample extension and edit
the data file by replacing the first and second line with your username and
password respectively.


Running JavaChat
----------------
In order to run the JavaChat server enter the following:

    java -cp "bin;mysql-connector.jar" chatserver/Server

In order to run an instance of the JavaChat client enter the following

    java -cp "bin" chatclient/ChatFrame


