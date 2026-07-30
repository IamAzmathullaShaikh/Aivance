package com.bangersoul.aivance.core.data.config;

import com.bangersoul.aivance.core.datastore.PreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class PrivacyManagerImpl_Factory implements Factory<PrivacyManagerImpl> {
  private final Provider<PreferencesManager> preferencesManagerProvider;

  private PrivacyManagerImpl_Factory(Provider<PreferencesManager> preferencesManagerProvider) {
    this.preferencesManagerProvider = preferencesManagerProvider;
  }

  @Override
  public PrivacyManagerImpl get() {
    return newInstance(preferencesManagerProvider.get());
  }

  public static PrivacyManagerImpl_Factory create(
      Provider<PreferencesManager> preferencesManagerProvider) {
    return new PrivacyManagerImpl_Factory(preferencesManagerProvider);
  }

  public static PrivacyManagerImpl newInstance(PreferencesManager preferencesManager) {
    return new PrivacyManagerImpl(preferencesManager);
  }
}
