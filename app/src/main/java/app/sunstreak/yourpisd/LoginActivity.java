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

import java.io.IOException;
import java.io.InputStream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Config;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import app.sunstreak.yourpisd.net.Session;
import app.sunstreak.yourpisd.net.Student;
import app.sunstreak.yourpisd.util.DateHelper;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends ActionBarActivity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	private Session session;

	// Values for email and password at the time of the login attempt.
	private String mEmail = "";
	private String mPassword = "";
	private String encryptedPass;
	private boolean mRememberPassword;
	private boolean mAutoLogin;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private LinearLayout mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private CheckBox mRememberPasswordCheckBox;
	private CheckBox mAutoLoginCheckBox;
    private int height;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
		final SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y;
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = (LinearLayout)findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		if(DateHelper.isAprilFools())
		{
			LinearLayout container = (LinearLayout)mLoginFormView.findViewById(R.id.container);
			ImageView logo = (ImageView)container.findViewById(R.id.logo);
			InputStream is;
			try {
				is = getAssets().open("doge.png");
				logo.setImageBitmap(BitmapFactory.decodeStream(is));
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mAutoLogin = sharedPrefs.getBoolean("auto_login", false);
		System.out.println(mAutoLogin);

		session = ((YPApplication)getApplication()).session;

		try {
			boolean refresh = getIntent().getExtras().getBoolean("Refresh");

			if (refresh) {
				mEmail = session.getUsername();
				mPassword = session.getPassword();
				showProgress(true);
				mAuthTask = new UserLoginTask();
				mAuthTask.execute((Void) null);

				InputMethodManager imm = 
						(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
			}
			else
				mLoginFormView.setVisibility(View.VISIBLE);
		} catch (NullPointerException e) {
			// Keep going.
		}



		if (sharedPrefs.getBoolean("patched", false)) {
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.remove("password");
			editor.putBoolean("patched", true);
			editor.commit();
		}


		if ( ! sharedPrefs.getBoolean("AcceptedUserAgreement", false) ) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.user_agreement_title));
			builder.setMessage(getResources().getString(R.string.user_agreement));
			// Setting Positive "Yes" Button
			builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int which) {
					sharedPrefs.edit().putBoolean("AcceptedUserAgreement", true).commit();
					dialog.cancel();
				}
			});

			// Setting Negative "NO" Button
			builder.setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Write your code here to invoke NO event
					sharedPrefs.edit().putBoolean("AcceptedUserAgreement", false).commit();
					Toast.makeText(LoginActivity.this, "Quitting app", Toast.LENGTH_SHORT).show();
					finish();
				}
			});

			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}

		// Set up the remember_password CheckBox
        mRememberPasswordCheckBox = (CheckBox) findViewById(R.id.remember_password);
        mRememberPasswordCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRememberPassword = isChecked;
            }
        });

		mRememberPassword = sharedPrefs.getBoolean("remember_password", false);
		mRememberPasswordCheckBox.setChecked(mRememberPassword);

		// Set up the auto_login CheckBox
		mAutoLoginCheckBox = (CheckBox) findViewById(R.id.auto_login);
		mAutoLoginCheckBox.setChecked(mAutoLogin);
		mAutoLoginCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				mAutoLogin = isChecked;
				if (isChecked) {
					mRememberPasswordCheckBox.setChecked(true);
				}
			}

		});

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});


		//Load stored username/password
		mEmailView.setText(sharedPrefs.getString("email", mEmail));
		mPasswordView.setText(new String(Base64.decode(sharedPrefs.getString("e_password", "")
				, Base64.DEFAULT )));
		// If the password was not saved, give focus to the password.
		if(mPasswordView.getText().equals(""))
			mPasswordView.requestFocus();

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = (LinearLayout)findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {


						attemptLogin();
					}
				});
		findViewById(R.id.sign_in_button).setOnTouchListener(
				new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						InputMethodManager imm = 
								(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
						return false;
					}
				});
		mLoginFormView.setVisibility(View.VISIBLE);
		// Login if auto-login is checked.
		if (mAutoLogin)
			attemptLogin();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);

		return true;
	}


	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		encryptedPass = Base64.encodeToString(mPasswordView.getText().toString()
				.getBytes(), Base64.DEFAULT );
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString("email", mEmail);
			editor.putString("e_password", mRememberPassword? encryptedPass: "");
			editor.putBoolean("remember_password", mRememberPassword);
			editor.putBoolean("auto_login", mAutoLogin);
			editor.commit();

			// Modified from default.


			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {


			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);


