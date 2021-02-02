package org.elastos.hive;

import org.elastos.did.DIDDocument;

import java.util.concurrent.CompletableFuture;

public interface ApplicationContext {
	/**
	 * @return Token cache path
	 */
	String getLocalDataDir();

	/**
	 * @return App instance DIDDocument
	 */
	DIDDocument getAppInstanceDocument();

	/**
	 * This is the interface to make authorization from users, and it would be
	 * provided by application.
	 *
	 * @param jwtToken Sign in challenge jwt
	 * @return User authorization token
	 */
	CompletableFuture<String> getAuthorization(String jwtToken);
}