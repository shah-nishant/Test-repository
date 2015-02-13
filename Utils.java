package com.practo.droid.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.util.ArrayMap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestTickle;
import com.android.volley.toolbox.VolleyTickle;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.mygson.Gson;
import com.joshdholtz.sentry.Sentry;
import com.joshdholtz.sentry.Sentry.SentryEventBuilder;
import com.joshdholtz.sentry.Sentry.SentryEventBuilder.SentryEventLevel;
import com.practo.droid.PractoApplication;
import com.practo.droid.R;
import com.practo.droid.entity.Appointments.Appointment;
import com.practo.droid.entity.DeviceSubscription;
import com.practo.droid.entity.Practice.PracticeColumns;
import com.practo.droid.entity.Roles;
import com.practo.droid.provider.PractoDataContentProvider;
import com.showcaseview.ShowcaseView;
import com.showcaseview.ShowcaseView.ConfigOptions;

import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Utility functions and settings
 */
public class Utils {

	public static final String API_URL = "https://solo.practo.com";
	public static final int SYNC_FREQUENCY_HOURLY = 3600; // 60 minutes, this
															// number is IN
															// SECONDS!
	public static final int SYNC_FREQUENCY_FOUR_HOURS = 4 * SYNC_FREQUENCY_HOURLY; // 4
																					// hours

	public static final int ACCOUNT_AUTHORIZATION_FAILED_NOTIFICATION = 42;
	public static final int INIT_SYNC_FINISHED_NOTIFICATION_ID = 43;
	public static final int API_DATA_ERROR_NOTIFICATION = 44;
	public static final int EMAIL_SENT_NOTIFICATION_ID = 45;

	public static final String LAST_ACCESSED_APPOINTMENTS = "com.practo.droid.misc.U.LAST_ACCESSED_APPOINTMENTS";
	public static final String LAST_ACCESSED_PATIENTS = "com.practo.droid.misc.U.LAST_ACCESSED_PATIENTS";
	public static final String LAST_ACCESSED_SETTINGS = "com.practo.droid.misc.U.LAST_ACCESSED_SETTINGS";

	private static final String TAG = "Utils";
	public static final String FRAGMENT_OPERATION = "operation";
	public static final String CROUTON_MSG = "crouton_msg";

	public static final String REQUEST_CODE = "request_code";
	public static final int RESULT_SUCCESS = 10;
	public static final int RESULT_FAILURE = 20;

	// Patient starts with 101
	public static final int REQUEST_PATIENT_ADD = 101;
	public static final int REQUEST_PATIENT_EDIT = 102;
	public static final int REQUEST_PATIENT_DELETE = 103;
	public static final int REQUEST_PATIENT_APPOINTMENT_ADD = 104;
	public static final int REQUEST_PATIENT_VIEW = 105;
	public static final int REQUEST_PATIENT_FILE_ADD = 106;
	public static final int REQUEST_PATIENT_FILE_PICK = 107;

	// Appointment starts with 201
	public static final int REQUEST_APPPOINTMENT_ADD = 201;
	public static final int REQUEST_APPPOINTMENT_PATIENT_ADD = 202;
	public static final int REQUEST_APPPOINTMENT_EDIT = 203;
	public static final int REQUEST_APPPOINTMENT_DELETE = 204;

	public static final int REQUEST_APPOINTMENT_SELECT_PATIENT = 251;
	public static final int REQUEST_APPOINTMENT_SELECT_DOCTOR = 252;
	public static final int REQUEST_APPOINTMENT_SELECT_APPOINTMENT_CATEGORY = 253;
	public static final int REQUEST_APPOINTMENT_SELECT_TREATMENT_CATEGORY = 254;
	public static final int REQUEST_APPOINTMENT_SELECT_TIME = 255;
	public static final int REQUEST_APPPOINTMENT_VIEW = 256;
	public static final int REQUEST_APPOINTMENT_SELECT_DATE = 257;

	// Misc starts with 301
	public static final int REQUEST_CAPTURE_IMAGE = 301;
	public static final int REQUEST_SELECT_IMAGE = 302;

	// Call stats with 401
	public static final int REQUEST_CALL_VIEW = 401;
	public static final int REQUEST_OUTBOUNDCALL_VIEW = 402;

	// Generic starts with 901
	public static final int REQUEST_LOGOUT = 901;

	public static final String APPOINTMENTS_COUNT = "appointments_count";
	public static final String PATIENT_FILE_ID = "file_id";
	public static final String MIME_IMAGE_JPEG = "image/jpeg";

	// Appintment add
	public static final String APPOINTMENT_PATIENT_SET = "patient_set";
	public static final String APPOINTMENT_DOCTOR_SET = "doctor_set";

	// cache
	// public static final String IMAGE_CACHE_FILES_DIR = "thumbs_files";
	public static final String IMAGE_CACHE_DIR = "thumbs";
	public static final String IMAGE_EXTRA_CACHE_DIR = "extra";
	public static final String IMAGE_CACHE_PROFILE_DIR = "profile_files";
	public static final String IMAGE_CACHE_PATIENT_FILES_DIR = "patient_files";
	public static final String IMAGE_CACHE_CALL_IN_DIR = "call_incoming_recording";
	public static final String IMAGE_CACHE_CALL_OUT_DIR = "call_outcoming_recording";
	public static final String IMAGE_CACHE_SHARE = "extra_share";

	public static final String DOT = ".";
	public static final String AS = " AS ";
	public static final String COMMA = " , ";
	public static final String IS = " = ";
	public static final String DOCTOR = "doctor";
	public static final String PATIENT = "patient";
	public static final String COLOR = "color";
	public static final String APPOINTMENT_TIME = "appointment_time";
	public static final String CATEGORY_NAME = "category_name";

	public static final String USER_NAME = "username";
	public static final String PASSWORD = "password";
	public static final String AUTH_TOKEN = "auth_token";
	public static final String ACCOUNT_ID = "account_id";
	public static final String ACCOUNT_NAME = "name";
	public static final String DEVICE_SUBSCRIPTION_ID = "device_subscription_id";
	public static final String CALLER_ID = "caller_id";

	public static final String SYNC_EXTRAS_MANUAL = "force";
	public static final String SYNC_EXTRAS_UPLOAD = "upload";
	public static final String SYNC_EXTRAS_INIT = "init";

	public static final String LOGIN_STATE = "login_state";
	public static final String SYNC_FREQUENCY = "sync_frequency";
	public static final String INIT_SYNC_DONE = "init_sync_done";
	public static final String FULL_SYNC_DONE = "full_sync_done";
	public static final String SYNC_STATE = "sync_state";
	public static final String LEGACY_LOGIN_STATE = "legacy_login_state";
	public static final String LEGACY_MIGRATED_STATE = "legacy_migrated_state";
	public static final String LEGACY_PRACTICE_SELECTED = "legacy_practice_selected";
	public static final String VERSION_UPDATED = "version_updated";
	public static final String VERSION_DEPRECATED = "version_deprecated";
	public static final String VERSION_DEPRECATED_CODE = "version_deprecated_code";
	public static final String LOGOUT_STATE = "logout_state";
	public static final String TOUR_INITIALIZED = "tour_initialized";
	public static final String APP_DATA_NOT_CLEARED = "app_data_not_cleared";

	public static final String SYNC_CHANGED = "sync_changed";
	public static final String DATA_CHANGED = "data_changed";
	public static final String SYNC_FULL_DONE = "sync_full_done";
	public static final String SYNC_INIT_DONE = "sync_init_done";
	public static final String SYNC_STATUS = "sync_status";
	public static final String SYNC_STATUS_DONE = "sync_status_done";
	public static final String SYNC_LAST = "sync_last";
	public static final String SYNC_FINISHED = "sync_finished";
	public static final String SYNC_ROLE_UPDATED = "sync_role_updated";
	public static final String SYNC_ONCE = "sync_once";

