package com.canarys.fb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;
import android.widget.ZoomControls;

import com.canarys.fb.SessionEvents.AuthListener;
import com.canarys.fb.SessionEvents.LogoutListener;
import com.canarys.fb.utills.MyLocation;
import com.canarys.fb.utills.MyLocation.LocationResult;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Util;

public class MainActivity extends Activity {

	/*
	 * Your Facebook Application ID must be set before running this example See
	 * http://www.facebook.com/developers/createapp.php
	 */

	Uri ImageCaptureUri;

	public static final String APP_ID = "106123152886969";

	private LoginButton mLoginButton;
	private TextView mText, uploadimage_text;
	private ImageView mUserPic, uploadedPic, captured_image;
	private Handler mHandler;
	ProgressDialog dialog;

	final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
	final static int PICK_FROM_CAMERA = 1;
	final static int PICK_EXISTING_PHOTO_RESULT_CODE = 2;
	final static int GEOLOCATION_ENABLE = 3;
	final static int VIDEOPLAY = 4;

	private String graph_or_fql;
	
	LinearLayout image_layout;

	Bitmap uploadBitmap;
	// String[] main_items = { "Update Status", "App Requests", "Get Friends",
	// "Upload Photo",
	// "Place Check-in", "Run FQL Query", "Graph API Explorer", "Token Refresh"
	// };

	public static String latitude = null;
	public static String longitude = null;
	public static String locationData = null;

	public static String fbplaceid = null;

	String[] permissions = { "offline_access", "publish_stream", "user_photos",
			"publish_checkins", "photo_upload" };

	private final static String TAG = "FacebookAlbum";

	private RelativeLayout fblayout;
	String ALBUMURL;

	ProgressDialog progressDialog;

	private LinearLayout leftPanel;
	File file;

	Button Capture, reTake, VideoRecord, videopreview_Button,
			videoupload_Button, reset_Button, discard_Button,
			uploadimage_Button,zoomin ,zoomout;

	Preview preview;

	FrameLayout frame;

	public byte[] data = null;;

	LocationManager lm;

	// ViewFlipper surfaceFlip;

	public MediaRecorder mrec = new MediaRecorder();

	// private Button stopRecording = null;
	File video;
	private Camera mCamera;

	Boolean recordingFlag = false;
	String VideoPath, Videoname;

	// timer..
	private final long startTime = 120000;
	private final long interval = 1000;
	private static final String tag = "Main";
	private VideoCountDownTimer countDownTimer;

	GeoLocationCountDownTimer geoControlTimer;
	private long timeElapsed;
	private boolean timerHasStarted = false;

	private boolean geolocationControl = true;
	private final long geocontrolTime = 15000;

	// private CheckBox location_box;

	private ViewFlipper _previewflipper, _layoutflipper;
	VideoView Videopreview;

	ZoomControls zoom_control;
	int zoom = 0;

	
	byte[] videodata;
	Bitmap bmp = null;
	Bitmap Resizedbmp=null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
		if (APP_ID == null) {
			Util.showAlert(this, "Warning", "Facebook Applicaton ID must be "
					+ "specified before running this example: see FbAPIs.java");
			return;
		}

		setContentView(R.layout.main_activity);
		mHandler = new Handler();

		// getLocation();

		mText = (TextView) MainActivity.this.findViewById(R.id.txt);
		mUserPic = (ImageView) MainActivity.this.findViewById(R.id.user_pic);
		// uploadedPic = (ImageView)
		// MainActivity.this.findViewById(R.id.uploadedimage);

		_previewflipper = (ViewFlipper) MainActivity.this
				.findViewById(R.id.previewflip);

		_layoutflipper = (ViewFlipper) MainActivity.this
				.findViewById(R.id.layout_flip);

		videopreview_Button = (Button) findViewById(R.id.videopreview_button);

		videoupload_Button = (Button) findViewById(R.id.video_upload);

		reset_Button = (Button) findViewById(R.id.reset_button);

		discard_Button = (Button) findViewById(R.id.image_discard);

		uploadimage_Button = (Button) findViewById(R.id.upload_image);

		uploadimage_text = (TextView) MainActivity.this
				.findViewById(R.id.uploadimage_response);

		fblayout = (RelativeLayout) MainActivity.this
				.findViewById(R.id.fbheader);

		leftPanel = (LinearLayout) MainActivity.this
				.findViewById(R.id.leftpanel_ll);

