package com.gymapp.android.data.remote.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ$\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ$\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ$\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011\u00a8\u0006\u0012"}, d2 = {"Lcom/gymapp/android/data/remote/api/AuthApi;", "", "login", "Lretrofit2/Response;", "Lcom/gymapp/android/data/remote/api/ApiResponse;", "Lcom/gymapp/android/data/remote/api/JwtResponse;", "request", "Lcom/gymapp/android/data/remote/api/LoginRequest;", "(Lcom/gymapp/android/data/remote/api/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "Ljava/lang/Void;", "Lcom/gymapp/android/data/remote/api/TokenRefreshRequest;", "(Lcom/gymapp/android/data/remote/api/TokenRefreshRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshToken", "Lcom/gymapp/android/data/remote/api/TokenRefreshResponse;", "register", "Lcom/gymapp/android/data/remote/api/RegisterRequest;", "(Lcom/gymapp/android/data/remote/api/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface AuthApi {
    
    @retrofit2.http.POST(value = "/api/v1/auth/login")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.api.LoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.gymapp.android.data.remote.api.ApiResponse<com.gymapp.android.data.remote.api.JwtResponse>>> $completion);
    
    @retrofit2.http.POST(value = "/api/v1/auth/register")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object register(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.api.RegisterRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.gymapp.android.data.remote.api.ApiResponse<com.gymapp.android.data.remote.api.JwtResponse>>> $completion);
    
    @retrofit2.http.POST(value = "/api/v1/auth/refresh")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object refreshToken(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.api.TokenRefreshRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.gymapp.android.data.remote.api.ApiResponse<com.gymapp.android.data.remote.api.TokenRefreshResponse>>> $completion);
    
    @retrofit2.http.POST(value = "/api/v1/auth/logout")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logout(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.api.TokenRefreshRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.gymapp.android.data.remote.api.ApiResponse<java.lang.Void>>> $completion);
}