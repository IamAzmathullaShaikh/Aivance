package com.bangersoul.aivance.job.cache;

import com.bangersoul.aivance.core.database.dao.CompanyDao;
import com.bangersoul.aivance.core.database.dao.JobDao;
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
public final class RoomJobCache_Factory implements Factory<RoomJobCache> {
  private final Provider<JobDao> jobDaoProvider;

  private final Provider<CompanyDao> companyDaoProvider;

  private RoomJobCache_Factory(Provider<JobDao> jobDaoProvider,
      Provider<CompanyDao> companyDaoProvider) {
    this.jobDaoProvider = jobDaoProvider;
    this.companyDaoProvider = companyDaoProvider;
  }

  @Override
  public RoomJobCache get() {
    return newInstance(jobDaoProvider.get(), companyDaoProvider.get());
  }

  public static RoomJobCache_Factory create(Provider<JobDao> jobDaoProvider,
      Provider<CompanyDao> companyDaoProvider) {
    return new RoomJobCache_Factory(jobDaoProvider, companyDaoProvider);
  }

  public static RoomJobCache newInstance(JobDao jobDao, CompanyDao companyDao) {
    return new RoomJobCache(jobDao, companyDao);
  }
}
