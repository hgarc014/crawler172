package ui.client.services;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ui.shared.Tweet;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("luceneServlet")
public interface LuceneService extends RemoteService {
	public List<Tweet> getTweets(String searchText, String type);

}