	public static final String CURRNENT_INIT_SYNC_DONE = "current_init_sync_done";
	public static final String CURRNENT_FULL_SYNC_DONE = "current_full_sync_done";
	public static final String CURRNENT_PRACTICE_ID = "current_practice_id";
	public static final String CURRNENT_PRACTICE_LOCAL_ID = "current_practice_local_id";
	public static final String CURRNENT_PRACTICE_NAME = "current_practice_name";
	public static final String CURRNENT_AUTH_TOKEN = "current_auth_token";
	public static final String CURRNENT_APPOINTMENT_DURATION = "current_appointment_duration";
	public static final String CURRNENT_ROLE_NAME = "current_role_name";
	public static final String CURRNENT_USER_ID = "current_user_id";
	public static final String CURRNENT_AUTO_INDEXED = "current_auto_indexed";
	public static final String CURRNENT_PATIENT_LABEL = "current_patient_label";
	public static final String CURRNENT_DOCTOR_LABEL = "current_doctor_label";
	public static final String CURRNENT_COUNTRY_CODE = "current_country_code";
	public static final String CURRNENT_NATIONAL_ID_LABEL = "currnent_national_id_label";
	public static final String CURRNENT_HAS_FREE = "currnent_has_free";
	public static final String CURRNENT_HAS_RAY = "currnent_has_ray";
	public static final String CURRNENT_HAS_HELLO = "currnent_has_hello";
	public static final int SYNC_MONTH = 6;
	public static final int SCALE_DOWN_SIZE = 1024;
	public static final int SCALE_DOWN_SIZE_SMALL = 512;
	public static final int WALKIN_OFFSET = 5;
	public static final int IMAGE_SIZE = 150;
	public static final int IMAGE_SIZE_BIG = 300;

	// Bundle
	public static final String BUNDLE_ID = "bundle_id";
	public static final String BUNDLE_PRACTO_ID = "bundle_practo_id";
	public static final String BUNDLE_PRACTICE_ID = "bundle_practice_id";
	public static final String BUNDLE_PRACTICE_NAME = "bundle_practice_name";

	public static final String BUNDLE_CATEGORY_ID = "bundle_category_id";
	public static final String BUNDLE_FILE_ID = "bundle_file_id";
	public static final String BUNDLE_APPOINTMENT_LOCAL_ID = "bundle_appointment_local_id";
	public static final String BUNDLE_APPOINTMENT_PRACTO_ID = "bundle_appointment_id";
	public static final String BUNDLE_APPOINTMENT_STATUS = "bundle_appointment_status";
	public static final String BUNDLE_APPOINTMENT_STATE = "bundle_appointment_state";
	public static final String BUNDLE_APPOINTMENT_NOTES = "bundle_appointment_notes";
	public static final String BUNDLE_APPOINTMENT_SCHEDULED_AT = "bundle_appointment_scheduled_at";
	public static final String BUNDLE_APPOINTMENT_SCHEDULED_TILL = "bundle_appointment_scheduled_till";
	public static final String BUNDLE_APPOINTMENT_NOTIFY_PATIENT_SMS = "bundle_appointment_notify_patient_sms";
	public static final String BUNDLE_APPOINTMENT_NOTIFY_PATIENT_EMAIL = "bundle_appointment_notify_patient_email";
	public static final String BUNDLE_APPOINTMENT_NOTIFY_DOCTOR_SMS = "bundle_appointment_notify_doctor_sms";
	public static final String BUNDLE_APPOINTMENT_NOTIFY_DOCTOR_EMAIL = "bundle_appointment_notify_doctor_email";

	public static final String BUNDLE_APPOINTMENT_CATEGORY_ID = "bundle_appointment_category_id";
	public static final String BUNDLE_APPOINTMENT_CATEGORY_NAME = "bundle_appointment_category_name";
	public static final String BUNDLE_APPOINTMENT_CATEGORY_COLOR = "bundle_appointment_category_color";

	public static final String BUNDLE_PATIENT_LOCAL_ID = "bundle_patient_local_id";
	public static final String BUNDLE_PATIENT_PRACTO_ID = "bundle_patient_id";
	public static final String BUNDLE_PATIENT_NAME = "bundle_patient_name";
	public static final String BUNDLE_PATIENT_NUMBER = "bundle_patient_number";
	public static final String BUNDLE_PATIENT_EMAIL = "bundle_patient_email";
	public static final String BUNDLE_PATIENT_MOBILE = "bundle_patient_mobile";
	public static final String BUNDLE_PATIENT_HAS_PHOTO = "bundle_patient_has_photo";
	public static final String BUNDLE_PATIENT_DELETED = "bundle_patient_deleted";
	public static final String BUNDLE_PATIENT_FOCUS = "bundle_patient_focus";
	public static final String BUNDLE_PATIENT_FILE_NAME = "bundle_patient_file_name";
	public static final String BUNDLE_PATIENT_NATIONALID = "bundle_patient_nationalid";
	public static final String BUNDLE_PATIENT_GENDER = "bundle_patient_gender";
	public static final String BUNDLE_PATIENT_ADDRESS = "bundle_patient_address";
	public static final String BUNDLE_PATIENT_LOCALITY = "bundle_patient_locality";
	public static final String BUNDLE_PATIENT_CITY = "bundle_patient_city";
	public static final String BUNDLE_PATIENT_PINCODE = "bundle_patient_pincode";
	public static final String BUNDLE_PATIENT_AGE = "bundle_patient_age";
	public static final String BUNDLE_PATIENT_DOB = "bundle_patient_dob";
	public static final String BUNDLE_PATIENT_BLOODGROUP = "bundle_patient_bloodgroup";
	public static final String BUNDLE_PATIENT_PHOTO_CHANGED = "bundle_patient_photo_changed";

	public static final String BUNDLE_FILE_LOCAL_ID = "bundle_file_local_id";
	public static final String BUNDLE_FILE_PRACTO_ID = "bundle_file_id";
	public static final String BUNDLE_FILE_NAME = "bundle_file_name";
	public static final String BUNDLE_FILE_CAPTION = "bundle_file_caption";
	public static final String BUNDLE_FILE_TYPE = "bundle_file_type";
	public static final String BUNDLE_FILE_DATE = "bundle_file_date";
	public static final String BUNDLE_FILE_FROM_MOBILE = "bundle_file_from_mobile";
	public static final String BUNDLE_FILE_POSITION = "bundle_file_position";
	public static final String BUNDLE_FILE_ID_ARRAY = "bundle_file_id_array";
	public static final String BUNDLE_FILE_SEND_EMAIL = "bundle_file_send_email";

	public static final String BUNDLE_DOCTOR_ID = "bundle_doctor_id";
	public static final String BUNDLE_DOCTOR_NAME = "bundle_doctor_name";
	public static final String BUNDLE_DOCTOR_COLOR = "bundle_doctor_color";
	public static final String BUNDLE_DOCTOR_EMAIL = "bundle_doctor_email";
	public static final String BUNDLE_DOCTOR_MOBILE = "bundle_doctor_mobile";
	public static final String BUNDLE_DOCTOR_CONFIRMATION_SMS = "bundle_doctor_confirmation_sms";
	public static final String BUNDLE_DOCTOR_CONFIRMATION_EMAIL = "bundle_doctor_confirmation_email";

	public static final String BUNDLE_CALL_ID = "bundle_call_id";
	public static final String BUNDLE_CALL_PRACTO_ID = "bundle_call_practo_id";
	public static final String BUNDLE_CALL_APPOINTMENT_ID = "bundle_call_appointment_id";
	public static final String BUNDLE_CALL_PATIENT_LOCAL_ID = "bundle_patient_local_id";
	public static final String BUNDLE_CALL_PATIENT_PRACTO_ID = "bundle_patient_id";
	public static final String BUNDLE_CALL_PATIENT_NAME = "bundle_patient_name";
	public static final String BUNDLE_CALL_PATIENT_NUMBER = "bundle_patient_number";
	public static final String BUNDLE_CALL_PATIENT_EMAIL = "bundle_patient_email";
	public static final String BUNDLE_CALL_PATIENT_HAS_PHOTO = "bundle_patient_has_photo";

	public static final String BUNDLE_CALL_INCOMING = "bundle_call_incoming";
	public static final String BUNDLE_CALL_NAME = "bundle_call_name";
	public static final String BUNDLE_CALL_ICON = "bundle_call_icon";
	public static final String BUNDLE_CALL_STATUS = "bundle_call_status";
	public static final String BUNDLE_CALL_RECORDING_TITLE = "bundle_call_recording_title";
	public static final String BUNDLE_CALL_ALERTS = "bundle_call_alerts";

