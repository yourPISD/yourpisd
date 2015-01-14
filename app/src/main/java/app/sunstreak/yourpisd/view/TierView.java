/**
 * This file is part of yourPISD.
 *
 *  yourPISD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  yourPISD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with yourPISD.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.sunstreak.yourpisd.view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import app.sunstreak.yourpisd.R;

public class TierView extends TextSwitcher {

	public static String[] VALUES = {"Pass", "C-", "C", "C+", "B-", "B", "B+", "A-", "A", "A+"};
	public static int[]    RANGES = {    70,   71,  73,   77,   80,  83,   87,   90,  93,   97};
	public static int DEFAULT_INDEX = 9;
	
	private final Animation upIn = AnimationUtils.loadAnimation(getContext(), R.anim.in_from_right);
	private final Animation upOut = AnimationUtils.loadAnimation(getContext(), R.anim.out_to_left);
	private final Animation downIn = AnimationUtils.loadAnimation(getContext(), R.anim.in_from_left);
	private final Animation downOut = AnimationUtils.loadAnimation(getContext(), R.anim.out_to_right);
	
	private int index;
	
	public TierView(final Context context, final Typeface typeface) {
		super(context);
		setFactory(new ViewFactory() {
			@Override
			public View makeView() {
				TextView tv = new TextView(context);
				tv.setWidth(70);
				tv.setTypeface(typeface);
				tv.setTextSize(25);
				tv.setGravity(Gravity.CENTER);
				return tv;
			}
		});
		
		index = DEFAULT_INDEX;
		setText(VALUES[index]);
	}
	
	public TierView(Context context, int index) {
		super(context);
		this.index = index;
		setText(VALUES[this.index]);
	}
	
	public void setText (int index) {
		this.index = index;
		setText(VALUES[index]);
	}
	
	public int getIndex () {
		return index;
	}
	
	public boolean increment() {
		if (index < VALUES.length - 1) {
			setInAnimation(upIn);
			setOutAnimation(upOut);
			setText(VALUES[++index]);
			return true;
		}
		return false;
			
	}
	
	public boolean decrement() {
		if (index > 0) {
			setInAnimation(downIn);
			setOutAnimation(downOut);
			setText(VALUES[--index]);
			return true;
		}
		return false;
			
	}

}
