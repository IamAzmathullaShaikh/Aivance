package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.database.dao.CoverLetterDao;
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
public final class CoverLetterLocalDataSourceImpl_Factory implements Factory<CoverLetterLocalDataSourceImpl> {
  private final Provider<CoverLetterDao> coverLetterDaoProvider;

  private CoverLetterLocalDataSourceImpl_Factory(Provider<CoverLetterDao> coverLetterDaoProvider) {
    this.coverLetterDaoProvider = coverLetterDaoProvider;
  }

  @Override
  public CoverLetterLocalDataSourceImpl get() {
    return newInstance(coverLetterDaoProvider.get());
  }

  public static CoverLetterLocalDataSourceImpl_Factory create(
      Provider<CoverLetterDao> coverLetterDaoProvider) {
    return new CoverLetterLocalDataSourceImpl_Factory(coverLetterDaoProvider);
  }

  public static CoverLetterLocalDataSourceImpl newInstance(CoverLetterDao coverLetterDao) {
    return new CoverLetterLocalDataSourceImpl(coverLetterDao);
  }
}
