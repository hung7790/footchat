package comp437.footchat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class PostRequest{
	
	private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	private String url;
	
	public void setUrl(String urlStr){
		this.url=urlStr;
	}
	
	public void addPost(String s1, String s2){
        nameValuePairs.add(new BasicNameValuePair(s1, s2));
	}
	
	public void clearPost(){
		nameValuePairs.clear();
	}
	
	public InputStream post() throws ClientProtocolException, IOException{
		InputStream in;
		
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(url);
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse response;
		response = httpclient.execute(httppost);
		in = response.getEntity().getContent();
		
		return in;
	}

}
