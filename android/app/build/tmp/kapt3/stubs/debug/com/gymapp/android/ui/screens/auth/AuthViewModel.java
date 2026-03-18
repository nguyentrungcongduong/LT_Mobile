package com.gymapp.android.ui.screens.auth;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0011J\u0016\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018J\u0006\u0010\u0019\u001a\u00020\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\f\u0010\r\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000b\u00a8\u0006\u001a"}, d2 = {"Lcom/gymapp/android/ui/screens/auth/AuthViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/gymapp/android/data/repository/AuthRepository;", "(Lcom/gymapp/android/data/repository/AuthRepository;)V", "<set-?>", "Lcom/gymapp/android/ui/screens/auth/AuthState;", "uiState", "getUiState", "()Lcom/gymapp/android/ui/screens/auth/AuthState;", "setUiState", "(Lcom/gymapp/android/ui/screens/auth/AuthState;)V", "uiState$delegate", "Landroidx/compose/runtime/MutableState;", "isLoggedIn", "", "login", "", "request", "Lcom/gymapp/android/data/remote/api/LoginRequest;", "logout", "register", "Lcom/gymapp/android/data/remote/api/RegisterRequest;", "passConfirm", "", "resetState", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AuthViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.gymapp.android.data.repository.AuthRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState uiState$delegate = null;
    
    @javax.inject.Inject()
    public AuthViewModel(@org.jetbrains.annotations.NotNull()
    com.gymapp.android.data.repository.AuthRepository repository) {
        super();
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