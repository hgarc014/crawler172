package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;

public class Main extends Thread {

	public static void main(String[] args) throws IOException,
			InterruptedException {

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

		long maxTweets = 0;

		final HashMap<Long, Integer> hash = new HashMap<Long, Integer>();

		if (args.length < 4) {
			System.out.println("Invalid number of arguments");
			System.out.println("length: " + args.length);
			System.out
					.println("<Max Tweets> <File Sizes (MB)> <Threads> <Output Directory>");
			return;
		}

		maxTweets = checkNumber(args[0], "Max Tweets");
		fileSizes = checkNumber(args[1], "File Sizes(MB)");
		threads = checkNumber(args[2], "Threads");
		outputdir = args[3];

		if (fileSizes <= 0) {
			System.out.println("WHOA! You passed in " + fileSizes
					+ " for file size. We are going to make it 1mb");
			fileSizes = 1;
		}
		fileSizes *= convertToMB;

		if (threads <= 0) {
			System.out.println("WHOA! You passed in " + threads
					+ " for threads. We are going to make it 1");
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

		CrawlerInformation info = new CrawlerInformation(fileSizes, maxTweets,
				outputdir);
		info.setHash(hash);
		info.setTweetWriter(tweetWriter);
		info.setHashWriter(hashWriter);
		info.setTweetFile(tweetFile);

		// for (int i = 0; i < threads; ++i) {
		// Crawler c = new Crawler(info, "Thread-" + i);
		// c.start();
		// }

		Crawler c = new Crawler(info, "TweetCrawler");
		c.start();
		c.join();
	}

	public static Integer checkNumber(String convert, String name) {
		try {
			int x = Integer.valueOf(convert);
			if (x == 0) {
				System.out.println("WHOA! You passed " + x + " for " + name
						+ ". We will convert it to 1");
				x = 1;
			} else if (x < 0) {
				int y = -1 * x;
				System.out.println("WHOA! You passed " + x + " for " + name
						+ ". We will convert it to " + y);
				x = y;
			}
			return x;
		} catch (NumberFormatException n) {
			System.out.println(convert + " is not a valid integer value for "
					+ name + "!");
			System.exit(0);
			return 0;
		}
	}
}
