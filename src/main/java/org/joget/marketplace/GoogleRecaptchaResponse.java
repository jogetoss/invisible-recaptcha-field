package org.joget.marketplace;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Class to hold mapped JSON data of Google reCAPTCHA response.<br>
 * Reference: <a href="https://developers.google.com/recaptcha/docs/verify">
 * https://developers.google.com/recaptcha/docs/verify</a>
 */
public class GoogleRecaptchaResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("challenge_ts")
    private String challengeTs;

    @SerializedName("hostname")
    private String hostname;

    @SerializedName("error-codes")
    private List<String> errorCodes;

    public boolean isSuccess() {
        return success;
    }

    public String getChallengeTime() {
        return challengeTs;
    }

    public String getHostname() {
        return hostname;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    @Override
    public String toString() {
        return "GoogleRecaptchaResponse{"
                + "success=" + success
                + ", challengeTs='" + challengeTs + '\''
                + ", hostname='" + hostname + '\''
                + ", errorCodes=" + errorCodes
                + '}';
    }
}
