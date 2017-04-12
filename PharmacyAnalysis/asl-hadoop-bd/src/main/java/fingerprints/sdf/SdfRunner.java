package fingerprints.sdf;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openscience.cdk.exception.CDKException;

import fingerprints.SDFRunnerWholeFile;
import fingerprints.ScaffoldGenerator;


public class SdfRunner extends Configured implements Tool {

	

	public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new SdfRunner(), args);
        System.exit(res);
    }
	
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
	
			  
			
			/*  // Create a new JobConf
			     JobConf job = new JobConf(new Configuration(), FingerMapperRunner.class);
			     
			     // Specify various job-specific parameters     
			     job.setJobName("myjob");
			     
			     MultipleInputs.addInputPath(job,new Path(args[0]), TextInputFormat.class);
			     FileOutputFormat.setOutputPath(job, new Path(args[1]));
			     
			     job.setMapperClass(FingerMapperRunner.FingerMapper.class);
			     job.setCombinerClass(MyJob.MyReducer.class);
			     job.setReducerClass(MyJob.MyReducer.class);
			     
			     job.setInputFormat(SequenceFileInputFormat.class);
			     job.setOutputFormat(SequenceFileOutputFormat.class);
			 
			  
			  
			  */
			  
			  
			    Configuration conf =  getConf();
			    
			    
			   // conf.set("yarn.nodemanager.resource.memory-mb", "7000");
			   // conf.set("yarn.scheduler.maximum-allocation-mb", "2048");
			   // conf.set("yarn.scheduler.minimum-allocation-vcores", "3");
			    //conf.set("mapred.tasktracker.map.tasks.maximum", "3");
			    //conf.set("mapreduce.job.maps", "70");
			    //conf.set("mapreduce.map.memory.mb", "1200");
			    //conf.set("mapreduce.map.java.opts.max.heap", "2500");
			    //conf.set("mapreduce.map.java.opts", "-Xmx1000m");
			    //conf.set("mapreduce.task.timeout", "6000000");
			    //Job job = new Job(new Configuration(), FingerMapperRunner.class);
			    //Job job = Job.getInstance(conf, "word count");
			    //conf.set("mapred.max.split.size", "32870912");
			    
			    Job job = Job.getInstance(conf, "fingerprinter");
			    job.setJarByClass(SdfRunner.class);
			    job.setMapperClass(SDFMapper.class);
			    //job.setCombinerClass(IntSumReducer.class);
			   // job.setReducerClass(IntSumReducer.class);
			    job.setOutputKeyClass(Text.class);
			    job.setOutputValueClass(Text.class);
			    
			    job.setInputFormatClass(SDFInputFormat.class);
			    
			    
			   // MultipleInputs.addInputPath(job,new Path(args[0]), InputFormatSDF.class);
			    SDFInputFormat.addInputPath(job, new Path(args[0]));
			    FileOutputFormat.setOutputPath(job, new Path(args[1]));
			    return job.waitForCompletion(true) ? 0 : 1;
			  }



	
	
		// TODO Auto-generated method stub
	

}
