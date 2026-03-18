package com.gymapp.android.data.remote;

import com.gymapp.android.data.local.TokenStorage;
import com.gymapp.android.data.remote.api.AuthApi;
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
public final class TokenAuthenticator_Factory implements Factory<TokenAuthenticator> {
  private final Provider<TokenStorage> tokenStorageProvider;

  private final Provider<AuthApi> authApiProvider;

  public TokenAuthenticator_Factory(Provider<TokenStorage> tokenStorageProvider,
      Provider<AuthApi> authApiProvider) {
    this.tokenStorageProvider = tokenStorageProvider;
    this.authApiProvider = authApiProvider;
  }

  @Override
  public TokenAuthenticator get() {
    return newInstance(tokenStorageProvider, authApiProvider);
  }

  public static TokenAuthenticator_Factory create(Provider<TokenStorage> tokenStorageProvider,
      Provider<AuthApi> authApiProvider) {
    return new TokenAuthenticator_Factory(tokenStorageProvider, authApiProvider);
  }

  public static TokenAuthenticator newInstance(Provider<TokenStorage> tokenStorageProvider,
      Provider<AuthApi> authApiProvider) {
    return new TokenAuthenticator(tokenStorageProvider, authApiProvider);
  }
}
