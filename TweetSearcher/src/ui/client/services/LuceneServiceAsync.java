package ui.client.services;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ui.shared.Tweet;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LuceneServiceAsync {
	public void getTweets(String searchText, String type,
			AsyncCallback<List<Tweet>> callback);

}
