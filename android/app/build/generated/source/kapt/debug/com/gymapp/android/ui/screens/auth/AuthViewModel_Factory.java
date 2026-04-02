package com.gymapp.android.ui.screens.auth;

import com.gymapp.android.data.remote.AuthEventBus;
import com.gymapp.android.data.repository.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthRepository> repositoryProvider;

  private final Provider<AuthEventBus> authEventBusProvider;

  public AuthViewModel_Factory(Provider<AuthRepository> repositoryProvider,
      Provider<AuthEventBus> authEventBusProvider) {
    this.repositoryProvider = repositoryProvider;
    this.authEventBusProvider = authEventBusProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(repositoryProvider.get(), authEventBusProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<AuthRepository> repositoryProvider,
      Provider<AuthEventBus> authEventBusProvider) {
    return new AuthViewModel_Factory(repositoryProvider, authEventBusProvider);
  }

  public static AuthViewModel newInstance(AuthRepository repository, AuthEventBus authEventBus) {
    return new AuthViewModel(repository, authEventBus);
  }
}
