package com.tweetsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.stream.Stream;

import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class IndexBuilder {

	public static void main(String[] args) {

		String[] fields = { "user", "text", "created_at", "geo_location",
				"linkTitle", "favorite_count", "retweet_count", "language", "hashtags"};

		StandardAnalyzer analyzer = new StandardAnalyzer();
		Directory directory = null;

		// TODO verify args
		Path indexDir = Paths.get(args[0]);
		Path tweetsDir = Paths.get(args[1]);

		try {
			directory = FSDirectory.open(indexDir.toFile());
			File[] dirList = indexDir.toFile().listFiles();
			if (dirList != null && dirList.length >= 1) {
				System.err.println("Index directory " + indexDir.toString()
						+ " is occupied, please provide an empty directory");
				System.exit(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to read directory: " + e.getMessage());
			System.exit(1);
		}

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
				analyzer);
		IndexWriter iwriter = null;
		try {
			iwriter = new IndexWriter(directory, config);
		} catch (IOException e) {
			System.err.println("Failed to write to index: " + e.getMessage());
			System.exit(1);
		}

		JSONParser jsonParser = new JSONParser();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(tweetsDir,
				"tweets*.json")) {

			for (Path tweetFile : stream) {

				Stream<String> lines = Files.lines(tweetFile);

				Document[] docs = lines
						.map((line) -> {
							try {
								return (JSONObject) jsonParser.parse(line);
							} catch (ParseException e) {
								System.err.println("JSON parse error: "
										+ e.getMessage());
							}
							return null;
						})
						.filter(a -> a != null)
						.map((json) -> {
							Document doc = new Document();

							for (String field : fields) {

								if (json.containsKey(field)) {
									switch (field) {
										case "user": 
											String userObj = json.get("user").toString();
											String username = extractFromTwitter4jObject("screenName='", userObj, "'");
											String imageUrl = extractFromTwitter4jObject("profileImageUrl='", userObj, "'");
											AddStringField("user", username, doc);
											AddStringField("profileImageUrl", imageUrl, doc);
											break;
										case "text": AddTextField("text", json.get("text").toString(), doc);
											break;
										case "created_at": 
											try {
												String dateString = json.get("created_at").toString();
												long dateMS = Date.parse(dateString); // deprecated, but works on this date format
												AddLongTimeField("created_at", dateMS, doc);
											} catch (Exception e) {
												System.err.println("time parse error!: " + e.getMessage());
											}
											break;
										case "geo_location":
											String longLatObj = json.get("geo_location").toString();
											String lat = extractFromTwitter4jObject("latitude=", longLatObj, ",");
											String lon = extractFromTwitter4jObject("longitude=", longLatObj, "}");
											AddFloatField("latitude", Float.parseFloat(lat), doc);
											AddFloatField("longitude", Float.parseFloat(lon), doc);
											break;
										case "linkTitle": AddTextField("link", json.get("linkTitle").toString(), doc);
											break;
										case "favorite_count": 
											Integer favoriteCount = Integer.parseInt(json.get("favorite_count").toString());
											AddIntField("favoriteCount", favoriteCount, doc);
											break;
										case "retweet_count":
											Integer retweets = Integer.parseInt(json.get("retweet_count").toString());
											AddIntField("retweets", retweets, doc);
											break;
										case "language": AddStringField("language", json.get("language").toString(), doc);
											break;
										case "hashtags":
										try {
											JSONArray tagsJson = (JSONArray) jsonParser.parse(json.get("hashtags").toString());
											String tagsList = "";
											for (Object hashtagObj : tagsJson) {
												JSONObject hashtag = (JSONObject) hashtagObj;
												tagsList += hashtag.get("text").toString() + " ";
											}
											if (!tagsList.isEmpty()) {
												AddTextField("hashtags", // chop off trailing " "
														tagsList.substring(0, tagsList.length()-1), doc);
											}
										} catch (Exception e) {
											System.err.println("Hashtag parse error: " + e.getMessage());
										}
											break;
									}
								}
							}
							return doc;

						}).toArray(Document[]::new);

				for (Document doc : docs) {
					iwriter.addDocument(doc);
				}
				lines.close();
			}
		} catch (IOException e) {
			System.err.println("Failed to finish directory stream: "
					+ e.getMessage());
			System.exit(1);
		}

		try {
			iwriter.close();
			directory.close();
		} catch (IOException e) {
			System.err.println("Failed to close index: " + e.getMessage());
			System.exit(1);
		}

	}
	
	private static String extractFromTwitter4jObject(String fieldname, String t4jObj, 
			String delimit) {
		int start = t4jObj.indexOf(fieldname) + fieldname.length();
		int end = t4jObj.indexOf(delimit, start);
		
		return t4jObj.substring(start, end);
	}
	
	private static void AddTextField(String name, String text, Document d) {
		d.add(new TextField(name, text, Field.Store.YES));
		d.getField(name).boost();

	}
	private static void AddStringField(String name, String value, Document d) {
		d.add(new StringField(name, value, Field.Store.YES));
	}

	private static void AddIntField(String name, int value, Document d) {
		d.add(new IntField(name, value, Field.Store.YES));
	}
	
	private static void AddFloatField(String name, float value, Document d) {
		d.add(new FloatField(name, value, Field.Store.YES));
	}

	private static void AddLongTimeField(String name, Long value, Document d) {
		d.add(new LongField(name, value, Field.Store.YES));
		d.getField(name).boost();
	}

}
