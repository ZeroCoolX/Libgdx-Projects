package com.thunderpaws.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.thunderpaws.AnimationDemo;
import com.thunderpaws.MapDemo;
import com.thunderpaws.PlayerAndMapDemo;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new PlayerAndMapDemo(), config);
	}
}
