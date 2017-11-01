/**
 * Created by akshayjaitly on 10/07/17.
 */
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
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
import java.util.regex.Pattern;
import java.util.*;

public class WordCounter {

	// WordMapper Class
	public static class WordMappper extends Mapper<Object, Text, Text, IntWritable>{

		private final static IntWritable write = new IntWritable(1);

		private Text word = new Text();

		String[] search = new String[] {"education", "politics", "sports", "agriculture"};

		// Map function
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String itr = value.toString();
		    itr = itr.replaceAll( "[^A-Za-z ]", " " ).toLowerCase();

			String filepath = ((FileSplit) context.getInputSplit()).getPath().toString();
			
			for (String curr_str: itr.split(" ")) {
				if (curr_str.length() > 0) {
					
					if(curr_str.equalsIgnoreCase("politics") || curr_str.equalsIgnoreCase("education") || curr_str.equalsIgnoreCase("agriculture")|| curr_str.equalsIgnoreCase("sports")) {
						context.write(new Text(filepath + "/" + curr_str), write);
					}
				}
			}
			
			
		}
	}

	// WordReducer Class
	public static class WordReducer extends Reducer<Text,IntWritable,Text,IntWritable> {

		private IntWritable Output = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			String tmp = key.toString().toLowerCase();

			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			Output.set(sum);
			context.write(key, Output);
			
		}
	}

	// DominantMapper Class
	public static class DominantMapper extends Mapper<Object, Text, Text, Text>{

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

			int f_index = values[0].lastIndexOf("/");
			String state = values[0].substring(0, f_index);
			String phrase = values[0].substring(f_index + 1, values[0].length());


			StringBuilder sb = new StringBuilder();
			sb.append(phrase).append("-").append(values[1]);

			context.write(new Text(state), new Text(sb.toString()));

		}
	}

	// DominantReducer Class
	public static class DominantReducer extends Reducer<Text,Text,Text,Text> {


		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			ArrayList<String> Outputs = new ArrayList<String>();
			int maxCount = 0;

			ArrayList<String> str = new ArrayList<String>();

			for (Text val : values) {
				str.add(val.toString());
			}

			// Determine max index for reducer.
			int index = 0;
			int count = -1;
			for(int i = 0; i < str.size(); i++) {
				if( Integer.parseInt( str.get(i).split("-")[1] ) > count ) {
					index = i;
					count = Integer.parseInt( str.get(i).split("-")[1] );
				}
			}

			context.write( new Text(str.get(index)), new Text(str.get(index)) );
		}
	}

	// CMapper Class
	public static class CMapper extends Mapper<Object, Text, Text, IntWritable>{

		private final static IntWritable write = new IntWritable(1);

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			String phrase = value.toString().trim().split("-")[0].trim();
			context.write(new Text(phrase), new IntWritable(1));

		}
	}

	// CReducer Class
	public static class CReducer extends Reducer<Text,IntWritable,Text,IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));

		}
	}

	// Main function
	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();

		// Set up Job 1
		Job job = Job.getInstance(conf, "Entire Count");

		job.setJarByClass(WordCounter.class);
		job.setMapperClass(WordMappper.class);
		job.setCombinerClass(WordReducer.class);
		job.setReducerClass(WordReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path( "/WordCount/Ecount" /*args[1]*/));

		job.waitForCompletion(true);

		
		// Set up Job 2
		Job job2 = Job.getInstance(conf, "Dominant Job");

		job2.setJarByClass(WordCounter.class);
		job2.setMapperClass(DominantMapper.class);
		job2.setCombinerClass(DominantReducer.class);
		job2.setReducerClass(DominantReducer.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job2, new Path("/WordCount/Ecount" ));
		FileOutputFormat.setOutputPath(job2, new Path("/WordCount/Djob" ));

		job2.waitForCompletion(true);

		// Set up Job 3
		Job job3 = Job.getInstance(conf, "DominantCount Job");

		job3.setJarByClass(WordCounter.class);
		job3.setMapperClass(CMapper.class);
		job3.setCombinerClass(CReducer.class);
		job3.setReducerClass(CReducer.class);
		job3.setOutputKeyClass(Text.class);
		job3.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job3, new Path("/WordCount/Djob" ));
		FileOutputFormat.setOutputPath(job3, new Path(args[1]));

		System.exit(job3.waitForCompletion(true) ? 0 : 1);

	}

}
