package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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
import com.rilixtech.CountryCodePicker;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.service.TimerService;
import com.yso.charp.utils.AnimationUtils;
import com.yso.charp.utils.handlers.UiHandlerWrapper;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneAuthFragment extends Fragment implements View.OnClickListener, TimerService.TimerServiceListener
{

    private static final String TAG = PhoneAuthFragment.class.getSimpleName();

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private final int SPLASH_DISPLAY_LENGTH = 1000;

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private boolean mVerificationInProgress = false;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    //    private Timer mTimer;
    //    private TimerTask mTimerTask;

    private ViewGroup mPhoneNumberViews;

    private TextView mTitleText;
    private TextView mStatusText;
    private TextView mDetailText;
    private TextView mTimerText;

    private EditText mPhoneNumberField;
    private CountryCodePicker mCountryCodePicker;
    private EditText mVerificationField;
    private EditText mNameField;

    private Button mStartButton;

    private RelativeLayout mStartWrapper;
    private RelativeLayout mVerifyWrapper;
    private UiHandlerWrapper mHandlerWrapper = new UiHandlerWrapper();

    public PhoneAuthFragment()
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

        mPhoneNumberField = view.findViewById(R.id.get_sms_field_phone_number);
        mCountryCodePicker = view.findViewById(R.id.ccp);
        mVerificationField = view.findViewById(R.id.field_verification_code);
        mNameField = view.findViewById(R.id.field_user_name);

        mStartWrapper = view.findViewById(R.id.start_wrapper);
        mVerifyWrapper = view.findViewById(R.id.verify_wrapper);

        mStartButton = view.findViewById(R.id.get_sms_button_start_verification);
        mStartButton.setOnClickListener(this);
        mTimerText.setOnClickListener(this);
        mTimerText.setClickable(false);
        Button verifyButton = view.findViewById(R.id.button_verify_phone);
        Button sendNameButton = view.findViewById(R.id.button_send_name);
        verifyButton.setOnClickListener(this);
        sendNameButton.setOnClickListener(this);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential)
            {
                AnimationUtils.slideDown(mStartWrapper);
                mVerifyWrapper.setVisibility(View.VISIBLE);
                AnimationUtils.slideUp(mVerifyWrapper);
                mVerificationInProgress = false;

                updateUI(STATE_VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                mVerificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    mPhoneNumberField.setError("מספר לא תקין");
                    //                    mTimer.cancel();
                    //                    mTimerTask.cancel();
                    mStartButton.setEnabled(true);
                    mTimerText.setText("הכנס מס' תקין ולחץ שוב start");
                }
                else if (e instanceof FirebaseTooManyRequestsException)
                {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "חרגת ממכסת SMS", Snackbar.LENGTH_SHORT).show();
                    //                    mTimer.cancel();
                    //                    mTimerTask.cancel();
                    mTimerText.setText("נסה בעוד כמה דקות start");
                }

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token)
            {
                Intent intent = new Intent(getActivity(), TimerService.class);
                getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

                if (mVerifyWrapper.getVisibility() == View.GONE)
                {
                    AnimationUtils.slideDown(mStartWrapper);
                    mVerifyWrapper.setVisibility(View.VISIBLE);
                    AnimationUtils.slideUp(mVerifyWrapper);
                }

                PersistenceManager.getInstance().setVerificationId(verificationId);
                mResendToken = token;

                //                startTimer();

                updateUI(STATE_CODE_SENT);
            }
        };
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = FireBaseManager.getFirebaseAuth().getCurrentUser();
        if (PersistenceManager.getInstance().getVerificationId() == null)
        {
            updateUI(currentUser);
        }
        else
        {
            if (mVerifyWrapper.getVisibility() == View.GONE)
            {
                AnimationUtils.slideDown(mStartWrapper);
                mVerifyWrapper.setVisibility(View.VISIBLE);
                AnimationUtils.slideUp(mVerifyWrapper);
            }

            updateUI(STATE_CODE_SENT);
        }

        //        if (mVerificationInProgress && validatePhoneNumber())
        //        {
        //            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
        //        }
    }

    /*@Override
    public void onStop()
    {
        super.onStop();
        if (bound) {
            myService.setCallbacks(null); // unregister
            getActivity().unbindService(serviceConnection);
            bound = false;
        }
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
        {
            mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        FireBaseManager.getFirebaseAuth().signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    //                    mTimer.cancel();
                    //                    mTimerTask.cancel();

                    FirebaseUser user = task.getResult().getUser();
                    updateUI(STATE_SIGNIN_SUCCESS, user);
                }
                else
                {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        mVerificationField.setError("Invalid code.");
                    }
                    updateUI(STATE_SIGNIN_FAILED);
                }
            }
        });
    }

    private boolean validatePhoneNumber()
    {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber))
        {
            mPhoneNumberField.setError("מס' לא תקין");
            return false;
        }

        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, getActivity(), mCallbacks);
        mVerificationInProgress = true;
    }

    /*private void startTimer()
    {
        final int i = 0;
        final int[] timeCounter = {59};
        mTimer = new Timer();
        mTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            if (timeCounter[0] == i)
                            {
                                PersistenceManager.getInstance().setVerificationId(null);
                                mTimerText.setText("לחץ כאן לשליחה מחדש");
                                mTimerText.setClickable(true);
                                mTimer.cancel();
                                cancel();
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
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 1000);
    }*/

    private void verifyPhoneNumberWithCode(String verificationId, String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, getActivity(), mCallbacks, token);
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
                enableViews(mStartButton, mPhoneNumberField);
                mDetailText.setText(null);
                break;

            case STATE_CODE_SENT:
                enableViews(mPhoneNumberField);
                disableViews(mStartButton);
                mDetailText.setText(R.string.status_code_sent);
                break;

            case STATE_VERIFY_FAILED:
                enableViews(mStartButton, mPhoneNumberField);
                mDetailText.setText(R.string.status_verification_failed);
                break;

            case STATE_VERIFY_SUCCESS:
                disableViews(mStartButton, mPhoneNumberField);
                mDetailText.setText(R.string.status_verification_succeeded);

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
                mDetailText.setText(R.string.status_sign_in_failed);
                break;

            case STATE_SIGNIN_SUCCESS:
                break;
        }

        if (user == null)
        {
            mPhoneNumberViews.setVisibility(View.VISIBLE);

            mTitleText.setText("");
            mStatusText.setText(R.string.signed_out);
        }
        else
        {
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

    @SuppressLint ("StringFormatInvalid")
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
                String num = mPhoneNumberField.getText().toString();
                if ((num.startsWith("+")))
                {
                    num = mPhoneNumberField.getText().toString().substring(1);
                }
                if (num.startsWith("0"))
                {
                    num = num.substring(1);
                }
                if (num.substring(0, 3).equals(mCountryCodePicker.getSelectedCountryCode()))
                {
                    num = num.substring(3);
                }
                showAlertDialog(R.string.confirm_number_title, String.format(getString(R.string.confirm_number_message), "+" + mCountryCodePicker.getSelectedCountryCode() + num), "+" + mCountryCodePicker.getSelectedCountryCode() + num);
                break;

            case R.id.get_sms_timer:
                resendVerificationCode(mPhoneNumberField.getText().toString(), mResendToken);
                break;

            case R.id.button_verify_phone:
                mTimerText.setText("");

                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code))
                {
                    mVerificationField.setError("אינו יכול להיות ריק..");
                    return;
                }

                verifyPhoneNumberWithCode(PersistenceManager.getInstance().getVerificationId(), code);
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

    private void showAlertDialog(final int title, final String message, final String num)
    {
        mHandlerWrapper.post(new Runnable()
        {
            @Override
            public void run()
            {
                new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme).setCancelable(false).setTitle(title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mStartButton.setEnabled(false);
                        startPhoneNumberVerification(num);
                    }
                }).setNegativeButton("ערוך", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                }).show();
            }
        });
    }

    //    private boolean bound = false;

    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            TimerService timerService = binder.getService();
            //            bound = true;
            timerService.setCallbacks(PhoneAuthFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            //            bound = false;
        }
    };

    @Override
    public void onTick(final String time)
    {
        if (time == null)
        {
            getActivity().unbindService(mServiceConnection);
            PersistenceManager.getInstance().setVerificationId(null);
            mTimerText.setText("לחץ כאן לשליחה מחדש");
            mTimerText.setClickable(true);
        }
        else
        {
            mTimerText.setText(time);
        }
    }
}
