package crawler;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class main {

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

				printInformation(status);
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
		System.out.println(line + "\nPostID: " + status.getId()+ "\n@" + status.getUser().getScreenName()
				+ "\nTimeZone: " + status.getUser().getTimeZone()
				+ "\nUserLocation: " + status.getUser().getLocation()
				+ "\nFriends: " + status.getUser().getFriendsCount()
				+ "\nFollowers: " + status.getUser().getFollowersCount()
				+ "\nGeoLocation: " + status.getGeoLocation());
		System.out.println("\nURLS: ");
		for (URLEntity e : status.getURLEntities()) {
			System.out.println("Display: " + e.getDisplayURL() + "\nExpanded: "
					+ e.getExpandedURL() + "\nURL: " + e.getURL());
		}
		System.out.println("\nMENTIONS:");
		for (UserMentionEntity u : status.getUserMentionEntities()) {
			System.out.println("@" + u.getScreenName());
		}

		System.out.println("\nFavorites: " + status.getFavoriteCount()
				+ "\nbody:" + status.getText() + "\n");
	}
}
