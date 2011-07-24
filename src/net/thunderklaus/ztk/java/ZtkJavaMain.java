package net.thunderklaus.ztk.java;

import net.thunderklaus.ztk.core.MyGame;
import forplay.core.ForPlay;
import forplay.java.JavaPlatform;

public class ZtkJavaMain {
	
	public static void main(String[] args) {
		JavaPlatform.register();
		ForPlay.run(new MyGame());
	}
}
