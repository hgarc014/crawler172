package ui.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ClientBundle.Source;

public interface CSSAndImageResources extends ClientBundle {
	public static final CSSAndImageResources INSTANCE = GWT
			.create(CSSAndImageResources.class);
	
	@Source("images/redMarker.png")
	ImageResource markerRed();
	
	@Source("images/default.jpg")
	ImageResource defaultImg();
	
	@Source("images/header.jpg")
	ImageResource headerImg();
	

	@Source("css/header.css")
	header header();

	@Source("css/main.css")
	main main();

	public interface header extends CssResource {
		String center();
		String pageAlign();
	}

	public interface main extends CssResource {
		String button();
		String sep();
		String sepLeft();
		String center();
		String largeText();
		String pointer();
		String hover();
		String nowrap();
		String standout();
		String sameWidth();
	}
}