package app.sunstreak.yourpisd.util;

import java.util.Random;

import android.graphics.Color;

public class RandomStuff {
	
	public static final String[][] leetReplacements = new String[][] {
		{"Evaluations", "3v17"},
		{"Work", "Drudg3ry"},
		{"and", "&"},
		{"And", "&"},
		{"at", "@"},
		{"At", "@"},
		{"or$", "x0r"},
		{"ors$", "x0rz"},
		{"new", "n00b"},
		{"New", "n00b"},
		{"a", "4"},
		{"A", "4"},
		{"e", "3"},
		{"E", "3"},
		{"i", "1"},
		{"I", "1"},
		{"o", "0"},
		{"O", "0"},
		{"s", "5"},
		{"S", "5"}
	};

	public static int randomColor() {
		Random r = new Random();
		int red = r.nextInt(256);
		int green = r.nextInt(256);
		int blue = r.nextInt(256);
		return Color.rgb(red, green, blue);
	}
	
	public static CharSequence toLeetString (CharSequence text) {
		String s = text.toString();
		for (String[] pair : leetReplacements) {
			s = s.replaceAll(pair[0], pair[1]);
		}
		
		return s;
	}
}
