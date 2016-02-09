package com.thunderpaws.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.thunderpaws.ThunderPaws;
import com.thunderpaws.HelloWorld;
import com.thunderpaws.GraphicsDemo;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new GraphicsDemo(), config);
	}
}
