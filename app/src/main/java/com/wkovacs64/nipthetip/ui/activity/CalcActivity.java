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

import butterknife.Bind;
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
    private TextWatcher mBillAmountWatcher;
    private TextWatcher mTipPercentWatcher;
    private TextWatcher mTipAmountWatcher;
    private TextWatcher mTotalAmountWatcher;
    private TextWatcher mNumberOfPeopleWatcher;
    private TextWatcher mEachPersonPaysWatcher;

    @State
    BigDecimal mBillAmount;
    @State
    BigDecimal mTipPercent;
    @State
    BigDecimal mTipAmount;
    @State
    BigDecimal mTotalAmount;
    @State
    BigDecimal mNumberOfPeople;
    @State
    BigDecimal mEachPersonPays;

    @Bind(R.id.view_root)
    LinearLayout mRootView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.edit_bill_amount)
    EditText mBillAmountField;
    @Bind(R.id.edit_tip_percent)
    EditText mTipPercentField;
    @Bind(R.id.edit_tip_amount)
    EditText mTipAmountField;
    @Bind(R.id.edit_total_amount)
    EditText mTotalAmountField;
    @Bind(R.id.edit_number_of_people)
    EditText mNumberOfPeopleField;
    @Bind(R.id.edit_each_person_pays)
    EditText mEachPersonPaysField;

    @Inject
    SharedPreferences sPrefs;

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
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
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
        Timber.d("Bill Amount user input: " + input);
        mBillAmount = new BigDecimal(input);
        mBillAmountField.setText(FORMATTER.format(mBillAmount));
    }

    @OnClick(R.id.edit_tip_percent)
    void onEditTipPercentClicked() {
        InputDialog.show(this, FIELD_TIP_PERCENT);
    }

    @Override
    public void onTipPercentInput(String input) {
        Timber.d("Tip Percent user input: " + input);
        mTipPercent = new BigDecimal(input);
        mTipPercentField.setText(INT_FORMATTER.format(mTipPercent));
    }

    @OnClick(R.id.edit_tip_amount)
    void onEditTipAmountClicked() {
        InputDialog.show(this, FIELD_TIP_AMOUNT);
    }

    @Override
    public void onTipAmountInput(String input) {
        Timber.d("Tip Amount user input: " + input);
        mTipAmount = new BigDecimal(input);
        mTipAmountField.setText(FORMATTER.format(mTipAmount));
    }

    @OnClick(R.id.edit_total_amount)
    void onEditTotalAmountClicked() {
        InputDialog.show(this, FIELD_TOTAL_AMOUNT);
    }

    @Override
    public void onTotalAmountInput(String input) {
        Timber.d("Total Amount user input: " + input);
        BigDecimal proposedValue = new BigDecimal(input);

        if (proposedValue.compareTo(mBillAmount) >= 0) {
            mTotalAmount = proposedValue;
            mTotalAmountField.setText(FORMATTER.format(mTotalAmount));
        } else {
            Snackbar.make(mRootView, R.string.snack_invalid, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.edit_number_of_people)
    void onEditNumberOfPeopleClicked() {
        InputDialog.show(this, FIELD_NUMBER_OF_PEOPLE);
    }

    @Override
    public void onNumberOfPeopleInput(String input) {
        Timber.d("Number Of People user input: " + input);
        BigDecimal proposedValue = new BigDecimal(input);

        if (proposedValue.compareTo(BigDecimal.ZERO) > 0) {
            mNumberOfPeople = proposedValue;
            mNumberOfPeopleField.setText(INT_FORMATTER.format(mNumberOfPeople));
        } else {
            Snackbar.make(mRootView, R.string.snack_no_people, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.edit_each_person_pays)
    void onEditEachPersonPaysClicked() {
        InputDialog.show(this, FIELD_EACH_PERSON_PAYS);
    }

    @Override
    public void onEachPersonPaysInput(String input) {
        Timber.d("Each Person Pays user input: " + input);
        BigDecimal proposedValue = new BigDecimal(input);
        BigDecimal lowerLimit = mBillAmount.divide(mNumberOfPeople, ROUNDING_MODE);

        if (proposedValue.compareTo(lowerLimit) >= 0) {
            mEachPersonPays = proposedValue;
            mEachPersonPaysField.setText(FORMATTER.format(mEachPersonPays));
        } else {
            Snackbar.make(mRootView, R.string.snack_invalid, Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.image_bill_amount_down)
    void onBillAmountDown() {
        if (mBillAmount.compareTo(BigDecimal.ONE) > 0) {
            if (mBillAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                mBillAmount = mBillAmount.subtract(BigDecimal.ONE);
            } else {
                mBillAmount = mBillAmount.setScale(0, BigDecimal.ROUND_DOWN);
            }
            mBillAmountField.setText(FORMATTER.format(mBillAmount));
        }
    }

    @OnClick(R.id.image_bill_amount_up)
    void onBillAmountUp() {
        if (mBillAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            mBillAmount = mBillAmount.add(BigDecimal.ONE);
        } else {
            mBillAmount = mBillAmount.setScale(0, BigDecimal.ROUND_UP);
        }
        mBillAmountField.setText(FORMATTER.format(mBillAmount));
    }

    @OnClick(R.id.image_tip_percent_down)
    void onTipPercentDown() {
        if (mTipPercent.compareTo(BigDecimal.ZERO) > 0) {
            if (mTipPercent.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                mTipPercent = mTipPercent.subtract(BigDecimal.ONE);
            } else {
                mTipPercent = mTipPercent.setScale(0, BigDecimal.ROUND_DOWN);
            }
            mTipPercentField.setText(INT_FORMATTER.format(mTipPercent));
        }
    }

    @OnClick(R.id.image_tip_percent_up)
    void onTipPercentUp() {
        if (mTipPercent.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            mTipPercent = mTipPercent.add(BigDecimal.ONE);
        } else {
            mTipPercent = mTipPercent.setScale(0, BigDecimal.ROUND_UP);
        }
        mTipPercentField.setText(INT_FORMATTER.format(mTipPercent));
    }

    @OnClick(R.id.image_tip_amount_down)
    void onTipAmountDown() {
        if (mTipAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (mTipAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                mTipAmount = mTipAmount.subtract(BigDecimal.ONE);
            } else {
                mTipAmount = mTipAmount.setScale(0, BigDecimal.ROUND_DOWN);
            }
            mTipAmountField.setText(FORMATTER.format(mTipAmount));
        }
    }

    @OnClick(R.id.image_tip_amount_up)
    void onTipAmountUp() {
        if (mTipAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            mTipAmount = mTipAmount.add(BigDecimal.ONE);
        } else {
            mTipAmount = mTipAmount.setScale(0, BigDecimal.ROUND_UP);
        }
        mTipAmountField.setText(FORMATTER.format(mTipAmount));
    }

    @OnClick(R.id.image_total_amount_down)
    void onTotalAmountDown() {
        if (mTotalAmount.compareTo(mBillAmount) > 0) {
            if (mTotalAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                mTotalAmount = mTotalAmount.subtract(BigDecimal.ONE);
            } else {
                mTotalAmount = mTotalAmount.setScale(0, BigDecimal.ROUND_DOWN);
            }
            mTotalAmountField.setText(FORMATTER.format(mTotalAmount));
        }
    }

    @OnClick(R.id.image_total_amount_up)
    void onTotalAmountUp() {
        if (mTotalAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            mTotalAmount = mTotalAmount.add(BigDecimal.ONE);
        } else {
            mTotalAmount = mTotalAmount.setScale(0, BigDecimal.ROUND_UP);
        }
        mTotalAmountField.setText(FORMATTER.format(mTotalAmount));
    }

    @OnClick(R.id.image_number_of_people_down)
    void onNumberOfPeopleDown() {
        if (mNumberOfPeople.compareTo(BigDecimal.ONE) > 0) {
            if (mNumberOfPeople.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                mNumberOfPeople = mNumberOfPeople.subtract(BigDecimal.ONE);
            } else {
                mNumberOfPeople = mNumberOfPeople.setScale(0, BigDecimal.ROUND_DOWN);
            }
            mNumberOfPeopleField.setText(INT_FORMATTER.format(mNumberOfPeople));
        }
    }

    @OnClick(R.id.image_number_of_people_up)
    void onNumberOfPeopleUp() {
        if (mNumberOfPeople.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            mNumberOfPeople = mNumberOfPeople.add(BigDecimal.ONE);
        } else {
            mNumberOfPeople = mNumberOfPeople.setScale(0, BigDecimal.ROUND_UP);
        }
        mNumberOfPeopleField.setText(INT_FORMATTER.format(mNumberOfPeople));
    }

    @OnClick(R.id.image_each_person_pays_down)
    void onEachPersonPaysDown() {
        BigDecimal lowerLimit = mBillAmount.divide(mNumberOfPeople, ROUNDING_MODE);
        if (mEachPersonPays.compareTo(lowerLimit) > 0) {
            if (mEachPersonPays.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                mEachPersonPays = mEachPersonPays.subtract(BigDecimal.ONE);
            } else {
                mEachPersonPays = mEachPersonPays.setScale(0, BigDecimal.ROUND_DOWN);
            }

            if (mEachPersonPays.compareTo(lowerLimit) < 0) {
                mEachPersonPays = lowerLimit;
            }

            mEachPersonPaysField.setText(FORMATTER.format(mEachPersonPays));
        }
    }

    @OnClick(R.id.image_each_person_pays_up)
    void onEachPersonPaysUp() {
        if (mEachPersonPays.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            mEachPersonPays = mEachPersonPays.add(BigDecimal.ONE);
        } else {
            mEachPersonPays = mEachPersonPays.setScale(0, BigDecimal.ROUND_UP);
        }
        mEachPersonPaysField.setText(FORMATTER.format(mEachPersonPays));
    }

    private void initValues() {
        // Bill Amount (from layout default)
        mBillAmount = new BigDecimal(mBillAmountField.getText().toString());

        // Tip Percent (from prefs or default)
        String defaultTipPercent = sPrefs.getString(App.KEY_PREF_TIP_PERCENT, null);
        if (defaultTipPercent != null) {
            mTipPercent = new BigDecimal(defaultTipPercent);
        } else {
            mTipPercent = DEFAULT_TIP_PERCENT;
        }

        // Tip Amount (calculated)
        mTipAmount = mTipPercent.divide(ONE_HUNDRED, ROUNDING_MODE);
        mTipAmount = mTipAmount.multiply(mBillAmount);

        // Total Amount (calculated)
        mTotalAmount = mBillAmount.add(mTipAmount);

        // Number of People (from prefs or default)
        String defaultNumberOfPeople = sPrefs.getString(App.KEY_PREF_NUM_PEOPLE, null);
        if (defaultNumberOfPeople != null) {
            mNumberOfPeople = new BigDecimal(defaultNumberOfPeople);
        } else {
            mNumberOfPeople = DEFAULT_NUM_PEOPLE;
        }

        // Each Person Pays (calculated)
        mEachPersonPays = mTotalAmount.divide(mNumberOfPeople, ROUNDING_MODE);
    }

    private void initWatchers() {
        mBillAmountWatcher = new TextWatcher() {
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

        mTipPercentWatcher = new TextWatcher() {
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

        mTipAmountWatcher = new TextWatcher() {
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

        mTotalAmountWatcher = new TextWatcher() {
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

        mNumberOfPeopleWatcher = new TextWatcher() {
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

        mEachPersonPaysWatcher = new TextWatcher() {
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
        mBillAmountField.addTextChangedListener(mBillAmountWatcher);
        mTipPercentField.addTextChangedListener(mTipPercentWatcher);
        mTipAmountField.addTextChangedListener(mTipAmountWatcher);
        mTotalAmountField.addTextChangedListener(mTotalAmountWatcher);
        mNumberOfPeopleField.addTextChangedListener(mNumberOfPeopleWatcher);
        mEachPersonPaysField.addTextChangedListener(mEachPersonPaysWatcher);
    }

    private void stopObservations() {
        mBillAmountField.removeTextChangedListener(mBillAmountWatcher);
        mTipPercentField.removeTextChangedListener(mTipPercentWatcher);
        mTipAmountField.removeTextChangedListener(mTipAmountWatcher);
        mTotalAmountField.removeTextChangedListener(mTotalAmountWatcher);
        mNumberOfPeopleField.removeTextChangedListener(mNumberOfPeopleWatcher);
        mEachPersonPaysField.removeTextChangedListener(mEachPersonPaysWatcher);
    }

    private void updateUi() {
        stopObservations();
        mBillAmountField.setText(FORMATTER.format(mBillAmount));
        mTipPercentField.setText(INT_FORMATTER.format(mTipPercent));
        mTipAmountField.setText(FORMATTER.format(mTipAmount));
        mTotalAmountField.setText(FORMATTER.format(mTotalAmount));
        mNumberOfPeopleField.setText(INT_FORMATTER.format(mNumberOfPeople));
        mEachPersonPaysField.setText(FORMATTER.format(mEachPersonPays));
        startObservations();
    }

    private void onBillAmountChange(String billAmount) {
        Timber.d("New mBillAmountField: " + billAmount);
        mBillAmount = new BigDecimal(billAmount);
        // Tip Amount
        mTipAmount = mTipPercent.divide(ONE_HUNDRED, DECIMALS_PCT, ROUNDING_MODE);
        mTipAmount = mTipAmount.multiply(mBillAmount);
        // Total Amount
        mTotalAmount = mBillAmount.add(mTipAmount);
        // Each Person Pays
        mEachPersonPays = mTotalAmount.divide(mNumberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onTipPercentChange(String tipPercent) {
        Timber.d("New mTipPercentField: " + tipPercent);
        mTipPercent = new BigDecimal(tipPercent);
        // Tip Amount
        mTipAmount = mTipPercent.divide(ONE_HUNDRED, DECIMALS_PCT, ROUNDING_MODE);
        mTipAmount = mTipAmount.multiply(mBillAmount);
        // Total Amount
        mTotalAmount = mBillAmount.add(mTipAmount);
        // Each Person Pays
        mEachPersonPays = mTotalAmount.divide(mNumberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onTipAmountChange(String tipAmount) {
        Timber.d("New mTipAmountField: " + tipAmount);
        mTipAmount = new BigDecimal(tipAmount);
        // Tip Percent
        if (mBillAmount.compareTo(BigDecimal.ZERO) != 0) {
            mTipPercent = mTipAmount.divide(mBillAmount, ROUNDING_MODE);
            mTipPercent = mTipPercent.multiply(ONE_HUNDRED);
        }
        // Total Amount
        mTotalAmount = mBillAmount.add(mTipAmount);
        // Each Person Pays
        mEachPersonPays = mTotalAmount.divide(mNumberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onTotalAmountChange(String totalAmount) {
        Timber.d("New mTotalAmountField: " + totalAmount);
        mTotalAmount = new BigDecimal(totalAmount);
        // Tip Amount
        mTipAmount = mTotalAmount.subtract(mBillAmount);
        // Tip Percent
        if (mBillAmount.compareTo(BigDecimal.ZERO) != 0) {
            mTipPercent = mTipAmount.divide(mBillAmount, ROUNDING_MODE);
            mTipPercent = mTipPercent.multiply(ONE_HUNDRED);
        }
        // Each Person Pays
        mEachPersonPays = mTotalAmount.divide(mNumberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onNumberOfPeopleChange(String numberOfPeople) {
        Timber.d("New mNumberOfPeopleField: " + numberOfPeople);
        mNumberOfPeople = new BigDecimal(numberOfPeople);
        // Each Person Pays
        mEachPersonPays = mTotalAmount.divide(mNumberOfPeople, ROUNDING_MODE);

        // Populate the UI with new values
        updateUi();
    }

    private void onEachPersonPaysChange(String eachPersonPays) {
        Timber.d("New mEachPersonPaysField: " + eachPersonPays);
        mEachPersonPays = new BigDecimal(eachPersonPays);
        // Total Amount
        mTotalAmount = mEachPersonPays.multiply(mNumberOfPeople);
        if (mTotalAmount.compareTo(mBillAmount) < 0) {
            // Hack to avoid rounding below 0
            mTotalAmount = mBillAmount;
        }
        // Tip Amount
        mTipAmount = mTotalAmount.subtract(mBillAmount);
        // Tip Percent
        if (mBillAmount.compareTo(BigDecimal.ZERO) != 0) {
            mTipPercent = mTipAmount.divide(mBillAmount, ROUNDING_MODE);
            mTipPercent = mTipPercent.multiply(ONE_HUNDRED);
        }

        // Populate the UI with new values
        updateUi();
    }
}
