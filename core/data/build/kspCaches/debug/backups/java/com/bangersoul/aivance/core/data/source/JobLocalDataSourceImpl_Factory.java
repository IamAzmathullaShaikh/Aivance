package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.database.dao.CompanyDao;
import com.bangersoul.aivance.core.database.dao.JobDao;
import com.bangersoul.aivance.core.database.dao.SearchDao;
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
public final class JobLocalDataSourceImpl_Factory implements Factory<JobLocalDataSourceImpl> {
  private final Provider<JobDao> jobDaoProvider;

  private final Provider<CompanyDao> companyDaoProvider;

  private final Provider<SearchDao> searchDaoProvider;

  private final Provider<TrackerDao> trackerDaoProvider;

  private JobLocalDataSourceImpl_Factory(Provider<JobDao> jobDaoProvider,
      Provider<CompanyDao> companyDaoProvider, Provider<SearchDao> searchDaoProvider,
      Provider<TrackerDao> trackerDaoProvider) {
    this.jobDaoProvider = jobDaoProvider;
    this.companyDaoProvider = companyDaoProvider;
    this.searchDaoProvider = searchDaoProvider;
    this.trackerDaoProvider = trackerDaoProvider;
  }

  @Override
  public JobLocalDataSourceImpl get() {
    return newInstance(jobDaoProvider.get(), companyDaoProvider.get(), searchDaoProvider.get(), trackerDaoProvider.get());
  }

  public static JobLocalDataSourceImpl_Factory create(Provider<JobDao> jobDaoProvider,
      Provider<CompanyDao> companyDaoProvider, Provider<SearchDao> searchDaoProvider,
      Provider<TrackerDao> trackerDaoProvider) {
    return new JobLocalDataSourceImpl_Factory(jobDaoProvider, companyDaoProvider, searchDaoProvider, trackerDaoProvider);
  }

  public static JobLocalDataSourceImpl newInstance(JobDao jobDao, CompanyDao companyDao,
      SearchDao searchDao, TrackerDao trackerDao) {
    return new JobLocalDataSourceImpl(jobDao, companyDao, searchDao, trackerDao);
  }
}