//			mLoginFormView.setVisibility(View.VISIBLE);
//			mLoginFormView.animate().setDuration(shortAnimTime)
//			//.translationY(-200)
//                    .alpha(show?0:1)
//			.setListener(new AnimatorListenerAdapter() {
//				@Override
//				public void onAnimationEnd(Animator animation) {
//					mLoginFormView.setVisibility(show ? View.GONE
//							: View.VISIBLE);
//				}
//			});
//			mLoginStatusView.setVisibility(View.VISIBLE);
//			mLoginStatusView.animate().setDuration(shortAnimTime)
//			.alpha(show ? 1:0)
//			.setListener(new AnimatorListenerAdapter() {
//				@Override
//				public void onAnimationEnd(Animator animation) {
//					mLoginStatusView.setVisibility(show ? View.VISIBLE
//							: View.GONE);
//				}
//			});
//            mLoginStatusView.animate().setDuration(0).translationY(500);
            System.out.println(height);
            mLoginFormView.animate().setDuration(500).setInterpolator(new DecelerateInterpolator())
                    .translationYBy(height*-1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(View.GONE);
				}
			});
            mLoginStatusView.setVisibility(View.VISIBLE);
//            mLoginStatusView.animate().setDuration(shortAnimTime)
//			.setListener(new AnimatorListenerAdapter() {
//				@Override
//				public void onAnimationEnd(Animator animation) {
//					mLoginStatusView.setVisibility(show ? View.VISIBLE
//							: View.GONE);
//				}
//			});

			if (DateHelper.isAprilFools()) {
				mLoginStatusView.removeAllViews();


				try {
					ImageView img = new ImageView(this);
                    //noinspection ResourceType
                    img.setId(1337);
					InputStream is = getAssets().open("nyan.png");
					img.setImageBitmap(BitmapFactory.decodeStream(is));
					is.close();
					TextView april = new TextView(this);
					april.setText("Today and tomorrow, we shall pay \"homage\" to the numerous poor designs of the internet");
					april.setGravity(Gravity.CENTER_HORIZONTAL);
					mLoginStatusView.addView(img);
					mLoginStatusView.addView(april);

					RotateAnimation rotateAnimation1 = new RotateAnimation(0, 360,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					rotateAnimation1.setInterpolator(new LinearInterpolator());
					rotateAnimation1.setDuration(500);
					rotateAnimation1.setRepeatCount(Animation.INFINITE);
					img.startAnimation(rotateAnimation1);
					
					

				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

			}


			//			mLoginStatusView.animate().setDuration(shortAnimTime)
			//					.alpha(show ? 1 : 0)
			//					.setListener(new AnimatorListenerAdapter() {
			//						@Override
			//						public void onAnimationEnd(Animator animation) {
			//							mLoginStatusView.setVisibility(show ? View.VISIBLE
			//									: View.GONE);
			//						}
			//					});

			//			mLoginFormView.setVisibility(View.VISIBLE);
			//			mLoginFormView.animate().setDuration(shortAnimTime)
			//					.alpha(show ? 0 : 1)
			//					.setListener(new AnimatorListenerAdapter() {
			//						@Override
			//						public void onAnimationEnd(Animator animation) {
			//							mLoginFormView.setVisibility(show ? View.GONE
			//									: View.VISIBLE);
			//						}
			//					});


		}/* else if(getIntent().getExtras().getBoolean("Refresh")){
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}*/

	}



	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Integer, Integer> {

		private Session session;

		@Override
		protected void onPreExecute() {
			invalidateOptionsMenu();

		}

		@Override
		protected Integer doInBackground(Void... params) {


			// Lock screen orientation to prevent onCreateView() being called.
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			try {
				ConnectivityManager connMgr = (ConnectivityManager) 
						getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					// Simulate network access.
					session = Session.createSession(mEmail, mPassword);

					((YPApplication)getApplication()).session = session;

					// Update the loading screen: Signing into myPISD...
					publishProgress(0);

					int loginSuccess = session.login();
					switch (loginSuccess) {
					case -1: // Parent login error
						return -1;	// Bad password display
					case -2: // Server error
						return -2; // Server error
					case 1:
					default:
						break;
					}

					// Update the loading screen: Signing into Gradebook...
					publishProgress(1);

					int gradebookLoginSuccess = session.tryLoginGradebook();
					if (gradebookLoginSuccess != 1)
						return gradebookLoginSuccess;

					// Update the loading screen: Downloading class grades...
					publishProgress(2);

					for (Student st : session.getStudents())
						st.loadGradeSummary();

				} else {
					System.err.println("No internet connection");
					return -3;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}


			Intent startMain = new Intent(LoginActivity.this, MainActivity.class);

			finish();
			startActivity(startMain);
//            ActivityOptionsCompat options = ActivityOptionsCompat
//                    .makeSceneTransitionAnimation(activity, transitionView, DetailActivity.EXTRA_IMAGE);
//            ActivityCompat.startActivity(activity, new Intent(activity, DetailActivity.class), options.toBundle());
			//			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			// What's the purpose of the sleep below?
			// Commented out 30 December 2013 by Sidharth
			/*
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {

			}
			 */

			return 1;
		}

		@Override
		protected void onPostExecute(final Integer success) {

			mAuthTask = null;
			showProgress(false);

			switch (success) {
			case 1:
				// Un-lock the screen orientation
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				break;
			case -1:
				// Bad password
                /*
                Deprecated as of 1/14/15 @Han Li
                 */
//				mPasswordView.setError(getString(R.string.error_incorrect_password));
                SnackbarManager.show(
                        Snackbar.with(LoginActivity.this)
                                .text(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
				break;
			case -2: { // Server error
//				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int id) {
//						// User clicked OK button
//					}
//				});
//				AlertDialog alertDialog = builder.create();
//				alertDialog.setTitle("Info");
//				alertDialog.setMessage("Gradebook encountered an error. Please try again.");
//				alertDialog.setIcon(R.drawable.ic_alerts_and_states_warning);
//
//				alertDialog.show();
                SnackbarManager.show(
                        Snackbar.with(LoginActivity.this).type(SnackbarType.MULTI_LINE)
                                .text("Gradebook encountered an error. Please try again."));
				break;
			}
			case -3: { // No internet connection
//				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int id) {
//						// User clicked OK button
//					}
//				});
//				AlertDialog alertDialog = builder.create();
//				alertDialog.setTitle("Info");
//				alertDialog.setMessage("No internet connection found! Please find a connection and try again.");
//				alertDialog.setIcon(R.drawable.ic_alerts_and_states_warning);
//
//				alertDialog.show();
                SnackbarManager.show(
                        Snackbar.with(LoginActivity.this).type(SnackbarType.MULTI_LINE)
                                .text("No internet connection found! Please find a connection and try again."));
				break;
			}

			}

		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			int message;
			switch (progress[0]) {
			case 0:	message = R.string.login_progress_mypisd;			break;
			case 1: message = R.string.login_progress_gradebook;		break;
			case 2:	message = R.string.login_progress_downloading_data;	break;
			default: /* Should not occur */	return;
			}

			mLoginStatusMessageView.setText(message);
		}

	}
}
