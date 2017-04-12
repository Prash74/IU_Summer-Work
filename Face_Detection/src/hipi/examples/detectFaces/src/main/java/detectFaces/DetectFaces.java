package org.hipi.apps.detectFaces;

import java.io.*;
import java.util.*;
import java.net.*;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import org.hipi.image.FloatImage;
import org.hipi.image.HipiImageHeader;
import org.hipi.imagebundle.mapreduce.HibInputFormat;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;


public class DetectFaces extends Configured implements Tool {
  
  public static class DetectFacesMapper extends Mapper<HipiImageHeader, FloatImage, Text, Text> {
    public void map(HipiImageHeader key, FloatImage value, Context context) 
      throws IOException, InterruptedException {

      //Load OpenCV shared library
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
      
      //Get image name from metadata
      String filename = key.getMetaData("source");

      //Initialize classifier
      CascadeClassifier faceDetector = new CascadeClassifier("/home/cc/haarcascade_frontalface_alt.xml");

      //Generate openCV Mat format image from HIPI FloatImage
      Mat imageMat = this.convertFloatImageToOpenCVMat(value);

      //Initialize array of rectangles for face bounding
      MatOfRect faceRects = new MatOfRect();

      //Perform magic here...
      faceDetector.detectMultiScale(imageMat, faceRects);      

      //convert results to array
      Rect[] faceArray = faceRects.toArray();      
    
      //write result as csv 
      String result = String.valueOf(faceArray.length);
 
      for (int i = 0; i < faceArray.length; i++) {

        result = result + ", " + faceArray[i].x;
        result = result + ", " + faceArray[i].y;
        result = result + ", " + faceArray[i].width;
        result = result + ", " + faceArray[i].height;

      }

      context.write(new Text(filename), new Text(result));

    }

    // Thank you to Dinesh Malav for the following function
    // Convert HIPI FloatImage to OpenCV Mat
    public Mat convertFloatImageToOpenCVMat(FloatImage floatImage) {


      // Get dimensions of image
      int w = floatImage.getWidth();
      int h = floatImage.getHeight();


      // Get pointer to image data
      float[] valData = floatImage.getData();


      // Initialize 3 element array to hold RGB pixel average
      double[] rgb = {0.0,0.0,0.0};


      Mat mat = new Mat(h, w, CvType.CV_8UC3);


      // Traverse image pixel data in raster-scan order and update running average
      for (int j = 0; j < h; j++) {
        for (int i = 0; i < w; i++) {
          rgb[0] = (double) valData[(j*w+i)*3+0] * 255.0; // R
          rgb[1] = (double) valData[(j*w+i)*3+1] * 255.0; // G
          rgb[2] = (double) valData[(j*w+i)*3+2] * 255.0; // B
          mat.put(j, i, rgb);
        }
      }

      return mat;

    }

  }
  
  public static class DetectFacesReducer extends Reducer<Text, Text, Text, Text> {
    public void reduce(Text key, Iterable<Text> values, Context context) 
      throws IOException, InterruptedException {

      String result = "";

      for (Text val : values) {
        result = result + val.toString();
      }

      context.write(key, new Text(result));

    }
  }
  
  public int run(String[] args) throws Exception {
    // Check input arguments
    if (args.length != 2) {
      System.out.println("Usage: helloWorld <input HIB> <output directory>");
      System.exit(0);
    }
    
    // Initialize and configure MapReduce job
    Job job = Job.getInstance();
    // Set input format class which parses the input HIB and spawns map tasks
    job.setInputFormatClass(HibInputFormat.class);
    // Set the driver, mapper, and reducer classes which express the computation
    job.setJarByClass(DetectFaces.class);
    job.setMapperClass(DetectFacesMapper.class);
    job.setReducerClass(DetectFacesReducer.class);
    // Set the types for the key/value pairs passed to/from map and reduce layers
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    // Set the input and output paths on the HDFS
    FileInputFormat.setInputPaths(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    // Execute the MapReduce job and block until it complets
    boolean success = job.waitForCompletion(true);
    
    // Return success or failure
    return success ? 0 : 1;
  }

  public static void outputResults(String[] args) throws Exception {

    try{

      String mongoHost = System.getenv("MONGO_HOST");

      if (mongoHost.length() == 0) {
        System.out.println("Environment variable MONGO_HOST not set.  Could not write results to MongoDB.");
        return; 
      }
      int mongoPort = 27017;
      
      String mongoDbName = "results";
      String mongoCollName = "images";

      String nameNode = System.getenv("HDFS_HOST");
      String outputFolder = args[1];

      MongoClient mongoClient = new MongoClient( mongoHost, mongoPort);
      DB db = mongoClient.getDB(mongoDbName);

      DBCollection coll = null;

      boolean collExists = db.collectionExists(mongoCollName);
      if (!collExists) {
        coll = db.createCollection(mongoCollName, null);
      } else {
        coll = db.getCollection(mongoCollName);
      }

      Path pt = new Path("hdfs://" + nameNode + "/user/root/" + outputFolder + "/part-r-00000");
      FileSystem fs = FileSystem.get(new Configuration());
      BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
      String line;
     
      String[] tabs;
      String[] coords;

      line=br.readLine();
      while (line != null){
        //System.out.println(line);

        //split line into file name and coords
        tabs = line.split("\t");
        coords = tabs[1].split(",");

        BasicDBObject doc = new BasicDBObject("filename", tabs[0]).
          append("numFaces", coords[0]);

        for (int ct = 0; ct < (coords.length - 1) / 4; ct++) {

          doc.append("face-" + String.valueOf(ct) + "-x", coords[(ct * 4) + 1]);
          doc.append("face-" + String.valueOf(ct) + "-y", coords[(ct * 4) + 2]);
          doc.append("face-" + String.valueOf(ct) + "-width", coords[(ct * 4) + 3]);
          doc.append("face-" + String.valueOf(ct) + "-height", coords[(ct * 4) + 4]);

        }
				
        coll.insert(doc);         

        line=br.readLine();
      }
    }catch(Exception e){
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
    }
  }
  
  public static void main(String[] args) throws Exception {
    ToolRunner.run(new DetectFaces(), args);
    outputResults(args);
    System.exit(0);
  }
  
}


