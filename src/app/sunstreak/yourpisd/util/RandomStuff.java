package app.sunstreak.yourpisd.util;

import java.util.Random;

import android.graphics.Color;
import app.sunstreak.yourpisd.R;

public class RandomStuff {

	public static final String[][] leetReplacements = new String[][] {
			{ "Evaluations", "3v17" }, { "Work", "Drudg3ry" }, { "and", "&" },
			{ "And", "&" }, { "at", "@" }, { "At", "@" }, { "or$", "x0r" },
			{ "ors$", "x0rz" }, { "new", "n00b" }, { "New", "n00b" },
			{ "a", "4" }, { "A", "4" }, { "e", "3" }, { "E", "3" },
			{ "i", "1" }, { "I", "1" }, { "o", "0" }, { "O", "0" },
			{ "s", "5" }, { "S", "5" } };

	public static String[] QUOTES = new String[] {
			"Education is the most powerful weapon which you can use to change the world.",
			"Any man who reads too much and uses his own brain too little falls into lazy habits of thinking.",
			"It is the mark of an educated mind to be able to entertain a thought without accepting it.",
			"The only thing that interferes with my learning is my education.",
			"My mother said I must always be intolerant of ignorance but understanding of illiteracy. That some people, unable to go to school, were more educated and more intelligent than college professors.",
			"Education is an admirable thing, but it is well to remember from time to time that nothing that is worth knowing can be taught.",
			"The things I want to know are in books; my best friend is the man who'll get me a book I ain't read.",
			"Education is the ability to listen to almost anything without losing your temper or your self-confidence.",
			"In the first place, God made idiots. That was for practice. Then he made school boards.",
			"The roots of education are bitter, but the fruit is sweet.",
			"An investment in knowledge pays the best interest.",
			"Education is the best friend. An educated person is respected everywhere. Education beats the beauty and the youth.",
			"Education is not the filling of a pail, but the lighting of a fire.",
			"An education isn't how much you have committed to memory, or even how much you know. It's being able to differentiate between what you know and what you don't.",
			"The only person who is educated is the one who has learned how to learn and change.",
			"A child miseducated is a child lost.",
			"The goal of education is the advancement of knowledge and the dissemination of truth.",
			"It is what you read when you don't have to that determines what you will be when you can't help it.",
			"It is a thousand times better to have common sense without education than to have education without common sense.",
			"Education is not preparation for life; education is life itself.",
			"He who opens a school door, closes a prison.",
			"Don't limit a child to your own learning, for he was born in another time.",
			"A man who has never gone to school may steal from a freight car; but if he has a university education, he may steal the whole railroad.",
			"Develop a passion for learning. If you do, you will never cease to grow.",
			"Education is the key to unlock the golden door of freedom." };

	public static int randomColor() {
		Random r = new Random();
		int red = r.nextInt(256);
		int green = r.nextInt(256);
		int blue = r.nextInt(256);
		return Color.rgb(red, green, blue);
	}

	public static CharSequence toLeetString(CharSequence text) {
		String s = text.toString();
		for (String[] pair : leetReplacements) {
			s = s.replaceAll(pair[0], pair[1]);
		}

		return s;
	}

	public static String getRandomQuote() {
		int n = (int) (1 + Math.random() * QUOTES.length);
		return QUOTES[n];
	}

	public static int gradeColor(int grade) {
		if (grade > 100)
			return R.color.black;
		if (grade >= 90)
			return R.color.green;
		if (grade >= 80)
			return R.color.yellow;

		return R.color.red;
	}
}
