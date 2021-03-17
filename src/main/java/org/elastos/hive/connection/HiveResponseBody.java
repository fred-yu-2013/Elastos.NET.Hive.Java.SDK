package org.elastos.hive.connection;

import com.google.gson.annotations.SerializedName;
import org.elastos.hive.exception.HiveSdkException;

public class HiveResponseBody<T> {
    private static final String SUCCESS = "OK";

    @SerializedName("_status")
    private String status;

    @SerializedName("_error")
    private HiveResponseBody.Error error;

    @SerializedName("file_info_list")
    private T fileInfoList;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getErrorCode() {
        return error == null ? -1 : error.code;
    }

    public String getErrorMessage() {
        return error == null ? "" : error.message;
    }

    public T getFileInfoList() {
        if (!SUCCESS.equals(status))
            throw new HiveSdkException("Response body status contains error.");
        return this.fileInfoList;
    }

    static class Error {
        @SerializedName("code")
        int code;
        @SerializedName("message")
        String message;
    }
}
