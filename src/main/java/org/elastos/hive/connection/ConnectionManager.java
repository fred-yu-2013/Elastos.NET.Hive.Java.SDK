/*
 * Copyright (c) 2019 Elastos Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.elastos.hive.connection;

import okhttp3.OkHttpClient;
import org.elastos.hive.ServiceEndpoint;
import org.elastos.hive.network.*;
import org.elastos.hive.network.response.HiveResponseBody;
import org.elastos.hive.utils.LogUtil;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {
	private static final int DEFAULT_TIMEOUT = 30;

	private ServiceEndpoint serviceEndpoint;
	private RequestInterceptor authRequestInterceptor;
	private RequestInterceptor plainRequestInterceptor;
	private RequestInterceptor plainV2RequestInterceptor;

	private SubscriptionApi subscriptionApi;
	private PaymentApi paymentApi;
	private DatabaseApi databaseApi;

	private AuthApi authApi;
	private FilesApi filesApi;
	private ScriptingApi scriptingApi;
	private ScriptingV2Api scriptingV2Api;
	private BackupApi backupApi;
	private NodeManageApi nodeManageApi;

	public ConnectionManager(ServiceEndpoint serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
		this.plainRequestInterceptor = new RequestInterceptor(this);
		this.plainV2RequestInterceptor = new RequestV2Interceptor(this);
		this.authRequestInterceptor  = new RequestInterceptor(this, false);
	}

	public ServiceEndpoint getServiceEndpoint() {
		return this.serviceEndpoint;
	}

	public AuthApi getAuthApi() {
		if (authApi == null)
			authApi = createService(AuthApi.class, serviceEndpoint.getProviderAddress(), this.authRequestInterceptor);

		return authApi;
	}

	public NodeManageApi getNodeManagerApi() {
		if (nodeManageApi == null)
			nodeManageApi = createService(NodeManageApi.class, serviceEndpoint.getProviderAddress(), this.authRequestInterceptor);

		return nodeManageApi;
	}

	public FilesApi getFilesApi() {
		if (filesApi == null)
			filesApi = createService(FilesApi.class, serviceEndpoint.getProviderAddress(), this.plainRequestInterceptor);

		return filesApi;
	}

	public SubscriptionApi getSubscriptionApi() {
		if (subscriptionApi == null) {
			subscriptionApi = createService(SubscriptionApi.class, serviceEndpoint.getProviderAddress(), this.plainRequestInterceptor);
		}
		return subscriptionApi;
	}

	public PaymentApi getPaymentApi() {
		if (paymentApi == null) {
			paymentApi = createService(PaymentApi.class, serviceEndpoint.getProviderAddress(), this.plainRequestInterceptor);
		}
		return paymentApi;
	}

	public DatabaseApi getDatabaseApi() {
		if (databaseApi == null) {
			databaseApi = createService(DatabaseApi.class, serviceEndpoint.getProviderAddress(), this.plainRequestInterceptor);
		}
		return databaseApi;
	}

	public ScriptingApi getScriptingApi() {
		if (scriptingApi == null) {
			scriptingApi = createService(ScriptingApi.class, serviceEndpoint.getProviderAddress(), this.plainRequestInterceptor);
		}
		return scriptingApi;
	}

	public ScriptingV2Api getScriptingV2Api() {
		if (scriptingV2Api == null) {
			scriptingV2Api = createService(ScriptingV2Api.class, serviceEndpoint.getProviderAddress(), this.plainV2RequestInterceptor);
		}
		return scriptingV2Api;
	}

	public BackupApi getBackupApi() {
		if (backupApi == null) {
			backupApi = createService(BackupApi.class, serviceEndpoint.getProviderAddress(), this.plainRequestInterceptor);
		}
		return backupApi;
	}

	public HttpURLConnection openConnection(String path) throws IOException {
		String url = serviceEndpoint.getProviderAddress() + BaseApi.API_VERSION + path;
		LogUtil.d("open connection with URL: " + url);
		HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		httpURLConnection.setConnectTimeout(5000);
		httpURLConnection.setReadTimeout(5000);

		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setUseCaches(false);
		httpURLConnection.setRequestProperty("Transfer-Encoding", "chunked");
		httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
		httpURLConnection.setRequestProperty("Authorization", this.plainRequestInterceptor.getAuthToken().getCanonicalizedAccessToken());

		httpURLConnection.setChunkedStreamingMode(0);
		return httpURLConnection;
	}

	public static void readConnection(HttpURLConnection httpURLConnection) throws IOException {
		int code = httpURLConnection.getResponseCode();
		if (code == 200) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line = "";
			while ((line = reader.readLine()) != null)
				if (line.length() > 0)
					result.append(line.trim());
			LogUtil.d("connection", "response content: " + result.toString());
		} else {
			throw HiveResponseBody.getHttpExceptionByCode(code, HiveResponseBody.getHttpErrorMessages().get(code));
		}
	}

	private static <S> S createService(Class<S> serviceClass, String baseUrl, RequestInterceptor requestInterceptor) {
		OkHttpClient.Builder clientBuilder;
		Retrofit.Builder retrofitBuilder;

		clientBuilder = new OkHttpClient.Builder()
				.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

		clientBuilder.interceptors().clear();
		clientBuilder.interceptors().add(requestInterceptor);
		clientBuilder.interceptors().add(new LoggerInterceptor());

		retrofitBuilder = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(StringConverterFactory.create())
				.addConverterFactory(GsonConverterFactory.create());

		return retrofitBuilder.client(clientBuilder.build()).build().create(serviceClass);
	}
}
