package org.hipi.apps.detectFaces;

import java.io.IOException;

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

      //Perform Voodoo here...
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
  
  public static void main(String[] args) throws Exception {
    ToolRunner.run(new DetectFaces(), args);
    System.exit(0);
  }
  
}


