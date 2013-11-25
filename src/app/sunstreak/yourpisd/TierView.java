package app.sunstreak.yourpisd;

import android.content.Context;
import android.widget.TextView;

public class TierView extends TextView {

	public static String[] VALUES = {"Pass", "C-", "C", "C+", "B-", "B", "B+", "A-", "A", "A+"};
	public static int[]    RANGES = {    70,   71,  73,   77,   80,  83,   87,   90,  93,   97};
	public static int DEFAULT_INDEX = 9;
	
	public int index;
	
	public TierView(Context context) {
		super(context);
		index = DEFAULT_INDEX;
		setText(VALUES[index]);
	}
	
	public TierView(Context context, int index) {
		super(context);
		this.index = index;
		setText(VALUES[index]);
	}
	
	public boolean increment() {
		if (index < VALUES.length - 1)
		{
			setText(VALUES[++index]);
			return true;
		}
		return false;
			
	}
	
	public boolean decrement() {
		if (index > 0)
		{
			setText(VALUES[--index]);
			return true;
		}
		return false;
			
	}

}
