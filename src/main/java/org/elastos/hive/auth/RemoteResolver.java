package org.elastos.hive.auth;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.elastos.did.jwt.Claims;
import org.elastos.hive.AppContext;
import org.elastos.hive.AppContextProvider;
import org.elastos.hive.AuthToken;
import org.elastos.hive.connection.ConnectionManager;
import org.elastos.hive.exception.HiveException;
import org.elastos.hive.utils.JwtUtil;
import org.elastos.hive.utils.ResponseHelper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RemoteResolver implements TokenResolver {
	private AppContext context;
	private AppContextProvider contextProvider;
	private ConnectionManager connectionManager;

	public RemoteResolver(AppContext context, ConnectionManager connectionManager) {
		this.context = context;
		this.contextProvider = context.getAppContextProvider();
		this.connectionManager = connectionManager;
	}

	@NotNull
	@Override
	public AuthToken getToken() throws HiveException {
		return auth(signIn());
	}

	private String signIn() throws HiveException {
		Map<String, Object> map = new HashMap<>();
		map.put("document", new JSONObject(contextProvider.getAppInstanceDocument().toString()));
		String json = new JSONObject(map).toString();

		try {
			Response response = connectionManager.getAuthApi()
					.signIn(getJsonRequestBoy(json))
					.execute();
			checkResponse(response);
			JsonNode ret = ResponseHelper.getValue(response, JsonNode.class).get("challenge");
			if(null == ret) throw new HiveException("Sign-in request failed");

			String jwtToken = ret.textValue();
			if (jwtToken == null || !verifyToken(jwtToken)) throw new HiveException("Token from sign-in request not valid.");

			return contextProvider.getAuthorization(jwtToken).get();
		} catch (Exception e) {
			throw new HiveException(e.getMessage());
		}
	}

	private RequestBody getJsonRequestBoy(String json) {
		return RequestBody.create(MediaType.parse("Content-Type, application/json"), json);
	}

	public void checkResponse(Response response) throws HiveException, IOException {
		if (response == null) throw new HiveException("response is null");

		int code = response.code();
		if (code >= 300 || code < 200) {
			throw new HiveException(response.errorBody().string());
		}
	}

	private boolean verifyToken(String jwtToken) {
		try {
			Claims claims = JwtUtil.getBody(jwtToken);
			long exp = claims.getExpiration().getTime();
			String aud = claims.getAudience();

			String did = contextProvider.getAppInstanceDocument().getSubject().toString();
			if (null == did || !did.equals(aud)) return false;

			return exp > System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private AuthToken auth(String token) throws HiveException {
		Map<String, Object> map = new HashMap<>();
		map.put("jwt", token);
		String json = new JSONObject(map).toString();
		try {
			Response response = connectionManager.getAuthApi()
					.auth(getJsonRequestBoy(json))
					.execute();
			checkResponse(response);
			return handleAuthResponse(response);
		} catch (Exception e) {
			throw new HiveException(e.getMessage());
		}
	}

	private AuthToken handleAuthResponse(Response response) throws HiveException, IOException {
		JsonNode ret = ResponseHelper.getValue(response, JsonNode.class).get("access_token");
		if(null == ret) throw new HiveException("Auth request failed");

		String accessToken = ret.textValue();
		if (null == accessToken) throw new HiveException("No token returned from auth request.");

		long exp = JwtUtil.getBody(accessToken).getExpiration().getTime();
		long expiresTime = System.currentTimeMillis() / 1000 + exp / 1000;
		return new AuthToken(accessToken, expiresTime, "token");
	}

	@Override
	public void saveToken() {
		// Do nothing.
	}

	@Override
	public void setNextResolver(TokenResolver resolver) {
		// Do nothing;
	}

	private void challengeRequest() {
		// TODO;
	}

	private void challengeResponse() {
		// TODO;
	}
}
