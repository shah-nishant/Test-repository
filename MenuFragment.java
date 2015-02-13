package com.practo.droid;

import static com.practo.droid.misc.Utils.USER_NAME;

import java.util.Calendar;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncInfo;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.cache.SimpleImageLoader;
import com.android.volley.ui.NetworkImageView;
import com.practo.droid.entity.Practice.PracticeColumns;
import com.practo.droid.fragments.AboutFragment;
import com.practo.droid.misc.Utils;
import com.practo.droid.provider.PractoDataContentProvider;
import com.practo.droid.ui.CircularImageView;
import com.practo.droid.ui.Spinner;
import com.practo.droid.ui.Switch;

import dev.dworks.libs.actionbarplus.app.ActionBarListFragment;
import dev.dworks.libs.actionbarplus.dialog.SimpleDialogFragment;
import dev.dworks.libs.actionbarplus.widget.AdapterViewICS;
import dev.dworks.libs.actionbarplus.widget.AdapterViewICS.OnItemSelectedListener;

public class MenuFragment extends ActionBarListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener,
		OnCheckedChangeListener, OnItemSelectedListener {
	
	private FragmentActivity context;
	private SharedPreferences mSharedPreferences;
	@SuppressWarnings("unused")
	private String selectedPracticeId;
	private View root;
	private int curPosition;
	private TextView user_name;
	private TextView role_name;
	private CircularImageView user_image;
	private SimpleImageLoader mImageFetcher;
    private Object mSyncObserverHandle;
	private ImageView sync_normal;
	private ProgressBar sync_progress;
	private FrameLayout sync_layout;
    private Account account;
	private boolean currentProgress;
	private View logout;
	private Switch caller_id;
	private View about;
	private TextView last_synced;
	private PracticeCursorAdapter mAdapter;
	private int selectedPracticeLocalId;
	private Spinner practice_list;
	private boolean mOpened;
	private ImageButton support_call;
	private ImageButton support_email;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.sendView("Settings");
		context = getActivity();
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String username = mSharedPreferences.getString(USER_NAME,  "");
		if(!TextUtils.isEmpty(username)){
			account = new Account(username, getString(R.string.app_account));
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_menu, null);
		initControls();
		return root;
	}

	private void initControls() {
		user_name = (TextView)root.findViewById(R.id.user_name);
		role_name = (TextView)root.findViewById(R.id.role_name);
		user_image = (CircularImageView)root.findViewById(R.id.user_image);
		last_synced = (TextView)root.findViewById(R.id.last_synced);
		
		sync_layout = (FrameLayout)root.findViewById(R.id.sync_layout);
		sync_normal = (ImageView)root.findViewById(R.id.sync_normal);
		sync_progress = (ProgressBar)root.findViewById(R.id.sync_progress);
		
		user_name.setText(mSharedPreferences.getString(Utils.ACCOUNT_NAME,  ""));//.toUpperCase(PractoApplication.getInstance().getLocale()));
		role_name.setText(Utils.toTitleCase(mSharedPreferences.getString(Utils.CURRNENT_ROLE_NAME,  "")));
		
		sync_layout.setOnClickListener(this);
		
		logout = root.findViewById(R.id.logout); 
		logout.setOnClickListener(this);
		
		caller_id = (Switch) root.findViewById(R.id.caller_id);
		caller_id.setOnCheckedChangeListener(this);
		caller_id.setChecked(mSharedPreferences.getBoolean(Utils.CALLER_ID, false));
		
		about = root.findViewById(R.id.about);
		about.setOnClickListener(this);
		
		support_call = (ImageButton) root.findViewById(R.id.support_call);
		support_call.setOnClickListener(this);
		support_email = (ImageButton) root.findViewById(R.id.support_email);
		support_email.setOnClickListener(this);

		practice_list = (Spinner) root.findViewById(R.id.practice_list);
		practice_list.setOnItemSelectedListener(this);
		setLastSynced(false);
	}

	private void setLastSynced(Boolean isSyncing) {
		Long lastSyncedTimeInMillis = mSharedPreferences.getLong(Utils.SYNC_LAST, 0);
		if(!isSyncing && lastSyncedTimeInMillis != 0){
			String last = (String) DateUtils.getRelativeTimeSpanString(
					lastSyncedTimeInMillis,
					Calendar.getInstance(PractoApplication.getInstance().getLocale()).getTimeInMillis(),
					DateUtils.MINUTE_IN_MILLIS);
			if(last.equalsIgnoreCase("0 minutes ago")){
				last = "Just now";
			}
			last_synced.setText("Last synced - "+last);	
		}
		else{
			last_synced.setText("Syncing...");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
        setLastSynced(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(!Utils.isActivityAlive(getActivity())){
			return;
		}
        if (PractoHome.class.isInstance(getActivity())) {
            mImageFetcher = ((PractoHome) getActivity()).getImageFetcher();
        }
		final int account_id = mSharedPreferences.getInt(Utils.ACCOUNT_ID, 0);
		if(0 != account_id){
			final String url = Utils.getAccountURL(String.valueOf(account_id)); 
            user_image.setDefaultImageResId(R.drawable.male_avatar);
            user_image.setImageUrl(url, mImageFetcher);
		}

        mAdapter = new PracticeCursorAdapter(getActivity(),
							                R.layout.item_menu, 
							                null,
							                new String[] { PracticeColumns.NAME,
    													   PracticeColumns.PRACTICE_LOGO_ID},
							                new int[] { R.id.title,
        												R.id.icon },
											0);
        practice_list.setAdapter(mAdapter);
        setListShown(false);
        
        if (savedInstanceState == null) {
            restartLoading();
        }
	}
	
	public void restartLoading() {
		if (mAdapter != null) {
			Utils.log("Inside reloading", "Practices");
			Loader<Cursor> loader = getLoaderManager().getLoader(0);
			setListShownNoAnimation(false);
			if (loader != null && !loader.isReset()) {
				getLoaderManager().restartLoader(0, null, this);
			} else {
				getLoaderManager().initLoader(0, null, this);
			}
		}
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Cursor practiceCursor = mAdapter.getCursor();
		if(null == practiceCursor){
			return;
		}
		int local_id = practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.ID));
		if(local_id != selectedPracticeLocalId){
			selectedPracticeLocalId = local_id;
			selectedPracticeId = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.PRACTICE_ID));
			String role = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.ROLE_NAME));
			role_name.setText(Utils.toTitleCase(role));
			
			if (getActivity() instanceof PractoHome) {
				PractoHome practoHome = (PractoHome) getActivity();
				practoHome.onNavigationChanged(local_id);
			}
		}
		else{
			closeMenu();
		}
		mAdapter.notifyDataSetChanged();
	}
	
	public int getCurrentPosition(){
		return curPosition;
	}

	private void closeMenu(){
		if (getActivity() instanceof PractoHome) {
			PractoHome practoHome = (PractoHome) getActivity();
			practoHome.closePane();
			mOpened = false;
		}
	}

	private void showProgress(boolean progress) {
		currentProgress = progress;
		sync_normal.setVisibility(progress ? View.GONE : View.VISIBLE);
		sync_progress.setVisibility(progress ? View.VISIBLE : View.GONE);
		setLastSynced(progress);
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	private boolean changeSyncStatus() {
		if(null == account || null == getActivity()){
			return false;
		}
		
		boolean syncActive = false;
		try {
			if(!Utils.hasHoneycomb()){
				SyncInfo currentSync = ContentResolver.getCurrentSync();
				syncActive = currentSync != null && currentSync.account.equals(account) && currentSync.authority.equals(PractoDataContentProvider.CONTENT_AUTHORITY);
			}
			else{
				for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
					if (syncInfo.account.equals(account) && syncInfo.authority.equals(PractoDataContentProvider.CONTENT_AUTHORITY)) {
						syncActive = true;
					}
				}
			}
			
		} catch (Exception e) {
		}

        return syncActive;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			
		case R.id.support_call:
			Utils.sendEvent(
					"Settings",  // Category
					"Support call",  // Action
					"", // Label
					(long) 1);
			Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Utils.SUPPORT_PHONE_NUMBER));
			startActivity(callIntent);
			break;

		case R.id.support_email:
			Utils.sendEvent(
					"Settings",  // Category
					"Support email",  // Action
					"", // Label
					(long) 1);
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType("text/plain");
			emailIntent.putExtra(Intent.EXTRA_EMAIL, Utils.SUPPORT_EMAIL_IDS);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, Utils.SUPPORT_EMAIL_SUBJECT);
			try {
				startActivity(Intent.createChooser(emailIntent, "Email to Ray Support"));
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
			}
			
			break;
			
		case R.id.about:
			Utils.sendEvent("Settings",
	                   "About",
	                   "",
	                   (long) 1);
			AboutFragment.show(getActivity());
			closeMenu();
			break;
		case R.id.sync_layout:
			if(!Utils.checkConnection(getActivity())){
				return;
			}
			if(!currentProgress){
				Utils.sendEvent(
						"Settings",  // Category
						"Sync now",  // Action
						"", // Label
						(long) 1);
				Utils.syncDataForce(getActivity());
				changeSyncStatus();
	            showProgress(changeSyncStatus());
			}
			break;
	
		case R.id.logout:
			closeMenu();
			((SimpleDialogFragment.SimpleDialogBuilder) SimpleDialogFragment.createBuilder(getActionBarActivity(), getFragmentManager())
					.setTitle(R.string.logout_from_ray)
					.setPositiveButtonText("Logout")
					.setNegativeButtonText("Cancel")
					.setMessage("Logging out will delete all your Practo data from this device!"))
					.setCancelable(false)
					.setRequestCode(R.id.logout)
					.show();
			break;			
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Utils.sendEvent(
				"Settings",  // Category
				"Caller Id",  // Action
				buttonView.isChecked()+"", // Label
				(long) 1);
		mSharedPreferences.edit().putBoolean(Utils.CALLER_ID, buttonView.isChecked()).commit();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {PracticeColumns.ID,
				PracticeColumns.PRACTICE_ID,
				PracticeColumns.NAME,
				PracticeColumns.PRACTICE_LOGO_ID,
				PracticeColumns.PRACTICE_SUBSCRIPTION_PLAN,
				PracticeColumns.ROLE_NAME };

        //FIXME : Now we show which are not Ray-Lite. We should do proper changes to handle lite
        String selection =  PracticeColumns.PRACTICE_SUBSCRIPTION_PLAN + " IS NOT NULL AND "
                + PracticeColumns.PRACTICE_SUBSCRIPTION_PLAN + " != ? AND "
                + PracticeColumns.ROLE_RAY_SUBSCRIPTION_ACTIVE + " = ?";
        String[] selectionArgs = new String[] {"", Utils.getBooleanValue(true)};
		
        return new CursorLoader(getActivity(),
        		PractoDataContentProvider.PRACTICES_URI,
        		projection,
        		selection,
        		selectionArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		selectedPracticeId = mSharedPreferences.getString(Utils.CURRNENT_PRACTICE_ID,"");
		String local_id = mSharedPreferences.getString(Utils.CURRNENT_PRACTICE_LOCAL_ID, "");
		if(!TextUtils.isEmpty(local_id) && TextUtils.isDigitsOnly(local_id)){
			selectedPracticeLocalId = Integer.valueOf(local_id);
		}

        mAdapter.swapCursor(data);
        practice_list.setSelection(getIndex(data));
        setListShownNoAnimation(true);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
	
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
            	
				@Override
                public void run() {
                    if (account == null) {
                    	showProgress(false);
                        return;
                    }
                    showProgress(changeSyncStatus());
                }
            });
        }
    };

	@Override
	public void onItemSelected(AdapterViewICS<?> parent, View view, int position, long id) {
		Cursor practiceCursor = mAdapter.getCursor();
		if(null == practiceCursor){
			return;
		}
		int local_id = practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.ID));
		if(local_id != selectedPracticeLocalId){
			selectedPracticeLocalId = local_id;
			selectedPracticeId = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.PRACTICE_ID));
			String role = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.ROLE_NAME));
			role_name.setText(Utils.toTitleCase(role));
			
			if (getActivity() instanceof PractoHome) {
				PractoHome practoHome = (PractoHome) getActivity();
				practoHome.onNavigationChanged(local_id);
			}
		}
		else{
			closeMenu();
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterViewICS<?> parent) {
		
	}
	
	public int getIndex(Cursor cursor) {
		int index = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					if(selectedPracticeLocalId == cursor.getInt(cursor.getColumnIndex(PracticeColumns.ID))){
						return index;
					}
					index++;
				} while (cursor.moveToNext());
			}
		}
		return index;
	}

	public void setStatus(boolean opened) {
		mOpened = opened;
		if(!opened && null != practice_list){
			practice_list.onDetachedFromWindow();
		}
		if(mOpened){
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public boolean isPeriodic(){
		return !(mSharedPreferences.getBoolean(Utils.CURRNENT_FULL_SYNC_DONE, false) || mSharedPreferences.getBoolean(Utils.CURRNENT_INIT_SYNC_DONE, false));
	}
	

	public class PracticeCursorAdapter extends SimpleCursorAdapter{

		private LayoutInflater mInflater;
		
		public PracticeCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView title;
			TextView plan_name;
			ImageView icon;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.item_menu_selected, parent, false);
			}
			
			title = ViewHolder.get(convertView, R.id.title);
			plan_name = ViewHolder.get(convertView, R.id.plan_name);
			icon = ViewHolder.get(convertView, R.id.icon);
			
			Cursor cursor = (Cursor) getItem(position);
			if(null == cursor){
				return null;
			}
			String name = cursor.getString(cursor.getColumnIndex(PracticeColumns.NAME));
			String plan = cursor.getString(cursor.getColumnIndex(PracticeColumns.PRACTICE_SUBSCRIPTION_PLAN));
			plan_name.setText(plan.replace("-", " "));
			title.setText(name);

			if(mOpened || isPeriodic()){
	    		int logo_id = cursor.getInt(cursor.getColumnIndex(PracticeColumns.PRACTICE_LOGO_ID));
				String url = Utils.getPracticeURL(String.valueOf(logo_id));
				if(logo_id != 0){
					mImageFetcher.get(url, icon, 1);
				}
				else{
					icon.setImageResource(R.drawable.practice_avatar);
				}

				//icon.setImageUrl(url, mImageFetcher);
				//icon.setDefaultImageResId(R.drawable.practice_avatar);
			}
			else{
				icon.setImageResource(R.drawable.practice_avatar);
			}

			return convertView;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			TextView title;
			TextView plan_name;
			NetworkImageView icon;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.item_menu, parent, false);
			}
			
			title = ViewHolder.get(convertView, R.id.title);
			plan_name = ViewHolder.get(convertView, R.id.plan_name);
			icon = ViewHolder.get(convertView, R.id.icon);
			
			Cursor cursor = (Cursor) getItem(position);
			if(null == cursor){
				return null;
			}
			String name = cursor.getString(cursor.getColumnIndex(PracticeColumns.NAME));
			String plan = cursor.getString(cursor.getColumnIndex(PracticeColumns.PRACTICE_SUBSCRIPTION_PLAN));
			plan_name.setText(plan.replace("-", " "));
			title.setText(name);

    		int logo_id = cursor.getInt(cursor.getColumnIndex(PracticeColumns.PRACTICE_LOGO_ID));
			String url = Utils.getPracticeURL(String.valueOf(logo_id));
			if(logo_id == 0){
				url = "";
			}
			//mImageFetcher.get(url, icon, 1);
			icon.setImageUrl(url, mImageFetcher);
			icon.setDefaultImageResId(R.drawable.practice_avatar);
			return convertView;
		}
	}
	
	public static class ViewHolder {
		@SuppressWarnings("unchecked")
		public static <T extends View> T get(View view, int id) {
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
			if (viewHolder == null) {
				viewHolder = new SparseArray<View>();
				view.setTag(viewHolder);
			}
			View childView = viewHolder.get(id);
			if (childView == null) {
				childView = view.findViewById(id);
				viewHolder.put(id, childView);
			}
			return (T) childView;
		}
	}
	
}