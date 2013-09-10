package com.sunstreaks.mypisd;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.sunstreaks.mypisd.net.DataGrabber;
import com.sunstreaks.mypisd.net.Domain;
import com.sunstreaks.mypisd.net.PISDException;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private boolean mRememberPassword;
	
	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private Spinner domainSpinner;
	private CheckBox rememberPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		// Set up the Spinner.
		List<String> SpinnerArray =  new ArrayList<String>();
		for (Domain d : Domain.values()) {
			SpinnerArray.add(d.name());
		}
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SpinnerArray);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    domainSpinner = (Spinner) findViewById(R.id.domain_spinner);
	    domainSpinner.setAdapter(adapter);
		
	    //Set up the remember_password checkbox
	    rememberPassword = (CheckBox) findViewById(R.id.remember_password);
		rememberPassword.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (rememberPassword.isChecked()) {
						mRememberPassword = true;
					}
					else {
						mRememberPassword = false;
					}
				}
				
			});
		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		//mEmailView.setText(mEmail);

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
		SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
		mEmailView.setText(sharedPrefs.getString("email", mEmail));
		mPasswordView.setText(sharedPrefs.getString("password", ""));
		mRememberPassword = sharedPrefs.getBoolean("remember_password", false);
		rememberPassword.setChecked(mRememberPassword);
		try {
			domainSpinner.setSelection(sharedPrefs.getInt("domain", 0));
		} catch (IndexOutOfBoundsException e) {
			// Do not set the index.
		}
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
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
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} /*else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}*/

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}
		//requires @ if parent account selected
		else if (domainSpinner.getSelectedItem().toString().equals("PARENT") && !mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
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
			editor.putInt("domain", domainSpinner.getSelectedItemPosition());
			editor.putString("email", mEmail);
			editor.putBoolean("remember_password", mRememberPassword);
			editor.putString("password", mRememberPassword? mPassword: "");
			editor.commit();
			
			// Modified from default.
			
			

			//mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
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

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
			

		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
		
	}


	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		
		private DataGrabber dg;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				ConnectivityManager connMgr = (ConnectivityManager) 
				        getSystemService(Context.CONNECTIVITY_SERVICE);
				    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				    if (networkInfo != null && networkInfo.isConnected()) {
						// Simulate network access.
						dg = new DataGrabber(
								Domain.valueOf(domainSpinner.getSelectedItem().toString()),
								mEmail,
								mPassword);
						
						// Update the loading screen.
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 ((TextView)mLoginStatusMessageView).setText(R.string.login_progress_mypisd);
						    }
						});
						

						
						dg.login();
						if (dg.getEditureLogin() == -1) {
					    	return false;
						}
						
						
						// Update the loading screen.
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 ((TextView)mLoginStatusMessageView).setText(R.string.login_progress_gradebook);
						    }
						});
						
						{
							String[] ptc = dg.getPassthroughCredentials();
							boolean loginAttempt = false;
							int counter = 0;
							do {
								if (counter > 0) {
									try {
										Thread.sleep(3000);
										System.out.println("trying again");
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								loginAttempt = dg.loginGradebook(ptc[0], ptc[1], mEmail, mPassword);
								counter++;
							} while (counter < 5 && loginAttempt == false);
						}

						// Update the loading screen.
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 ((TextView)mLoginStatusMessageView).setText(R.string.login_progress_downloading_data);
						    }
						});
						
						// No longer gets all class grades on load. Too slow!
						//JSONArray classGrades = d.getAllClassGrades();
						JSONArray gradeSummary = dg.loadGradeSummary();
//						System.out.println(gradeSummary.toString());
						// Store class grades in Shared Preferences.
						System.out.println("Done getting data.");
						SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
						SharedPreferences.Editor editor = sharedPrefs.edit();
						editor.putString("gradeSummary", gradeSummary.toString());
						editor.commit();
				    } else {
				    	System.err.println("No internet connection");
				    	// Program exits mysteriously here.
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 AlertDialog.Builder popupBuilder = new AlertDialog.Builder(LoginActivity.this);
							    	TextView myMsg = new TextView(LoginActivity.this);
							    	myMsg.setText("No internet connection detected! Please find a connection and try again.");
							    	myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
							    	popupBuilder.setView(myMsg);
						    }
						});
				    }
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}

			// TODO: register the new account here.
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
				Intent startMain = new Intent(LoginActivity.this, MainActivity.class);
				startMain.putExtra("DataGrabber", dg);
				startActivity(startMain);
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
