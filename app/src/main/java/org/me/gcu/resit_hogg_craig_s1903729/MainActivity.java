package org.me.gcu.resit_hogg_craig_s1903729;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private Button startButton;
    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";
    private ListView exchangeRateListView;
    private CurrencyAdapter currencyAdapter;
    private ArrayList<CurrencyItem> currencyItemList;
    private SearchView searchView;
    private String result;
    private ToggleButton popularToggle;

    // Method to check if a string is numeric
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        TextView rawDataDisplay = findViewById(R.id.rawDataDisplay);
        rawDataDisplay.setVisibility(View.VISIBLE);

        popularToggle = findViewById(R.id.popularToggle);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        exchangeRateListView = findViewById(R.id.exchangeRateListView);
        currencyItemList = new ArrayList<>();
        currencyAdapter = new CurrencyAdapter(this, currencyItemList);
        exchangeRateListView.setAdapter(currencyAdapter);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currencyAdapter.getFilter().filter(newText);
                return false;
            }
        });

        // Toggle for popular currencies
        popularToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Hide the search view
                    searchView.setVisibility(View.GONE);

                    // Filter popular currencies
                    ArrayList<CurrencyItem> popularCurrencies = new ArrayList<>();
                    for (CurrencyItem item : currencyItemList) {
                        if (item.getTitle().contains("(EUR)") ||
                                item.getTitle().contains("(USD)") ||
                                item.getTitle().contains("(JPY)")) {
                            popularCurrencies.add(item);
                        }
                    }
                    currencyAdapter.updateCurrencyList(popularCurrencies);
                } else {
                    // Show the search view
                    searchView.setVisibility(View.VISIBLE);

                    // Reset the currency list
                    currencyAdapter.updateCurrencyList(currencyItemList);
                }
            }
        });
    }

    @Override
    public void onClick(View aview) {
        startProgress();
    }

    public void startProgress() {
        Log.d("MainActivity", "startProgress called");

        if (currencyItemList.isEmpty()) {
            Log.d("MainActivity", "currencyItemList is empty, starting NetworkTask");
            new NetworkTask().execute(urlSource);
        } else {
            Log.d("MainActivity", "currencyItemList is not empty, updating adapter");

            // Clear the existing list before populating with new data
            currencyItemList.clear();

            // Start the NetworkTask again to fetch the updated data
            new NetworkTask().execute(urlSource);
        }
    }

    private class NetworkTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                // Perform network access to retrieve XML data
                URL aurl = new URL(params[0]);
                URLConnection yc = aurl.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Store the retrieved XML data in 'result'
                result = response.toString();

                // Clear the existing list before populating with new data
                currencyItemList.clear();

                // Parse the XML data and populate currencyItemList
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(result));

                int eventType = parser.getEventType();
                CurrencyItem currencyItem = null; // Create a variable to hold the current CurrencyItem
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String tagName = parser.getName();

                        if ("item".equals(tagName)) {
                            // Start of a new <item> element, create a new CurrencyItem instance
                            currencyItem = new CurrencyItem();
                            Log.d("NetworkTask", "Created new CurrencyItem instance");

                        } else if ("title".equals(tagName) && currencyItem != null) {
                            String title = parser.nextText();
                            Log.d("NetworkTask", "Title: " + title);
                            currencyItem.setTitle(title);

                        } else if ("description".equals(tagName) && currencyItem != null) {
                            String description = parser.nextText();
                            Log.d("NetworkTask", "Raw description: " + description);

                            // Split the description to extract the exchange rate and abbreviation
                            String[] parts = description.split(" = ");
                            if (parts.length == 2) {
                                String[] currencyInfo = parts[1].split(" ");

                                double exchangeRate = 0.0;
                                String type = "";
                                // Loop through the currencyInfo array to find the exchange rate and type
                                for (int i = 0; i < currencyInfo.length; i++) {
                                    if (isNumeric(currencyInfo[i])) {
                                        exchangeRate = Double.parseDouble(currencyInfo[i]);
                                    } else {
                                        type = currencyInfo[i];
                                    }
                                }

                                currencyItem.setExchangeRate(exchangeRate);
                                currencyItem.setType(type);

                                // Log the exchange rate for the current currency item
                                Log.d("NetworkTask", "Exchange Rate: " + exchangeRate);
                                Log.d("NetworkTask", "Type: " + type);
                            }

                        } else if ("link".equals(tagName) && currencyItem != null) {
                            String link = parser.nextText();
                            Log.d("NetworkTask", "Link: " + link);
                            currencyItem.setLink(link);

                        } else if ("guid".equals(tagName) && currencyItem != null) {
                            String guid = parser.nextText();
                            Log.d("NetworkTask", "Guid: " + guid);
                            currencyItem.setGuid(guid);

                        } else if ("pubDate".equals(tagName) && currencyItem != null) {
                            String pubDate = parser.nextText();
                            Log.d("NetworkTask", "Publish Date: " + pubDate);
                            currencyItem.setPubDate(pubDate);

                        } else if ("category".equals(tagName) && currencyItem != null) {
                            String category = parser.nextText();
                            Log.d("NetworkTask", "Category: " + category);
                            currencyItem.setCategory(category);
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {
                        String tagName = parser.getName();
                        if ("item".equals(tagName) && currencyItem != null) {
                            // End of <item> element, add currencyItem to currencyItemList
                            currencyItemList.add(currencyItem);
                            currencyItem = null; // Reset currencyItem
                        }
                    }
                    eventType = parser.next();
                }

                // Log the size of the currencyItemList after parsing
                Log.d("NetworkTask", "CurrencyItemList size: " + currencyItemList.size());

                // Update the currencyItemList on the UI thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        // Hide the raw data display text once the List is passed to the UI
                        TextView rawDataDisplay = findViewById(R.id.rawDataDisplay);
                        rawDataDisplay.setVisibility(View.INVISIBLE);

                        // Recreate and set the adapter
                        currencyAdapter = new CurrencyAdapter(MainActivity.this, currencyItemList);
                        exchangeRateListView.setAdapter(currencyAdapter);
                    }
                });

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
