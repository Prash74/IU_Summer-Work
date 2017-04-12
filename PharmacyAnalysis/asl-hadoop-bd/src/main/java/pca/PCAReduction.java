package pca;



	import java.util.Arrays;
import java.util.LinkedList;

	import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.rdd.RDD;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;

import scala.Tuple2;
import fingerprints.ScaffoldGenerator;

	public class PCAReduction {
	  public static void main(String[] args) {
	    //SparkConf conf = new SparkConf().setAppName("PCAExample").setMaster("local[3]");
	    SparkConf conf = new SparkConf().setAppName("PCAExample");
	    JavaSparkContext sc = new JavaSparkContext(conf);
	    String pathToFingerprints=args[0]; 
	    String output = args[1];
	    //JavaRDD<String> fileRdd = sc.textFile(pathToFingerprints,1000);
	    JavaRDD<String> fileRdd = sc.textFile(pathToFingerprints);
	    
	   // val parsedData = rawData.map(s => Vectors.dense(s.split(',').map(_.toDouble)))
	    
	  //SparkConf conf = new SparkConf().setAppName("Spark").setMaster("local[3]");
	    
	    JavaRDD<Vector> fingers = fileRdd.map(new Function<String,Vector>() {
	    	  public Vector call(String s) { 
	    		  String[]ary=s.split("\\|");
	    		  
	    		  if (ary.length == 2) {
	    		  
	    		  char[]chars=ary[1].toCharArray();
	    		  double[]vals=new double[chars.length];
	    		  
	    		
	    		  System.err.println("ary:"+ary[1]);
	    		  //System.err.println("chars:"+chars[0]);
	    		      		  
	    		  for (int i=0; i< chars.length; i++) {
	    			  vals[i]=Double.valueOf(chars[i]);
	    		  }
	    		  
	    		  Vector  currentRow = Vectors.dense(vals);
	    		  return currentRow; 
	    		  }
	    		  double[]valsdum=new double[1024];
	    		  return Vectors.dense(valsdum);
	    	  }

			
	    	  
	    	});
	    
	    
	  /*  double[][] array = null ;
	    LinkedList<Vector> rowsList = new LinkedList<Vector>();
	    for (int i = 0; i < array.length; i++) {
	      Vector currentRow = Vectors.dense(array[i]);
	      rowsList.add(currentRow);
	    }*/
	    //JavaRDD<Vector> rows = JavaSparkContext.fromSparkContext(sc).parallelize(rowsList);

	    // Create a RowMatrix from JavaRDD<Vector>.
	    RowMatrix mat = new RowMatrix(fingers.rdd());

	    // Compute the top 3 principal components.
	    Matrix pc = mat.computePrincipalComponents(2);
	    RowMatrix projected = mat.multiply(pc);
	    projected.rows().saveAsTextFile(output);
	    //System.err.println("PRINT PROJECTEDDDDDDDDDDDDDDDD");
	    //System.err.println(projected.toString());
	    
	  }
	

}
