package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.database.dao.AtsDao;
import com.bangersoul.aivance.core.database.dao.ResumeDao;
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
public final class ResumeLocalDataSourceImpl_Factory implements Factory<ResumeLocalDataSourceImpl> {
  private final Provider<ResumeDao> resumeDaoProvider;

  private final Provider<AtsDao> atsDaoProvider;

  private ResumeLocalDataSourceImpl_Factory(Provider<ResumeDao> resumeDaoProvider,
      Provider<AtsDao> atsDaoProvider) {
    this.resumeDaoProvider = resumeDaoProvider;
    this.atsDaoProvider = atsDaoProvider;
  }

  @Override
  public ResumeLocalDataSourceImpl get() {
    return newInstance(resumeDaoProvider.get(), atsDaoProvider.get());
  }

  public static ResumeLocalDataSourceImpl_Factory create(Provider<ResumeDao> resumeDaoProvider,
      Provider<AtsDao> atsDaoProvider) {
    return new ResumeLocalDataSourceImpl_Factory(resumeDaoProvider, atsDaoProvider);
  }

  public static ResumeLocalDataSourceImpl newInstance(ResumeDao resumeDao, AtsDao atsDao) {
    return new ResumeLocalDataSourceImpl(resumeDao, atsDao);
  }
}