	public static final String BUNDLE_STAFF_ID = "bundle_staff_id";
	public static final String BUNDLE_STAFF_NAME = "bundle_staff_name";
	public static final String BUNDLE_DEVICE_NUMBER = "bundle_device_number";
	public static final String BUNDLE_USER_ID = "bundle_user_id";

	public static final String BUNDLE_CALENDAR = "bundle_calendar";
	public static final String BUNDLE_CALENDAR_DURATION = "bundle_calendar_duration";
	public static final String BUNDLE_CALENDAR_DURATION_VALUE = "bundle_calendar_duration_value";
	public static final String BUNDLE_SYNC_STATUS = "bundle_sync_status";
	public static final String BUNDLE_SYNC_TOTAL_COUNT = "bundle_sync_total_count";
	public static final String BUNDLE_SYNC_CURRENT_COUNT = "bundle_sync_current_count";
	public static final String BUNDLE_FORCE_SYNC = "bundle_force_sync";

	public static final String BUNDLE_IS_PUSH_SYNC = "bundle_is_push_sync";
	public static final String BUNDLE_PUSH_SYNC_COMMAND = "bundle_push_sync_command";

	public static final String BUNDLE_IS_SPECIAL_SYNC = "bundle_is_special_sync";
	public static final String BUNDLE_SPECIAL_SYNC_COMMAND = "bundle_is_special_sync_command";

	public static final String BUNDLE_SEARCH_MODE = "bundle_search_mode";
	public static final String BUNDLE_REQUEST_CODE = "bundle_request_code";

	// caller id
	public static final String PHONE_NUMBER_EXTRA_TAG = "phone_tag";
	public static final String PREFIX_PLUS = "+";
	public static final String PREFIX_ZERO = "0";
	public static final String PREFIX_PLUS_INDIA = "+91";

	// GCM
	public static final String SERVER_URL = "/devicesubscriptions"; // server
																	// url
	public static final String SENDER_ID = "825508183586"; // Google API project
															// id
	public static final String DISPLAY_MESSAGE_ACTION = "com.practo.droid.DISPLAY_MESSAGE";
	public static final String EXTRA_MESSAGE = "message";
	public static final String APP_NAME = "Ray Droid";
	public static final String OS_NAME = "Android";
	public static final String OS_VER = Build.VERSION.RELEASE;
	public static final String PHONE_MODEL = Build.MANUFACTURER + Build.MODEL;

	// GCM payload
	public static final String GCM_TYPE = "type";
	public static final String GCM_TYPE_REGISTRATION_ID = "registration_id";
	public static final String GCM_TYPE_UPDATE_AVAILABLE = "upgrade_available";
	public static final String GCM_TYPE_UPDATE_REQUIRED = "upgrade_required";
	public static final String GCM_TYPE_CUSTOM_MESSAGE = "custom_message";
	public static final String GCM_TYPE_PUSH2SYNC = "push2sync";

	public static final String GCM_UPDATE_VERSION = "new_version";
	public static final String GCM_MESSAGE = "message";
	public static final String GCM_PUSH2SYNC = "push2sync_command";

	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	// public static final String PROPERTY_ON_SERVER_EXPIRATION_TIME =
	// "onServerExpirationTimeMs";
	public static final String STATUS_SCHEDULED = "Scheduled";
	public static final String STATUS_CANCELLED = "Cancelled";

	public static final String SUPPORT_PHONE_NUMBER = "+918880588999";
	public static final String[] SUPPORT_EMAIL_IDS = { "support@practo.com" };
	public static final String SUPPORT_EMAIL_SUBJECT = "[Practo-Android]";

	// calls
	public static final String TYPE_QUERY = "query";
	public static final String TYPE_CALLFORWARD = "call_forwarding";
	public static final String TYPE_VOICEMAIL = "voicemail";
	public static final String TYPE_APPOINTMENT = "appointment";

	public static final String TYPE_QUERY_TIMING = "TIMINGS";
	public static final String TYPE_QUERY_ADDRESS = "ADDRESS";

	public static final String TYPE_CALLFORWARD_OUTSIDE = "OUTSIDE_CLINIC_HOUR";
	public static final String TYPE_CALLFORWARD_DURING = "DURING_CLINIC_HOUR";
	public static final String TYPE_CALLFORWARD_EMERGENCY = "EMERGENCY";
	public static final String TYPE_CALLFORWARD_ANSWERED = "answered";
	public static final String TYPE_CALLFORWARD_NOT_ANSWERED = "not_answered";

	public static final String TYPE_MOBILE = "mobile";
	public static final String TYPE_CLINIC = "clinic";
	public static final String TYPE_HOME = "home";

	public static final String CALL_PARAM_TYPE = "type";
	public static final String CALL_PARAM_PRACTICE_ID = "practiceId";
	public static final String CALL_PARAM_PATIENT_NUMBER = "patient_number";
	public static final String CALL_OUTBOUND_DEVICE_ID = "outbound_device_id";
	public static final String CALL_PARAM_STAFF_ID = "staff_id";
	public static final String CALL_PARAM_PHONE_NO = "phone_no";
	public static final String CALL_PARAM_LABEL = "label";
	
	//Subscription Plans
	public static final String PLAN_RAY_FREE = "Ray-Free";
	public static final String PLAN_RAY_LITE = "Ray-Lite";
	public static final String PLAN_RAY_XPRESS = "Ray-Xpress";
	public static final String PLAN_RAY_PERSONAL = "Ray-Personal";
	public static final String PLAN_RAY_PROFESSIONAL = "Ray-Professional";
	public static final String PLAN_RAY_ADVANCED = "Ray-Advanced";
	public static final String SUBSCRIPTION_STATUS = "INFINITE";
	
	public static final int FILTER_ALL_CALLS = 0;
	public static final int FILTER_APPOINTMENT_CALLS = 1;
	public static final int FILTER_VOICEMAIL_CALLS = 2;
	public static final int FILTER_MISSED_CALLS = 3;
	
	public static final Pattern EMAIL_ADDRESS = Pattern.compile("^[a-z0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+(\\.[a-z0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.([a-z]{2,})$");
    public static final String NEW_RELIC_ID = "AAd3d6312583f1cb30d7cc17d60ccfe45cdf44a46a";

    private Utils() {
	};

	public static enum FragmentMode {
		NONE, VIEW, ADD, EDIT
	}

	public static enum AppointmentType {
		NONE, FUTURE, WALKIN, PAST,
	}

	public static enum SYNC_TYPE {
		INIT, FULL, PERIODIC, PUSH, SPECIAL, NONE;

		public static SYNC_TYPE getEnum(int i) {
			switch (i) {
			case 0:
				return INIT;
			case 1:
				return FULL;
			case 2:
				return PERIODIC;
			case 3:
				return PUSH;
			default:
				return NONE;
			}
		}

		public static int getValue(SYNC_TYPE type) {
			switch (type) {
			case INIT:
				return 0;
			case FULL:
				return 1;
			case PERIODIC:
				return 2;
			case PUSH:
				return 3;
			default:
				return -1;
			}
		}
	}

	public static enum SPECIAL_SYNC_COMMAND {
		a, // - Appointments
		A, // - Appointments deleted
		p, // - Patients
		P, // - Patients deleted
		f, // - Files
		F, // - Patient Profile
		S, // - Practice Settings
	}

	public static enum PUSH_SYNC_COMMAND {
		a, // - Appointments
		p, // - Patients
		s, // - Staffs
		t, // - Treatments
		P, // - Practice Profile
		S, // - Practice Settings
		d, // - Doctors
		T, // - Treatment Plans
		r, // - Prescriptions
		f, // - Files
		c, // - Calls
		o, // - Outbound calls
		D, // - Staff Devices
		A, // - Full Sync
	}

	public static enum CURSOR_ADD_TYPE {
		New, After, Before,
	}

	public static void log(String t, String message) {
		LogUtils.LOGW(LogUtils.makeLogTag(t), message);
	}

	public static void log(String message) {
		LogUtils.LOGW(LogUtils.makeLogTag(""), message);
	}

