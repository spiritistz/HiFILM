package com.wordpress.hifilmtheapp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;
 
public class MainActivity extends FragmentActivity implements LocationListener, InfoWindowAdapter, OnMarkerClickListener, OnMarkerDragListener{
	
    static final LatLng LONDON = new LatLng(51.5072, 0.1275);
	static final LatLng UOW = new LatLng(51.57761, -0.324568);
	static final LatLng PARIS = new LatLng(48.8567, 2.3508);
	static final LatLng BERLIN = new LatLng(52.5167, 13.3833);
	static final LatLng MADRID = new LatLng(40.4000, 3.6833);
	static final LatLng ROME = new LatLng(41.9000, 12.5000);

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri fileUri; // File url to store image/video
    private VideoView videoPreview;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    @SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_DCIM), "HiFILM");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("HiFILM", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
               
        Button cb = (Button) findViewById(R.id.btn_Camera);
        Button vb = (Button) findViewById(R.id.btn_Video);
        videoPreview = (VideoView) findViewById(R.id.videoPreview);
        
        cb.setOnClickListener(new View.OnClickListener() {			
			
        	@Override
			public void onClick(View v) {
    		    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

			    fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  // create a file to save the video
			    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); // set the length limit to 10 second
			    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high

			    // start the Video Capture Intent
			    startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);				
			}
		});
        
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

            public void OnMarkerClickListener(Marker marker) {
            	startActivity(new Intent("android.intent.action.Video"));
            }

			@Override
			public boolean onMarkerClick(Marker arg0) {
				// TODO Auto-generated method stub
				return true;
			}
        });
                
        vb.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent("android.intent.action.Video"));
			}
		});                        
    }
    
    /** Receiving activity result method will be called after closing the camera */    
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// If the result is capturing Image
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" +
                         data.getData(), Toast.LENGTH_LONG)
                         .show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(getApplicationContext(),
                        "Image capturing been cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // Image capture failed, advise user
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Preview the recorded video
                previewVideo();
            	// Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved to:\n" +
                         data.getData(), Toast.LENGTH_LONG)
                         .show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
                Toast.makeText(getApplicationContext(),
                        "Video recording been cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // Video capture failed, advise user
            	Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

	/** Previewing recorded video */
    private void previewVideo() {
        try {
            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoURI(fileUri);
            // start playing
            videoPreview.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Here we store the file url as it will be null after returning from App */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);    
        // Save file url in bundle as it will be null on screen orientation
        outState.putParcelable("file_uri", fileUri);
    }
     
    /** Here we restore the fileUri again*/
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);     
        // Get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }    
    
	@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

	/**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
    	
    	mMap.setMyLocationEnabled(true);
    	mMap.getUiSettings().setCompassEnabled(true);
    	mMap.getUiSettings().setRotateGesturesEnabled(true);
    	
        mMap.addMarker(new MarkerOptions()
                .position(UOW)
                .title("University of Westminster")
                .snippet("Check out films here!")
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.marker_a)));
        mMap.addMarker(new MarkerOptions()
                .position(LONDON)
                .title("London")
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.marker_na)));
        mMap.addMarker(new MarkerOptions()
                .position(PARIS)
                .title("Paris")                
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.marker_na)));
        mMap.addMarker(new MarkerOptions()
                .position(BERLIN)
                .title("Berlin")                
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.marker_na)));
        mMap.addMarker(new MarkerOptions()
                .position(MADRID)
                .title("Madrid")                
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.marker_na)));
        mMap.addMarker(new MarkerOptions()
                .position(ROME)
                .title("Rome")                
                .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.marker_na)));
        
    }

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}
 
    
}