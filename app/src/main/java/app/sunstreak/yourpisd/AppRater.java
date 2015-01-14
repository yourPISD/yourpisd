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

package app.sunstreak.yourpisd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {
    private static final String RATE_YOUR_PISD = "Rate yourPISD";
	private static final String REMIND_ME_LATER = "Remind me later";
	private static final String NO_THANKS = "No, thanks";
	private static final String RATE_MESSAGE = "Enjoying yourPISD? Please take a few seconds to rate us. Thank you for your support.";
	private final static String APP_TITLE = "yourPISD";
    private final static String APP_PNAME = "app.sunstreak.yourpisd";
    
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;
    
    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }
        
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + 
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }
        
        editor.commit();
    }   
    
    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("Rate " + APP_TITLE);
        dialog.setMessage(RATE_MESSAGE);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, RATE_YOUR_PISD, new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog, int id) {
            	mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
          } }); 

          dialog.setButton(DialogInterface.BUTTON_NEGATIVE, NO_THANKS, new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog, int id) {
            	if (editor != null) {
                  editor.putBoolean("dontshowagain", true);
                  editor.commit();
              }
              dialog.dismiss();
          }}); 

          dialog.setButton(DialogInterface.BUTTON_NEUTRAL, REMIND_ME_LATER, new DialogInterface.OnClickListener() {

            @Override
			public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
          }});

                
        dialog.show();        
    }
}