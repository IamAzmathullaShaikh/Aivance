package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AiLocalDataSourceImpl_Factory implements Factory<AiLocalDataSourceImpl> {
  private final Provider<AiAnalyticsDao> aiAnalyticsDaoProvider;

  private AiLocalDataSourceImpl_Factory(Provider<AiAnalyticsDao> aiAnalyticsDaoProvider) {
    this.aiAnalyticsDaoProvider = aiAnalyticsDaoProvider;
  }

  @Override
  public AiLocalDataSourceImpl get() {
    return newInstance(aiAnalyticsDaoProvider.get());
  }

  public static AiLocalDataSourceImpl_Factory create(
      Provider<AiAnalyticsDao> aiAnalyticsDaoProvider) {
    return new AiLocalDataSourceImpl_Factory(aiAnalyticsDaoProvider);
  }

  public static AiLocalDataSourceImpl newInstance(AiAnalyticsDao aiAnalyticsDao) {
    return new AiLocalDataSourceImpl(aiAnalyticsDao);
  }
}
