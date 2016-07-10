package com.wherewolf;

import android.content.Context;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;

import org.json.JSONObject;

/**
 * Created by Greg on 3/31/2015.
 */
public class FacebookIntegration
{
    public static boolean IsValidated = false;
    public static boolean detailsPopulated = false;
    public static User user;

    public static boolean IsLoggedIn()
    {
        if(AccessToken.getCurrentAccessToken() != null)
        {
            if(IsValidated)
            {
                return true;
            }
        }
        return false;
    }

    public static String GetCurrentAccessToken()
    {
        return AccessToken.getCurrentAccessToken().getToken();
    }

    public static void ValidateAccessToken()
    {
        GraphRequestAsyncTask request =  GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse)
            {
                if(graphResponse.getError() == null)
                {
                    IsValidated = true;

                    user = new User();

                    user.Gender = jsonObject.optString("gender");
                    user.Email = jsonObject.optString("email");
                    user.Birthday = jsonObject.optString("birthday");

                    detailsPopulated = true;
                }
                else
                {
                    IsValidated = false;
                }
                MainActivity.UpdateUI(null);

            }
        }).executeAsync();
    }
}






