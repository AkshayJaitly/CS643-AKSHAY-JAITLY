/**
 * Created by akshayjaitly on 10/07/17.
 */
import org.apache.hadoop.conf.Configuration;//Hadoop import
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.*;

//Declare class SameRank which identifies the states with same rank
public class SameRank {
    
    // WordMapper Class
	public static class WordMapper extends Mapper<Object, Text, Text, IntWritable>{
        
        private final static IntWritable write = new IntWritable(1);
		private Text word = new Text();
        String[] search = new String[] {"education", "politics", "sports", "agriculture"};
        
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
            String filepath = ((FileSplit) context.getInputSplit()).getPath().toString();

			while (itr.hasMoreTokens()) {

				word.set(itr.nextToken().toLowerCase());

				for(int i = 0; i < search.length; i++) {
					while(word.toString().contains(search[i])) {
						context.write(new Text(filepath + "/" + search[i]), write);
						word.set(word.toString().replaceFirst(search[i], ""));
					}
				}
			}
		}
	}

	// WordReducer Class
	public static class WordReducer extends Reducer<Text,IntWritable,Text,IntWritable> {

		private IntWritable res = new IntWritable();

		// Reduce function.
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			String tmp = key.toString().toLowerCase();

			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			res.set(sum);
			context.write(key, res);
		}
	}

	// ManMapper Class
	public static class ManMapper extends Mapper<Object, Text, Text, Text>{

		private final static IntWritable write = new IntWritable(1);

        private Text word = new Text();

		String[] search = new String[] {"education", "politics", "sports", "agriculture"};

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String[] values = new String[] {"", ""};

			int counter = 0;

			StringTokenizer st = new StringTokenizer(value.toString());
			while (st.hasMoreTokens()) {

				values[counter] = st.nextToken();
				counter += 1;
            
            }
			counter = 0;

			int f_Index = values[0].lastIndexOf("/");
			String state = values[0].substring(0, f_Index);
			String phrase = values[0].substring(f_Index + 1, values[0].length());

    
			StringBuilder sb = new StringBuilder();
			sb.append(phrase).append("-").append(values[1]);

			context.write(new Text(state), new Text(sb.toString()));

		}
	}

	// ManReducer Class
	public static class ManReducer extends Reducer<Text,Text,Text,Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			StringBuilder sb = new StringBuilder();

			for (Text val : values) {
				sb.append(val.toString()).append(",");
			}

			context.write( key, new Text( sb.toString() ) );

		}
	}

	// SMapper Class which sorts
	public static class SMapper extends Mapper<Object, Text, Text, IntWritable>{

		// Map function
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String[] breakdown = value.toString().split("\t");
			String list = breakdown[1];
			String[] rank = list.split(",");

			Map<Integer, String> map = new HashMap<Integer, String>();

			for(int i = 0; i < rank.length; i++) {
				String[] current = rank[i].split("-");
				map.put( Integer.parseInt( current[1] ) , current[0] );
			}

			StringBuilder res = new StringBuilder();
			Map<Integer, String> treeMap = new TreeMap<Integer, String>(map);
			for(Integer val : treeMap.keySet()) {
				res.append( map.get( val ) ).append(",");
				map.remove( val );
			}

			context.write( new Text( res.toString() ), new IntWritable(1));

		}
	}

	// SReducer Class
	public static class SReducer extends Reducer<Text, IntWritable, Text, IntWritable>{

		// Map function
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			int sum = 0;
			for(IntWritable val : values) {
				sum += val.get();
			}

			context.write( key , new IntWritable(sum));

		}
	}

	// Main Configuration
	public static void main(String[] args) throws Exception {
        
		Configuration conf = new Configuration();

        // Set up Job 1
		Job job = Job.getInstance(conf, "Entire Count");

		job.setJarByClass(SameRank.class);
		job.setMapperClass(WordMapper.class);
		job.setCombinerClass(WordReducer.class);
		job.setReducerClass(WordReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path( "/Rank/f_count" ));
		job.waitForCompletion(true);

		
		// Set up Job 2
		Job job2 = Job.getInstance(conf, "Man Job");

		job2.setJarByClass(SameRank.class);
        job2.setMapperClass(ManMapper.class);
        job2.setCombinerClass(ManReducer.class);
        job2.setReducerClass(ManReducer.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job2, new Path( "/Rank/f_count"));
		FileOutputFormat.setOutputPath(job2, new Path("/Rank/man_job"));
        job2.waitForCompletion(true);

		
		// Set up Job 3
		Job job3 = Job.getInstance(conf, "Sort Job");
        
        job3.setJarByClass(SameRank.class);
        job3.setMapperClass(SMapper.class);
        job3.setCombinerClass(SReducer.class);
        job3.setReducerClass(SReducer.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job3, new Path("/Rank/man_job"));
		FileOutputFormat.setOutputPath(job3, new Path(args[1]));

		System.exit(job3.waitForCompletion(true) ? 0 : 1);

	}

}
