package ui.client.search;

import ui.client.header.ContentContainer;
import ui.client.header.Header;
import ui.client.resources.CSSAndImageResources;
import ui.client.services.LuceneService;
import ui.client.services.LuceneServiceAsync;
import ui.shared.Tweet;

import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class DisplayTweet extends Composite {
	private static DisplayTweetUiBinder uiBinder = GWT
			.create(DisplayTweetUiBinder.class);

	interface DisplayTweetUiBinder extends UiBinder<Widget, DisplayTweet> {

	}

	@UiField
	Anchor userName;
	@UiField
	Label body;
	@UiField
	Label retweets;
	@UiField
	Label creation;
	@UiField
	Anchor linkTitle;
	@UiField
	Label favorites;
	@UiField
	Image userImg;
	@UiField
	HorizontalPanel tags;
	@UiField
	HorizontalPanel retweetPanel;
	@UiField
	HorizontalPanel favoritePanel;
	@UiField
	HTMLPanel mainPanel;

	public HTMLPanel getMainPanel() {
		return mainPanel;
	}

	DisplayTweet(final Tweet t, double curTime) {
		long timeAgo = (long) ((curTime - t.getCreatedAt()) / 1000);
		initWidget(uiBinder.createAndBindUi(this));

		userName.setText(t.getUsername());
		userName.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (ContentContainer.getInstance().getHeader().getList()
						.getSelectedIndex() != 1) {
					updateSearchText(t.getUsername(), 1);

				} else {
					updateSearchText(ContentContainer.getInstance().getHeader()
							.getSearchBar().getText()
							+ " " + t.getUsername(), 1);
				}
			}
		});
		body.setText(t.getText());
		userImg.addErrorHandler(new ErrorHandler() {
			@Override
			public void onError(ErrorEvent event) {
				userImg.setResource(CSSAndImageResources.INSTANCE.defaultImg());
			}
		});
		if (t.getUserImg() == null) {
			userImg.setResource(CSSAndImageResources.INSTANCE.defaultImg());
		} else {
			userImg.setUrl(t.getUserImg());
		}
		userImg.setSize("50px", "50px");

		if (t.getRetweets() == 0)
			retweetPanel.setVisible(false);
		if (t.getFavoriteCount() == 0)
			favoritePanel.setVisible(false);

		retweets.setText("" + t.getRetweets());
		favorites.setText("" + t.getFavoriteCount());
		creation.setText(getTimeAgo(timeAgo));

		if (t.getLink() == null)
			linkTitle.setVisible(false);
		else {
			linkTitle.setText(t.getLinkTitle());
			linkTitle.setHref(t.getLink());
		}

		if (t.getHashtags() != null) {
			String[] separate = t.getHashtags().split(" ");
			for (final String s : separate) {
				Anchor newTag = new Anchor("#" + s);
				newTag.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (ContentContainer.getInstance().getHeader()
								.getList().getSelectedIndex() != 2) {
							updateSearchText(s, 2);
						} else {
							updateSearchText(ContentContainer.getInstance()
									.getHeader().getSearchBar().getText()
									+ " " + s, 2);
						}

					}
				});

				tags.add(newTag);
			}
		}
	}

	private void updateSearchText(String s, int index) {
		ContentContainer.getInstance().getHeader().getSearchBar().setText(s);
		ContentContainer.getInstance().getHeader().getList()
				.setSelectedIndex(index);
		ContentContainer.getInstance().updatePosition(0);
		ContentContainer.getInstance().getHeader().getSearchBar()
				.setFocus(true);
	}

	private String getTimeAgo(long seconds) {
		if (seconds / 60 == 0) {
			if (seconds == 1)
				return "1 second ago";
			else
				return seconds + " seconds ago";
		}
		seconds /= 60;
		if (seconds / 60 == 0) {
			if (seconds == 1)
				return "1 minute ago";
			else
				return seconds + " minutes ago";
		}
		seconds /= 60;
		if (seconds / 24 == 0) {
			if (seconds == 1)
				return "1 hour ago";
			else
				return seconds + " hours ago";
		}
		seconds /= 24;
		if (seconds / 7 == 0) {
			if (seconds == 1)
				return "1 day ago";
			else
				return seconds + " days ago";
		}
		seconds /= 7;
		if (seconds / 4 == 0) {
			if (seconds == 1)
				return "1 week ago";
			else
				return seconds + " weeks ago";
		}
		seconds /= 4;
		if (seconds / 12 == 0) {
			if (seconds == 1)
				return "1 month";
			else
				return seconds + " months ago";
		}
		seconds /= 12;
		if (seconds == 1)
			return "1 year";
		else
			return seconds + " years ago";

	}

}
