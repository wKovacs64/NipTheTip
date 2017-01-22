/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Justin R. Hall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.wkovacs64.nipthetip.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.wkovacs64.nipthetip.App;
import com.wkovacs64.nipthetip.R;
import com.wkovacs64.nipthetip.ui.dialog.InputDialog;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import timber.log.Timber;

import static com.wkovacs64.nipthetip.util.GeneralUtils.tintStatusBarOnKitKat;

/**
 * The splitter/calculator view.
 */
public final class CalcActivity extends AppCompatActivity implements InputDialog.Callback {
    /*
     * Identifiers to pass to InputDialog so it knows which callback method to call with the result
     */
    public static final int FIELD_UNDEFINED = -1;
    public static final int FIELD_BILL_AMOUNT = 0;
    public static final int FIELD_TIP_PERCENT = 1;
    public static final int FIELD_TIP_AMOUNT = 2;
    public static final int FIELD_TOTAL_AMOUNT = 3;
    public static final int FIELD_NUMBER_OF_PEOPLE = 4;
    public static final int FIELD_EACH_PERSON_PAYS = 5;

    /*
     * Numeric formatters to use when setting EditText contents
     */
    private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");
    private static final NumberFormat INT_FORMATTER = NumberFormat.getIntegerInstance();

    /*
     * Default/common BigDecimal values
     */
    private static final BigDecimal DEFAULT_TIP_PERCENT = new BigDecimal("10");
    private static final BigDecimal DEFAULT_NUM_PEOPLE = new BigDecimal("1");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    /*
     * Configuration items for arithmetic operations
     */
    private static final int DECIMALS_PCT = 2;
    private static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_EVEN;

    /*
     * TextWatchers
     */
    private TextWatcher billAmountWatcher;
    private TextWatcher tipPercentWatcher;
    private TextWatcher tipAmountWatcher;
    private TextWatcher totalAmountWatcher;
    private TextWatcher numberOfPeopleWatcher;
    private TextWatcher eachPersonPaysWatcher;

    @State
    BigDecimal billAmount;
    @State
    BigDecimal tipPercent;
    @State
    BigDecimal tipAmount;
    @State
    BigDecimal totalAmount;
    @State
    BigDecimal numberOfPeople;
    @State
    BigDecimal eachPersonPays;

