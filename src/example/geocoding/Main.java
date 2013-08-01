package example.geocoding;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.Math;
import android.util.Log;
import android.util.FloatMath;



public class Main extends Activity 
{
	private LocationManager locationManager;
	private Location currentLocation;
	private TextView txtLatitude;
	private TextView txtLongitude;
	private Button btnReverseGeocode;
	private Button btnTestTest;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Get handles to the elements on our android activity page.
        this.txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        this.txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        this.btnReverseGeocode = (Button) findViewById(R.id.btnReverseGeocode);
        this.btnTestTest = (Button) findViewById(R.id.btnTestTest);
        
        // Subscribe to our button's click event
        this.btnReverseGeocode.setOnClickListener(
            	new OnClickListener() {
            		public void onClick(View v)
            		{
            			handleReverseGeocodeClick();
            		}
            	}
        );
        
        // Subscribe to our second button's click event
        this.btnTestTest.setOnClickListener(
            	new OnClickListener() {            		
            		public void onClick(View v)
            		{
            		    // Gander - Last test in Canada 48.956944, -54.608889
            		    // Houston - Last test in USA 29.661111, -95.229722
            		    
            			// Current points - note integer values - essentially AbsoluteValue(Truncate(point * 1000))
            		    int curLat = 29661;
            		    int curLon = 95230;
            			
            			int duration = Toast.LENGTH_LONG;
            			boolean fFoundIt = false;

            			// Loop to find the closest city
            			int cityCounter = 398;
            			String sMyCityDef;
            			String sClosestCity = "";
            			String sClosestState = "";
            			double dClosestDist = 100000000;
            			double tmpDist = 10000000;
            			double tmpLat = 0;
            			double tmpLon = 0;
            			for (int z = 0; z < cityCounter; z++) {
            				
            				// Retrieve the city from the xml file
            				sMyCityDef = getStringFromArray(R.array.cities, z);
	            			Log.v("onClick",sMyCityDef);
            				
            		    	String[] subCityResult = sMyCityDef.split(",");
            		    	if (!subCityResult[1].equals("")) {
            		    		tmpLat = Math.abs(Double.valueOf(subCityResult[1]));
            		    	}
            		    	if (!subCityResult[2].equals("")) {
            		    		tmpLon = Math.abs(Double.valueOf(subCityResult[2]));
            		    	}

            		    	// Get the distance to this city
            		    	tmpDist = distance((float)tmpLat, (float)tmpLon, curLat / 1000, curLon / 1000);
            		    	
            		    	// Are we closer than the last one ?
	            			if (tmpDist < dClosestDist) {
	            				dClosestDist = tmpDist;
	            				sClosestCity = sMyCityDef;

	            		    	String[] subCityResultNew = sMyCityDef.split(",");
	            				if (!subCityResult[2].equals("")) {
	            					sClosestState = subCityResultNew[3];
	            		    	}
	            			
	            			}
            			}
            			
            			// Log the closest city - based on the results of the loop
            			Log.v("Closest City: ", sClosestCity);
            			
            			
            			// Do a "quick lookup" to see if we are in the same state/province as the closest city
            			String sMyPolygonDef;
            			int iJurisdictionCount = 365;
            			for (int y = 0; y < iJurisdictionCount; y++) {
            				if (getStringFromArray(R.array.jurisdiction_names, y).equals(sClosestState)) {
                				sMyPolygonDef = getStringFromArray(R.array.jurisdiction_bounds, y);
            					
    	            			// This splits the def into a series of Lon/Lat pairs
    	            		    String[] result = sMyPolygonDef.split(" ");
    	            		    int[] aLat = new int[result.length];
    	            		    int[] aLon = new int[result.length];
    	            		    for (int x=0; x<result.length; x++)
    	            		    {
    	            		    	// This splits the pairs into individual parts
    	            		    	String[] subResult = result[x].split(",");
    	            		    	if (!subResult[0].equals("")) {
    	            		    		aLon[x] = (int)(Math.abs(Double.valueOf(subResult[0]) * 1000));
    	            		    	}
    	            		    	if (!subResult[1].equals("")) {
    	            		    		aLat[x] = (int)(Math.abs(Double.valueOf(subResult[1]) * 1000));
    	            		    	}
    	            		    }

    	            		    // This defines the polygon to do the contains function
    	            		    Polygon poly = new Polygon(aLat,aLon,result.length);
    	            		                			            		    
    	            		    // Check if it contains the point
    	            		    if (poly.contains(curLat, curLon) == true) 
    	            		    {
    	            		    	// Toast the world if it does
			            			Log.v("Found it: ",getStringFromArray(R.array.jurisdiction_names, y));
		            		    	Toast toast2 = Toast.makeText(getApplicationContext(), sClosestCity + " - " + getStringFromArray(R.array.jurisdiction_names, y), duration);
		            		    	toast2.show();
    	            		    	fFoundIt = true;
    	            		    }		
            				}
            			}
            			
            			// if we couldn't find it in the local state, check the rest
            			if (!fFoundIt) {
            			
	            			// Create the loop to determine which jurisdiction it's in
	            			// Note the first part of the xml includes the territories, alaska and hawaii
	            			// This lets you cut out a vast number of island comparisons (with the starting y value)
	            			iJurisdictionCount = 365;
	            			for (int y = 163; y < iJurisdictionCount; y++) {
	            				
		            			// This retrieves the poly def from the xml file
	            				sMyPolygonDef = getStringFromArray(R.array.jurisdiction_bounds, y);
		            			Log.v("onClick",sMyPolygonDef);
		            			
		            			// This splits the def into a series of Lon/Lat pairs
		            		    String[] result = sMyPolygonDef.split(" ");
		            		    int[] aLat = new int[result.length];
		            		    int[] aLon = new int[result.length];
		            		    for (int x=0; x<result.length; x++)
		            		    {
		            		    	// This splits the pairs into individual parts
		            		    	String[] subResult = result[x].split(",");
		            		    	if (!subResult[0].equals("")) {
		            		    		aLon[x] = (int)(Math.abs(Double.valueOf(subResult[0]) * 1000));
		            		    	}
		            		    	if (!subResult[1].equals("")) {
		            		    		aLat[x] = (int)(Math.abs(Double.valueOf(subResult[1]) * 1000));
		            		    	}
		            		    }
	
		            		    // This defines the polygon to do the contains function
		            		    Polygon poly = new Polygon(aLat,aLon,result.length);
		            		                			            		    
		            		    // Check if it contains the point
		            		    if (poly.contains(curLat, curLon) == true) 
		            		    {
		            		    	// Toast the world if it does
			            			Log.v("Found it: ",getStringFromArray(R.array.jurisdiction_names, y));
		            		    	Toast toast2 = Toast.makeText(getApplicationContext(), sClosestCity + " - " + getStringFromArray(R.array.jurisdiction_names, y), duration);
		            		    	toast2.show();
		            		    	break;
		            		    }
	            		    }
            			}
            		}
            	}
        );

