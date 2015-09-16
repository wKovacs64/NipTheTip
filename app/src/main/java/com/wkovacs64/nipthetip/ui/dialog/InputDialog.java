package com.wkovacs64.nipthetip.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wkovacs64.nipthetip.R;
import com.wkovacs64.nipthetip.ui.activity.CalcActivity;

import timber.log.Timber;

/**
 * A simple dialog fragment designed to prompt the user for input.
 */
public final class InputDialog extends DialogFragment {

    private static final String TAG = InputDialog.class.getSimpleName();

    private static final String KEY_FIELD = "field";

    private Callback mCallback;

    /**
     * A listener object equipped to process user input from an {@link InputDialog}.
     */
    public interface Callback {
        /**
         * Reacts to user input for the Bill Amount field.
         */
        void onBillAmountInput(String input);

        /**
         * Reacts to user input for the Tip Percent field.
         */
        void onTipPercentInput(String input);

        /**
         * Reacts to user input for the Tip Amount field.
         */
        void onTipAmountInput(String input);

        /**
         * Reacts to user input for the Total Amount field.
         */
        void onTotalAmountInput(String input);

        /**
         * Reacts to user input for the Number Of People field.
         */
        void onNumberOfPeopleInput(String input);

        /**
         * Reacts to user input for the Each Person Pays field.
         */
        void onEachPersonPaysInput(String input);
    }

    /**
     * Constructs a new InputDialog instance.
     *
     * @return a new InputDialog
     */
    public static InputDialog newInstance(int field) {
        InputDialog dialog = new InputDialog();
        Bundle args = new Bundle();
        args.putInt(KEY_FIELD, field);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // If arguments were present, get the data from them
        Bundle args = getArguments();
        final int field;
        if (args != null) {
            field = args.getInt(KEY_FIELD);
        } else {
            field = CalcActivity.FIELD_UNDEFINED;
        }

        // Build and return the MaterialDialog
        return new MaterialDialog.Builder(getActivity())
                .positiveText(R.string.action_ok)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .input(R.string.hint_input, R.string.prefill_input, false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                String input = charSequence.toString();
                                switch (field) {
                                    case CalcActivity.FIELD_BILL_AMOUNT:
                                        mCallback.onBillAmountInput(input);
                                        break;
                                    case CalcActivity.FIELD_TIP_PERCENT:
                                        mCallback.onTipPercentInput(input);
                                        break;
                                    case CalcActivity.FIELD_TIP_AMOUNT:
                                        mCallback.onTipAmountInput(input);
                                        break;
                                    case CalcActivity.FIELD_TOTAL_AMOUNT:
                                        mCallback.onTotalAmountInput(input);
                                        break;
                                    case CalcActivity.FIELD_NUMBER_OF_PEOPLE:
                                        mCallback.onNumberOfPeopleInput(input);
                                        break;
                                    case CalcActivity.FIELD_EACH_PERSON_PAYS:
                                        mCallback.onEachPersonPaysInput(input);
                                        break;
                                    default:
                                        throw new IllegalArgumentException(TAG + " instantiated"
                                                + " using unrecognized field index!");
                                }
                            }
                        })
                .build();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            Timber.e(e, activity.getClass().getSimpleName() + " must implement " + TAG
                    + ".Callback!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    // Workaround for a bug in the support library (issue 17423)
    // https://code.google.com/p/android/issues/detail?id=17423
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    /**
     * Creates and shows an input dialog if one is not already showing.
     *
     * @param activity the AppCompatActivity context for the dialog fragment (must implement {@link
     *                 InputDialog.Callback})
     * @param field    the input field index (defined in {@link CalcActivity}) for which the
     *                 InputDialog will be gathering input
     */
    public static void show(final AppCompatActivity activity, final int field) {
        final FragmentManager fm = activity.getSupportFragmentManager();
        InputDialog dialog = (InputDialog) fm.findFragmentByTag(TAG);

        /*
         * If the dialog isn't already present, create and show it. This will almost always be the
         * case. In the event the dialog was instantiated before, it will either already be showing
         * (in which case, we do nothing) or it will have been removed by the FragmentManager upon
         * being dismissed and needs instantiated again.
         */
        if (dialog == null) {
            dialog = InputDialog.newInstance(field);
            dialog.show(fm, TAG);
        }
    }
}
