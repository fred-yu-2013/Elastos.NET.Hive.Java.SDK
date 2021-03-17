package org.elastos.hive.connection;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;

final class HttpGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    @SuppressWarnings("unused")
    private final TypeAdapter<T> adapter;

    HttpGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override public T convert(ResponseBody value) throws IOException {
        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        try {
            HiveResponseBody<T> result = gson.fromJson(jsonReader, HiveResponseBody.class);
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonIOException("JSON document was not fully consumed.");
            }
            return result.getFileInfoList();
        } finally {
            value.close();
        }
    }
}
