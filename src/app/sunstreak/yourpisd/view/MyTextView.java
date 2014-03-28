package app.sunstreak.yourpisd.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import app.sunstreak.yourpisd.util.DateHelper;
import app.sunstreak.yourpisd.util.RandomStuff;

public class MyTextView extends TextView {
	
	public static Typeface typeface;

	public MyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyTextView(Context context) {
		super(context);
		init();
	}

	private void init() {
		if (!isInEditMode()) {
			setTypeface(typeface);
		}
		if (DateHelper.isAprilFools()) {
			setTextColor(RandomStuff.randomColor());
			setTextSize(getTextSize() * 1.25f);
		}
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
	    // code to check text for null omitted
	    text = RandomStuff.toLeetString(text);
	    super.setText(text, type);
	}
}