	public interface OnPickerInteractionListener {
		public void onPickerInteraction(Bundle bundle);
	}

	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(Bundle bundle);
	}

	public interface AppointmentFragmentInteractionListener {
		public void notifyDataChanged();

		public Bundle getArgs();
	}

	public interface AppointmentInteractionListener {
		public boolean isCompleted();

		public void reload();
	}

	/**
	 * Interface for components that are internally scrollable left-to-right.
	 */
	public static interface HorizontallyScrollable {
		/**
		 * Return {@code true} if the component needs to receive right-to-left
		 * touch movements.
		 * 
		 * @param origX
		 *            the raw x coordinate of the initial touch
		 * @param origY
		 *            the raw y coordinate of the initial touch
		 */

		public boolean interceptMoveLeft(float origX, float origY);

		/**
		 * Return {@code true} if the component needs to receive left-to-right
		 * touch movements.
		 * 
		 * @param origX
		 *            the raw x coordinate of the initial touch
		 * @param origY
		 *            the raw y coordinate of the initial touch
		 */
		public boolean interceptMoveRight(float origX, float origY);
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public static List<Integer> buildIntArray(int[] integers) {
		List<Integer> ints = new ArrayList<Integer>();
		for (Integer n : integers) {
			ints.add(n);
		}
		return ints;
	}

	public static String toStandardTime(String militaryTime) {
		SimpleDateFormat militaryTimeFormat = new SimpleDateFormat("H:mm",
				PractoApplication.getInstance().getLocale());
		Date militaryDate = new Date();
		try {
			militaryDate = militaryTimeFormat.parse(militaryTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		SimpleDateFormat standardTimeFormat = new SimpleDateFormat("h:mm aa",
				PractoApplication.getInstance().getLocale());
		return standardTimeFormat.format(militaryDate);// .toUpperCase(PractoApplication.getInstance().getLocale());
	}

	public static String toStandardDate(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				PractoApplication.getInstance().getLocale());
		Date militaryDate = new Date();
		String dateString = "";
		try {
			militaryDate = dateFormat.parse(date);
			SimpleDateFormat standardTimeFormat = new SimpleDateFormat(
					"d MMMM yyyy", PractoApplication.getInstance().getLocale());
			dateString = standardTimeFormat.format(militaryDate);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateString;
	}

	public static String formatTime(long timeInSeconds) {
		String formattedTime = "";
		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		String hoursFormat = "";
		String minutesFormat = "";
		String secondsFormat = "";

		if (timeInSeconds >= 3600) {
			hours = timeInSeconds / 3600;
			timeInSeconds -= hours * 3600;
		}
		if (timeInSeconds >= 60) {
			minutes = timeInSeconds / 60;
			timeInSeconds -= minutes * 60;
		}
		seconds = timeInSeconds;

		if (hours != 0) {
			hoursFormat = String.format(PractoApplication.getInstance().getLocale(),
					hours == 1 ? "%d hr " : "%d hrs ", hours);
		}
		if (minutes != 0) {
			minutesFormat = String.format(PractoApplication.getInstance().getLocale(),
					hours == 1 ? "%d min " : "%d mins ", minutes);
		}
		if (seconds != 0) {
			secondsFormat = String.format(PractoApplication.getInstance().getLocale(),
					hours == 1 ? "%d sec" : "%d secs", seconds);
		} else {
			if (hours == 0 && minutes == 0) {
				secondsFormat = "0 sec";
			}
		}

		formattedTime = hoursFormat + minutesFormat + secondsFormat;

		return formattedTime;
	}

	public static void hideSoftKeyboard(Activity activity) {
		try {
			if (activity != null) {
				InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
				if (inputMethodManager.isAcceptingText()) {
					inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
				}
			}
		} catch (Exception e) {
		}
	}

	public static String getAccountURL(String account_id) {
		return "https://accounts.practo.com/profile_picture/" + account_id + "/" + (Utils.hasMoreHeap() ? "medium_thumbnail" : "small_thumbnail");
	}

	public static String getPracticeURL(String practice_id) {
		return Utils.API_URL + "/practicefiles/" + practice_id + "?size=medium_thumbnail";
	}

	public static String getPatientURL(String patient_id) {
		return Utils.API_URL + "/patients/" + patient_id + "/photo?size=" + (Utils.hasMoreHeap() ? "medium_thumbnail" : "small_thumbnail");
	}

	public static String getPatientFileURL(String file_id) {
		return Utils.API_URL + "/files/" + file_id + "?size=" + (Utils.hasMoreHeap() ? "medium_thumbnail" : "small_thumbnail");
	}

	public static String getPatientFileLargeURL(String file_id) {
		return Utils.API_URL + "/files/" + file_id + (Utils.hasMoreHeap() ? "" : "?size=large_thumbnail");
	}

	public static String convert24HoursTo12Hours(int mHour, int mMinute,
			int mSec) {
		String time = String.format("%02d", mHour) + ":"
				+ String.format("%02d", mMinute) + ":"
				+ (mSec == 0 ? "00" : String.format("%02d", mSec));
		DateFormat f1 = new SimpleDateFormat("HH:mm:ss",
				PractoApplication.getInstance().getLocale());
		String convertedTime = null;
		try {
			Date d = f1.parse(time);
			DateFormat f2 = new SimpleDateFormat("h:mm a",
					PractoApplication.getInstance().getLocale());
			convertedTime = f2.format(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return convertedTime;
	}

	public static String decodeSampledBitmap2(String fromFilename, String toFilename, int reqWidth, int reqHeight, boolean fixOrientation) {
		Utils.log(TAG, "path" + fromFilename);
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(fromFilename, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		try {
			if (fixOrientation) {
				FileOutputStream out = new FileOutputStream(toFilename);
				getOrientationFixedBitmap(fromFilename, BitmapFactory.decodeFile(fromFilename, options))
						.compress(Bitmap.CompressFormat.JPEG, 70, out);
				out.close();
			} else {
				FileOutputStream out = new FileOutputStream(toFilename);
				BitmapFactory.decodeFile(fromFilename, options).compress(Bitmap.CompressFormat.JPEG, 70, out);
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.log(TAG, "error");
			toFilename = "";
		}
		return toFilename;
	}

	public static Bitmap decodeSampledBitmap2(String filename, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

			final float totalPixels = width * height;
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		Utils.log(TAG, "size" + inSampleSize + "::" + options.outHeight + "::" + options.outWidth);
		return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmap(String filename, int reqWidth, int reqHeight) {
		Utils.log(TAG, "path" + filename);
		FileInputStream inputStream;
        File file = new File(filename);
        try {
            inputStream = new FileInputStream(file);
            return decodeSampledBitmapFromResourceMemOpt(inputStream, reqWidth, reqHeight);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		return null;
	}
	
	public static String decodeSampledBitmap(String fromFilename, String toFilename, int reqWidth, int reqHeight, boolean fixOrientation) {
		FileInputStream inputStream;
        File file = new File(fromFilename);
		try {
            inputStream = new FileInputStream(file);
			if (fixOrientation) {
				FileOutputStream out = new FileOutputStream(toFilename);
				getOrientationFixedBitmap(fromFilename, decodeSampledBitmapFromResourceMemOpt(inputStream, reqWidth, reqHeight))
						.compress(Bitmap.CompressFormat.JPEG, 70, out);
				out.close();
			} else {
				FileOutputStream out = new FileOutputStream(toFilename);
				decodeSampledBitmapFromResourceMemOpt(inputStream, reqWidth, reqHeight).compress(Bitmap.CompressFormat.JPEG, 70, out);
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.log(TAG, "error");
			toFilename = "";
		}
		return toFilename;
	}

	public static Bitmap decodeSampledBitmapFromResourceMemOpt(
            InputStream inputStream, int reqWidth, int reqHeight) {

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

           //int[] pids = { android.os.Process.myPid() };
            //MemoryInfo myMemInfo = mAM.getProcessMemoryInfo(pids)[0];
            //Log.e(TAG, "dalvikPss (decoding) = " + myMemInfo.dalvikPss);

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

	public static Bitmap getOrientationFixedBitmap(String filename, Bitmap bitmap) {
		try {
			final ExifInterface exif = new ExifInterface(filename);
			final int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			int angle = 0;
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				angle = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				angle = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				angle = 270;
				break;
			default:
				return bitmap;
			}
			Matrix matrix = new Matrix();
			matrix.postRotate(angle);
			return rotateImage(bitmap, angle);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	private static Bitmap rotateImage(Bitmap source, float angle) {

	    Bitmap bitmap = null;
	    Matrix matrix = new Matrix();
	    matrix.postRotate(angle);
	    try {
	        bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	        source.recycle();
	    } catch (OutOfMemoryError e) {
	        e.printStackTrace();
	        try {
	        	Bitmap tempBitmap = source.copy(Bitmap.Config.RGB_565, false);
		    	bitmap = Bitmap.createBitmap(tempBitmap, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
		    	source.recycle();
		    	tempBitmap.recycle();
			} catch (Exception e2) {
			}
	    }
	    return bitmap;
	}

	public static boolean isDownloadManagerAvailable(Context context) {
		try {
			if (!Utils.hasHoneycomb()) {
				return false;
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
			List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Get a usable file path directory
	 * 
	 */
	public static File getDownloadPath(Context context, String uniqueName) {
		final String downloadPath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !isExternalStorageRemovable() ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				.getPath() : "";
		Utils.log("Download path", downloadPath + File.separator + uniqueName);
		return downloadPath != "" ? new File(downloadPath + File.separator + uniqueName) : null;
	}
	
	@SuppressLint("NewApi")
	public static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

	public static void showDialog(Context context) {

		new AlertDialog.Builder(context).setTitle("No Internet")
				.setMessage("Not connected to the internet. Please check your internet connectivity.")
				.setPositiveButton("", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();
	}

	public static String toString(String[] array) {
		if (array == null) {
			return "";
		}
		if (array.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(array.length * 7);
		sb.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			sb.append(", ");
			sb.append(array[i]);
		}
		return sb.toString();
	}

	public static String toString(ArrayList<String> list) {
		if (list == null) {
			return "";
		}
		if (list.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(list.get(0));
		for (int i = 1; i < list.size(); i++) {
			sb.append(", ");
			sb.append(list.get(i));
		}
		return sb.toString();
	}

	@TargetApi(11)
	public static void enableStrictMode() {
		if (Utils.hasGingerbread()) {
			StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog();
			StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();

			/*
			 * if (Utils.hasHoneycomb()) {
			 * threadPolicyBuilder.penaltyFlashScreen(); vmPolicyBuilder
			 * .setClassInstanceLimit(ImageGridActivity.class, 1)
			 * .setClassInstanceLimit(ImageDetailActivity.class, 1); }
			 */
			StrictMode.setThreadPolicy(threadPolicyBuilder.build());
			StrictMode.setVmPolicy(vmPolicyBuilder.build());
		}
	}

	public static boolean hasFroyo() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}
	
	public static boolean hasJellyBeanMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	}

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	public static boolean hasMoreHeap() {
		return Utils.hasHoneycomb() && Runtime.getRuntime().maxMemory() > 20971520;
	}

	public static String getBooleanValue(boolean bool) {
		return bool ? "1" : "0";
	}

	public static String getBooleanStringValue(int value) {
		return value == 1 ? "true" : "false";
	}

	public final ArrayMap<String, String> getParamValues(Cursor cursor) {
		ArrayMap<String, String> param = new ArrayMap<String, String>();
		for (int i = 0; i < cursor.getColumnCount(); i++) {
			param.put(cursor.getColumnName(i), cursor.getString(i));
		}
		return param;
	}

	public static SpannableString getSpannableString(Context context, String text) {
		SpannableString s = new SpannableString(text);
		s.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.practo_blue)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return s;
	}

	@SuppressLint("NewApi")
	public static CharSequence highlight(Context context, String search, String originalText) {
		// ignore case and accents
		// the same thing should have been done for the search text
		String normalizer = Normalizer.normalize(originalText, Normalizer.Form.NFD);
		if(null == normalizer){
			return originalText;
		}
		String normalizedText = normalizer.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.toLowerCase(PractoApplication.getInstance().getLocale());

		int start = normalizedText.indexOf(search);
		if (start < 0) {
			// not found, nothing to to
			return originalText;
		} else {
			// highlight each appearance in the original text
			// while searching in normalized text
			Spannable highlighted = new SpannableString(originalText);
			while (start >= 0) {
				int spanStart = Math.min(start, originalText.length());
				int spanEnd = Math.min(start + search.length(), originalText.length());

				highlighted.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.practo_blue)), spanStart, spanEnd,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				start = normalizedText.indexOf(search, spanEnd);
			}

			return highlighted;
		}
	}

	public static boolean isNetConnected(Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
			return false;
		}
		return true;
	}

	/**
	 * Simple network connection check.
	 * 
	 * @param context
	 * @return true if connection is present else false
	 */
	public static boolean checkConnection(Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
			Toast.makeText(context, "No Internet", Toast.LENGTH_LONG).show();
			Utils.log(TAG, "checkConnection - no connection found");
			return false;
		}
		return true;
	}

	public static boolean isSyncActive(Account account, String authority) {
		return ContentResolver.isSyncActive(account, authority);
	}

	public static void syncData(Context context) {
		AccountManager am = AccountManager.get(context);
		Account[] acts = am.getAccountsByType(context.getString(R.string.app_account));

		if (acts.length != 0) {
			new AsyncTask<Account, Void, Void>() {

				@Override
				protected Void doInBackground(Account... params) {
					Bundle syncBundle = new Bundle();
					syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
					syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
					ContentResolver.requestSync(params[0], PractoDataContentProvider.CONTENT_AUTHORITY, syncBundle);
					return null;
				}
			}.execute(acts[0]);
		}
	}

	public static void syncDataForce(Context context) {
		AccountManager am = AccountManager.get(context);
		Account[] acts = am.getAccountsByType(context.getString(R.string.app_account));

		if (acts.length != 0) {
			new AsyncTask<Account, Void, Void>() {

				@Override
				protected Void doInBackground(Account... params) {
					Bundle syncBundle = new Bundle();
					syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
					syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
					syncBundle.putBoolean(BUNDLE_FORCE_SYNC, true);
					ContentResolver.requestSync(params[0], PractoDataContentProvider.CONTENT_AUTHORITY, syncBundle);
					return null;
				}
			}.execute(acts[0]);
		}
	}

	public static void syncData(Context context, final Bundle syncBundle) {
		AccountManager am = AccountManager.get(context);
		Account[] acts = am.getAccountsByType(context.getString(R.string.app_account));

		if (acts.length != 0) {
			new AsyncTask<Account, Void, Void>() {

				@Override
				protected Void doInBackground(Account... params) {
					syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
					syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
					syncBundle.putBoolean(BUNDLE_FORCE_SYNC, true);
					ContentResolver.requestSync(params[0], PractoDataContentProvider.CONTENT_AUTHORITY, syncBundle);
					return null;
				}
			}.execute(acts[0]);
		}
	}

	public static void cancelSyncData(Context context) {
		AccountManager am = AccountManager.get(context);
		Account[] acts = am.getAccountsByType(context.getString(R.string.app_account));

		if (acts.length != 0) {
			new AsyncTask<Account, Void, Void>() {
				@Override
				protected Void doInBackground(Account... params) {
					ContentResolver.cancelSync(params[0], PractoDataContentProvider.CONTENT_AUTHORITY);
					return null;
				}
			}.execute(acts[0]);
		}
	}

	/**
	 * Register this account/device pair within the server.
	 * 
	 * @return whether the registration succeeded or not.
	 */
	public static boolean register(final Context context, final String regId) {
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = mSharedPreferences.edit();
		String auth_token = mSharedPreferences.getString(Utils.CURRNENT_AUTH_TOKEN, "");
		RequestTickle mRequestTickle = PractoApplication.getInstance().getRequestTickle();
		final Gson gson = new Gson();
		String device_subscription_id = mSharedPreferences.getString(Utils.DEVICE_SUBSCRIPTION_ID, "");
		Utils.log(TAG, "registering device (regId = " + regId + ")");
		String serverUrl = Utils.API_URL + (TextUtils.isEmpty(device_subscription_id) ? Utils.SERVER_URL : Utils.SERVER_URL + "/" + device_subscription_id);

		ArrayMap<String, String> params = new ArrayMap<String, String>();
		params.put("push_token", regId);
		params.put("os_name", Utils.OS_NAME);
		params.put("os_version", Utils.OS_VER);
		params.put("app_name", Utils.APP_NAME);
		params.put("app_version", PractoApplication.APP_VERSION);
		params.put("model_number", Utils.PHONE_MODEL);
		int method = TextUtils.isEmpty(device_subscription_id) ? Method.POST : Method.PATCH;
    	PractoGsonRequest<Appointment> request = new PractoGsonRequest<Appointment>(method,
    			serverUrl,
                Appointment.class,
                auth_token,
                params,
                null,
                null);
    	mRequestTickle.add(request);
    	NetworkResponse response = mRequestTickle.start();
		if (response.statusCode == 200 || response.statusCode == 201) {
			String data = VolleyTickle.parseResponse(response);
			DeviceSubscription deviceSubscription = gson.fromJson(data, DeviceSubscription.class);
			Utils.log("Practo", deviceSubscription.id + "");
			editor.putString(Utils.DEVICE_SUBSCRIPTION_ID, String.valueOf(deviceSubscription.id));
			editor.commit();
			return true;
		}
		return false;
	}

	/**
	 * Gets the current registration id for application on GCM service.
	 * <p>
	 * If result is empty, the registration has failed.
	 * 
	 * @return registration id, or empty string if the registration is not
	 *         complete.
	 */
	public static String getRegistrationId(Context context) {
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String registrationId = mSharedPreferences.getString(Utils.PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			Utils.log(TAG, "Registration not found.");
			return "";
		}
		int registeredVersion = mSharedPreferences.getInt(
				Utils.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = PractoApplication.APP_VERSION_CODE;
		if (registeredVersion != currentVersion) {
			Utils.log(TAG, "App version changed or registration expired.");
			setRegistrationId(context, "");
			return "";
		}
		return registrationId;
	}

	/**
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration id
	 */
	public static void setRegistrationId(Context context, String regId) {
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = mSharedPreferences.edit();
		int appVersion = PractoApplication.APP_VERSION_CODE;
		Utils.log(TAG, "Saving regId on app version " + appVersion);
		editor.putString(Utils.PROPERTY_REG_ID, regId);
		editor.putInt(Utils.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Unregister this account/device pair within the server.
	 */
	public static void unregisterDevice(final Context context) {
		Utils.log(TAG, "unregistering device ");
		String device_subscription_id = PreferenceManager.getDefaultSharedPreferences(context).getString(Utils.DEVICE_SUBSCRIPTION_ID, "");
		String auth_token = PreferenceManager.getDefaultSharedPreferences(context).getString(Utils.CURRNENT_AUTH_TOKEN, "");
		RequestTickle mRequestTickle = PractoApplication.getInstance().getRequestTickle();
		if (!TextUtils.isEmpty(device_subscription_id)) {
			String serverUrl = Utils.API_URL + Utils.SERVER_URL + "/" + device_subscription_id;
			try {
	        	PractoStringRequest request = new PractoStringRequest(com.android.volley.Request.Method.DELETE,
	        			serverUrl,
	        			auth_token,
	                    null,
	                    null,
	                    null);
	        	mRequestTickle.add(request);
	        	mRequestTickle.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a 'ghost' bitmap version of the given source drawable (ideally a
	 * BitmapDrawable). In the ghost bitmap, the RGB values take on the values
	 * from the 'color' argument, while the alpha values are derived from the
	 * source's grayscaled RGB values. The effect is that you can see through
	 * darker parts of the source bitmap, while lighter parts show up as the
	 * given color. The 'invert' argument inverts the computation of alpha
	 * values, and looks best when the given color is a dark.
	 */
	public static Bitmap createGhostIcon(Drawable src, int color, boolean invert) {
		int width = src.getIntrinsicWidth();
		int height = src.getIntrinsicHeight();
		if (width <= 0 || height <= 0) {
			throw new UnsupportedOperationException("Source drawable needs an intrinsic size.");
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint colorToAlphaPaint = new Paint();
		int invMul = invert ? -1 : 1;
		colorToAlphaPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[] { 0, 0, 0, 0, Color.red(color), 0, 0, 0, 0,
				Color.green(color), 0, 0, 0, 0, Color.blue(color), invMul * 0.213f, invMul * 0.715f, invMul * 0.072f, 0, invert ? 255 : 0, })));
		canvas.saveLayer(0, 0, width, height, colorToAlphaPaint, Canvas.ALL_SAVE_FLAG);
		canvas.drawColor(invert ? Color.WHITE : Color.BLACK);
		src.setBounds(0, 0, width, height);
		src.draw(canvas);
		canvas.restore();
		return bitmap;
	}

	public static String getFormattedPhoneNumber(String mobileStr, String countryCode) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber phoneNumber;
		String phoneNumberE164 = mobileStr;

		try {
			phoneNumber = phoneUtil.parse(mobileStr, countryCode);
			phoneNumberE164 = phoneUtil.format(phoneNumber, PhoneNumberFormat.E164);
		} catch (NumberParseException e) {
			e.printStackTrace();
		}
		return phoneNumberE164;
	}

	public static boolean isValidMobile(String mobileStr, String countryCode) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber phoneNumber;
		try {
			phoneNumber = phoneUtil.parse(mobileStr, countryCode);
		} catch (NumberParseException e) {
			return false;
		}

		if (phoneUtil.isValidNumber(phoneNumber)) {
			PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
			Utils.log("Mobile Number ", "Phone Number " + numberType);
			if (numberType == PhoneNumberType.FIXED_LINE) {
				return false;
			}
			if (numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE
					|| numberType == PhoneNumberType.MOBILE) {
				return true;
			}
		}
		// Hack for indian mobile numbers
		if (phoneNumber.getCountryCode() == 91 && String.valueOf(phoneNumber.getNationalNumber()).matches("[789]\\d{9}")) {
			return true;
		}

		return false;
	}

	public static boolean isValidPhone(String mobileStr, String countryCode) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber phoneNumber;
		try {
			phoneNumber = phoneUtil.parse(mobileStr, countryCode);
		} catch (NumberParseException e) {
			return false;
		}

		if (phoneUtil.isValidNumber(phoneNumber)) {
			PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
			Utils.log("Phone Number ", "Phone Number " + numberType);
			if (numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE
					|| numberType == PhoneNumberType.MOBILE) {
				return true;
			}
		}
		// Hack for indian mobile numbers
		if (phoneNumber.getCountryCode() == 91 && String.valueOf(phoneNumber.getNationalNumber()).matches("[789]\\d{9}")) {
			return true;
		}

		return false;
	}

	public static boolean isValidEmail(String target) {
		if (target == null) {
			return false;
		} else {
			return EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	public static Bitmap drawViewOntoBitmap(View view) {
		Bitmap image = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		view.draw(canvas);

		return image;
	}

	/**
	 * Recursively delete everything in {@code dir}.
	 */
	public static void deleteContents(File dir) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				deleteContents(file);
			}
			if (!file.delete()) {

			}
		}
	}

	public static int lookupHost(String hostname) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			return -1;
		}
		byte[] addrBytes;
		int addr;
		addrBytes = inetAddress.getAddress();
		addr = ((addrBytes[3] & 0xff) << 24) | ((addrBytes[2] & 0xff) << 16) | ((addrBytes[1] & 0xff) << 8) | (addrBytes[0] & 0xff);
		return addr;
	}

	/**
	 * @param file
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private static String getFileExt(File file) {
		String end = "";
		int lastIndex = file.getName().lastIndexOf(".");
		if (lastIndex != -1) {
			end = file.getName().substring(lastIndex + 1, file.getName().length()).toLowerCase();
		}
		return end;
	}

	/**
	 * @param file
	 * @return
	 */
	public static String getMIMEType(File file) {
		String end = getFileExt(file);
		String type = "";
		MimeTypeMap map = MimeTypeMap.getSingleton();
		type = map.getMimeTypeFromExtension(end) != null ? map.getMimeTypeFromExtension(end) : file.isDirectory() ? "directory" : "application/*";
		return type;
	}

	public static String toTitleCase(String input) {
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;
		input = input.toLowerCase(PractoApplication.getInstance().getLocale());
		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}

			titleCase.append(c);
		}

		return titleCase.toString();
	}

	public static void setCurrentPractice(Editor editor, Cursor practiceCursor) {
		
		boolean init_sync = practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.INIT_SYNC_PATIENT_DONE)) == 1
				&& practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.INIT_SYNC_APPOINTMENT_DONE)) == 1;
		boolean full_sync = practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.SYNC_PATIENT_DONE)) == 1
				&& practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.SYNC_APPOINTMENT_DONE)) == 1;

		editor.putBoolean(Utils.CURRNENT_INIT_SYNC_DONE, init_sync);
		editor.putBoolean(Utils.CURRNENT_FULL_SYNC_DONE, full_sync);
		editor.putString(Utils.CURRNENT_PRACTICE_ID, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.PRACTICE_ID)));
		editor.putString(Utils.CURRNENT_PRACTICE_LOCAL_ID, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.ID)));
		editor.putString(Utils.CURRNENT_PRACTICE_NAME, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.NAME)));
		editor.putString(Utils.CURRNENT_ROLE_NAME, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.ROLE_NAME)));
		editor.putString(Utils.CURRNENT_USER_ID, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.USER_ID)));
		editor.putString(Utils.CURRNENT_AUTH_TOKEN, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.AUTH_TOKEN)));
		editor.putString(Utils.CURRNENT_APPOINTMENT_DURATION,
				practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.DEFAULT_APPOINTMENT_DURATION)));
		editor.putInt(Utils.CURRNENT_AUTO_INDEXED, practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.AUTO_INDEXED)));
		editor.putString(Utils.CURRNENT_COUNTRY_CODE, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.PRACTICE_COUNTRY_CODE)));
		editor.putString(Utils.CURRNENT_NATIONAL_ID_LABEL, practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.NATIONAL_ID_LABEL)));
		
		final String practice_subscription_plan = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.PRACTICE_SUBSCRIPTION_PLAN));
		final String practice_subscription_status = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.PRACTICE_SUBSCRIPTION_STATUS));
		final String practice_subscription_enddate = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.PRACTICE_SUBSCRIPTION_ENDDATE));
		final boolean isInfinite = !TextUtils.isEmpty(practice_subscription_status) && practice_subscription_status.compareTo(Utils.SUBSCRIPTION_STATUS) == 0;
		boolean isValidSubscription = 
				isInfinite || ! Utils.isExpired(practice_subscription_enddate);
		boolean isHelloNumberValid = practiceCursor.getInt(practiceCursor.getColumnIndex(PracticeColumns.ROLE_HELLO_SUBSCRIPTION_ACTIVE)) == 1;
		boolean has_free = false;
		boolean has_ray = false;
		boolean has_hello = isHelloNumberValid;
		if(!TextUtils.isEmpty(practice_subscription_plan)){
			if(practice_subscription_plan.startsWith(Utils.PLAN_RAY_XPRESS)){
				has_free = true;
			}
			else{
				if(isValidSubscription){
					if(practice_subscription_plan.startsWith(Utils.PLAN_RAY_PERSONAL)
							|| practice_subscription_plan.startsWith(Utils.PLAN_RAY_PROFESSIONAL)){
						has_ray = true;
					}
					else if(practice_subscription_plan.startsWith(Utils.PLAN_RAY_ADVANCED)){
						has_ray = true;
						has_hello = true;
					}
				}	
			}
		}

		editor.putBoolean(Utils.CURRNENT_HAS_FREE, has_free);
		editor.putBoolean(Utils.CURRNENT_HAS_RAY, has_ray);
		editor.putBoolean(Utils.CURRNENT_HAS_HELLO, has_hello && isHelloNumberValid);
		
		if (practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.CUSTOMIZED_FOR)) != null
				&& practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.CUSTOMIZED_FOR)).equals("GENERAL")) {
			editor.putString(Utils.CURRNENT_PATIENT_LABEL, "Client");
			editor.putString(Utils.CURRNENT_DOCTOR_LABEL, "Therapist");
		} else {
			editor.putString(Utils.CURRNENT_PATIENT_LABEL, "Patient");
			editor.putString(Utils.CURRNENT_DOCTOR_LABEL, "Doctor");
		}
		editor.commit();
	}
	
	public static boolean isExpired(String date){
		if(TextUtils.isEmpty(date)){
			return false;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", PractoApplication.getInstance().getLocale());
		final Calendar currentDateTime = Calendar.getInstance(PractoApplication.getInstance().getLocale());
		final Calendar endDateTime = Calendar.getInstance(PractoApplication.getInstance().getLocale());
		try {
			endDateTime.setTime(dateFormat.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return endDateTime.before(currentDateTime);
	}

	public static String getPlayedDuration(int miliseconds) {
		int seconds = (int) Math.ceil(miliseconds / 1000);
		// int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		seconds = seconds % 60;
		String time = "";
		if (minutes > 0)
			time += twoDigitString(minutes) + ":";
		else
			time += "00:";
		if (seconds > 0)
			time += twoDigitString(seconds);
		else
			time += "00";
		return time;
	}

	private static String twoDigitString(int number) {
		if (number == 0) {
			return "00";
		}
		if (number / 10 == 0) {
			return "0" + number;
		}
		return String.valueOf(number);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@SuppressWarnings("deprecation")
	public static void disableFinishActivitiesOption(final Context context) {
		int result;
		if (Build.VERSION.SDK_INT >= 17) {
			result = Settings.Global.getInt(context.getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
		} else {
			result = Settings.System.getInt(context.getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);
		}
		if (result == 1) {
			new AlertDialog.Builder(context).setTitle("Developer Options").setMessage(R.string.developer_options_msg).setCancelable(false)
					.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							context.startActivity(intent);
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							((Activity) context).finish();
						}
					}).create().show();

		}
	}

	public static ConfigOptions getShowCaseOptions() {
		ShowcaseView.ConfigOptions options = new ShowcaseView.ConfigOptions();
		options.hideOnClickOutside = true;
		options.shotType = ShowcaseView.TYPE_ONE_SHOT;
		options.fadeInDuration = 700;
		options.fadeOutDuration = 700;
		return options;
	}

	public static void startTracking(Activity activity) {
		AnalyticsManager.startTracking(activity);
	}

	public static void stopTracking(Activity activity) {
		AnalyticsManager.stopTracking(activity);
	}

	public static void sendView(String screenName) {
		AnalyticsManager.sendView(screenName);
	}

	public static void sendEvent(String category, String action, String label, Long value) {
		AnalyticsManager.sendEvent(category, action, label, value);
	}

	public static boolean getRoles(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Gson gson = new Gson();
		RequestTickle mRequestTickle = PractoApplication.getInstance().getRequestTickle();
		String endpoint = "/roles";
		int count = 0;
    	PractoGsonRequest<Roles> request = new PractoGsonRequest<Roles>(Method.GET,
                Utils.API_URL + endpoint,
                Roles.class,
                sharedPreferences.getString(Utils.AUTH_TOKEN, ""),
                null,
                null,
                null);
    	mRequestTickle.add(request);
    	NetworkResponse response = mRequestTickle.start();
    	
		if (response.statusCode == 200 || response.statusCode == 201) {
			ArrayList<String> practice_list = new ArrayList<String>();
			boolean first = true;
			String data = VolleyTickle.parseResponse(response);
			Roles roles = gson.fromJson(data, Roles.class);
			ContentValues contentValues = new ContentValues();
			for (int i = 0; i < roles.roles.size(); i++) {
				Roles.Role role = roles.roles.get(i);
				String rolename = role.role_name;
				int practice_id = role.practice.id;
				practice_list.add(String.valueOf(practice_id));

                if (!role.practice.has_active_ray_subscription) {
                    continue;
                } else if (!(rolename.compareToIgnoreCase("owner") == 0 || rolename.compareToIgnoreCase("administrator") == 0)) {
                    continue;
                }

				contentValues.put(PracticeColumns.ROLE_NAME, rolename);
				contentValues.put(PracticeColumns.USER_ROLE_ID, role.id);
				contentValues.put(PracticeColumns.USER_ID, role.user_id);
				contentValues.put(PracticeColumns.NAME, role.practice.name);
				contentValues.put(PracticeColumns.ROLE_RAY_SUBSCRIPTION_ACTIVE, role.practice.has_active_ray_subscription);
				contentValues.put(PracticeColumns.ROLE_HELLO_SUBSCRIPTION_ACTIVE, role.practice.has_active_hello_subscription);
				contentValues.put(PracticeColumns.PRACTICE_ID, String.valueOf(role.practice.id));
				if (first) {
					first = false;
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(Utils.CURRNENT_ROLE_NAME, rolename);
					editor.putString(Utils.CURRNENT_AUTH_TOKEN, sharedPreferences.getString(Utils.AUTH_TOKEN, ""));
					editor.commit();
				}
				Cursor cursor = context.getContentResolver().query(PractoDataContentProvider.PRACTICES_URI, null,
						PracticeColumns.PRACTICE_ID + " IS ? ", new String[] { String.valueOf(role.practice.id) }, null);

				if (null != cursor) {
					if (cursor.getCount() == 1) {
						int id = context.getContentResolver().update(PractoDataContentProvider.PRACTICES_URI, contentValues,
								PracticeColumns.PRACTICE_ID + " IS ? ", new String[] { String.valueOf(role.practice.id) });
						if (id == 1) {
							Utils.log(TAG, "updated");
							count++;
						} else {
							Utils.log("Updated... nothing! CV is...");
							Utils.log(contentValues.toString());
						}
					} else {
						Uri u = context.getContentResolver().insert(PractoDataContentProvider.PRACTICES_URI, contentValues);
						if (u != null) {
							Utils.log(TAG, "inserted");
							count++;
						} else {
							Utils.log("Inserted... nothing! CV is...");
							Utils.log(contentValues.toString());
						}
					}
				}
			}

			// TODO:For removed practices
			/*
			 * ContentValues values = new ContentValues();
			 * values.put(PracticeColumns.ROLE_RAY_SUBSCRIPTION_ACTIVE, false);
			 * getContentResolver().update(
			 * PractoDataContentProvider.PRACTICES_URI, contentValues,
			 * PracticeColumns.PRACTICE_ID + " NOT IN ? " , new String[] {
			 * "("+Utils.toString(practice_list)+")" });
			 */
		}
		return count > 0;
	}

	public static boolean getAllRoles(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		RequestTickle mRequestTickle = PractoApplication.getInstance().getRequestTickle();
		Gson gson = new Gson();
		String endpoint = "/roles";
		int count = 0;
		final Cursor practiceCursor = context.getContentResolver().query(PractoDataContentProvider.PRACTICES_URI, null, null, null, null);
		if (practiceCursor != null) {
			if (practiceCursor.moveToNext()) {
				String authtoken = practiceCursor.getString(practiceCursor.getColumnIndex(PracticeColumns.AUTH_TOKEN));
	        	PractoGsonRequest<Roles> request = new PractoGsonRequest<Roles>(Method.GET,
	                    Utils.API_URL + endpoint,
	                    Roles.class,
	                    authtoken,
	                    null,
	                    null,
	                    null);
	        	mRequestTickle.add(request);
	        	NetworkResponse response = mRequestTickle.start();
	        	
				if (response.statusCode == 200 || response.statusCode == 201) {
					ArrayList<String> practice_list = new ArrayList<String>();
					boolean first = true;
					String data = VolleyTickle.parseResponse(response);
					Roles roles = gson.fromJson(data, Roles.class);
					ContentValues contentValues = new ContentValues();
					for (int i = 0; i < roles.roles.size(); i++) {
						Roles.Role role = roles.roles.get(i);
						String rolename = role.role_name;
						int practice_id = role.practice.id;
						practice_list.add(String.valueOf(practice_id));
						if (!(rolename.compareToIgnoreCase("owner") == 0 || rolename.compareToIgnoreCase("administrator") == 0)) {
							continue;
						}

						contentValues.put(PracticeColumns.ROLE_NAME, rolename);
						contentValues.put(PracticeColumns.USER_ROLE_ID, role.id);
						contentValues.put(PracticeColumns.USER_ID, role.user_id);
						contentValues.put(PracticeColumns.NAME, role.practice.name);
						contentValues.put(PracticeColumns.ROLE_RAY_SUBSCRIPTION_ACTIVE, role.practice.has_active_ray_subscription);
						//contentValues.put(PracticeColumns.ROLE_HELLO_SUBSCRIPTION_ACTIVE, role.practice.has_active_hello_subscription);
						contentValues.put(PracticeColumns.PRACTICE_ID, String.valueOf(role.practice.id));
						if (first) {
							first = false;
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString(Utils.CURRNENT_ROLE_NAME, rolename);
							editor.putString(Utils.CURRNENT_AUTH_TOKEN, sharedPreferences.getString(Utils.AUTH_TOKEN, ""));
							editor.commit();
						}
						Cursor cursor = context.getContentResolver().query(PractoDataContentProvider.PRACTICES_URI, null,
								PracticeColumns.PRACTICE_ID + " IS ? ", new String[] { String.valueOf(role.practice.id) }, null);

						if (null != cursor) {
							if (cursor.getCount() == 1) {
								int id = context.getContentResolver().update(PractoDataContentProvider.PRACTICES_URI, contentValues,
										PracticeColumns.PRACTICE_ID + " IS ? ", new String[] { String.valueOf(role.practice.id) });
								if (id == 1) {
									Utils.log(TAG, "updated");
									count++;
								} else {
									Utils.log("Updated... nothing! CV is...");
									Utils.log(contentValues.toString());
								}
							} else {
								Uri u = context.getContentResolver().insert(PractoDataContentProvider.PRACTICES_URI, contentValues);
								if (u != null) {
									Utils.log(TAG, "inserted");
									count++;
								} else {
									Utils.log("Inserted... nothing! CV is...");
									Utils.log(contentValues.toString());
								}
							}
						}
					}

					// TODO:For removed practices
					/*
					 * ContentValues values = new ContentValues();
					 * values.put(PracticeColumns.ROLE_RAY_SUBSCRIPTION_ACTIVE,
					 * false); getContentResolver().update(
					 * PractoDataContentProvider.PRACTICES_URI, contentValues,
					 * PracticeColumns.PRACTICE_ID + " NOT IN ? " , new String[]
					 * { "("+Utils.toString(practice_list)+")" });
					 */
				}

			}

			practiceCursor.close();
		}

		return count > 0;
	}

	public static String getCurrentAuthtoken(Context context) {
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return mSharedPreferences.getString(Utils.CURRNENT_AUTH_TOKEN, "");
	}
	
	public static boolean isRayFree(String plan_name){
		boolean has_free = false;
		if(!TextUtils.isEmpty(plan_name)){
			if(plan_name.startsWith(Utils.PLAN_RAY_XPRESS)){
				has_free = true;
			}
		}
		return has_free;
	}
	
	@SuppressLint("NewApi")
	public static boolean isActivityAlive(Activity activity) {
		if(Utils.hasJellyBeanMR1()){
			if(null == activity || (null != activity && activity.isDestroyed())){
				return false;
			}
		}
		else{
			if(null == activity || (null != activity && activity.isFinishing())){
				return false;
			}
		}
		return true;
	}
	
	public static void log2Sentry(String message, ArrayMap<String, String> map){
		log2Sentry(SentryEventLevel.ERROR, message, map);
	}
	
	public static void log2Sentry(String message){
		log2Sentry(SentryEventLevel.ERROR, message, null);
	}
	
	public static void log2Sentry(SentryEventLevel level, String message, ArrayMap<String, String> map){
		SentryEventBuilder sentryEventBuilder = new SentryEventBuilder();
		sentryEventBuilder.setLevel(level);
		sentryEventBuilder.setMessage(message);
		if(null != map){
			sentryEventBuilder.setExtra(map);
		}
		sentryEventBuilder.setTags(Sentry.getSystemTags());
		Sentry.captureEvent(sentryEventBuilder);
	}
}