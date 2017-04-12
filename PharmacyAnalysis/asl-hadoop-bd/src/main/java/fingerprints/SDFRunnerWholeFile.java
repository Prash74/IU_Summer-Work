package fingerprints;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.openscience.cdk.exception.CDKException;


public class SDFRunnerWholeFile {

	
	public static class FingerMapper extends Mapper<Object, Text, Text, Text>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	 StringBuilder fragfinger = null;
    	/* Path pt=new Path(value.toString());
         FileSystem fs = FileSystem.get(new Configuration());
         BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
         StringBuilder sdf = new StringBuilder();
         String line;
         line=br.readLine();
        
         while (line != null){
                 System.out.println(line);
                 line=br.readLine();
                 sdf.append(line);
         }*/
        try {
        	fragfinger = ScaffoldGenerator.processChemicalEntities(value.toString(), "sdf", true);
        	word.set(fragfinger.toString());
        	System.err.println("wordgGGGGGGGGGGGGGGGGG:"+word);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        context.write(word,word);
      
    }
  }

	
	
		  public static void main(String[] args) throws Exception {
			  
			
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
			  
			  
			    Configuration conf = new Configuration();
			    conf.set("mapreduce.map.memory.mb", "7000");
			    //conf.set("mapreduce.map.java.opts.max.heap", "2500");
			    conf.set("mapreduce.map.java.opts", "-Xmx6000m");
			    conf.set("mapreduce.task.timeout", "6000000");
			    //Job job = new Job(new Configuration(), FingerMapperRunner.class);
			    //Job job = Job.getInstance(conf, "word count");
			    
			    Job job = Job.getInstance(conf, "fingerprinter");
			    job.setJarByClass(SDFRunnerWholeFile.class);
			    job.setMapperClass(SDFRunnerWholeFile.FingerMapper.class);
			    //job.setCombinerClass(IntSumReducer.class);
			   // job.setReducerClass(IntSumReducer.class);
			    job.setOutputKeyClass(Text.class);
			    job.setOutputValueClass(Text.class);
			    
			    job.setInputFormatClass(WholeFileInputFormat.class);
			    
			    
			   // MultipleInputs.addInputPath(job,new Path(args[0]), InputFormatSDF.class);
			    WholeFileInputFormat.addInputPath(job, new Path(args[0]));
			    FileOutputFormat.setOutputPath(job, new Path(args[1]));
			    System.exit(job.waitForCompletion(true) ? 0 : 1);
			  }
	
		// TODO Auto-generated method stub
	

}
