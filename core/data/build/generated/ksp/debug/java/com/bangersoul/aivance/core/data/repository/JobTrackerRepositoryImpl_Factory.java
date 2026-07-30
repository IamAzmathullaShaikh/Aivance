package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.JobLocalDataSource;
import com.bangersoul.aivance.core.database.dao.TrackerDao;
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
public final class JobTrackerRepositoryImpl_Factory implements Factory<JobTrackerRepositoryImpl> {
  private final Provider<JobLocalDataSource> localDataSourceProvider;

  private final Provider<TrackerDao> trackerDaoProvider;

  private JobTrackerRepositoryImpl_Factory(Provider<JobLocalDataSource> localDataSourceProvider,
      Provider<TrackerDao> trackerDaoProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.trackerDaoProvider = trackerDaoProvider;
  }

  @Override
  public JobTrackerRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get(), trackerDaoProvider.get());
  }

  public static JobTrackerRepositoryImpl_Factory create(
      Provider<JobLocalDataSource> localDataSourceProvider,
      Provider<TrackerDao> trackerDaoProvider) {
    return new JobTrackerRepositoryImpl_Factory(localDataSourceProvider, trackerDaoProvider);
  }

  public static JobTrackerRepositoryImpl newInstance(JobLocalDataSource localDataSource,
      TrackerDao trackerDao) {
    return new JobTrackerRepositoryImpl(localDataSource, trackerDao);
  }
}
