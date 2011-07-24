package net.thunderklaus.ztk.gwt;

import net.thunderklaus.ztk.core.MyGame;

import com.google.gwt.core.client.EntryPoint;

import forplay.core.ForPlay;
import forplay.html.HtmlPlatform;

public class ZtkEntryPoint implements EntryPoint {
	
	public void onModuleLoad() {
		HtmlPlatform.register();
		ForPlay.run(new MyGame());
	}
}