        // Get an instance of the android system LocationManager 
	// so we can access the phone's GPS receiver
	this.locationManager = 
		(LocationManager) getSystemService(Context.LOCATION_SERVICE);
	
	// Subscribe to the location manager's updates on the current location
	this.locationManager.requestLocationUpdates("gps", (long)30000, (float) 10.0, new LocationListener()
		{
			public void onLocationChanged(Location arg0) 
			{
				handleLocationChanged(arg0);
			}
		
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}
		
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
				
			}
		
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub
			}
		});
    }
    
    // This is defined to be shorthand for data retrieval
    private String getStringFromArray(int id, int index) 
    {
    	try 
    	{
    		String[] bases = getResources().getStringArray(id);
    		return bases[index];
    	} catch (Exception e) {
    		return "";
    	}
    }
    
    private double distance(float lat1,float lon1,float lat2,float lon2)
    {
    	float magic = 57.2958f;
    	double totalDistance = 3963.0 * java.lang.Math.acos(FloatMath.sin(lat1 / magic) * FloatMath.sin(lat2 / magic) + FloatMath.cos(lat1 / magic) * FloatMath.cos(lat2 / magic) * FloatMath.cos(lon2 / magic - lon1 / magic));

        return totalDistance;
    }
    
    private void handleLocationChanged(Location loc)
    {
    	// Save the latest location
    	this.currentLocation = loc;
    	// Update the latitude & longitude TextViews
    	this.txtLatitude.setText(Double.toString(loc.getLatitude()));
    	this.txtLongitude.setText(Double.toString(loc.getLongitude()));
    }
    
    private void handleReverseGeocodeClick()
    {
    	if (this.currentLocation != null)
    	{
    		// Kickoff an asynchronous task to fire the reverse geocoding
    		// request off to google
    		ReverseGeocodeLookupTask task = new ReverseGeocodeLookupTask();
    		task.applicationContext = this;
    		task.execute();
    	}
    	else
    	{
    		// If we don't know our location yet, we can't do reverse
    		// geocoding - display a please wait message
    		showToast("Please wait until we have a location fix from the gps");
    	}
    }
    
	public void showToast(CharSequence message)
    {
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getApplicationContext(), message, duration);
		toast.show();
    }
	
	public class ReverseGeocodeLookupTask extends AsyncTask <Void, Void, String>
    {
    	private ProgressDialog dialog;
    	protected Context applicationContext;
    	
    	@Override
    	protected void onPreExecute()
    	{
    		this.dialog = ProgressDialog.show(applicationContext, "Please wait...contacting the tubes.", 
                    "Requesting reverse geocode lookup", true);
    	}
    	
		@Override
		protected String doInBackground(Void... params) 
		{
			String localityName = "";
			
			if (currentLocation != null)
			{
				localityName = Geocoder.reverseGeocode(currentLocation);
			}
			
			return localityName;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			this.dialog.cancel();
			Utilities.showToast("Your Locality is: " + result, applicationContext);
		}
    }
}