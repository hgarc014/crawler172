package com.tweetsearch;

import static spark.Spark.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.index.IndexableField;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class SearchServer {

	public static void main(String[] args) {
		get("/search/*/*", (request, response) -> {
			Directory directory = null;
			
			// TODO verify args
			File indexDir  = new File(args[0]);
			
			try {
			    directory = FSDirectory.open(indexDir);
			    if (directory.listAll().length <= 0) {
			    	halt (401, "Index has yet to be initialized");
			    	System.exit(1);
			    }
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to read directory: " + e.getMessage());
				halt(401, "Server failure");
			}
			
			
			
			DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    StandardAnalyzer analyzer = new StandardAnalyzer();
		    
		    String fieldname = request.splat()[0];
		    QueryParser queryParser = new QueryParser(fieldname,analyzer);
		    
		    String queryText = request.splat()[1];
		    Query query = queryParser.parse(queryText);
			
		    ScoreDoc[] hits = isearcher.search(query, null, 10).scoreDocs; // top 10 results
		    
		    List<ScoreDoc> hitsList = new ArrayList<ScoreDoc>(Arrays.asList(hits));
			JSONArray jsonArray = new JSONArray();
		    
		    for (ScoreDoc hit: hitsList) {
		    	Document d = isearcher.doc(hit.doc);
		    	List<IndexableField> fieldList = d.getFields();
		    	JSONObject json = new JSONObject();
		    	
		    	for (IndexableField field : fieldList) { 
		    		json.put(field.name(), d.get(field.name())); 
		    	}
		    	
		    	jsonArray.add(json);
		    }
					
			response.type("application/json");
			directory.close();
			return jsonArray.toJSONString();
			
		});
	}
}
