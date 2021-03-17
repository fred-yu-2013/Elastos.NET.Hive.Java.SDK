package org.elastos.hive.connection;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.elastos.hive.network.model.FileInfo;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

final class HttpGsonConverterFactory extends Converter.Factory {
    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static HttpGsonConverterFactory create() {
        return create(new Gson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static HttpGsonConverterFactory create(Gson gson) {
        if (gson == null)
            throw new NullPointerException("gson == null");
        return new HttpGsonConverterFactory(gson);
    }

    private final Gson gson;

    private HttpGsonConverterFactory(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                            Annotation[] annotations,
                                                            Retrofit retrofit) {
        //Only handle return type List<FileInfo>.
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            if (p.getActualTypeArguments()[0] == FileInfo.class) {
                TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
                return new HttpGsonResponseBodyConverter<>(gson, adapter);
            }
        }
        return null;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations,
                                                          Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new HttpGsonRequestBodyConverter<>(gson, adapter);
    }
}
