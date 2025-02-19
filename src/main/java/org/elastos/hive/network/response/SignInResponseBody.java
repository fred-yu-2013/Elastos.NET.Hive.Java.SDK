package org.elastos.hive.network.response;

import org.elastos.did.jwt.Claims;
import org.elastos.hive.exception.HiveSdkException;
import org.elastos.hive.utils.JwtUtil;

import com.google.gson.annotations.SerializedName;

public class SignInResponseBody extends HiveResponseBody {
    @SerializedName("challenge")
    private String challenge;

    public String getChallenge() {
        return challenge;
    }

    public Claims checkValid(String validAudience) {
        Claims claims = JwtUtil.getBody(challenge);

        if (claims.getExpiration().getTime() <= System.currentTimeMillis() )
            throw new HiveSdkException("Bad jwt expiration date");

        if (!claims.getAudience().equals(validAudience))
            throw new HiveSdkException("Bad jwt audience value");

        return claims;
    }
}
