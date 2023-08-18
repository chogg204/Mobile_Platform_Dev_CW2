package org.me.gcu.resit_hogg_craig_s1903729;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class CurrencyAdapter extends ArrayAdapter<CurrencyItem> implements Filterable {
    private Context mContext;
    private ArrayList<CurrencyItem> mCurrencyItemList;
    private ArrayList<CurrencyItem> mFilteredCurrencyList;
    private Filter mFilter = new CurrencyFilter();

    // Constructor to initialize the adapter
    public CurrencyAdapter(Context context, ArrayList<CurrencyItem> currencyItemList) {
        super(context, 0, currencyItemList);
        mContext = context;
        mCurrencyItemList = currencyItemList;
        mFilteredCurrencyList = new ArrayList<>(currencyItemList);
    }

    // Update the currency list with new data
    public void updateCurrencyList(ArrayList<CurrencyItem> newCurrencyList) {
        mFilteredCurrencyList = new ArrayList<>(newCurrencyList);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item_currency, parent, false);
        }

        CurrencyItem currentItem = mFilteredCurrencyList.get(position);

        // Initialize UI elements
        TextView titleView = listItem.findViewById(R.id.titleTextView);
        titleView.setText(currentItem.getTitle());

        TextView descriptionView = listItem.findViewById(R.id.textDescription);
        descriptionView.setText(currentItem.getDescription()); // Set the description

        EditText multiplierEditText = listItem.findViewById(R.id.multiplierEditText);
        Button calculateButton = listItem.findViewById(R.id.calculateButton);
        TextView resultTextView = listItem.findViewById(R.id.resultTextView);
        CheckBox reverseCheckBox = listItem.findViewById(R.id.reverseCheckBox);

        int visibility = multiplierEditText.getVisibility();
        multiplierEditText.setVisibility(visibility);
        calculateButton.setVisibility(visibility);
        resultTextView.setVisibility(View.GONE);

        // Checkbox listener for exchange rate reversal
        reverseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                double exchangeRate = currentItem.getExchangeRate();
                if (isChecked) {
                    exchangeRate = 1.0 / exchangeRate;
                }
                currentItem.setExchangeRate(exchangeRate);

                // Call the updateHint method and pass the multiplierEditText
                updateHint(reverseCheckBox, currentItem, multiplierEditText);

                notifyDataSetChanged();
            }
        });

        // Button listener to calculate exchange
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String multiplierText = multiplierEditText.getText().toString();
                if (!multiplierText.isEmpty()) {
                    double multiplier = Double.parseDouble(multiplierText);
                    double calculatedResult = currentItem.getExchangeRate() * multiplier;

                    String resultText;
                    if (reverseCheckBox.isChecked()) {
                        resultText = String.format("%.2f %s", calculatedResult, "British Pounds");
                    } else {
                        resultText = String.format("%.2f %s", calculatedResult, currentItem.getType());
                    }

                    resultTextView.setText(resultText);
                    resultTextView.setVisibility(View.VISIBLE);
                } else {
                    // Show an error message when the multiplier is empty
                    resultTextView.setText("Please enter a multiplier");
                    resultTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        // ListItem click listener to toggle visibility
        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int visibility = multiplierEditText.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                multiplierEditText.setVisibility(visibility);
                calculateButton.setVisibility(visibility);
                resultTextView.setVisibility(View.GONE);

                // Set visibility of reverseCheckBox based on the visibility of multiplierEditText
                reverseCheckBox.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);

                // Call the updateHint method and pass the multiplierEditText
                updateHint(reverseCheckBox, currentItem, multiplierEditText);
            }
        });

        return listItem;
    }

    // Method to update the hint of multiplierEditText based on checkbox state
    private void updateHint(CheckBox reverseCheckBox, CurrencyItem currentItem, EditText multiplierEditText) {
        if (reverseCheckBox.isChecked()) {
            multiplierEditText.setHint("Enter amount (" + currentItem.getType() + ")");
        } else {
            multiplierEditText.setHint("Enter amount (£)");
        }
    }

    @Override
    public int getCount() {
        return mFilteredCurrencyList.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    // Custom Filter class for currency list filtering
    private class CurrencyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d("CurrencyAdapter", "performFiltering called with constraint: " + constraint);
            FilterResults results = new FilterResults();
            List<CurrencyItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mCurrencyItemList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (CurrencyItem item : mCurrencyItemList) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredCurrencyList.clear();
            mFilteredCurrencyList.addAll((List<CurrencyItem>) results.values);
            notifyDataSetChanged();
        }
    }
}
