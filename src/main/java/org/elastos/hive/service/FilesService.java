package org.elastos.hive.service;

import org.elastos.hive.network.model.FileInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Vault provides a storage for files saving.
 * Files can be uploading, downloading and getting the status and information.
 */
public interface FilesService {
	/**
	 * Initiates an upload sequence by returning a Write or OutputStream object that can be
	 * used to write small file chunks. After writing, flush()/close() must be called
	 * to actually send the data remotely.
	 *
	 * @param path the path for the remote file
	 * @param resultType Write or OutputStream class
	 * @param <T> Write, OutputStream
	 * @return the new CompletionStage, the result is the Writer or OutputStream interface for
	 *              upload the file content if success; null otherwise
	 */
	<T> CompletableFuture<T> upload(String path, Class<T> resultType);

	/**
	 * Initiates a download sequence by returning a Reader or InputStream object that can
	 * be used to read the downloaded file in chunks.
	 *
	 * @param path the path for the remote file
	 * @param resultType Reader or InputStream class
	 * @return the new CompletionStage, the result is the Reader or InputStream interface for
	 *      read the file content if success; null otherwise
	 */
	<T> CompletableFuture<T> download(String path, Class<T> resultType);

	/**
	 * Deletes a file, or a folder. In case the given path is a folder,
	 * deletion is recursive.
	 *
	 * @param path the path for the remote file or folder
	 * @return the new CompletionStage, the result is true if the file or folder
	 *         successfully deleted; false otherwise
	 */
	CompletableFuture<Boolean> delete(String path);

	/**
	 * Moves (or renames) a file or folder.
	 *
	 * @param source the path to the file or folder to move
	 * @param target the path to the target file or folder
	 * @return The future object that would hold the result of moving operation.
	 *  	   When the result value is true, it means the file or folder has
	 *  	   been moved to target path in success. Otherwise, it will return
	 *  	   result with false.
	 */
	CompletableFuture<Boolean> move(String source, String target);

	/**
	 * Copies a file or a folder (recursively).
	 *
	 * @param source the path for the remote source file or folder
	 * @param target the path for the remote destination file or folder
	 * @return the new CompletionStage, the result is true if the file or folder
	 *         successfully copied; false otherwise
	 */
	CompletableFuture<Boolean> copy(String source, String target);

	/**
	 * Returns the SHA256 hash of the given file.
	 *
	 * @param path path for the remote file
	 * @return the new CompletionStage, the result is the base64 hash string
	 *         if the hash successfully calculated; null otherwise
	 */
	CompletableFuture<String> hash(String path);

	/**
	 * Returns the list of all files in a given folder.
	 *
	 * @param path the path for the remote folder
	 * @return the new CompletionStage, the result is List if success; null otherwise
	 */
	CompletableFuture<List<FileInfo>> list(String path);

	/**
	 * Information about the target file or folder.
	 *
	 * @param path the path for the remote file or folder
	 * @return the new CompletionStage, the result is FileInfo
	 *         if success; null otherwise
	 */
	CompletableFuture<FileInfo> stat(String path);

}
