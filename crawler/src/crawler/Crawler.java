package crawler;

import java.io.IOException;
import java.util.Arrays;
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

public class Crawler extends Thread {
	CrawlerInformation info = null;
	long time;
	String threadName = null;
	private Thread t;
	 Boolean notfinished=true;

	Crawler(CrawlerInformation info, String threadName) throws IOException {
		this.info = info;
		this.threadName = threadName;
	}

	// grabs most fields from the tweet and places them in a json object
	public JSONObject createJsonObj(Status status) throws JSONException {
		JSONObject j = new JSONObject();

		j.put("contributors", status.getContributors());
		j.put("created_at", status.getCreatedAt());
		j.put("current_user_retweet_id", status.getCurrentUserRetweetId());
		j.put("favorite_count", status.getFavoriteCount());
		j.put("is_favorited", status.isFavorited());
		j.put("geo_location", status.getGeoLocation());
		j.put("id", status.getId());
		j.put("in_reply_to_screen_name", status.getInReplyToScreenName());
		j.put("in_reply_to_status_id", status.getInReplyToStatusId());
		j.put("in_reply_to_user_id", status.getInReplyToUserId());
		j.put("language", status.getLang());
		j.put("palce", status.getPlace());
		j.put("is_possibly_sensitive", status.isPossiblySensitive());
		j.put("retweet_count", status.getRetweetCount());
		j.put("is_retweeted", status.isRetweeted());
		j.put("retweeted_status", status.getRetweetedStatus());
		// j.put("source", status.getSource());
		j.put("text", status.getText());
		j.put("is_truncated", status.isTruncated());
		j.put("user", status.getUser());

		return j;
	}

	// called when saving a valid tweet
	public void saveTweet(Status status) {

		// check if the given file is still less than the max file size
		// If the file size is larger than it will loop until it finds a file
		// that is smaller than the given amount
		// this is useful when a user has already crawled tweets in given files
		for (; info.getTweetFile().length() >= info.getFileSizes();) {
			System.out.println(threadName + ": "
					+ info.getTweetFile().getName() + " has reached "
					+ getSize(info.getTweetFile().length())
					+ " creating new file...");
			try {
				info.updateTweetFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			// create json object of tweet
			JSONObject tweet = createJsonObj(status);
			// write json objecet to file
			info.getTweetWriter().write(tweet.toString() + "\n");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			info.closeTweetWriter();
		} finally {
			// increment tweets obtained
			info.incrementTweetsObtained();
			System.out.println(threadName + ": Saved Tweet #"
					+ info.getTweetsObtained()
					+ " containing location information in "
					+ info.getFileName());
		}
	}

	public void run() {
		// create twitterstream and fill in tokens
		final TwitterStream twitterStream = new TwitterStreamFactory()
				.getInstance();
		AccessToken ac = new AccessToken(info.getAccToken(),
				info.getAccTokensec());
		twitterStream.setOAuthConsumer(info.getConsumer(),
				info.getConsumersec());
		twitterStream.setOAuthAccessToken(ac);

		FilterQuery filter = new FilterQuery();

		// languages and location to filter tweets
		String[] languages = { "en" };
		// location encompases most if not all of the entire world
		double[][] locations = { { -180.0d, -90.0d }, { 180.0d, 90.0d } };

		filter.language(languages);
		filter.locations(locations);
		
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {

				try {
					// if obtained tweets are larger or equal than max tweets
					// exit system
					if (info.getTweetsObtained() >= info.getMaxTweets()) {
						if(notfinished){
						time = System.currentTimeMillis() - time;
						time /= 1000;
						System.out.println(threadName + ": Obtained "
								+ info.getTweetsObtained() + " tweets in "
								+ getTimeAgo(time));
						info.closeHashWriter();
						info.closeTweetWriter();
						notfinished=false;
						}
						// System.exit(0);
						twitterStream.shutdown();
						twitterStream.cleanUp();
					}
					// check if the tweet has been crawled
					else if (!info.getHash().containsKey(status.getId())) {
						// check if tweet contains location information then
						// place tweet into hash and save to hashfile
						// then save tweet
						if (status.getGeoLocation() != null) {
							info.getHash().put(status.getId(), 1);
							info.getHashWriter().write(
									String.valueOf(status.getId()) + "\n");
							saveTweet(status);
						} else
							System.out
									.println(threadName
											+ ": No Location Information!! Did not save tweet...");
					} else
						System.out
								.println(threadName
										+ ": Tweet has already been crawled, moving to next tweet...");
				} catch (IOException e) {
					e.printStackTrace();
					info.closeHashWriter();
					info.closeTweetWriter();
				}
			}

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:"
						+ statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:"
						+ numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId
						+ " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		twitterStream.addListener(listener);
		// grab current time before starting connection
		time = System.currentTimeMillis();
		twitterStream.filter(filter);
	}

	// used to return the size as a string
	public String getSize(long n) {
		int kb = (int) (n / 1000);
		int mb = kb / 1000;
		int gb = mb / 1000;
		if (gb != 0)
			return gb + "gbs";
		else if (mb != 0)
			return mb + "mbs";
		else
			return kb + "kbs";
	}

	// used for printing information of the tweet,
	// NOT used in crawling
	public void printInformation(Status status) {
		char[] charArray = new char[20];
		Arrays.fill(charArray, '-');
		String line = new String(charArray);
		System.out.println(line + "\nUSER INFORMATION" + "\n@"
				+ status.getUser().getScreenName() + "\nTimeZone: "
				+ status.getUser().getTimeZone() + "\nUserLocation: "
				+ status.getUser().getLocation() + "\nFriends: "
				+ status.getUser().getFriendsCount() + "\nFollowers: "
				+ status.getUser().getFollowersCount()
				+ "\nProfileDescription: " + status.getUser().getDescription()
				+ "\nStatus: " + status.getUser().getStatus()
				+ "\nStatusCount: " + status.getUser().getStatusesCount()
				+ "\nLanguage: " + status.getUser().getLang());

		System.out.println("\nTWEET INFORMATION" + "\nTweetID: "
				+ status.getId() + "Language: " + status.getLang()
				+ "\nGeoLocation: " + status.getGeoLocation() + "\nRetweets: "
				+ status.getRetweetCount() + "\nFavorites: "
				+ status.getFavoriteCount() + "\nPlace: " + status.getPlace()
				+ "\nCreatedAt: " + status.getCreatedAt());

		System.out.println("\nMENTIONS:");
		for (UserMentionEntity u : status.getUserMentionEntities()) {
			System.out.println("@" + u.getScreenName());
		}
		System.out.println("\nPOUNDSIGNS:");
		for (HashtagEntity h : status.getHashtagEntities()) {
			System.out.println("#" + h.getText());
		}
		System.out.println("\nBody:\n" + status.getText());
	}

	// return time took to crawl as string
	public String getTimeAgo(long t) {
		if (t / 60 == 0)
			return t + " seconds";
		else if (t / 3600 == 0)
			return t / 60 + " minutes";
		else if (t / 86400 == 0)
			return t / 3600 + " hours";
		else
			return t / 86400 + " days";
	}

	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}
}
