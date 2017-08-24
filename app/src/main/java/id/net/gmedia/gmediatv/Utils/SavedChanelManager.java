package id.net.gmedia.gmediatv.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SavedChanelManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "GmediaTV";

	// All Shared Preferences Keys
	public static final String TAG_Nama = "nama";
	public static final String TAG_Link = "link";

	// Constructor
	public SavedChanelManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void saveLastChanel(String nama, String link){

		editor.putString(TAG_Nama, nama);
		
		editor.putString(TAG_Link, link);

		editor.commit();
	}	

	
	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getLastChanel(){

		HashMap<String, String> user = new HashMap<String, String>();
		user.put(TAG_Nama, pref.getString(TAG_Nama, null));
		user.put(TAG_Link, pref.getString(TAG_Link, null));
		return user;
	}

	public String getNama(){
		return pref.getString(TAG_Nama, null);
	}

	public String getLink(){
		return pref.getString(TAG_Link, null);
	}

	// Get Login State
	public boolean isSaved(){
		if(getLastChanel().get(TAG_Link) != null && !getLastChanel().get(TAG_Link).equals("")){
			return true;
		}else{
			return false;
		}
	}
}
