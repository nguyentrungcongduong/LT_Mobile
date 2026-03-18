package com.gymapp.android.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J5\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n\"\u0004\b\u0000\u0010\f2\u0012\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\f0\u000f0\u000eH\u0002\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0010\u0010\u0011J\u0006\u0010\u0012\u001a\u00020\u0013J$\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0017\u0010\u0018J\u0006\u0010\u0019\u001a\u00020\u000bJ\"\u0010\u001a\u001a\u00020\u001b\"\u0004\b\u0000\u0010\f2\u0012\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\f0\u000f0\u000eH\u0002J$\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u0015\u001a\u00020\u001dH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001e\u0010\u001fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006 "}, d2 = {"Lcom/gymapp/android/data/repository/AuthRepository;", "", "authApi", "Lcom/gymapp/android/data/remote/api/AuthApi;", "tokenStorage", "Lcom/gymapp/android/data/local/TokenStorage;", "(Lcom/gymapp/android/data/remote/api/AuthApi;Lcom/gymapp/android/data/local/TokenStorage;)V", "gson", "Lcom/google/gson/Gson;", "handleAuthResponse", "Lkotlin/Result;", "", "T", "response", "Lretrofit2/Response;", "Lcom/gymapp/android/data/remote/api/ApiResponse;", "handleAuthResponse-IoAF18A", "(Lretrofit2/Response;)Ljava/lang/Object;", "hasToken", "", "login", "request", "Lcom/gymapp/android/data/remote/api/LoginRequest;", "login-gIAlu-s", "(Lcom/gymapp/android/data/remote/api/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "parseErrorMessage", "", "register", "Lcom/gymapp/android/data/remote/api/RegisterRequest;", "register-gIAlu-s", "(Lcom/gymapp/android/data/remote/api/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class AuthRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.gymapp.android.data.remote.api.AuthApi authApi = null;
    @org.jetbrains.annotations.NotNull()
    private final com.gymapp.android.data.local.TokenStorage tokenStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    
    @javax.inject.Inject()
    public AuthRepository(@org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.api.AuthApi authApi, @org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.local.TokenStorage tokenStorage) {
        super();
    }
    
    private final <T extends java.lang.Object>java.lang.String parseErrorMessage(retrofit2.Response<com.gymapp.android.data.remote.api.ApiResponse<T>> response) {
        return null;
    }
    
    public final void logout() {
    }
    
    public final boolean hasToken() {
        return false;
    }
}