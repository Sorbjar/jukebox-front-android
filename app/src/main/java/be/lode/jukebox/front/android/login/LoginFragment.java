package be.lode.jukebox.front.android.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import be.lode.jukebox.front.android.R;
import be.lode.jukebox.front.android.choosejukebox.ChooseJukeboxActivity;

public class LoginFragment extends Fragment {

    private CallbackManager callbackManager;
    private FacebookCallback<LoginResult> callback;
    private ProfileTracker pt;

    public LoginFragment() {
        super();
        callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accesToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                nextPage(profile);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        };
    }

    private void nextPage(Profile profile) {
        Intent intent = new Intent(getActivity(),ChooseJukeboxActivity.class);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        instantiateProfileTracker();

        Profile profile = Profile.getCurrentProfile();
        if(profile != null)
        {
            nextPage(profile);
        }
    }

    private void instantiateProfileTracker() {
        if(pt != null) {
            pt = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                    if (oldProfile == null && newProfile != null)
                        nextPage(newProfile);
                }
            };
            pt.startTracking();
        }
    }

    private void closeProfileTracker()
    {
        if(pt != null)
        {
            pt.stopTracking();
            pt = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        instantiateProfileTracker();
        if(profile != null)
        {
            nextPage(profile);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager,callback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeProfileTracker();
    }
}
