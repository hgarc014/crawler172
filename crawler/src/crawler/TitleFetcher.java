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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TitleFetcher {
	private static int crawlTimeoutMs = 6000;
	private static boolean verbose = true;	
	
	public static void main(String[] args) throws IOException{
		// TODO Add Input validation
		crawlTimeoutMs = Integer.parseInt(args[1]);
		verbose = Boolean.parseBoolean(args[3]);
		TitleFetcher.fetchTitles(args[0], Integer.parseInt(args[2]));
	}
	
	public static void fetchTitles(String inputdir, int numThreads) throws IOException {		
		System.out.println("Beginning crawl of embedded links");
		long startTime = System.nanoTime();	
		int matchCount = 0, titleCount = 0, tweetCount = 0;
		int fileCount = 0; 
		File[] directory = new File(inputdir).listFiles();
		
		for (File file : directory) {	
			// prevent any unwanted files from getting in
			
			if (file.getName().equals("hashedTweets.txt") || file.getName().equals(".DS_Store")) { // TODO replace guard with regular expression match
				if (verbose) {
					System.out.println("Skipping " + file.getName());
				}
				continue; // figured out how to use continue after 2.5 years of programming
			}
			
			// file is good, set things up and load it
			
			++fileCount;
			ConcurrentLinkedQueue<JSONObject> inputJsonQueue = new ConcurrentLinkedQueue<JSONObject>();	
			ConcurrentLinkedQueue<JSONObject> outputJsonQueue = new ConcurrentLinkedQueue<JSONObject>();			
			JSONParser parser = new JSONParser();
			BufferedReader jsonReader = null;
			
			try {
				jsonReader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			for (String line; (line = jsonReader.readLine()) != null;) { // too lazy to catch this exception
				try {
					JSONObject json = (JSONObject) parser.parse(line);
					inputJsonQueue.add(json);
				} catch (ParseException e) {
					if (verbose) {
						System.out.println(e.getMessage());
					}
				}
			}
			jsonReader.close();
			
			// pop the tweet, process it, add it to the list
			
			while (!inputJsonQueue.isEmpty()) {
				++tweetCount;
				JSONObject tweet = inputJsonQueue.remove();
				
				if (!tweet.containsKey("linkTitle") && !tweet.containsKey("hasBadLink")) { // only consider new tweets
					String text = tweet.get("text").toString();
					String maybeUrlString = getUrlString(text); // read maybe as "could be null or..."
					
					if (maybeUrlString != null) {
						++matchCount;
						String urlString = maybeUrlString;
						
						if (verbose) {
							System.out.println("URL: " + urlString);
						}
						
						String maybeTitleOrEmpty = getTitle(urlString);
						
						if (maybeTitleOrEmpty != null) {
							String titleOrEmpty = maybeTitleOrEmpty;
							
							if (!titleOrEmpty.isEmpty()) {
								++titleCount;
								String title = titleOrEmpty;
								tweet.put("linkTitle", title);
								
								if (verbose) {
									System.out.println("Title: \"" + title + "\" from tweet "
											+ Integer.toString(tweetCount));
								}
								
							} else {
								tweet.put("hasBadLink", true); 
							}
						}
					}
				}
				
				outputJsonQueue.add(tweet);
			}
						
			// write everything back to file
			
			BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(file,false));
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
		}
		
		// print some statistics 
		// TODO replace timing with google library stopwatch
		long endTime = System.nanoTime();
		long durationNs = endTime - startTime;
		long durationSeconds = durationNs / 1000000 / 1000;
		
		System.out.println("Processed " + Integer.toString(tweetCount) + " tweets across " + Integer.toString(fileCount) + " files");
		System.out.println("Retrieved titles for "+ Integer.toString(titleCount) + " out of " + Integer.toString(matchCount) + " links");
		System.out.println("Took " + Long.toString(durationSeconds) + " seconds");
	}
	
	// follows the link specified by url and if the response is html, returns the title as a string
	public static String getTitle(String urlString) {
		String title = null;
		try {
			Document doc = Jsoup.connect(urlString).timeout(crawlTimeoutMs).get();
			title = doc.title();
		} catch (IOException e) {
			title = "";
			if (verbose) {
				System.out.println(e.getMessage());
			}
		}
		
		return title;
	}
	
	// attempts to find the last url in a string of text, returns null otherwise
	// based on a snippet found at http://stackoverflow.com/questions/285619/how-to-detect-the-presence-of-url-in-a-string 
	// might still be cases where the regex results in an invalid url
	public static String getUrlString(String text) {
		String [] words = text.split("\\s+|”|\""); 
		String urlString = null;
		
		for (String word : words) try {
			URL url = new URL(word); // uses the built in parsing abilities of the URL constructor
			
			// TODO handle urls ending in '.'
			
			urlString = url.toString(); // but just returns a string
		} catch(MalformedURLException e) {
			// Exception-based control flow FTW
		}
		
		return urlString;
	}
	
}