		preview = new Preview(this); // <3>

		frame = (FrameLayout) findViewById(R.id.preview);
		frame.addView(preview);

		
		//preview.camera.autoFocus(myAutoFocusCallback);
		
		
		captured_image = (ImageView) findViewById(R.id.Captured_Image);
		
		
		//image_layout= (LinearLayout) findViewById(R.id.image_layout);

		// location_box = (CheckBox) findViewById(R.id.location_check);

		// Create the Facebook Object using the app id.
		Utility.mFacebook = new Facebook(APP_ID);
		// Instantiate the asynrunner object for asynchronous api calls.
		Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);

		mLoginButton = (LoginButton) findViewById(R.id.login);

		Capture = (Button) findViewById(R.id.capture);

		VideoRecord = (Button) findViewById(R.id.video_record);

		// zoom_control = (ZoomControls) findViewById(R.id.zoom_camera);
		zoomin = (Button) findViewById(R.id.zoom_in);
		zoomout = (Button) findViewById(R.id.zoom_out);

		// restore session if one exists
		SessionStore.restore(Utility.mFacebook, this);
		SessionEvents.addAuthListener(new FbAPIsAuthListener());
		SessionEvents.addLogoutListener(new FbAPIsLogoutListener());

		/*
		 * Source Tag: login_tag
		 */
		mLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE,
				Utility.mFacebook, permissions);

		if (Utility.mFacebook.isSessionValid()) {
			requestUserData();
		}

		ALBUMURL = "https://graph.facebook.com/" + "me"
				+ "/albums&access_token=" + Utility.mFacebook.getAccessToken()
				+ "?limit=10";

		countDownTimer = new VideoCountDownTimer(startTime, interval);

		geoControlTimer = new GeoLocationCountDownTimer(geocontrolTime,
				interval);

		Capture.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// location_box.setChecked(false);
				// Perform action on click

				// imageuploadDialog();
				if (!Utility.mFacebook.isSessionValid()) {
					Util.showAlert(MainActivity.this, "Warning",
							"You must first log in.");
				} else {
					// preview.camera.takePicture(shutterCallback, rawCallback,
					// jpegCallback);

					if (Capture.getText().toString().contains("Take Picture")) {

						getLocation();

						MainActivity.this.data = null;
						preview.camera.takePicture(shutterCallback,rawCallback, jpegCallback);
						// Capture.setText("");

					} else if (Capture.getText().toString().contains("ReTake")) {

						getLocation();

						_previewflipper.setDisplayedChild(0);

						_layoutflipper.setDisplayedChild(0);

						MainActivity.this.data = null;
						preview.camera.takePicture(shutterCallback,
								rawCallback, jpegCallback);

					} else if (Capture.getText().toString().contains("Upload")) {

						_previewflipper.setDisplayedChild(0);

						captured_image.refreshDrawableState();
						// captured_image.requestFocus();
						// preview.refreshDrawableState();
						// preview.requestFocus();

						frame.removeAllViewsInLayout();
						frame.addView(preview);

						MainActivity.this.uploadimage_text
								.setText("Uploading Photo to Facebook.Please wait...");
						MainActivity.this.uploadimage_text
								.setTextColor(Color.RED);
						progressDialog = ProgressDialog.show(MainActivity.this,
								"", "Please wait");

						uploadPhoto();

						Capture.setText("Take Picture");
						// System.out.println("index:"+vf.indexOfChild(findViewById(R.id.preview)));

					}
				}
			}
		});

		VideoRecord.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (!Utility.mFacebook.isSessionValid()) {
					Util.showAlert(MainActivity.this, "Warning",
							"You must first log in.");
				} else {
					
					//Capture.setText("");
					//Capture.setEnabled(false);
					//Capture.setClickable(false);
					
					Capture.setVisibility(View.INVISIBLE);

					getLocation();
					_previewflipper.setDisplayedChild(0);
					_layoutflipper.setDisplayedChild(0);
					handleRecording();
				}

			}
		});

		videopreview_Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// frame.setVisibility(View.GONE);

				LinearLayout ll = (LinearLayout) MainActivity.this
						.findViewById(R.id.videolayout);
				// ll.setVisibility(View.GONE);

				Intent intentToPlayVideo = new Intent(Intent.ACTION_VIEW);
				intentToPlayVideo.setDataAndType(Uri.parse(VideoPath),
						"video/*");
				// startActivity(intentToPlayVideo);
				startActivityForResult(intentToPlayVideo, VIDEOPLAY);

			//	Capture.setEnabled(false);
				//Capture.setClickable(false);

			}
		});

		videoupload_Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				uploadimage_text.setText("Uploading Video to facebook.");

				uploadVideo();
				
				
				Capture.setText("Take Picture");
				//Capture.setEnabled(true);
				//Capture.setClickable(true);
				Capture.setVisibility(View.VISIBLE);

			}
		});

		reset_Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

			resetlayouts();
			}
		});

		discard_Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				Capture.setText("Take Picture");
				//captured_image.refreshDrawableState();
				frame.removeAllViewsInLayout();
				frame.addView(preview);

				//Capture.setEnabled(true);
				//Capture.setClickable(true);
				
				Capture.setVisibility(View.VISIBLE);

				leftPanel.setVisibility(View.VISIBLE);
				_previewflipper.setDisplayedChild(0);
				_layoutflipper.setDisplayedChild(0);
				
				bmp.recycle();
				Resizedbmp.recycle();
			}
		});

		uploadimage_Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// leftPanel.setVisibility(View.VISIBLE);
				

				captured_image.refreshDrawableState();
				// captured_image.requestFocus();
				// preview.refreshDrawableState();
				// preview.requestFocus();

				frame.removeAllViewsInLayout();
				frame.addView(preview);

				MainActivity.this.uploadimage_text
						.setText("Uploading Photo to Facebook.Please wait...");
				MainActivity.this.uploadimage_text.setTextColor(Color.RED);
				progressDialog = ProgressDialog.show(MainActivity.this, "",
						"Please wait");
				
				_previewflipper.setDisplayedChild(0);
				_layoutflipper.setDisplayedChild(0);
				
				

				uploadPhoto();

				Capture.setText("Take Picture");

			}
		});

		/*
		 * location_box.setOnCheckedChangeListener(new OnCheckedChangeListener()
		 * {
		 * 
		 * public void onCheckedChanged(CompoundButton buttonView, boolean
		 * isChecked) { // TODO Auto-generated method stub if
		 * (buttonView.isChecked()) { Capture.setEnabled(true);
		 * Capture.setClickable(true); Capture.setText("Upload"); } else
		 * Capture.setText(""); } });
		 */
		/*
		 * zoom_control.setOnZoomInClickListener(new OnClickListener() { public
		 * void onClick(View v) {
		 * 
		 * Camera.Parameters parameters=preview.camera.getParameters();
		 * if(parameters.getZoom()<parameters.getMaxZoom()) {
		 * parameters.setZoom(parameters.getZoom()+1);
		 * preview.camera.setParameters(parameters); }
		 * 
		 * } });
		 */
		
		 zoomin.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Camera.Parameters parameters=preview.camera.getParameters();
					if(parameters.getZoom()<parameters.getMaxZoom())
					{
					parameters.setZoom(parameters.getZoom()+1);
					preview.camera.setParameters(parameters);
					}
					// TODO Auto-generated method stub
					
				}
			});
		      
		      zoomout.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Camera.Parameters parameters=preview.camera.getParameters();
						if(parameters.getZoom()>1)
						{
						parameters.setZoom(parameters.getZoom()-1);
						preview.camera.setParameters(parameters);
						}
						// TODO Auto-generated method stub
						
					}
				});
	}

	
	
	
	
	
	private void resetlayouts()
	{
		
		
		_previewflipper.setDisplayedChild(0);
		_layoutflipper.setDisplayedChild(0);

		VideoRecord.setText("Take Video");
		
		Capture.setText("Take Picture");
		//Capture.setEnabled(true);
		//Capture.setClickable(true);
		Capture.setVisibility(View.VISIBLE);
	}
	
	public void handleRecording() {
		if (!recordingFlag) {

			try {

				countDownTimer.start();
				timerHasStarted = true;

				System.out.println("Recording..");
				startRecording();
			} catch (Exception e) {
				String message = e.getMessage();
				Log.i(null, "Problem Start" + message);
				mrec.release();
			}

		} else {

			countDownTimer.cancel();
			timerHasStarted = false;

			// uploadimage_text.setText("Uploading Video");
			VideoRecord.setText("Start");

			stopRecording();

		}

	}

	@SuppressLint("SdCardPath")
	protected void startRecording() throws IOException {

		// http://stackoverflow.com/questions/7225571/camcorderprofile-quality-high-resolution-produces-green-flickering-video

		Videoname = String.format("%d.3gp", System.currentTimeMillis());
		VideoPath = String.format("/sdcard/" + Videoname);

		recordingFlag = true;
		VideoRecord.setText("Stop");

		mrec = new MediaRecorder(); // Works well
		// mCamera.unlock();
		System.out.println("recording");
		preview.camera.unlock();
		mrec.setCamera(preview.camera);

		mrec.setPreviewDisplay(preview.mHolder.getSurface());
		mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
		// mrec.setMaxDuration(100000);//10kms=1min
		// mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		/*
		 * CamcorderProfile profile = CamcorderProfile
		 * .get(CamcorderProfile.QUALITY_HIGH); profile.videoFrameWidth = 640;
		 * profile.videoFrameHeight = 480; mrec.setProfile(profile);
		 */

		CamcorderProfile profile = CamcorderProfile
				.get(CamcorderProfile.QUALITY_LOW);
		profile.videoFrameWidth = 640;
		profile.videoFrameHeight = 480;
		mrec.setProfile(profile);

		// mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

		mrec.setMaxFileSize(10000000); // Set max file size 5M
		mrec.setPreviewDisplay(preview.mHolder.getSurface());

		mrec.setOutputFile(VideoPath);

		mrec.prepare();
		mrec.start();
	}

	protected void stopRecording() {

		System.out.println("stopping");
		mrec.stop();
		mrec.release();
		mrec = null;
		recordingFlag = false;

		// uploadVideo();

		_previewflipper.setDisplayedChild(1);

		VideoRecord.setText("Retake");
		System.gc();
	}

	private void uploadVideo() {
		System.out.println("uploading the video...");

		// preview.camera.release();
		// preview=null;

		/*
		 * MediaController controller = new MediaController(this);
		 * Videopreview.setMediaController(controller);
		 * 
		 * Videopreview.setVideoURI(Uri.parse(VideoPath));
		 * 
		 * 
		 * Videopreview.requestFocus(); // Videopreview.start();
		 */

		progressDialog = ProgressDialog.show(MainActivity.this, "",
				"Please wait");

	    videodata = null;
		String dataMsg = "Your video description here.";
		Bundle param;

		// Facebook facebook = new Facebook(APP_ID);
		AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(
				Utility.mFacebook);

		InputStream is = null;
		try {
			
			
			is = new FileInputStream(VideoPath);
			videodata = readBytes(is);
			param = new Bundle();
		//	param.putString("message", dataMsg);
			//param.putByteArray("video", videodata);

			System.gc();

			param = new Bundle();
			// param.putString("message", dataMsg);
			param.putString("filename", Videoname);

			if (MainActivity.locationData != null)
				dataMsg = MainActivity.locationData + " lat: "
						+ MainActivity.latitude + " long:"
						+ MainActivity.longitude;

			param.putString("message", dataMsg);

			if (MainActivity.fbplaceid != null)
				param.putString("place", MainActivity.fbplaceid);

			param.putByteArray("video", videodata);
			mAsyncRunner.request("me/videos", param, "POST",
					new VideoUploadListener(), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sucess uploaded");

	}

	public byte[] readBytes(InputStream inputStream) throws IOException {
		// This dynamically extends to take the bytes you read.
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		// This is storage overwritten on each iteration with bytes.
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// We need to know how may bytes were read to write them to the
		// byteBuffer.
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// And then we can return your byte array.
		return byteBuffer.toByteArray();
	}

	private void releaseMediaRecorder() {
		if (mrec != null) {
			mrec.reset(); // clear recorder configuration
			mrec.release(); // release the recorder object
			mrec = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Utility.mFacebook != null) {
			if (!Utility.mFacebook.isSessionValid()) {
				mText.setText("You are logged out! ");
				mUserPic.setImageBitmap(null);
			} else {

				fblayout.setVisibility(View.GONE);
				Utility.mFacebook.extendAccessTokenIfNeeded(this, null);

				// imageuploadDialog();

				lm = (LocationManager) MainActivity.this
						.getSystemService(Context.LOCATION_SERVICE);

				turnGPSOn();
				checkLocationProviders();

			}
		}

		/*
		 * if((this.albumArrayAdapter==null)||(this.albumArrayAdapter.getCount()<
		 * 1)){ new GetAlbumsTask().execute(""); }
		 */

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		/*
		 * if this is the activity result from authorization flow, do a call
		 * back to authorizeCallback Source Tag: login_tag
		 */
		case AUTHORIZE_ACTIVITY_RESULT_CODE: {
			Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
			break;
		}
			/*
			 * if this is the result for a photo picker from the gallery, upload
			 * the image after scaling it. You can use the Utility.scaleImage()
			 * function for scaling
			 */
		case PICK_FROM_CAMERA: {
			if (resultCode == Activity.RESULT_OK) {

				MainActivity.this.uploadimage_text
						.setText("Uploading to Facebook.Please wait...");
				MainActivity.this.uploadimage_text.setTextColor(Color.RED);

				progressDialog = ProgressDialog.show(MainActivity.this, "",
						"Please wait");
				getLocation();

			} else {

				Toast.makeText(getApplicationContext(),
						"No image captured for upload.", Toast.LENGTH_SHORT)
						.show();

			}

			break;
		}
		case GEOLOCATION_ENABLE: {

			checkLocationProviders();

		}

		case VIDEOPLAY: {

			System.out.println("Sucess :" + MainActivity.this.VideoPath);
		}

		}
	}

	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
	public class UserRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);

				final String picURL = jsonObject.getString("picture");
				final String name = jsonObject.getString("name");
				Utility.userUID = jsonObject.getString("id");

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mText.setText("Welcome " + name + "!");
						mUserPic.setImageBitmap(Utility.getBitmap(picURL));
					}
				});

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/*
	 * The Callback for notifying the application when authorization succeeds or
	 * fails.
	 */

	public class FbAPIsAuthListener implements AuthListener {

		@Override
		public void onAuthSucceed() {

			fblayout.setVisibility(View.GONE);
			requestUserData();
		}

		@Override
		public void onAuthFail(String error) {
			mText.setText("Login Failed: " + error);
		}
	}

	/*
	 * The Callback for notifying the application when log out starts and
	 * finishes.
	 */
	public class FbAPIsLogoutListener implements LogoutListener {
		@Override
		public void onLogoutBegin() {
			mText.setText("Logging out...");
		}

		@Override
		public void onLogoutFinish() {
			mText.setText("You have logged out! ");
			mUserPic.setImageBitmap(null);
		}
	}

	/*
	 * Request user name, and picture to show on the main screen.
	 */
	public void requestUserData() {
		mText.setText("Fetching user name, profile pic...");
		Bundle params = new Bundle();
		params.putString("fields", "name, picture");
		Utility.mAsyncRunner.request("me", params, new UserRequestListener());
	}

	class ViewHolder {
		TextView main_list_item;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item1) {

			System.out.println("capturing");

			// captureImage();
			imageuploadDialog();

		}
		return true;
	}

	public void imageuploadDialog() {

		if (!Utility.mFacebook.isSessionValid()) {
			Util.showAlert(this, "Warning", "You must first log in.");
		} else {

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			this.file = new File(Environment.getExternalStorageDirectory(),
					"tmp_" + String.valueOf(System.currentTimeMillis())
							+ ".jpg");

			ImageCaptureUri = Uri.fromFile(file);

			try {
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
						ImageCaptureUri);
				intent.putExtra("return-data", true);

				startActivityForResult(intent, PICK_FROM_CAMERA);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	// get location and upload the image.....

	/*
	 * callback for the photo upload
	 */
	public class PhotoUploadListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			// to dismiss the alert dialog window
			// dialog.dismiss();
			mHandler.post(new Runnable() {
				@Override
				public void run() {

					// uploadBitmap=Bitmap.createScaledBitmap(uploadBitmap, 250,
					// 250, false);
					// MainActivity.this.uploadedPic.setImageBitmap(uploadBitmap);

					System.out.println("responce is " + response);
					MainActivity.this.uploadimage_text
							.setTextColor(Color.WHITE);
					MainActivity.this.uploadimage_text.setText(String
							.valueOf("Photo Uploaded at :"
									+ MainActivity.locationData + " lat:"
									+ MainActivity.latitude + "  long:"
									+ MainActivity.longitude));
					// leftPanel.setVisibility(View.VISIBLE);

					geolocationControl = false;

					progressDialog.dismiss();
					
					image_layout.setBackgroundDrawable(null);
					image_layout=null;
					
					
					// new
					// UploadPhotoResultDialog(MainActivity.this,"Upload Photo executed",
					// response).show();
					
					bmp.recycle();
					Resizedbmp.recycle();
				}
			});
		}

		public void onFacebookError(FacebookError error) {
			dialog.dismiss();
			Toast.makeText(getApplicationContext(),
					"Facebook Error: " + error.getMessage(), Toast.LENGTH_LONG)
					.show();
			MainActivity.this.uploadimage_text.setText("Facebook Error: "
					+ error.getMessage());
		}
	}

	public class VideoUploadListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			// to dismiss the alert dialog window
			// dialog.dismiss();
			mHandler.post(new Runnable() {
				@Override
				public void run() {

					System.out.println("responce is:" + response);
					uploadimage_text.setText("Video Uploaded");
					progressDialog.dismiss();

					_previewflipper.setDisplayedChild(0);
					_layoutflipper.setDisplayedChild(0);

					VideoRecord.setText("Take Video");
					
					data=null;
					
					Capture.setText("Take Picture");
					Capture.setVisibility(View.VISIBLE);
					
					videodata = null;
					//Capture.setClickable(true);
				}
			});
		}

		public void onFacebookError(FacebookError error) {
			dialog.dismiss();
			Toast.makeText(getApplicationContext(),
					"Facebook Error: " + error.getMessage(), Toast.LENGTH_LONG)
					.show();
			MainActivity.this.uploadimage_text.setText("Facebook Error: "
					+ error.getMessage());
			//Capture.setEnabled(true);
			//Capture.setClickable(true);
			Capture.setVisibility(View.VISIBLE);
			videodata = null;
			
		}
	}

	public class placesRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			Log.d("Facebook-FbAPIs", "Got response: " + response);
			// dialog.dismiss();

			try {
				JSONArray jsonArray = new JSONObject(response)
						.getJSONArray("data");
				if (jsonArray == null) {

					Toast.makeText(getApplicationContext(),
							"Error: nearby places could not be fetched",
							Toast.LENGTH_LONG).show();

					return;
				} else {
					// System.out.println("places"+jsonArray);

					JSONObject main_obj = jsonArray.getJSONObject(0);
					String placeid = main_obj.getString("id");

					System.out.println("firstplace :" + placeid);

					geolocationControl = false;
					geoControlTimer.cancel();

				}
				// }
			} catch (JSONException e) {
				// showToast("Error: " + e.getMessage());
				return;
			}

		}

		public void onFacebookError(FacebookError error) {
			dialog.dismiss();
			// showToast("Fetch Places Error: " + error.getMessage());
		}
	}

	// Handles data for jpeg picture
	PictureCallback jpegCallback = new PictureCallback() { // <8>

		@SuppressLint("SdCardPath")
		public void onPictureTaken(byte[] bytedata, Camera camera) {

			System.out.println("jpegCallback");

			// leftPanel.setVisibility(View.INVISIBLE);

			// location_box.setChecked(false);

			MainActivity.this.data = null;

			byte[] byteArray = null;
			// FileOutputStream outStream = null;
			// try {
			// Write to SD Card
			// Write to SD Card

			System.gc();

		
			try{
				
				BitmapFactory.Options op = new BitmapFactory.Options();
				op.inPreferredConfig = Bitmap.Config.ARGB_8888;
				op.inDither = true;
			
			bmp=BitmapFactory.decodeByteArray(bytedata, 0, bytedata.length,op);
			Resizedbmp = Bitmap.createScaledBitmap(bmp, 350, 400, true);

			 } catch (OutOfMemoryError e) {
				    e.printStackTrace();

				    System.gc();

				    try {
				    	
				    	
				    	//assigning the image dataarray variable to fbupload..
						//MainActivity.this.data = bytedata.clone();
						
				    	BitmapFactory.Options op = new BitmapFactory.Options();
						op.inPreferredConfig = Bitmap.Config.ARGB_8888;
						op.inDither = true;
					
					bmp=BitmapFactory.decodeByteArray(bytedata, 0, bytedata.length,op);
					
				    	Resizedbmp = Bitmap.createScaledBitmap(bmp, 350, 400, true);
				    } catch (OutOfMemoryError e2) {
				      e2.printStackTrace();
				      // handle gracefully.
				      
				      Toast.makeText(MainActivity.this, "unable to capture", Toast.LENGTH_LONG).show();
				      resetlayouts();
				      return;
				    }
				}
			System.gc();
			
			// outStream = new
			// FileOutputStream(String.format("/sdcard/%d.jpg",System.currentTimeMillis()));
			// // <9>
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Resizedbmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byteArray = stream.toByteArray();

			// outStream.write(byteArray);
			// outStream.close();

			Log.d(TAG, "onPictureTaken - wrote bytes: " + bytedata.length);
			Log.d(TAG, "onPictureTaken - wrote bytes: " + byteArray.length);
			/*
			 * } catch (FileNotFoundException e) { // <10> e.printStackTrace();
			 * } catch (IOException e) { e.printStackTrace(); } finally { }
			 */

			_layoutflipper.setDisplayedChild(1);
			// frame.setVisibility(View.GONE);
		//	captured_image.setImageBitmap(Resizedbmp);

			System.gc();
			// Drawable drawable = (Drawable)new BitmapDrawable(Resizedbmp);
			// captured_image.setBackgroundDrawable(drawable);
			// outStream.write(byteArray);
			
			 Drawable drawable = (Drawable)new BitmapDrawable(Resizedbmp);
			 
			 image_layout= (LinearLayout) findViewById(R.id.image_layout);
			 
			 
			 image_layout.setBackgroundDrawable(drawable);

			MainActivity.this.data = byteArray.clone();
			
			
			
			byteArray=null;
			bytedata=null;
			System.gc();
			// drawable=null;
			
			
			//bmp = null;
			drawable=null;
			System.gc();
			
			
			//Resizedbmp=null;
			//System.out.println(MainActivity.this.data + " " + bytedata);

		}
	};

	public void uploadPhoto() {

		Bundle params = new Bundle();

		params.putByteArray("photo", MainActivity.this.data);

		if (MainActivity.locationData != null)
			params.putString("caption", MainActivity.locationData + " lat: "
					+ MainActivity.latitude + " long:" + MainActivity.longitude);

		String place = "\"location\"" + ": {" + "\"latitude\"" + ":"
				+ "\"12.9833\"" + "," + "\"longitude\"" + ":" + "\"77.5833\""
				+ "}";

		if (MainActivity.fbplaceid != null)
			params.putString("place", MainActivity.fbplaceid);

		Utility.mAsyncRunner.request("me/photos", params, "POST",
				new PhotoUploadListener(), null);

	}

	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() { // <6>
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() { // <7>
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
			// Perform any saving here
			// new SaveFile().execute(data);

			// camera.startPreview();
		}
	};

	public void getLocation() {

		geolocationControl = true;

		geoControlTimer.start();

		MainActivity.this.uploadimage_text
				.setText("Fetching location data please wait..");

		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(MainActivity.this, locationResult);

	}

	public void getGeoloation(double latitude, double longitude) {

		Geocoder geocoder = new Geocoder(
				MainActivity.this.getApplicationContext(), Locale.getDefault());

		try {
			List<Address> addresses = geocoder.getFromLocation(latitude,
					longitude, 1);
			if (null != addresses && addresses.size() > 0) {

				// String _Location = addresses.get(0).getAddressLine(1);
				// locationtv.setText("location is :"+_Location);

				String location = addresses.get(0).getFeatureName() + "-"
						+ addresses.get(0).getLocality() + "-"
						+ addresses.get(0).getAdminArea() + "-"
						+ addresses.get(0).getCountryName() + "-"
						+ addresses.get(0).getPostalCode();

				StringBuilder result = new StringBuilder();
				// mUsefulTip.setText(location+"  :"+" "+latitude+" "+longitude);

				// this.locationData=location;
				MainActivity.locationData = location;
				MainActivity.latitude = String.valueOf(latitude);
				MainActivity.longitude = String.valueOf(longitude);

				System.out.println(" location is :" + location);

				if (MainActivity.latitude.length() > 2
						&& MainActivity.longitude.length() > 2) {

					getfbPlaceid(MainActivity.latitude, MainActivity.longitude);

					uploadimage_text.setText(location);

				} else {

					// progressDialog.dismiss();
					Toast.makeText(MainActivity.this,
							"Unable to fetch location", Toast.LENGTH_LONG)
							.show();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	LocationResult locationResult = new LocationResult() {
		@SuppressLint("ShowToast")
		@Override
		public void gotLocation(Location location) {
			// Got the location!
			// location.getLatitude();
			// startActivity(audiolist);
			// locationtv.setText("location is :"+location.getLatitude()+" ;"+location.getLongitude());
			getGeoloation(location.getLatitude(), location.getLongitude());

		}
	};

	public void getfbPlaceid(String latititude, String longitude) {

		// latitude="12.9833";
		// longitude="77.5833";

		String URL = "https://graph.facebook.com/search?type=place&value=1&center="
				+ latititude
				+ ","
				+ longitude
				+ "&distance=1000&&limit=10&access_token="
				+ Utility.mFacebook.getAccessToken();

		Bundle params = new Bundle();
		params.putString("type", "place");
		params.putString("center", latititude + "," + longitude);
		params.putString("distance", "1000");
		params.putString("limit", "10");
		Utility.mAsyncRunner.request("search", params,
				new placesRequestListener());

	}

	// CountDownTimer class
	public class GeoLocationCountDownTimer extends CountDownTimer {

		public GeoLocationCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onFinish() {

			if (geolocationControl) {
				// progressDialog.dismiss();
				uploadimage_text
						.setText("Unable to fetch location.Uploading image to facebook.");
				// progressDialog = ProgressDialog.show(MainActivity.this, "",
				// "Please wait");
				// uploadPhoto();
			}
			//

		}

		@Override
		public void onTick(long millisUntilFinished) {

			System.out.println("timeis:" + millisUntilFinished);

			int seconds = (int) ((millisUntilFinished / 1000) % 60);
			int minutes = (int) ((millisUntilFinished / 1000) / 60);

			// uploadimage_text.setText(minutes + ":" + seconds);
			// timeElapsed = startTime - millisUntilFinished;
			// mText.setText("Time Elapsed: "+ String.valueOf(timeElapsed));
		}
	}

	// CountDownTimer class
	public class VideoCountDownTimer extends CountDownTimer {

		public VideoCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onFinish() {
			// uploadimage_text.setText("Uploading Video");
			VideoRecord.setText("Start");

			stopRecording();
			// mText.setText("Time Elapsed: "+ String.valueOf(startTime));
		}

		@Override
		public void onTick(long millisUntilFinished) {

			System.out.println("timeis:" + millisUntilFinished);

			int seconds = (int) ((millisUntilFinished / 1000) % 60);
			int minutes = (int) ((millisUntilFinished / 1000) / 60);

			uploadimage_text.setText(minutes + ":" + seconds);
			// timeElapsed = startTime - millisUntilFinished;
			// mText.setText("Time Elapsed: "+ String.valueOf(timeElapsed));
		}
	}

	private Bitmap decodeFile(File f) {
		Bitmap b = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

			int scale = 1;
			if (o.outHeight > 400 || o.outWidth > 400) {
				scale = (int) Math.pow(
						2,
						(int) Math.round(Math.log(200 / (double) Math.max(
								o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
			fis.close();
		} catch (IOException e) {
		}
		return b;
	}

	private void checkLocationProviders() {
		// String provider =
		// Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			Toast.makeText(MainActivity.this, "GPS provider Enabled: ",
					Toast.LENGTH_LONG).show();

		} else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			Toast.makeText(MainActivity.this, "Network provider Enabled: ",
					Toast.LENGTH_LONG).show();

		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Location providers are not available. Enable GPS or network providers.")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent intent = new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivityForResult(intent,
											GEOLOCATION_ENABLE);
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									MainActivity.this.finish();
								}
							}).show();

		}

	}

	private void turnGPSOn() {

		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			// Toast.makeText(MainActivity.this,
			// "GPS provider Enabled: ",Toast.LENGTH_LONG).show();

		} else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			// Toast.makeText(MainActivity.this,
			// "Network provider Enabled: ",Toast.LENGTH_LONG).show();

		} else {

			String provider = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

			if (!provider.contains("gps")) { // if gps is disabled
				final Intent poke = new Intent();
				poke.setClassName("com.android.settings",
						"com.android.settings.widget.SettingsAppWidgetProvider");
				poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				poke.setData(Uri.parse("3"));
				sendBroadcast(poke);
			}

		}

	}

	private void turnGPSOff() {
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (provider.contains("gps")) { // if gps is enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}
	}

	
	 AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

		  @Override
		  public void onAutoFocus(boolean arg0, Camera arg1) {
		   // TODO Auto-generated method stub
		//   buttonTakePicture.setEnabled(true);
		  }};
}