    @BindView(R.id.view_root)
    LinearLayout rootView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edit_bill_amount)
    EditText billAmountField;
    @BindView(R.id.edit_tip_percent)
    EditText tipPercentField;
    @BindView(R.id.edit_tip_amount)
    EditText tipAmountField;
    @BindView(R.id.edit_total_amount)
    EditText totalAmountField;
    @BindView(R.id.edit_number_of_people)
    EditText numberOfPeopleField;
    @BindView(R.id.edit_each_person_pays)
    EditText eachPersonPaysField;

    @Inject
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("********** onCreate **********");
        setContentView(R.layout.activity_calc);

        // Initialize Butter Knife bindings
        ButterKnife.bind(this);

        // Restore Icepick states from savedInstanceState
        Icepick.restoreInstanceState(this, savedInstanceState);

        // Initialize Dagger dependency injections
        App.getAppComponent().inject(this);

        // Enable status bar tint on KitKat
        tintStatusBarOnKitKat(this);

        // Initialize default preferences
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // Initialize the Toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Initialize the TextWatcher objects
        initWatchers();

        // Initialize starting values
        if (savedInstanceState == null) {
            initValues();
            updateUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("********** onResume **********");

        // Start observing user input
        startObservations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("********** onCreateOptionsMenu **********");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("********** onPause **********");

        // Stop observing user input
        stopObservations();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store Icepick states in outState
        Icepick.saveInstanceState(this, outState);
    }

    @OnClick(R.id.edit_bill_amount)
    void onEditBillAmountClicked() {
        InputDialog.show(this, FIELD_BILL_AMOUNT);
    }

    @Override
    public void onBillAmountInput(String input) {
        Timber.d("Bill Amount user input: %s", input);
        billAmount = new BigDecimal(input);
        billAmountField.setText(FORMATTER.format(billAmount));
    }

    @OnClick(R.id.edit_tip_percent)
    void onEditTipPercentClicked() {
        InputDialog.show(this, FIELD_TIP_PERCENT);
    }

    @Override
    public void onTipPercentInput(String input) {
        Timber.d("Tip Percent user input: %s", input);
        tipPercent = new BigDecimal(input);
        tipPercentField.setText(INT_FORMATTER.format(tipPercent));
    }

    @OnClick(R.id.edit_tip_amount)
    void onEditTipAmountClicked() {
        InputDialog.show(this, FIELD_TIP_AMOUNT);
    }

    @Override
    public void onTipAmountInput(String input) {
        Timber.d("Tip Amount user input: %s", input);
        tipAmount = new BigDecimal(input);
        tipAmountField.setText(FORMATTER.format(tipAmount));
    }

    @OnClick(R.id.edit_total_amount)
    void onEditTotalAmountClicked() {
        InputDialog.show(this, FIELD_TOTAL_AMOUNT);
    }

    @Override
    public void onTotalAmountInput(String input) {
        Timber.d("Total Amount user input: %s", input);
        BigDecimal proposedValue = new BigDecimal(input);

        if (proposedValue.compareTo(billAmount) >= 0) {
            totalAmount = proposedValue;
            totalAmountField.setText(FORMATTER.format(totalAmount));
        } else {
            Snackbar.make(rootView, R.string.snack_invalid, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.edit_number_of_people)
    void onEditNumberOfPeopleClicked() {
        InputDialog.show(this, FIELD_NUMBER_OF_PEOPLE);
    }

    @Override
    public void onNumberOfPeopleInput(String input) {
        Timber.d("Number Of People user input: %s", input);
        BigDecimal proposedValue = new BigDecimal(input);

        if (proposedValue.compareTo(BigDecimal.ZERO) > 0) {
            numberOfPeople = proposedValue;
            numberOfPeopleField.setText(INT_FORMATTER.format(numberOfPeople));
        } else {
            Snackbar.make(rootView, R.string.snack_no_people, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.edit_each_person_pays)
    void onEditEachPersonPaysClicked() {
        InputDialog.show(this, FIELD_EACH_PERSON_PAYS);
    }

    @Override
    public void onEachPersonPaysInput(String input) {
        Timber.d("Each Person Pays user input: %s", input);
        BigDecimal proposedValue = new BigDecimal(input);
        BigDecimal lowerLimit = billAmount.divide(numberOfPeople, ROUNDING_MODE);

        if (proposedValue.compareTo(lowerLimit) >= 0) {
            eachPersonPays = proposedValue;
            eachPersonPaysField.setText(FORMATTER.format(eachPersonPays));
        } else {
            Snackbar.make(rootView, R.string.snack_invalid, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.image_bill_amount_down)
    void onBillAmountDown() {
        if (billAmount.compareTo(BigDecimal.ONE) >= 0) {
            if (billAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                billAmount = billAmount.subtract(BigDecimal.ONE);
            } else {
                billAmount = billAmount.setScale(0, BigDecimal.ROUND_DOWN);
            }
            billAmountField.setText(FORMATTER.format(billAmount));
        }
    }

    @OnClick(R.id.image_bill_amount_up)
    void onBillAmountUp() {
        if (billAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            billAmount = billAmount.add(BigDecimal.ONE);
        } else {
            billAmount = billAmount.setScale(0, BigDecimal.ROUND_UP);
        }
        billAmountField.setText(FORMATTER.format(billAmount));
    }

    @OnClick(R.id.image_tip_percent_down)
    void onTipPercentDown() {
        if (tipPercent.compareTo(BigDecimal.ZERO) > 0) {
            if (tipPercent.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                tipPercent = tipPercent.subtract(BigDecimal.ONE);
            } else {
                tipPercent = tipPercent.setScale(0, BigDecimal.ROUND_DOWN);
            }
            tipPercentField.setText(INT_FORMATTER.format(tipPercent));
        }
    }

    @OnClick(R.id.image_tip_percent_up)
    void onTipPercentUp() {
        if (tipPercent.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            tipPercent = tipPercent.add(BigDecimal.ONE);
        } else {
            tipPercent = tipPercent.setScale(0, BigDecimal.ROUND_UP);
        }
        tipPercentField.setText(INT_FORMATTER.format(tipPercent));
    }

    @OnClick(R.id.image_tip_amount_down)
    void onTipAmountDown() {
        if (tipAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (tipAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                tipAmount = tipAmount.subtract(BigDecimal.ONE);
            } else {
                tipAmount = tipAmount.setScale(0, BigDecimal.ROUND_DOWN);
            }
            tipAmountField.setText(FORMATTER.format(tipAmount));
        }
    }

    @OnClick(R.id.image_tip_amount_up)
    void onTipAmountUp() {
        if (tipAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            tipAmount = tipAmount.add(BigDecimal.ONE);
        } else {
            tipAmount = tipAmount.setScale(0, BigDecimal.ROUND_UP);
        }
        tipAmountField.setText(FORMATTER.format(tipAmount));
    }

    @OnClick(R.id.image_total_amount_down)
    void onTotalAmountDown() {
        if (totalAmount.compareTo(billAmount) > 0) {
            if (totalAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                totalAmount = totalAmount.subtract(BigDecimal.ONE);
            } else {
                totalAmount = totalAmount.setScale(0, BigDecimal.ROUND_DOWN);
            }
            totalAmountField.setText(FORMATTER.format(totalAmount));
        }
    }

    @OnClick(R.id.image_total_amount_up)
    void onTotalAmountUp() {
        if (totalAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            totalAmount = totalAmount.add(BigDecimal.ONE);
        } else {
            totalAmount = totalAmount.setScale(0, BigDecimal.ROUND_UP);
        }
        totalAmountField.setText(FORMATTER.format(totalAmount));
    }

    @OnClick(R.id.image_number_of_people_down)
    void onNumberOfPeopleDown() {
        if (numberOfPeople.compareTo(BigDecimal.ONE) > 0) {
            if (numberOfPeople.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                numberOfPeople = numberOfPeople.subtract(BigDecimal.ONE);
            } else {
                numberOfPeople = numberOfPeople.setScale(0, BigDecimal.ROUND_DOWN);
            }
            numberOfPeopleField.setText(INT_FORMATTER.format(numberOfPeople));
        }
    }

    @OnClick(R.id.image_number_of_people_up)
    void onNumberOfPeopleUp() {
        if (numberOfPeople.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            numberOfPeople = numberOfPeople.add(BigDecimal.ONE);
        } else {
            numberOfPeople = numberOfPeople.setScale(0, BigDecimal.ROUND_UP);
        }
        numberOfPeopleField.setText(INT_FORMATTER.format(numberOfPeople));
    }

    @OnClick(R.id.image_each_person_pays_down)
    void onEachPersonPaysDown() {
        BigDecimal lowerLimit = billAmount.divide(numberOfPeople, ROUNDING_MODE);
        if (eachPersonPays.compareTo(lowerLimit) > 0) {
            if (eachPersonPays.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                eachPersonPays = eachPersonPays.subtract(BigDecimal.ONE);
            } else {
                eachPersonPays = eachPersonPays.setScale(0, BigDecimal.ROUND_DOWN);
            }

            if (eachPersonPays.compareTo(lowerLimit) < 0) {
                eachPersonPays = lowerLimit;
            }

            eachPersonPaysField.setText(FORMATTER.format(eachPersonPays));
        }
    }

    @OnClick(R.id.image_each_person_pays_up)
    void onEachPersonPaysUp() {
        if (eachPersonPays.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            eachPersonPays = eachPersonPays.add(BigDecimal.ONE);
        } else {
            eachPersonPays = eachPersonPays.setScale(0, BigDecimal.ROUND_UP);
        }
        eachPersonPaysField.setText(FORMATTER.format(eachPersonPays));
    }

    private void initValues() {
        // Bill Amount (from layout default)
        billAmount = new BigDecimal(billAmountField.getText().toString());

        // Tip Percent (from prefs or default)
        String defaultTipPercent = prefs.getString(App.KEY_PREF_TIP_PERCENT, null);
        if (defaultTipPercent != null) {
            tipPercent = new BigDecimal(defaultTipPercent);
        } else {
            tipPercent = DEFAULT_TIP_PERCENT;
        }

        // Tip Amount (calculated)
        tipAmount = tipPercent.divide(ONE_HUNDRED, ROUNDING_MODE);
        tipAmount = tipAmount.multiply(billAmount);

        // Total Amount (calculated)
        totalAmount = billAmount.add(tipAmount);

        // Number of People (from prefs or default)
        String defaultNumberOfPeople = prefs.getString(App.KEY_PREF_NUM_PEOPLE, null);
        if (defaultNumberOfPeople != null) {
            numberOfPeople = new BigDecimal(defaultNumberOfPeople);
        } else {
            numberOfPeople = DEFAULT_NUM_PEOPLE;
        }

        // Each Person Pays (calculated)
        eachPersonPays = totalAmount.divide(numberOfPeople, ROUNDING_MODE);
    }

    private void initWatchers() {
        billAmountWatcher = new TextWatcher() {
            private String previous;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !s.toString().equals(previous)) {
                    onBillAmountChange(s.toString());
                }
            }
        };

        tipPercentWatcher = new TextWatcher() {
            private String previous;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !s.toString().equals(previous)) {
                    onTipPercentChange(s.toString());
                }
            }
        };

        tipAmountWatcher = new TextWatcher() {
            private String previous;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !s.toString().equals(previous)) {
                    onTipAmountChange(s.toString());
                }
            }
        };

        totalAmountWatcher = new TextWatcher() {
            private String previous;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !s.toString().equals(previous)) {
                    onTotalAmountChange(s.toString());
                }
            }
        };

        numberOfPeopleWatcher = new TextWatcher() {
            private String previous;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !s.toString().equals(previous)) {
                    onNumberOfPeopleChange(s.toString());
                }
            }
        };

        eachPersonPaysWatcher = new TextWatcher() {
            private String previous;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previous = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !s.toString().equals(previous)) {
                    onEachPersonPaysChange(s.toString());
                }
            }
        };
    }

    private void startObservations() {
        billAmountField.addTextChangedListener(billAmountWatcher);
        tipPercentField.addTextChangedListener(tipPercentWatcher);
        tipAmountField.addTextChangedListener(tipAmountWatcher);
        totalAmountField.addTextChangedListener(totalAmountWatcher);
        numberOfPeopleField.addTextChangedListener(numberOfPeopleWatcher);
        eachPersonPaysField.addTextChangedListener(eachPersonPaysWatcher);
    }

    private void stopObservations() {
        billAmountField.removeTextChangedListener(billAmountWatcher);
        tipPercentField.removeTextChangedListener(tipPercentWatcher);
        tipAmountField.removeTextChangedListener(tipAmountWatcher);
        totalAmountField.removeTextChangedListener(totalAmountWatcher);
        numberOfPeopleField.removeTextChangedListener(numberOfPeopleWatcher);
        eachPersonPaysField.removeTextChangedListener(eachPersonPaysWatcher);
    }

    private void updateUi() {
        stopObservations();
        billAmountField.setText(FORMATTER.format(billAmount));
        tipPercentField.setText(INT_FORMATTER.format(tipPercent));
        tipAmountField.setText(FORMATTER.format(tipAmount));
        totalAmountField.setText(FORMATTER.format(totalAmount));
        numberOfPeopleField.setText(INT_FORMATTER.format(numberOfPeople));
        eachPersonPaysField.setText(FORMATTER.format(eachPersonPays));
        startObservations();
    }

    private void onBillAmountChange(String billAmount) {
        Timber.d("New billAmountField: %s", billAmount);
        this.billAmount = new BigDecimal(billAmount);
        // Tip Amount
        tipAmount = tipPercent.divide(ONE_HUNDRED, DECIMALS_PCT, ROUNDING_MODE);
        tipAmount = tipAmount.multiply(this.billAmount);
        // Total Amount
        totalAmount = this.billAmount.add(tipAmount);
        // Each Person Pays
        eachPersonPays = totalAmount.divide(numberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onTipPercentChange(String tipPercent) {
        Timber.d("New tipPercentField: %s", tipPercent);
        this.tipPercent = new BigDecimal(tipPercent);
        // Tip Amount
        tipAmount = this.tipPercent.divide(ONE_HUNDRED, DECIMALS_PCT, ROUNDING_MODE);
        tipAmount = tipAmount.multiply(billAmount);
        // Total Amount
        totalAmount = billAmount.add(tipAmount);
        // Each Person Pays
        eachPersonPays = totalAmount.divide(numberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onTipAmountChange(String tipAmount) {
        Timber.d("New tipAmountField: %s", tipAmount);
        this.tipAmount = new BigDecimal(tipAmount);
        // Tip Percent
        if (billAmount.compareTo(BigDecimal.ZERO) != 0) {
            tipPercent = this.tipAmount.divide(billAmount, ROUNDING_MODE);
            tipPercent = tipPercent.multiply(ONE_HUNDRED);
        }
        // Total Amount
        totalAmount = billAmount.add(this.tipAmount);
        // Each Person Pays
        eachPersonPays = totalAmount.divide(numberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onTotalAmountChange(String totalAmount) {
        Timber.d("New totalAmountField: %s", totalAmount);
        this.totalAmount = new BigDecimal(totalAmount);
        // Tip Amount
        tipAmount = this.totalAmount.subtract(billAmount);
        // Tip Percent
        if (billAmount.compareTo(BigDecimal.ZERO) != 0) {
            tipPercent = tipAmount.divide(billAmount, ROUNDING_MODE);
            tipPercent = tipPercent.multiply(ONE_HUNDRED);
        }
        // Each Person Pays
        eachPersonPays = this.totalAmount.divide(numberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onNumberOfPeopleChange(String numberOfPeople) {
        Timber.d("New numberOfPeopleField: %s", numberOfPeople);
        this.numberOfPeople = new BigDecimal(numberOfPeople);
        // Each Person Pays
        eachPersonPays = totalAmount.divide(this.numberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onEachPersonPaysChange(String eachPersonPays) {
        Timber.d("New eachPersonPaysField: %s", eachPersonPays);
        this.eachPersonPays = new BigDecimal(eachPersonPays);
        // Total Amount
        totalAmount = this.eachPersonPays.multiply(numberOfPeople);
        if (totalAmount.compareTo(billAmount) < 0) {
            // Hack to avoid rounding below 0
            totalAmount = billAmount;
        }
        // Tip Amount
        tipAmount = totalAmount.subtract(billAmount);
        // Tip Percent
        if (billAmount.compareTo(BigDecimal.ZERO) != 0) {
            tipPercent = tipAmount.divide(billAmount, ROUNDING_MODE);
            tipPercent = tipPercent.multiply(ONE_HUNDRED);
        }

        // Populate the UI with new values
        updateUi();
    }
}
