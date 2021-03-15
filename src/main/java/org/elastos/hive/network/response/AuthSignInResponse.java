package org.elastos.hive.network.response;

import java.util.Date;

import org.elastos.did.jwt.Claims;
import org.elastos.hive.exception.HiveException;
import org.elastos.hive.utils.JwtUtil;

import com.google.gson.annotations.SerializedName;

public class AuthSignInResponse extends ResponseBase {
    @SerializedName("challenge")
    private String challenge;

    public String getChallenge() {
        return challenge;
    }

    public void checkValid(String validAudience) throws HiveException {
        Claims claims = JwtUtil.getBody(challenge);

        if (claims.getExpiration().getTime() <= System.currentTimeMillis() )
            throw new HiveException("Bad jwt expiration date");

        if (!claims.getAudience().equals(validAudience))
            throw new HiveException("Bad jwt audidence value");
    }
}
