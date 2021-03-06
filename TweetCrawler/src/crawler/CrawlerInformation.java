package crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;

import twitter4j.FilterQuery;
import twitter4j.Status;

public class CrawlerInformation {

	private ConcurrentLinkedQueue<Status> tweetsToSave = new ConcurrentLinkedQueue<Status>();
	private ConcurrentLinkedQueue<Long> hashsToSave = new ConcurrentLinkedQueue<Long>();

	private int fileNumber = 0;
	private int numThreads = 1;
	private int convertToMB = 1000000;
	private int saveEvery = 0;

	private long fileSizes = 0;
	private long tweetsObtained = 0;
	private long tweetsSaved = 0;
	private long maxTweets = 0;

	private String outputdir = null;
	private String fileName = "tweets" + fileNumber + ".json";
	private String hashName = "hashedTweets.txt";

	private FileWriter hashWriter = null;
	private FileWriter tweetWriter = null;
	private File tweetFile = null;

	private HashMap<Long, Integer> hash = null;
	private FilterQuery filter = null;

	private String accToken = "2995123279-tPsou5RS11xE1I682qUtKiIYCRx4FeKCG4rXiGb";
	private String accTokensec = "pQfusO6QK6D8TwkN1MYorMjJWl2brx6fSj6CSfynK0Asw";
	private String consumer = "GQMH51Jbw075KnlYrcgSqPjhb";
	private String consumersec = "DCKu6VdwUgokz8drPmWqR6mFTBeDc6yyd7eoDMU23u0kUcYxm9";

	private Boolean notFinished = true;

	CrawlerInformation(long fileSizes, long maxTweets, String outputdir,
			int numThreads, HashMap<Long, Integer> hash,
			FileWriter tweetWriter, FileWriter hashWriter, File tweetFile, int saveEvery) {
		this.fileSizes = fileSizes * convertToMB;
		this.maxTweets = maxTweets;
		this.outputdir = outputdir;
		this.numThreads = numThreads;
		this.hash = hash;
		this.tweetWriter = tweetWriter;
		this.hashWriter = hashWriter;
		this.tweetFile = tweetFile;
		this.saveEvery=saveEvery;
	}

	public int getSaveEvery(){
		return saveEvery;
	}
	public int getFileNumber() {
		return fileNumber;
	}

	public void incrementFileNumber() {
		++fileNumber;
	}

	public long getFileSizes() {
		return fileSizes;
	}

	public long getTweetsObtained() {
		return tweetsObtained;
	}

	public void incrementTweetsObtained() {
		++tweetsObtained;
	}

	public long getTweetsSaved() {
		return tweetsSaved;
	}

	public void incrementTweetsSaved() {
		++tweetsSaved;
	}

	public long getMaxTweets() {
		return maxTweets;
	}

	public void setMaxTweets(long maxTweets) {
		this.maxTweets = maxTweets;
	}

	public String getOutputdir() {
		return outputdir;
	}

	public void setOutputdir(String outputdir) {
		this.outputdir = outputdir;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getHashName() {
		return hashName;
	}

	public void setHashName(String hashName) {
		this.hashName = hashName;
	}

	public FileWriter getHashWriter() {
		return hashWriter;
	}

	public void setHashWriter(FileWriter hashWriter) {
		this.hashWriter = hashWriter;
	}

	public FileWriter getTweetWriter() {
		return tweetWriter;
	}

	public void setTweetWriter(FileWriter tweetWriter) {
		this.tweetWriter = tweetWriter;
	}

	public File getTweetFile() {
		return tweetFile;
	}

	public void setTweetFile(File tweetFile) {
		this.tweetFile = tweetFile;
	}

	public HashMap<Long, Integer> getHash() {
		return hash;
	}

	public void setHash(HashMap<Long, Integer> hash) {
		this.hash = hash;
	}

	public FilterQuery getFilter() {
		return filter;
	}

	public void setFilter(FilterQuery filter) {
		this.filter = filter;
	}

	public int getNumThreads() {
		return numThreads;
	}

	// used for updating the saving tweet file when the file is larger than the
	// given size
	public void updateTweetFile() throws IOException {
		++fileNumber;
		fileName = "tweets" + fileNumber + ".json";
		tweetFile = new File(outputdir + "/" + fileName);
		try {
			tweetWriter.close();
			tweetWriter = new FileWriter(outputdir + "/" + fileName, true);
		} catch (IOException e) {
			e.printStackTrace();
			if (tweetWriter != null)
				tweetWriter.close();
		}

	}

	public ConcurrentLinkedQueue<Status> getTweetList() {
		return tweetsToSave;
	}

	public ConcurrentLinkedQueue<Long> getHashList() {
		return hashsToSave;
	}

	public Boolean getNotFinished() {
		return notFinished;
	}

	public void setNotFinished(Boolean notFinished) {
		this.notFinished = notFinished;
	}

	public String getAccToken() {
		return accToken;
	}

	public String getAccTokensec() {
		return accTokensec;
	}

	public String getConsumer() {
		return consumer;
	}

	public String getConsumersec() {
		return consumersec;
	}

	public void closeTweetWriter() {
		try {
			if (tweetWriter != null)
				tweetWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeHashWriter() {
		try {
			if (hashWriter != null)
				hashWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
