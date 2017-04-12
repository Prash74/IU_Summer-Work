package fingerprints;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.openscience.cdk.exception.CDKException;

import scala.Tuple2;

public class SparkDriver {
	
	
	public static void doSmi(String filePathToSmiDir, String filePathOut, JavaSparkContext sc) {
		
		// partition makes a HUUUUGE difference - if running slow consider modifying it
		JavaRDD<String> fileRdd = sc.textFile(filePathToSmiDir);
		
		JavaRDD<String> trdd = fileRdd.map(new Function <String,String>(){

			public String call(String smilesLine) { 
				
				try {
										
					ScaffoldGenerator sg = new ScaffoldGenerator();
					StringBuilder sb = new StringBuilder();
					try {
						sb = sg.processChemicalEntities(smilesLine, "smi", true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return sb.toString();
				}
				catch (Exception e) {
					System.err.println(e.getMessage());
					return e.getMessage();
				}
			}	
		});
		
		// save return RDD full of bit strings to one big hdfs file, each line is one bit vector string
		trdd.saveAsTextFile(filePathOut);

		
	}
	
	public static void main(String[] args) {

		// hdfs path to all 4K SDF files each with 25K molecules, eg /pubchem
		String filesOfAllPathsToHdfs = args[0];
		String filePathOut = args[1];
		String smiOrSdf = args[2];		

		SparkConf conf = new SparkConf().setAppName("Spark");
		//SparkConf conf = new SparkConf().setAppName("Spark").setMaster("local[3]");
		JavaSparkContext sc   = new JavaSparkContext(conf);
		
		if (smiOrSdf.equals("smi")) {
			System.err.println("DOING SMI DUUUUUUUUUUUUUUUUUUUDE");
			doSmi(filesOfAllPathsToHdfs,  filePathOut,  sc);
		}

		else {
		// read all SDF files under the dir and put into distributed data structure rdd
		// force as many partitions as possible to maximize parallel tasks
		JavaPairRDD<String,String> fileRdd = sc.wholeTextFiles(filesOfAllPathsToHdfs,1000000);
		//fileRdd.repartition(4000);
		
		System.err.println("numberOfPartitionsFineSir:" + fileRdd.partitions().size());
		
		// Apply wordcount Processing on each File received in wholeTextFiles.
		
		JavaRDD<String> trdd = fileRdd.map(new Function<Tuple2<String,String>,String>(){

			public String call(Tuple2<String, String> v1) { 
				
				try {
					String sdf = v1._2();
					
					ScaffoldGenerator sg = new ScaffoldGenerator();
					StringBuilder sb = new StringBuilder();
					try {
						sb = sg.processChemicalEntities(sdf, "sdf", true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return sb.toString();
				}
				catch (Exception e) {
					System.err.println(e.getMessage());
					return e.getMessage();
				}
			}	
		});


		// save return RDD full of bit strings to one big hdfs file, each line is one bit vector string
		trdd.saveAsTextFile(filePathOut);
		
		}


	/*int totalLength = lineLengths.reduce((a, b) -> a + b);
    JavaRDD<String> words = textFile.flatMap(new FlatMapFunction<String, String>() {
    	public Iterable<String> call(String s) {
    		System.out.println(s);
    		return Arrays.asList(s.split(" ")); }
    });*/

	}
}
