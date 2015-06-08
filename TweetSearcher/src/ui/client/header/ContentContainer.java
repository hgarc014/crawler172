package ui.client.header;

import com.google.gwt.user.client.Window;

public class ContentContainer {
	private static ContentContainer myInstance = new ContentContainer();

	public static synchronized ContentContainer getInstance() {
		return myInstance;
	}

	private Header header;

	public void setHeader(Header h) {
		this.header = h;
	}
	
	public void updatePosition(int y){
		Window.scrollTo(0, y);
	}

	public Header getHeader() {
		return header;
	}
}
