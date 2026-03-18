package com.gymapp.android.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B#\b\u0007\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003\u00a2\u0006\u0002\u0010\u0007J\u001c\u0010\b\u001a\u0004\u0018\u00010\t2\b\u0010\n\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\f\u001a\u00020\rH\u0016R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/gymapp/android/data/remote/TokenAuthenticator;", "Lokhttp3/Authenticator;", "tokenStorageProvider", "Ljavax/inject/Provider;", "Lcom/gymapp/android/data/local/TokenStorage;", "authApiProvider", "Lcom/gymapp/android/data/remote/api/AuthApi;", "(Ljavax/inject/Provider;Ljavax/inject/Provider;)V", "authenticate", "Lokhttp3/Request;", "route", "Lokhttp3/Route;", "response", "Lokhttp3/Response;", "app_debug"})
public final class TokenAuthenticator implements okhttp3.Authenticator {
    @org.jetbrains.annotations.NotNull()
    private final javax.inject.Provider<com.gymapp.android.data.local.TokenStorage> tokenStorageProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.inject.Provider<com.gymapp.android.data.remote.api.AuthApi> authApiProvider = null;
    
    @javax.inject.Inject()
    public TokenAuthenticator(@org.jetbrains.annotations.NotNull()
    javax.inject.Provider<com.gymapp.android.data.local.TokenStorage> tokenStorageProvider, @org.jetbrains.annotations.NotNull()
    javax.inject.Provider<com.gymapp.android.data.remote.api.AuthApi> authApiProvider) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public okhttp3.Request authenticate(@org.jetbrains.annotations.Nullable()
    okhttp3.Route route, @org.jetbrains.annotations.NotNull()
    okhttp3.Response response) {
        return null;
    }
}