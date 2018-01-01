package com.yso.charp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.utils.AnimationUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetSMSFragment extends Fragment implements View.OnClickListener
{

    private static final String TAG = GetSMSFragment.class.getSimpleName();

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private final int SPLASH_DISPLAY_LENGTH = 1000;

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private Timer mTimer;

    private ViewGroup mPhoneNumberViews;

    private TextView mTitleText;
    private TextView mStatusText;
    private TextView mDetailText;
    private TextView mTimerText;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;
    private EditText mNameField;

    private Button mStartButton;
    private Button mVerifyButton;
    private Button mSendNameButton;

    private RelativeLayout mStartWrapper;
    private RelativeLayout mVerifyWrapper;

    public GetSMSFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_get_sms, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mPhoneNumberViews = view.findViewById(R.id.get_sms_phone_auth_fields);

        mTitleText = view.findViewById(R.id.get_sms_title_text);
        mStatusText = view.findViewById(R.id.get_sms_status);
        mDetailText = view.findViewById(R.id.get_sms_detail);
        mTimerText = view.findViewById(R.id.get_sms_timer);
        mTimerText.setClickable(false);

        mPhoneNumberField = view.findViewById(R.id.get_sms_field_phone_number);
        mVerificationField = (EditText) view.findViewById(R.id.field_verification_code);
        mNameField = (EditText) view.findViewById(R.id.field_user_name);

        mStartButton = view.findViewById(R.id.get_sms_button_start_verification);
        mVerifyButton = view.findViewById(R.id.button_verify_phone);
        mSendNameButton = view.findViewById(R.id.button_send_name);

        mStartWrapper = view.findViewById(R.id.start_wrapper);
        mVerifyWrapper = view.findViewById(R.id.verify_wrapper);

        // Assign click listeners
        mStartButton.setOnClickListener(this);
        mTimerText.setOnClickListener(this);
        mVerifyButton.setOnClickListener(this);
        mSendNameButton.setOnClickListener(this);

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential)
            {
                AnimationUtils.slideDown(mStartWrapper);
                mVerifyWrapper.setVisibility(View.VISIBLE);
                AnimationUtils.slideUp(mVerifyWrapper);
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user_list_item action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    // Invalid request
                    // [START_EXCLUDE]
                    mPhoneNumberField.setError("Invalid phone number.");
                    mTimer.cancel();
                    mStartButton.setEnabled(true);
                    mTimerText.setText("הכנס מס' תקין ולחץ שוב start");
                    // [END_EXCLUDE]
                }
                else if (e instanceof FirebaseTooManyRequestsException)
                {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Quota exceeded.", Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message_list_item and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token)
            {
                if(mVerifyWrapper.getVisibility() == View.GONE)
                {
                    AnimationUtils.slideDown(mStartWrapper);
                    mVerifyWrapper.setVisibility(View.VISIBLE);
                    AnimationUtils.slideUp(mVerifyWrapper);
                }
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user_list_item to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user_list_item is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FireBaseManager.getFirebaseAuth().getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber())
        {
            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
        }
    }

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        FireBaseManager.getFirebaseAuth().signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    // Sign in success, update UI with the signed-in user_list_item's information
                    Log.d(TAG, "signInWithCredential:success");

                    FirebaseUser user = task.getResult().getUser();
                    // [START_EXCLUDE]
                    updateUI(STATE_SIGNIN_SUCCESS, user);
                    // [END_EXCLUDE]
                }
                else
                {
                    // Sign in failed, display a message_list_item and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.getException());

                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        mVerificationField.setError("Invalid code.");
                        // [END_EXCLUDE]
                    }

                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGNIN_FAILED);
                    // [END_EXCLUDE]
                }
            }
        });
    }
    // [END sign_in_with_phone]

    private boolean validatePhoneNumber()
    {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber))
        {
            mPhoneNumberField.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber)
    {
        startTimer();
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void startTimer()
    {
        final int i = 0;
        final int[] timeCounter = {60};
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                getActivity().runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (timeCounter[0] == i)
                        {
                            mTimerText.setText("לחץ כאן לשליחה מחדש");
                            mTimerText.setClickable(true);
                            mTimer.cancel();
                            return;
                        }

                        String time = String.valueOf(timeCounter[0]);
                        if (time.length() == 1)
                        {
                            time = "0" + time;
                        }
                        mTimerText.setText("00:" + time);
                        timeCounter[0]--;
                    }
                });
            }
        }, 0, 1000);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code)
    {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token)
    {
        startTimer();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void updateUI(int uiState)
    {
        updateUI(uiState, FireBaseManager.getFirebaseUser(), null);
    }

    private void updateUI(FirebaseUser user)
    {
        if (user != null)
        {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        }
        else
        {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user)
    {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred)
    {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred)
    {
        switch (uiState)
        {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                enableViews(mStartButton, mPhoneNumberField);
                mDetailText.setText(null);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                enableViews(mPhoneNumberField);
                disableViews(mStartButton);
                mDetailText.setText(R.string.status_code_sent);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                enableViews(mStartButton, mPhoneNumberField);
                mDetailText.setText(R.string.status_verification_failed);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                disableViews(mStartButton, mPhoneNumberField);
                mDetailText.setText(R.string.status_verification_succeeded);

                // Set the verification text based on the credential
                if (cred != null)
                {
                    if (cred.getSmsCode() != null)
                    {
                        mVerificationField.setText(cred.getSmsCode());
                    }
                    else
                    {
                        mVerificationField.setText(R.string.instant_validation);
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                mDetailText.setText(R.string.status_sign_in_failed);
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                break;
        }

        if (user == null)
        {
            // Signed out
            mPhoneNumberViews.setVisibility(View.VISIBLE);

            mTitleText.setText("");
            mStatusText.setText(R.string.signed_out);
        }
        else
        {
            // Signed in
            mPhoneNumberViews.setVisibility(View.GONE);
            //            mSignedInViews.setVisibility(View.VISIBLE);

            enableViews(mPhoneNumberField);
            mPhoneNumberField.setText(null);

            mTitleText.setText(FireBaseManager.getFirebaseUserPhone());
            mStatusText.setText(R.string.signed_in);
            if (user.getDisplayName() == null || user.getDisplayName().equals(""))
            {
                mDetailText.setVisibility(View.GONE);
            }
            else
            {
                mDetailText.setText(getString(R.string.firebase_status_fmt, user.getDisplayName()));
                FireBaseManager.setUser();

                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        }
    }

    private void enableViews(View... views)
    {
        for (View v : views)
        {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views)
    {
        for (View v : views)
        {
            v.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.get_sms_button_start_verification:

                if (!validatePhoneNumber())
                {
                    return;
                }
                mStartButton.setEnabled(false);
                startPhoneNumberVerification(mPhoneNumberField.getText().toString());
                break;

            case R.id.get_sms_timer:
                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);
                break;

            case R.id.button_verify_phone:
                mTimerText.setText("");
                mTimer.cancel();

                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code))
                {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);
                break;

            case R.id.button_send_name:
                if (!mNameField.getText().toString().equals(""))
                {
                    FireBaseManager.updateName(mNameField.getText().toString());
                    ((MainActivity) getActivity()).goToFirstFragment();
                }
                break;
        }
    }
}
