package us.ajg0702.leaderboards.rest;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.leaderboards.rest.generated.geyser.JSON;
import us.ajg0702.leaderboards.rest.generated.geyser.api.SkinApi;
import us.ajg0702.leaderboards.rest.generated.geyser.model.ConvertedSkin;

public class SkinCache {
    static {
        JSON.setLenientOnJson(true);
    }

    private final SkinApi skinApi = new SkinApi();
    private final LoadingCache<Long, ConvertedSkin> skinCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES)
            .build(new CacheLoader<Long, ConvertedSkin>() {
                @Override
                public @NotNull ConvertedSkin load(@NotNull Long key) throws Exception {
                    try {
                        ConvertedSkin.openapiFields.add("last_update"); // this is so silly, but fixes broken OpenAPI spec
                        return skinApi.globalApiWebApiSkinControllerGetSkin(key.toString());
                    } catch (IllegalArgumentException validationException) {
                        // the openapi spec is broken so validation fails sometimes lol
                        throw new IllegalArgumentException("Validation Failed", validationException);
                    }
                }
            });

    public @Nullable ConvertedSkin getSkin(Long skinId) {
        try {
            return skinCache.get(skinId);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof IllegalArgumentException && e.getCause().getMessage().equals("Validation Failed")) {
                // This is a known issue with the OpenAPI spec, we can ignore it
                return null; // or return a default skin if needed
            }
            throw e; // rethrow other validation exceptions
        } catch (Exception e) {
            // Log the error or handle it as needed
            e.printStackTrace();
            return null; // or throw an exception, or return a default skin
        }
    }
}
