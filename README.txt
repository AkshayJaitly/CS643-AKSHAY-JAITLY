AMI NAME : CS643-Akshay-Jaitly-Namenode
AMI ID: ami-ec3bf696
Region: US East(N. Virginia)

#Cloud-Parta has the WordCounter.java code for Part A#
#Cloud-Partb has the SameRank.java code for Part B#

#Data Migration to HDFS#

After downloading the input file (states file), I followed 2 steps to transfer the contents from local machine to HDFS.

a.Copy this data from local to Namenode.
b.Copy this data from Namenode to hdfs.

Use the following commands:

Create a directory called states in Namenode and transfer the states content from local machine to Namenode/states

Namenode$ mkdir ~/states
local$ scp -i ~/.ssh/Cs643-Aks.pem  ~/Downloads/states/* Namenode:~/states/
Namenode$ hdfs dfs -mkdir /states
Namenode$ hdfs dfs -copyFromLocal ~/states/* /states/


Similarly do it for the Word and Rank folders 

Namenode$ mkdir ~/WordCount
local$ scp -i ~/.ssh/Cs643-Aks.pem  ~/Desktop/WordCounter.java Namenode:~/Word/
Namenode$ hdfs dfs -mkdir /Word
Namenode$ hdfs dfs -copyFromLocal ~/WordCount/* /WordCounter/

Namenode$ mkdir ~/Rank
local$ scp -i ~/.ssh/Cs643-Aks.pem  ~/Desktop/SameRank.java Namenode:~/Rank/
Namenode$ hdfs dfs -mkdir /Rank
Namenode$ hdfs dfs -copyFromLocal ~/Rank/* /Rank/




Make a directory called  in HDFS

Namenode$ mkdir ~/states
Namenode$ mkdir ~/WordCount
Namenode$ mkdir ~/Rank

Note : Pre Prep for any prior codes which are on HDFS which may prevent output
#WORDCOUNTER#
Namenode$ source ~/.profile
Namenode$ cd WordCount
Namenode$ rm WordCounter*.class
Namenode$ rm WordCounter.jar
Namenode$ hdfs dfs -rm -r /WordCount/Djob/
Namenode$ hdfs dfs -rm -r /WordCount/Ecount/
Namenode$ hdfs dfs -rm -r /WordCount/Final/


#WordCounter:#

Change the directory to the folder

Namenode$ cd WordCount

Namenode$ source ~/.profile

Compile the program

Namenode$ javac WordCounter.java -cp $(hadoop classpath)

Create a jar file called WordCounter.jar

Namenode$ jar cf WordCounter.jar WordCounter*.class

Run the application program

Namenode$ hadoop jar WordCounter.jar WordCounter /states /Word/Final

To check the output, use the command:

Namenode$ hdfs dfs -cat /WordCount/Ecount/part-r-00000

Namenode$ hdfs dfs -cat /WordCount/Djob/part-r-00000

Namenode$ hdfs dfs –cat /WordCount/Final/part-r-00000


Note : Pre Prep for any prior codes which are on HDFS which may prevent proper output 
#WORDCOUNTER#
Namenode$ source ~/.profile
Namenode$ cd Rank
Namenode$ rm SameRank*.class
Namenode$ rm SameRank.jar
Namenode$ hdfs dfs -rm -r /Rank/m_job/
Namenode$ hdfs dfs -rm -r /Rank/f_count/
Namenode$ hdfs dfs -rm -r /Rank/Final/

#SameRank:#

Change directory to folder

Namenode$ cd Rank

Namenode$ source ~/.profile

Compile the program

Namenode$ javac SameRank.java -cp $(hadoop classpath)

Create a jar file called SameRank.jar

Namenode$ jar cf SameRank.jar SameRank*.class

Run the application program

Namenode$ hadoop jar SameRank.jar SameRank /states /Rank/Final


To check the output, use the command:
Namenode$ hdfs dfs -cat /Rank/f_count/part-r-00000

Namenode$ hdfs dfs -cat /Rank/man_job/part-r-00000

Namenode$ hdfs dfs –cat /Rank/Final/part-r-00000

#Check Images folder for the snapshots as a reference# 