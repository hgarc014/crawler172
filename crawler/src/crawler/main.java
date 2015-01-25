package crawler;

import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes.Name;

public class main {
	
	static String saveFile = "/home/henry/Desktop/tweets.json";

	public static void main(String[] args) {

		String accToken = "2995123279-tPsou5RS11xE1I682qUtKiIYCRx4FeKCG4rXiGb";
		String accTokensec = "pQfusO6QK6D8TwkN1MYorMjJWl2brx6fSj6CSfynK0Asw";
		String consumer = "GQMH51Jbw075KnlYrcgSqPjhb";
		String consumersec = "DCKu6VdwUgokz8drPmWqR6mFTBeDc6yyd7eoDMU23u0kUcYxm9";

		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		AccessToken ac = new AccessToken(accToken, accTokensec);
		twitterStream.setOAuthConsumer(consumer, consumersec);
		twitterStream.setOAuthAccessToken(ac);
		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {

				 try {
					saveTweet(status);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				printInformation(status);
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
		twitterStream.sample();
	}

	public static void printInformation(Status status) {
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

	public static JSONObject createJsonObj(Status status) throws JSONException {
		JSONObject j = new JSONObject();

		//important fields
		j.put("TweetID", status.getId());
		j.put("User", status.getUser().getScreenName());
		j.put("TweetLanguage", status.getLang());
		j.put("TweetGeoLoc", status.getGeoLocation());
		j.put("TweetCreationDate", status.getCreatedAt());
		j.put("Body", status.getText());
		
		//optional fields
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
		j.put("TweetPlace", status.getPlace());
		
		System.out.println("Created Json Object");
		return j;
	}

	public static void saveTweet(Status status) throws IOException {
		FileWriter file = new FileWriter(saveFile, true);
		try {
			JSONObject tweet = createJsonObj(status);
			file.write(tweet.toString() + "\n");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
//			file.flush();
			file.close();
		}

	}
}
