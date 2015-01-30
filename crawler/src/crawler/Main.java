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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes.Name;

public class Main {

	int fileNumber = 0;
	int convertToMB = 1000000;
	long fileSizes;

	String outputdir;
	String fileName = "tweets" + fileNumber + ".json";

	long tweetsObtained = 0;
	long maxTweets;

	public static void main(String[] args) {
		Main m = new Main();
		m.crawlTweets(args);
	}

	// Languages:
	// fr = French
	// en = English
	// ar = Arabic
	// ja = Japanese
	// es = Spanish
	// de = German
	// it = Italian
	// id = Indonesian
	// pt = Portuguese
	// ko = Korean
	// tr = Turkis
	// ru = Russian
	// nl = Dutch
	// fil = Filipino
	// msa = Malay
	// zh-tw = Traditional Chinese
	// zh-cn = Simplified Chinese
	// hi = Hindi
	// no = Norwegian
	// sv = Swedish
	// fi = Finnish
	// da = Danish
	// pl = Polish
	// hu = Hungarian
	// fa = Farsi
	// he = Hebrew
	// ur = Urdu
	// th = Thai
	// en-gb = English UK

	public void crawlTweets(String[] args) {

		if (args.length < 3) {
			System.out.println("Invalid number of arguments");
			System.out
					.println("./runCrawlerExecutable.sh <Max Tweets> <File Sizes (MB)> <Output Directory>");
			return;
		}
		maxTweets = Integer.valueOf(args[0]);
		fileSizes = Integer.valueOf(args[1]);
		if(fileSizes == 0){
			System.out.println("WHOA! You passed in 0 for file size. We are going to make it 1mb");
			fileSizes = 1;
		}
		fileSizes *= convertToMB;
		outputdir = args[2];
		System.out.println("Tweets: " + maxTweets + "\nfile Sizes: "
				+ fileSizes + "\nOutputDir: " + outputdir);

		String accToken = "2995123279-tPsou5RS11xE1I682qUtKiIYCRx4FeKCG4rXiGb";
		String accTokensec = "pQfusO6QK6D8TwkN1MYorMjJWl2brx6fSj6CSfynK0Asw";
		String consumer = "GQMH51Jbw075KnlYrcgSqPjhb";
		String consumersec = "DCKu6VdwUgokz8drPmWqR6mFTBeDc6yyd7eoDMU23u0kUcYxm9";

		String[] languages = { "en" };
		double[][] locations = { { -180.0d, -90.0d }, { 180.0d, 90.0d } };

		FilterQuery filter = new FilterQuery();
		filter.language(languages);
		filter.locations(locations);

		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		AccessToken ac = new AccessToken(accToken, accTokensec);
		twitterStream.setOAuthConsumer(consumer, consumersec);
		twitterStream.setOAuthAccessToken(ac);
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {

				try {
					if (tweetsObtained >= maxTweets) {
						System.out.println("Obtained " + tweetsObtained
								+ " tweets exiting...");
						System.exit(0);
					}
					if (status.getGeoLocation() != null) {
						// System.out
						// .println("Going to save tweet with the following information....");
						// printInformation(status);
						saveTweet(status);
					} else
						System.out
								.println("NO LOCATION INFORMATION!! Didn't save tweet...");
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
		twitterStream.filter(filter);
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

		++tweetsObtained;
		File f = f = new File(outputdir + "/" + fileName);
		for (; f.length() >= fileSizes;) {
			System.out.println(f.getName() + " has reached "
					+ getSize(f.length()) + " creating new file...");
			++fileNumber;
			fileName = "tweets" + fileNumber + ".json";
			f = new File(outputdir + "/" + fileName);
		}
		FileWriter file = new FileWriter(outputdir + "/" + fileName, true);
		try {
			JSONObject tweet = createJsonObj(status);
			file.write(tweet.toString() + "\n");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// file.flush();
			file.close();
			System.out.println("Saved Tweet containing location information");
		}
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
}
