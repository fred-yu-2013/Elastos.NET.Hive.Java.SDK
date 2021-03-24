package org.elastos.hive.vault;

import okhttp3.ResponseBody;
import org.elastos.hive.Vault;
import org.elastos.hive.connection.ConnectionManager;
import org.elastos.hive.exception.HiveException;
import org.elastos.hive.network.FilesApi;
import org.elastos.hive.network.model.FileInfo;
import org.elastos.hive.network.request.FilesCopyRequestBody;
import org.elastos.hive.network.request.FilesDeleteRequestBody;
import org.elastos.hive.network.request.FilesMoveRequestBody;
import org.elastos.hive.network.response.FilesHashResponseBody;
import org.elastos.hive.network.response.FilesListResponseBody;
import org.elastos.hive.network.response.FilesPropertiesResponseBody;
import org.elastos.hive.network.response.HiveResponseBody;
import org.elastos.hive.service.FilesService;
import retrofit2.Response;

import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

class FilesServiceRender implements FilesService {
	private ConnectionManager connectionManager;

	public FilesServiceRender(Vault vault) {
		this.connectionManager = vault.getAppContext().getConnectionManager();
	}

	@Override
	public <T> CompletableFuture<T> upload(String path, Class<T> resultType) {
		return CompletableFuture.supplyAsync(() -> uploadImpl(path, resultType));
	}

	private <T> T uploadImpl(String path, Class<T> resultType) {
		try {
			return HiveResponseBody.getRequestStream(this.connectionManager.openConnection(FilesApi.API_UPLOAD + "/" + path), resultType);
		} catch (IOException e) {
			throw new CompletionException(e);
		}
	}

	@Override
	public CompletableFuture<List<FileInfo>> list(String path) {
		return CompletableFuture.supplyAsync(() -> listImpl(path));
	}

	private List<FileInfo> listImpl(String path) {
		try {
			Response<FilesListResponseBody> response = connectionManager.getFilesApi().list(path).execute();
			FilesListResponseBody body = HiveResponseBody.validateBody(response);
			return body.getFileInfoList();
		} catch (HiveException | IOException e) {
			throw new CompletionException(new HiveException(e.getMessage()));
		}
	}

	@Override
	public CompletableFuture<FileInfo> stat(String path) {
		return CompletableFuture.supplyAsync(() -> statImpl(path));
	}

	private FileInfo statImpl(String path) {
		try {
			Response<FilesPropertiesResponseBody> response = this.connectionManager.getFilesApi()
					.properties(path).execute();
			FilesPropertiesResponseBody body = HiveResponseBody.validateBody(response);
			return body.getFileInfo();
		} catch (Exception e) {
			throw new CompletionException(new HiveException(e.getMessage()));
		}
	}

	@Override
	public <T> CompletableFuture<T> download(String path, Class<T> resultType) {
		return CompletableFuture.supplyAsync(() -> downloadImpl(path, resultType));
	}

	private <T> T downloadImpl(String remoteFile, Class<T> resultType) {
		try {
			Response<ResponseBody> response = this.connectionManager.getFilesApi()
					.download(remoteFile)
					.execute();
			return HiveResponseBody.getResponseStream(response, resultType);
		} catch (HiveException|IOException e) {
			throw new CompletionException(new HiveException(e.getMessage()));
		}
	}

	@Override
	public CompletableFuture<Boolean> delete(String path) {
		return CompletableFuture.supplyAsync(() -> deleteImpl(path));
	}

	private Boolean deleteImpl(String path) {
		try {
			FilesDeleteRequestBody reqBody = new FilesDeleteRequestBody();
			reqBody.setPath(path);
			Response<HiveResponseBody> response = this.connectionManager.getFilesApi()
					.delete(reqBody)
					.execute();
			HiveResponseBody.validateBody(response);
			return true;
		} catch (Exception e) {
			throw new CompletionException(new HiveException(e.getMessage()));
		}
	}

	@Override
	public CompletableFuture<Boolean> move(String source, String target) {
		return CompletableFuture.supplyAsync(() -> moveImpl(source, target));
	}

	private Boolean moveImpl(String source, String target) {
		try {
			FilesMoveRequestBody reqBody = new FilesMoveRequestBody();
			reqBody.setSrcPath(source);
			reqBody.setDstPath(target);
			Response<HiveResponseBody> response = this.connectionManager.getFilesApi()
					.move(reqBody)
					.execute();
			HiveResponseBody.validateBody(response);
			return true;
		} catch (Exception e) {
			throw new CompletionException(new HiveException(e.getMessage()));
		}
	}

	@Override
	public CompletableFuture<Boolean> copy(String source, String target) {
		return CompletableFuture.supplyAsync(() -> copyImpl(source, target));
	}

	private Boolean copyImpl(String source, String target) {
		try {
			FilesCopyRequestBody reqBody = new FilesCopyRequestBody();
			reqBody.setSrcPath(source);
			reqBody.setDstPath(target);
			Response<HiveResponseBody> response = this.connectionManager.getFilesApi()
					.copy(reqBody)
					.execute();
			HiveResponseBody.validateBody(response);
			return true;
		} catch (Exception e) {
			throw new CompletionException(new HiveException(e.getMessage()));
		}
	}

	@Override
	public CompletableFuture<String> hash(String path) {
		return CompletableFuture.supplyAsync(() -> hashImp(path));
	}

	private String hashImp(String remoteFile) {
		try {
			Response<FilesHashResponseBody> response = connectionManager.getFilesApi().hash(remoteFile).execute();
			FilesHashResponseBody hashResponse = HiveResponseBody.validateBody(response);
			return hashResponse.getSha256();
		} catch (HiveException | IOException e) {
			throw new CompletionException(new HiveException(e.getMessage()));
		}
	}
}
