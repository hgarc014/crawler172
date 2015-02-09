package crawler;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TitleFetcher {
	private static boolean verbose = true;

	public static void main(String[] args) throws IOException {
		// TODO Add Input validation
		verbose = Boolean.parseBoolean(args[2]);
		TitleFetcher.fetchTitles(args[0], Integer.parseInt(args[1]));
	}

	public static void fetchTitles(String inputdir, int numThreads)
			throws IOException {
		System.out.println("Beginning crawl of embedded links");
		long startTime = System.nanoTime();
		AtomicInteger matchCount = new AtomicInteger(0), 
				      titleCount = new AtomicInteger(0), 
				      tweetCount = new AtomicInteger(0);
		int fileCount = 0;
		File[] directory = new File(inputdir).listFiles();

		for (File file : directory) {
			// prevent any unwanted files from getting in
			if (file.getName().matches("tweets[0-9]+.json")) {

				// file is good, set things up and load it

				++fileCount;
				ArrayList<JSONObject> inputJsonArray = new ArrayList<JSONObject>();
				ConcurrentLinkedQueue<JSONObject> outputJsonQueue = new ConcurrentLinkedQueue<JSONObject>();
				JSONParser parser = new JSONParser();
				BufferedReader jsonReader = null;
				BufferedWriter jsonWriter = null;
				
				try {
					jsonReader = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				for (String line; (line = jsonReader.readLine()) != null;) { // throws
					try {
						JSONObject json = (JSONObject) parser.parse(line);
						inputJsonArray.add(json);
					} catch (ParseException e) {
						if (verbose) {
							System.out.println(e.getMessage());
						}
					}
				}
				jsonReader.close();

				
				ExecutorService executor = Executors.newFixedThreadPool(numThreads);
				Iterator<JSONObject> iterJson = inputJsonArray.iterator();
				while (iterJson.hasNext()) {
					JSONObject json = iterJson.next();
					Runnable worker = new TitleFetchWorker(json, outputJsonQueue,
													      matchCount, titleCount, tweetCount, verbose);
					executor.execute(worker);
				}
				executor.shutdown();
				
				while(!executor.isTerminated()) {/* busy wait */}

				// write everything back to file

				try {
					jsonWriter = new BufferedWriter(new FileWriter(file, false));
				} catch (IOException e) {
					if (verbose) {
						System.out.println(e.getMessage());
					}
				}
				Iterator<JSONObject> iter = outputJsonQueue.iterator();

				for (String line; iter.hasNext();) try {
					line = JSONValue.toJSONString(iter.next());
					jsonWriter.write(line);
					jsonWriter.newLine();
				} catch (IOException e) {
					if (verbose) {
						System.out.println(e.getMessage());
					}
				}
				jsonWriter.close();
			} else if (verbose) {
				System.out.println("Skipping " + file.getName());
			}
		}

		// print some statistics
		// TODO replace timing with google library stopwatch
		long endTime = System.nanoTime();
		long durationNs = endTime - startTime;
		long durationSeconds = durationNs / 1000000 / 1000;

		System.out.println("Processed " + tweetCount.toString()
				+ " tweets across " + Integer.toString(fileCount) + " files");
		System.out.println("Retrieved titles for "
				+ titleCount.toString() + " out of "
				+ matchCount.toString() + " links");
		System.out.println("Took " + Long.toString(durationSeconds)
				+ " seconds");
	}
}