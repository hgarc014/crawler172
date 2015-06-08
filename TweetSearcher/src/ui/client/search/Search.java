package ui.client.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ui.client.header.ContentContainer;
import ui.client.resources.CSSAndImageResources;
import ui.client.services.LuceneService;
import ui.client.services.LuceneServiceAsync;
import ui.shared.Tweet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.events.MapEventType;
import com.google.gwt.maps.client.events.MapHandlerRegistration;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapEvent;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapHandler;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.Animation;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Search extends Composite {

	private static SearchUiBinder uiBinder = GWT.create(SearchUiBinder.class);

	interface SearchUiBinder extends UiBinder<Widget, Search> {
	}

	@UiField
	VerticalPanel results;
	@UiField
	HTMLPanel mapPanel;
	@UiField
	HorizontalPanel main;

	public Search() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	double start = 0;

	MapWidget map = null;
	LatLngBounds bounds = LatLngBounds.newInstance(LatLng.newInstance(0, 0),
			LatLng.newInstance(0, 0));

	// List<Marker> markers = new ArrayList<Marker>();

	public Search(String query, String type) {
		initWidget(uiBinder.createAndBindUi(this));

		main.setWidth("90%");
		main.setCellWidth(mapPanel, "50%");
		main.setCellWidth(results, "50%");
		results.setWidth("90%");
		mapPanel.setWidth("90%");
		mapPanel.clear();

		LatLng location = LatLng.newInstance(33.8833, -117.0167);
		MapOptions opts = MapOptions.newInstance();
		opts.setCenter(location);
		opts.setMapTypeId(MapTypeId.ROADMAP);
		opts.setZoom(6);
		// final MapWidget theMap = new MapWidget(opts);
		map = new MapWidget(opts);
		map.setSize("500px", "500px");
		mapPanel.add(map);
		mapPanel.setVisible(false);

		// map.setSize("500px", "500px");

		LuceneServiceAsync luceneService = GWT.create(LuceneService.class);
		start = System.currentTimeMillis();
		luceneService.getTweets(query, type, new AsyncCallback<List<Tweet>>() {

			@Override
			public void onFailure(Throwable caught) {
				System.err.println(caught.getLocalizedMessage());
			}

			@Override
			public void onSuccess(List<Tweet> result) {
				if (result.size() != 0) {
					double timeres = ((System.currentTimeMillis() - start) * 1.0) / 1000;
					results.add(new Label(result.size() + " tweets found in "
							+ timeres + " seconds"));
					for (Tweet t : result) {
						final DisplayTweet dis = new DisplayTweet(t, start);
						final Marker m = addMarkerToMap(t.getLatitude(),
								t.getLongitude());

						m.addClickHandler(new ClickMapHandler() {

							@Override
							public void onEvent(ClickMapEvent event) {
								dis.getMainPanel().setStyleName(
										CSSAndImageResources.INSTANCE.main()
												.standout(), true);
								ContentContainer.getInstance()
										.updatePosition(
												dis.getMainPanel()
														.getAbsoluteTop() - 2);
								Timer t = new Timer() {
									@Override
									public void run() {
										dis.getMainPanel().setStyleName(
												CSSAndImageResources.INSTANCE
														.main().sep());
										dis.getMainPanel().setStyleName(
												CSSAndImageResources.INSTANCE
														.main().hover(), true);
									}
								};

								t.schedule(1000);
							}
						});

						dis.getMainPanel().addDomHandler(new ClickHandler() {

							@Override
							public void onClick(ClickEvent event) {
								if (m.getAnimation() == null) {
									m.setAnimation(Animation.BOUNCE);
									Timer t = new Timer() {
										@Override
										public void run() {
											m.setAnimation(null);
											cancel();
										}
									};
									ContentContainer.getInstance()
											.updatePosition(0);
									t.schedule(780);
								}
							}
						}, ClickEvent.getType());

						results.add(dis);

					}
					resize(true);
				} else {
					Label nores = new Label("Sorry, no results were found!");
					nores.setStyleName(CSSAndImageResources.INSTANCE.main()
							.nowrap());
					results.add(nores);
					resize(false);
				}

			}
		});
	}

	private Marker addMarkerToMap(double lat, double lng) {
		LatLng loc = LatLng.newInstance(lat, lng);
		MarkerOptions mopts = MarkerOptions.newInstance();
		mopts.setIcon(MarkerImage.newInstance(CSSAndImageResources.INSTANCE
				.markerRed().getSafeUri().asString()));
		mopts.setPosition(loc);
		Marker localMark = Marker.newInstance(mopts);
		bounds.extend(loc);
		localMark.setMap(map);
		mapPanel.setVisible(true);
		return localMark;
	}

	public void resize(Boolean fitMap) {
		if (fitMap)
			map.fitBounds(bounds);
		LatLng center = map.getCenter();
		MapHandlerRegistration.trigger(map, MapEventType.RESIZE);
		map.setCenter(center);
	}
}