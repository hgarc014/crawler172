package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

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

	Crawler(CrawlerInformation info, String threadName) throws IOException {
		this.info = info;
		this.threadName = threadName;
	}

	public JSONObject createJsonObj(Status status) throws JSONException {
		JSONObject j = new JSONObject();

		// important fields
		j.put("TweetID", status.getId());
		j.put("User", status.getUser().getScreenName());
		j.put("TweetLanguage", status.getLang());
		j.put("TweetGeoLoc", status.getGeoLocation());
		j.put("TweetCreationDate", status.getCreatedAt());
		j.put("Body", status.getText());

		// optional fields
		j.put("UserFriends", status.getUser().getFriendsCount());
		j.put("UserFollowers", status.getUser().getFollowersCount());
		j.put("UserLocation", status.getUser().getLocation());
		j.put("UserLanguage", status.getUser().getLang());
		j.put("UserTimeZone", status.getUser().getTimeZone());
		j.put("UserDesc", status.getUser().getDescription());
		j.put("UserStatus", status.getUser().getStatus());
		j.put("UserStatusCount", status.getUser().getStatusesCount());

		j.put("Retweets", status.getRetweetCount());
		j.put("Favorites", status.getFavoriteCount());
		// j.put("TweetPlace", status.getPlace());

		return j;
	}

	public void saveTweet(Status status) throws IOException {

		for (; info.getTweetFile().length() >= info.getFileSizes();) {
			System.out.println(threadName + ": "
					+ info.getTweetFile().getName() + " has reached "
					+ getSize(info.getTweetFile().length())
					+ " creating new file...");
			info.updateTweetFile();
		}
		try {
			JSONObject tweet = createJsonObj(status);
			info.getTweetWriter().write(tweet.toString() + "\n");
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			info.incrementTweetsObtained();
			System.out.println(threadName + ": Saved Tweet #"
					+ info.getTweetsObtained()
					+ " containing location information in "
					+ info.getFileName());
		}
	}

	public void run() {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		AccessToken ac = new AccessToken(info.getAccToken(),
				info.getAccTokensec());
		twitterStream.setOAuthConsumer(info.getConsumer(),
				info.getConsumersec());
		twitterStream.setOAuthAccessToken(ac);
		
		FilterQuery filter = new FilterQuery();
		
		String[] languages = { "en" };
		double[][] locations = { { -180.0d, -90.0d }, { 180.0d, 90.0d } };

		filter.language(languages);
		filter.locations(locations);
		
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {

				try {
					if (info.getTweetsObtained() >= info.getMaxTweets()) {
						time = System.currentTimeMillis() - time;
						time /= 1000;
						System.out.println(threadName + ": Obtained "
								+ info.getTweetsObtained() + " tweets in "
								+ getTimeAgo(time));
						info.getHashWriter().close();
						info.getTweetWriter().close();
						System.exit(0);
						// twitterStream.shutdown();
					} else if (!info.getHash().containsKey(status.getId())) {
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
		time = System.currentTimeMillis();
		twitterStream.filter(filter);
	}

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
