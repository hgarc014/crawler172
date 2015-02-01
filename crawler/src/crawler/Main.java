package crawler;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes.Name;

public class Main extends Thread {

	public static void main(String[] args) throws IOException {

		int fileNumber = 0;
		int convertToMB = 1000000;
		int threads;
		long fileSizes;

		String outputdir;
		String fileName = "tweets" + fileNumber + ".json";
		String hashName = "hashedTweets.txt";

		FileWriter hashWriter = null;
		FileWriter tweetWriter = null;
		File tweetFile = null;

		long tweetsObtained = 0;
		long maxTweets;

		long time;

		FilterQuery filter = new FilterQuery();

		final HashMap<Long, Integer> hash = new HashMap<Long, Integer>();

		if (args.length < 4) {
			System.out.println("Invalid number of arguments");
			System.out.println("length: " + args.length);
			System.out
					.println("./runCrawlerExecutable.sh <Max Tweets> <File Sizes (MB)> <Threads> <Output Directory>");
			return;
		}
		maxTweets = Integer.valueOf(args[0]);
		fileSizes = Integer.valueOf(args[1]);
		if (fileSizes == 0) {
			System.out
					.println("WHOA! You passed in 0 for file size. We are going to make it 1mb");
			fileSizes = 1;
		}
		fileSizes *= convertToMB;
		threads = Integer.valueOf(args[2]);
		outputdir = args[3];
		
		if (threads <= 0) {
			System.out
					.println("WHOA! You passed an invalid value for threads. We are going to make it 1");
			threads = 1;
		}
		File fillHash = new File(outputdir + "/" + hashName);
		if (fillHash.isFile()) {
			System.out.println(hashName
					+ " was found, Importing already searched Tweets");
			Charset chs = Charset.forName("US-ASCII");
			BufferedReader r = Files.newBufferedReader(fillHash.toPath(), chs);
			String line = null;
			while ((line = r.readLine()) != null) {
				hash.put(Long.valueOf(line), 1);
			}
		}

		hashWriter = new FileWriter(outputdir + "/" + hashName, true);
		tweetWriter = new FileWriter(outputdir + "/" + fileName, true);
		tweetFile = new File(outputdir + "/" + fileName);

		String[] languages = { "en" };
		double[][] locations = { { -180.0d, -90.0d }, { 180.0d, 90.0d } };

		filter.language(languages);
		filter.locations(locations);

		CrawlerInformation info = new CrawlerInformation(fileSizes, maxTweets,
				outputdir);
		info.setFilter(filter);
		info.setHash(hash);
		info.setTweetWriter(tweetWriter);
		info.setHashWriter(hashWriter);
		info.setTweetFile(tweetFile);

		for (int i = 0; i < threads; ++i) {
			Crawler c = new Crawler(info, "Thread-" + i);
			c.start();
		}

		// Crawler c = new Crawler(info,"Thread-1");
		// c.start();
		// Crawler c2 = new Crawler(info,"Thread-2");
		// c2.start();
	}
}
