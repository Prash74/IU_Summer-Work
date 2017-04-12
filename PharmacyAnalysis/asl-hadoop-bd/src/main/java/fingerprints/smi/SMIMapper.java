package fingerprints.smi;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openscience.cdk.exception.CDKException;

import fingerprints.ScaffoldGenerator;

public  class SMIMapper extends Mapper<Object, Text, Text, Text> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private Text empty = new Text();
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
       
        	 StringBuilder fragfinger = null;
        	 empty.set("");
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
             	fragfinger = ScaffoldGenerator.processChemicalEntities(value.toString(), "smi", true);
             	word.set(fragfinger.toString());
             	//System.err.println("INPUUUUUUUUUUUTTTTTTT" + value);
             	//System.err.println("wordgGGGGGGGGGGGGGGGGG:"+word);
     		} catch (CDKException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
             context.write(word,empty);
       
    }
}
