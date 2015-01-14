package app.sunstreak.yourpisd.view;

import android.content.Context;
import android.graphics.Typeface;
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
			setTextSize(getTextSize() * 1.1f);
		}
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
	    // code to check text for null omitted
		if(DateHelper.isAprilFools())
			text = RandomStuff.toLeetString(text);
		
		// Even if the text is modified, we still need to call super.setText()
		// to actually put the text in the TextView.
		super.setText(text, type);
	}
}
