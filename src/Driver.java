package com.fiu.autocompletion;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

//import com.fiu.autocompletion.SentenceModel.ModelMapper;

public class Driver {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		Configuration conf1 = new Configuration();
		conf1.set("textinputformat.record.delimiter", ".");
		conf1.set("numOfGram", args[2]);
		
		Job job1 = new Job(conf1, "job1");
		job1.setJarByClass(Driver.class);
		
		job1.setMapperClass(NGramBuilder.NGramMapper.class);
		job1.setReducerClass(NGramBuilder.NGramReducer.class);
		
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);
		
		job1.setInputFormatClass(TextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);
		
		TextInputFormat.setInputPaths(job1, new Path(args[0]));
		TextOutputFormat.setOutputPath(job1, new Path(args[1]));
		
		job1.waitForCompletion(true);
		
		//2nd job
		Configuration conf2 = new Configuration();
		conf2.set("threshold", args[3]);
		conf2.set("n", args[4]);
		
		//set database configuration
		DBConfiguration.configureDB(conf2, 
				"com.mysql.jdbc.Driver", 
				"jdbc:mysql://192.168.0.15:3306/test", 
				"root", 
				"password");
		Job job2 = new Job(conf2,"job2");
		
		job2.setJarByClass(Driver.class);
		//job2.addArchiveToClassPath(new Path("/mysql/mysql-connector-java-5.1.39-bin.jar"));
		job2.addArchiveToClassPath(new Path("/mysql/mysql-connector-java-5.1.39-bin.jar"));
		
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setOutputKeyClass(DBOutputWritable.class);
		job2.setOutputValueClass(NullWritable.class);
		
		job2.setMapperClass(SentenceModel.Map.class);
		job2.setReducerClass(SentenceModel.Reduce.class);
		
		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(DBOutputFormat.class);
		
		DBOutputFormat.setOutput(job2, "output", 
				new String[] {"starting_phrase", "following_word", "count"});
		
		TextInputFormat.setInputPaths(job2, args[1]);
		job2.waitForCompletion(true);
	}

}
