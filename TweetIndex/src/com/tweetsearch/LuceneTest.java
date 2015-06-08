package com.tweetsearch;

import java.io.File;
import java.io.IOException;

import static java.nio.file.Paths.get;

import org.apache.lucene.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class LuceneTest {

	public static void main(String[] args) throws IOException, ParseException {
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Directory directory = null;
		
		try {
		    directory = FSDirectory.open(new File("/tmp/testindex"));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("failed to read directory: " + e.getMessage());
			System.exit(1);
		}
		
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,analyzer);
		IndexWriter iwriter = null;
		try {
			iwriter = new IndexWriter(directory, config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("failed to write to index" + e.getMessage());
			System.exit(1);
		}
		
		Document doc = new Document();
	    String text = "This is the text to be indexed.";
	    doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
	    iwriter.addDocument(doc);
	    iwriter.close();
	    
	    DirectoryReader ireader = DirectoryReader.open(directory);
	    IndexSearcher isearcher = new IndexSearcher(ireader);
	    QueryParser parser = new QueryParser("fieldname", analyzer); // notice that it gets instantiated with a given fieldname
	    Query query = parser.parse("text");
	    
	    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
	    
	    if (hits.length != 1) {
	    	System.err.println("Unruly sizes afoot");
	    	System.exit(1);
	    }
	    
	    for (int i = 0; i < hits.length; ++i) {
	    	Document hitDoc = isearcher.doc(hits[i].doc);
	    	if (!hitDoc.get("fieldname").equals("This is the text to be indexed.")) {
	    		System.err.println("Lucene failed you: " + hitDoc.get("fieldname"));
		    	System.exit(1);
	    	}
	    }
		
	    ireader.close();
	    directory.close();
	    
	    System.out.println("Successfully ran to completion");
		
	}

}
