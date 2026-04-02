package com.gymapp.android.ui.screens.auth;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u0006\u0010\u0018\u001a\u00020\u0015J\u0016\u0010\u0019\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cJ\u0006\u0010\u001d\u001a\u00020\u0015R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\u000b\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001e"}, d2 = {"Lcom/gymapp/android/ui/screens/auth/AuthViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/gymapp/android/data/repository/AuthRepository;", "authEventBus", "Lcom/gymapp/android/data/remote/AuthEventBus;", "(Lcom/gymapp/android/data/repository/AuthRepository;Lcom/gymapp/android/data/remote/AuthEventBus;)V", "getAuthEventBus", "()Lcom/gymapp/android/data/remote/AuthEventBus;", "<set-?>", "Lcom/gymapp/android/ui/screens/auth/AuthState;", "uiState", "getUiState", "()Lcom/gymapp/android/ui/screens/auth/AuthState;", "setUiState", "(Lcom/gymapp/android/ui/screens/auth/AuthState;)V", "uiState$delegate", "Landroidx/compose/runtime/MutableState;", "isLoggedIn", "", "login", "", "request", "Lcom/gymapp/android/data/remote/api/LoginRequest;", "logout", "register", "Lcom/gymapp/android/data/remote/api/RegisterRequest;", "passConfirm", "", "resetState", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AuthViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.gymapp.android.data.repository.AuthRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.gymapp.android.data.remote.AuthEventBus authEventBus = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState uiState$delegate = null;
    
    @javax.inject.Inject()
    public AuthViewModel(@org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.repository.AuthRepository repository, @org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.AuthEventBus authEventBus) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gymapp.android.data.remote.AuthEventBus getAuthEventBus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.gymapp.android.ui.screens.auth.AuthState getUiState() {
        return null;
    }
    
    private final void setUiState(com.gymapp.android.ui.screens.auth.AuthState p0) {
    }
    
    public final boolean isLoggedIn() {
        return false;
    }
    
    public final void login(@org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.api.LoginRequest request) {
    }
    
    public final void register(@org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.remote.api.RegisterRequest request, @org.jetbrains.annotations.NotNull()
    java.lang.String passConfirm) {
    }
    
    public final void logout() {
    }
    
    public final void resetState() {
    }
}