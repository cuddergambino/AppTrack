package com.dopamine.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class DopamineRequest extends AsyncTask<String, Void, String> {
	
	public String resultFunction, resultString, error = "", status;
	public JSONArray arguments;

	public DopamineRequest() {
	}

	@Override
	protected String doInBackground(String... params) {

		SSLContext ctx = null;
		InputStream inputStream = null;
		
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[] { new CustomX509TrustManager() }, new SecureRandom());

			String url_select = params[1];
			HttpClient httpClient = new DefaultHttpClient();


			SSLSocketFactory ssf = new CustomSSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = httpClient.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));



			HttpPost httpPost = new HttpPost(url_select);


			StringEntity se = null;
			try {
				se = new StringEntity(params[0]);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}


			httpPost.setEntity(se);
			//sets a request header so the page receving the request
			//will know what to do with it
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");


			// Read content & Log

			try {
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();

				inputStream = httpEntity.getContent();
			} catch (IOException e) {
				e.printStackTrace();
			}


			// Convert response to string using String Builder
			
			try {
				BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				StringBuilder sBuilder = new StringBuilder();

				String line = null;
				while ((line = bReader.readLine()) != null) {
					sBuilder.append(line + "\n");
				}

				inputStream.close();
				
				resultString = sBuilder.toString();
				System.out.println(resultString);
				JSONObject jsonResponse = new JSONObject(resultString);
				
				if(jsonResponse.has("reinforcementFunction"))
					resultFunction = jsonResponse.getString("reinforcementFunction");
				if(jsonResponse.has("reinforcementArguments")){
					String array = jsonResponse.getString("reinforcementArguments");
					arguments = new JSONArray(array);
				}
				if(jsonResponse.has("error")){
					JSONArray array = new JSONArray(jsonResponse.getString("error"));
					if(array.length() > 0)
						error = array.toString();
				}
				if(jsonResponse.has("status"))
					status = jsonResponse.getString("status");

			} catch (Exception e) {
				Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
			}


		} catch (Exception e) {

		}


		return resultFunction;
    }


    @Override
    protected void onPostExecute(String o) {
    	super.onPostExecute(o);
    }


}