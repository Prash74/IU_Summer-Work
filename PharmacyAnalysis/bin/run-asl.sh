
#git repo - need to clone - https://github.iu.edu/crgessne/sw-project-template/tree/next
# ROOT - dir directly above the git cloned dir

cd $ROOT/sw-project-template/asl-hadoop-bd
input=/test/test.sdf
output=/test/test_fingers_out001
asl_jar=$ROOT/sw-project-template/asl-hadoop-bd/target/asl-hadoop-bd-0.0.1-SNAPSHOT.jar
cdk_jar=$ROOT/sw-project-template/asl-hadoop-bd/lib/cdk-1.5.8.jar

mvn package

cd $ROOT

curl http://ftp.ncbi.nlm.nih.gov/pubchem/Compound/Daily/2016-04-22/SDF/Compound_000050001_000075000.sdf.gz > test.sdf.gz
gunzip test.sdf.gz
hdfs dfs -mkdir /test
hdfs dfs -put test.sdf /test/test.sdf 

echo yarn jar $asl_jar fingerprints.sdf.SdfRunner -D mapred.max.split.size=2000000 -D mapreduce.child.java.opts=-Xmx1700m -D mapreduce.map.memory.mb=1800 -D mapred.reduce.tasks=0 -D mapred.max.map.failures.percent=50 -D mapreduce.job.split.metainfo.maxsize=-1 -D mapreduce.task.timeout=1200000 -libjars $cdk_jar /$input /$output

yarn jar $asl_jar fingerprints.sdf.SdfRunner -D mapred.max.split.size=2000000 -D mapreduce.child.java.opts=-Xmx1700m -D mapreduce.map.memory.mb=1800 -D mapred.reduce.tasks=0 -D mapred.max.map.failures.percent=50 -D mapreduce.job.split.metainfo.maxsize=-1 -D mapreduce.task.timeout=1200000 -libjars $cdk_jar $input $output
