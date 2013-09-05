package com.sunstreaks.mypisd.net;

import java.io.IOException;
import java.util.ArrayList;

import android.os.AsyncTask;

public class RequestTask extends AsyncTask<Object, Void, Object> {

		Object[] response = new Object[]{};
	
		@Override
		protected Object doInBackground(Object... params) {
			
			
			try {
				
				
				String requestMode = (String) params[0];	// GET or POST
				String url = (String) params[1];			// url
				ArrayList<String> cookies = (ArrayList<String>) params[2];
				ArrayList<String[]> requestProperties = (ArrayList<String[]>) params[3];
				
				
				if (requestMode.equals("GET")) {
					response =  Request.sendGet(url, cookies, requestProperties, Request.isSecure(url));
				}
				else if (requestMode.equals("POST")) {
					String postParams = postParams = (String) params[5];
					response = Request.sendPost(url, cookies,requestProperties, Request.isSecure(url), postParams);
				} else {
					System.err.println("The request is neither a GET nor a POST.");
				}
			
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalUrlException e) {
				e.printStackTrace();
				return null;
			}
			
				return response;
			/*
			 * params for each Request method:
			 * 
			 * Request.sendGet
			 * (String url, ArrayList<String> cookies, ArrayList<String[]> requestProperties, boolean isSecure)
			 * 
			 * Request.sendPost
			 * (String url, ArrayList<String> cookies, ArrayList<String[]> requestProperties, boolean isSecure, String postParams)
			 */
			
		}
		
		
		protected void onPostExecute (Object result) {
			super.onPostExecute(result);
		}

	}