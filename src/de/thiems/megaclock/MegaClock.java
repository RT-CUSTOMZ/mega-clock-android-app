package de.thiems.megaclock;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MegaClock extends Activity {

	final static int max_color_value = 4095;

	SeekBar green, blue, red, brightness;
	EditText ip;
	Button temp;
	ToggleButton points;
	boolean showTemp = false;
	boolean togglePoints = false;
	String local_temp = "";
	int green_value, blue_value, red_value;
	float brightness_value;
	String ip_text = "";

	private static final String ns = null;
	String filename = "MegaClock";
	FileOutputStream outputStream;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		green = (SeekBar) findViewById(R.id.green);
		blue = (SeekBar) findViewById(R.id.blue);
		red = (SeekBar) findViewById(R.id.red);
		brightness = (SeekBar) findViewById(R.id.brightness);
		ip = (EditText) findViewById(R.id.server_ip);
		temp = (Button) findViewById(R.id.local_temp);
		points = (ToggleButton) findViewById(R.id.points);

		points.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				togglePoints = isChecked;
				new Thread(new ClientThread()).start();
			}
		});

		temp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTemp = true;
				local_temp = getTemperature();
				new Thread(new ClientThread()).start();
			}
		});

		green.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				new Thread(new ClientThread()).start();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				green_value = (int) (progress * brightness_value);
				new Thread(new ClientThread()).start();
			}
		});

		blue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				new Thread(new ClientThread()).start();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				blue_value = (int) (progress * brightness_value);
				new Thread(new ClientThread()).start();
			}
		});

		red.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				new Thread(new ClientThread()).start();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				red_value = (int) (progress * brightness_value);
				new Thread(new ClientThread()).start();
			}
		});

		brightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				brightness_value = (float) ((int) progress / (float) 100);

				red_value = (int) (red.getProgress() * brightness_value);
				green_value = (int) (green.getProgress() * brightness_value);
				blue_value = (int) (blue.getProgress() * brightness_value);

				// red.setProgress(red_value);
				// green.setProgress(green_value);
				// blue.setProgress(blue_value);

				// old_brightness = progress;

				new Thread(new ClientThread()).start();
			}
		});

		// ip.setText(ip_text);

		loadState();

		/*
		red_value = (int) (red.getProgress() * brightness_procent);
		green_value = (int) (green.getProgress() * brightness_procent);
		blue_value = (int) (blue.getProgress() * brightness_procent);*/

		new Thread(new ClientThread()).start();
	}

	@Override
	public void onDestroy() {
		saveState();
		super.onDestroy();
		System.exit(0);
	}

	public void loadState() {

		try {
			// deleteFile(filename);

			FileInputStream fIn = openFileInput(filename);

			InputStreamReader isr = new InputStreamReader(fIn);
			char[] inputBuffer = new char[4095];
			isr.read(inputBuffer);
			String readString = new String(inputBuffer);
			// red_value,green_value,blue_value,togglePoints,ip.getText().toString();

			String[] split = readString.split(":");

			if (split.length == 6) {
				red_value = Integer.parseInt(split[0]);
				green_value = Integer.parseInt(split[1]);
				blue_value = Integer.parseInt(split[2]);
				brightness_value = Float.parseFloat(split[3]);

				red.setProgress(red_value);
				green.setProgress(green_value);
				blue.setProgress(blue_value);
				brightness.setProgress((int) (brightness_value * 100));

				togglePoints = Boolean.parseBoolean(split[4]);
				points.setChecked(togglePoints);

				ip.setText(split[5].trim());
			}

		} catch (FileNotFoundException e) {
			Context context = getApplicationContext();
			CharSequence text = e.getMessage();
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} catch (IOException e) {
			Context context = getApplicationContext();
			CharSequence text = e.getMessage();
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

	}

	public void saveState() {
		try {
			deleteFile(filename);
			FileOutputStream fOut = openFileOutput(filename, MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			String string_to_write = red_value + ":" + green_value + ":"
					+ blue_value + ":" + brightness_value + ":" + togglePoints
					+ ":" + ip.getText().toString();

			osw.write(string_to_write);
			osw.flush();
			osw.close();

		} catch (FileNotFoundException e) {
			Context context = getApplicationContext();
			CharSequence text = e.getMessage();
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} catch (IOException e) {
			Context context = getApplicationContext();
			CharSequence text = e.getMessage();
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mega_clock, menu);
		return true;
	}

	public String getTemperature() {
		String temp = "-20";

		// http://api.openweathermap.org/data/2.5/weather?lat=35&lon=139&mode=xml

		try {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location locationGPS = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Location locationNet = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			URL url = null;

			if (locationGPS != null)
				url = new URL(
						"http://api.openweathermap.org/data/2.5/weather?lat="
								+ locationGPS.getLatitude() + "&lon="
								+ locationGPS.getLongitude() + "&mode=xml");
			else if (locationNet != null)
				url = new URL(
						"http://api.openweathermap.org/data/2.5/weather?lat="
								+ locationNet.getLatitude() + "&lon="
								+ locationNet.getLongitude() + "&mode=xml");

			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(con.getInputStream(), null);
			parser.nextTag();

			parser.require(XmlPullParser.START_TAG, ns, "current");
			while (true) {
				parser.next();
				if (parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}
				if (parser.getName().contains("temperature")) {
					temp = parser.getAttributeValue(0);
					break;
				}

				if (parser.getName().contains("city")) {
					Context context = getApplicationContext();
					CharSequence text = parser.getAttributeValue(1);
					int duration = Toast.LENGTH_LONG;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}

			// in.close();
		} catch (Exception e) {
			Context context = getApplicationContext();
			CharSequence text = "Please check your internet connection.";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

		int t = (int) (Float.parseFloat(temp) - 273.0);
		temp = String.valueOf(t);

		return temp;
	}

	class ClientThread implements Runnable {

		@Override
		public void run() {
			try {
				DatagramSocket socket = new DatagramSocket();
				InetAddress serverIP = InetAddress.getByName(ip.getText()
						.toString());

				if (showTemp == true) {
					byte[] temp = ("Temp:" + local_temp + "\n").getBytes();
					DatagramPacket out = new DatagramPacket(temp, temp.length,
							serverIP, 4242);
					socket.send(out);
					showTemp = false;
				} else {
					byte[] toggle = ("TogglePoints:"
							+ ((togglePoints) ? '1' : '0') + "\n").getBytes();
					DatagramPacket out2 = new DatagramPacket(toggle,
							toggle.length, serverIP, 4242);
					socket.send(out2);

					byte[] color = ("Color:" + green_value + ":" + blue_value
							+ ":" + red_value + "\n").getBytes();
					DatagramPacket out = new DatagramPacket(color,
							color.length, serverIP, 4242);
					socket.send(out);

				}

				socket.close();
			} catch (Exception e) {
				return;
			}
		}

	}

}
