package com.gymapp.android.di;

import android.content.Context;
import com.gymapp.android.data.local.TokenStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class StorageModule_ProvideTokenStorageFactory implements Factory<TokenStorage> {
  private final Provider<Context> contextProvider;

  public StorageModule_ProvideTokenStorageFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public TokenStorage get() {
    return provideTokenStorage(contextProvider.get());
  }

  public static StorageModule_ProvideTokenStorageFactory create(Provider<Context> contextProvider) {
    return new StorageModule_ProvideTokenStorageFactory(contextProvider);
  }

  public static TokenStorage provideTokenStorage(Context context) {
    return Preconditions.checkNotNullFromProvides(StorageModule.INSTANCE.provideTokenStorage(context));
  }
}
