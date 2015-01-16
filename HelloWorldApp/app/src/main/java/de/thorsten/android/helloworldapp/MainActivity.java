package de.thorsten.android.helloworldapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    public final static String EXTRA_MESSAGE = "de.thorsten.android.helloworldapp.MESSAGE";
    //private final static String URL = "http://www.thomas-bayer.com/sqlrest/CUSTOMER/18/";
    private final static String URL = "http://localhost:8080/RestHelloWorld/resources/greeting";

    private class MessageResult {
        private String id;
        private String firstname;
        private String lastname;
        private String street;
        private String city;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void sendMessage(View view) {
        new CallAPI().execute(this.URL);
    }


    private class CallAPI extends AsyncTask<String, String, String> {
        @Override

        protected String doInBackground(String... params) {
            System.out.println("doInBackground called");

            String urlString = params[0];
            String resultToDisplay = "???";
            InputStream in = null;
            MessageResult messageResult = null;

            // HTTP Get

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e) {
                System.out.println("HTTP Get produced error + " + e.getMessage());
                return e.getMessage();
            }

            // Parse XML

            XmlPullParserFactory pullParserFactory;

            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                messageResult = parseXML(parser);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (messageResult != null) {
                resultToDisplay = messageResult.firstname + ", " + messageResult.lastname;
            } else {
                resultToDisplay = "Fehler - kein Response!";
            }
            System.out.println("returning in doBackground() ResultToDisplay = " + resultToDisplay);
            return resultToDisplay;
        }


        protected void onPostExecute(String result) {
            System.out.println("onPostExecute called");
            Intent intent = new Intent(getApplicationContext(), DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, result);
            System.out.println("Messagecontent= " + result);
            startActivity(intent);
        }

        private MessageResult parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {

            System.out.println("parseXML");
            int eventType = parser.getEventType();

            MessageResult result = new MessageResult();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                String name = null;

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        name = parser.getName();

                        if (name.equals("Error")) {
                            System.out.println("Web API Error!");
                        } else if (name.equals("FIRSTNAME")) {
                            result.firstname = parser.nextText();
                            System.out.println(result.firstname + " (Result.firstName gefunden)");
                        } else if (name.equals("LASTNAME")) {
                            result.lastname = parser.nextText();
                            System.out.println(result.lastname + " (Result.lastname gefunden)");
                        } else {
                            System.out.println("Nixgefunden");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                } // end switch
                eventType = parser.next();
            } // end while
            System.out.println("Result-Message = " + result.lastname + "," + result.firstname);
            return result;
        }

    }

}
