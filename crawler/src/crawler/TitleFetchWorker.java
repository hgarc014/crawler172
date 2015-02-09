package crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TitleFetchWorker implements Runnable {
	private ConcurrentLinkedQueue<JSONObject> inputJsonQueue, outputJsonQueue;
	private AtomicInteger matchCount, titleCount, tweetCount; 
	private boolean verbose;
	
	public TitleFetchWorker(ConcurrentLinkedQueue<JSONObject> a, 
			                ConcurrentLinkedQueue<JSONObject> b,
			                AtomicInteger c, AtomicInteger d,
			                AtomicInteger e, boolean f) 
	{
		this.inputJsonQueue = a;
		this.outputJsonQueue = b;
		this.matchCount = c;
		this.titleCount = d;
		this.tweetCount = e;
		this.verbose = f;
	}
	
	@Override
	public void run() {
		processJson();
	}

	private void processJson() {
		
		// pop the tweet, process it, add it to the list
		JSONObject maybeTweet = inputJsonQueue.poll();

		if (maybeTweet != null) {
			JSONObject tweet = maybeTweet;
			tweetCount.incrementAndGet();
			if (!tweet.containsKey("linkTitle")
					&& !tweet.containsKey("hasBadLink")) { // only consider new tweets
				String text = tweet.get("text").toString();
				String maybeUrlString = getUrlString(text); // read maybe as "could be null or..."

				if (maybeUrlString != null) {
					matchCount.incrementAndGet();
					String urlString = maybeUrlString;

					if (verbose) {
						System.out.println("URL: " + urlString);
					}

					String maybeTitleOrEmpty = getTitle(urlString);

					if (maybeTitleOrEmpty != null) {
						String titleOrEmpty = maybeTitleOrEmpty;

						if (!titleOrEmpty.isEmpty()) {
							titleCount.incrementAndGet();
							String title = titleOrEmpty;
							tweet.put("linkTitle", title);

							if (verbose) {
								System.out.println("Title: \"" + title
										+ "\" from tweet "
										+ tweetCount.toString());
							}

						} else {
							tweet.put("hasBadLink", true);
						}
					}
				}
			}
			outputJsonQueue.add(tweet);
		}
	}
	
	// follows the link specified by url and if the response is html, returns
		// the title as a string
		public String getTitle(String urlString) {
			String title = null;
			try {
				System.out.println("Waiting to connect");
				Document doc = Jsoup.connect(urlString).timeout(5000).get();
				System.out.println("Connected");
				title = doc.title();
			} catch (Exception e) { // TODO change back to IOException when debugged
				title = "";
				if (this.verbose) {
					System.out.println(e.getMessage());
				}
			}

			return title;
		}

		// attempts to find the last url in a string of text, returns null otherwise
		// based on a snippet found at
		// http://stackoverflow.com/questions/285619/how-to-detect-the-presence-of-url-in-a-string
		// might still be cases where the regex results in an invalid url
		public String getUrlString(String text) {
			String[] words = text.split("\\s+|”|\"");
			String urlString = null;

			for (String word : words) try {
					URL url = new URL(word); // parse with URL constructor				
					urlString = url.toString(); // but just returns a string
					
					if (urlString.endsWith(".")) {
						urlString = urlString.substring(0, urlString.length()-1); // shave off "."
					}
				} catch (MalformedURLException e) {
					// Exception-based control flow FTW
				}

			return urlString;
		}

}
