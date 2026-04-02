package com.darshita.currencyconverterapp;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextFrom;
    private TextView textAmountTo;
    private Spinner spinnerFrom, spinnerTo;
    private Button btnSettings;

    private final String[] currencies = {"USD", "INR", "EUR", "JPY"};
    private final double[] rates = {1.0, 83.20, 0.92, 150.50};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFrom = findViewById(R.id.editTextFrom);
        textAmountTo = findViewById(R.id.textAmountTo);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnSettings = findViewById(R.id.btnSettings);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(1);

        editTextFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateConversion();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateConversion();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFrom.setOnItemSelectedListener(spinnerListener);
        spinnerTo.setOnItemSelectedListener(spinnerListener);

        btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void calculateConversion() {
        String input = editTextFrom.getText().toString();
        if (input.isEmpty()) {
            textAmountTo.setText("0.00");
            return;
        }

        try {
            double amount = Double.parseDouble(input);
            int fromIndex = spinnerFrom.getSelectedItemPosition();
            int toIndex = spinnerTo.getSelectedItemPosition();

            double amountInUsd = amount / rates[fromIndex];
            double result = amountInUsd * rates[toIndex];

            textAmountTo.setText(String.format("%.2f", result));
        } catch (NumberFormatException e) {
            textAmountTo.setText("Invalid");
        }
    }
}