package ui.server.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import ui.client.services.LuceneService;
import ui.shared.Constants;
import ui.shared.Tweet;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

import org.apache.lucene.search.SortField.Type;

public class LuceneServiceImpl extends RemoteServiceServlet implements
		LuceneService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IndexSearcher isearcher = null;
	DirectoryReader ireader = null;
	private Object luceneLock = new Object();

	@SuppressWarnings("deprecation")
	private void initSearcher() throws IOException {
		synchronized (luceneLock) {
			if (isearcher == null) {
				ireader = IndexReader.open(new SimpleFSDirectory(new File(
						"indexes/index")));
				isearcher = new IndexSearcher(ireader);
			}
		}
	}

	public List<Tweet> getTweets(String query, String type) {
		List<Tweet> tweets = new ArrayList<Tweet>();
		try {
			if (isearcher == null)
				initSearcher();
			ScoreDoc[] hits = null;

			if (query.length() == 0)
				hits = getRecentTweets();
			else {
				if (query.contains("#"))
					query = query.replaceAll("#", " ");
				if (query.contains("@"))
					query = query.replaceAll("@", " ");
				String[] separate = query.split(" ");
				if (type.equals(Constants.GENERAL))
					hits = generalSearh(separate, query);
				else if (type.equals(Constants.HASHTAGS)) {
					hits = getHashTags(separate);
				} else if (type.equals(Constants.USER)) {
					hits = getUser(separate);
				}
			}

			for (ScoreDoc hit : hits) {
				Document d = isearcher.doc(hit.doc);
				tweets.add(newTweet(d));
			}

		} catch (IOException | java.text.ParseException | ParseException e) {
			e.printStackTrace();
		}

		return tweets;
	}

	private ScoreDoc[] generalSearh(String[] query, String exact)
			throws IOException, ParseException {
		BooleanQuery bq = new BooleanQuery();
		Query q = null;
		Analyzer anl = new StandardAnalyzer();
		QueryParser pars = null;

		String m = null;
		for (String s : query) {
			if (s.length() == 0)
				continue;
			pars = new QueryParser("text", anl);
			q = pars.parse(QueryParser.escape(s));
			// q = new TermQuery(new Term("text", s));
			bq.add(q, Occur.SHOULD);

			pars = new QueryParser("user", anl);
			q = pars.parse(QueryParser.escape(s));
			// m = s.replaceAll("@", "");
			// q = new TermQuery(new Term("user", m));
			// q.setBoost(5f);
			bq.add(q, Occur.SHOULD);

			pars = new QueryParser("link", anl);
			q = pars.parse(QueryParser.escape(s));
			// q = new TermQuery(new Term("link", s));
			q.setBoost(2f);
			bq.add(q, Occur.SHOULD);

			m = s.replaceAll("#", "");
			pars = new QueryParser("hashtags", anl);
			q = pars.parse(QueryParser.escape(s));
			// q = new TermQuery(new Term("hashtags", m));
			q.setBoost(2f);
			bq.add(q, Occur.SHOULD);
		}

		return getScoreDoc(bq);
	}

	private ScoreDoc[] getRecentTweets() throws IOException {
		BooleanQuery bq = new BooleanQuery();
		Query q = null;
		q = NumericRangeQuery.newIntRange("retweets", 0, 100, true, true);
		bq.add(q, Occur.MUST);
		Sort sorter = new Sort();
		SortField sf = new SortField("created_at", Type.LONG, true);
		sorter.setSort(sf);
		TopFieldDocs tfd = isearcher.search(bq, 10, sorter);
		return tfd.scoreDocs;
	}

	private ScoreDoc[] getUser(String[] users) throws IOException,
			ParseException {
		BooleanQuery bq = new BooleanQuery();
		Query q = null;
		Analyzer anl = new StandardAnalyzer();
		QueryParser pars = null;

		for (String s : users) {
			if (s.length() == 0)
				continue;
			s = s.replaceAll("@", "");
			pars = new QueryParser("user", anl);
			q = pars.parse(QueryParser.escape(s));
			// q = new TermQuery(new Term("user", s));
			bq.add(q, Occur.SHOULD);
		}
		return getScoreDoc(bq);
	}

	private ScoreDoc[] getHashTags(String[] tags) throws IOException,
			ParseException {
		BooleanQuery bq = new BooleanQuery();
		Query q = null;
		Analyzer anl = new StandardAnalyzer();
		QueryParser pars = null;
		for (String s : tags) {
			if (s.length() == 0)
				continue;
			s = s.replaceAll("#", "");
			System.out.println("LT=" + s);
			pars = new QueryParser("hashtags", anl);
			q = pars.parse(QueryParser.escape(s));
			// q = new TermQuery(new Term("hashtags", s));
			bq.add(q, BooleanClause.Occur.SHOULD);
		}
		return getScoreDoc(bq);
	}

	private ScoreDoc[] getScoreDoc(BooleanQuery bq) throws IOException {
		TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
		isearcher.search(bq, collector);
		return collector.topDocs().scoreDocs;
	}

	private Tweet newTweet(Document d) throws java.text.ParseException {
		Tweet t = new Tweet(d.get("created_at"), d.get("favoriteCount"),
				d.get("retweets"), d.get("longitude"), d.get("latitude"),
				d.get("language"), d.get("user"), d.get("text"), d.get("link"),
				getUrlStringFrom(d.get("text")), d.get("hashtags"),
				d.get("profileImageUrl"));

		return t;
	}

	private String getUrlStringFrom(String text) {
		String[] words = text.split("\\s+|”|\"");
		String urlString = null;

		for (String word : words)
			try {
				URL url = new URL(word); // parse with URL constructor
				urlString = url.toString(); // but just returns a string

				if (urlString.endsWith(".")) {
					urlString = urlString.substring(0, urlString.length() - 1); // shave
																				// off
																				// "."
				}
			} catch (MalformedURLException e) {
				// Exception-based control flow FTW
			}

		return urlString;
	}
}
