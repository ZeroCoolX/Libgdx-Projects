package com.thunderpaws;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.thunderpaws.ThunderPaws;
import com.thunderpaws.HelloWorld;
import com.thunderpaws.GraphicsDemo;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GraphicsDemo(), config);
	}
}
