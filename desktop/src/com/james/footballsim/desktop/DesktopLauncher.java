package com.james.footballsim.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.james.footballsim.FootballSim;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 750/2;
		config.height = 1334/2;
		new LwjglApplication(new FootballSim(), config);
	}
}
