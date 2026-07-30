package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.database.dao.ProfileDao;
import com.bangersoul.aivance.core.database.dao.RoadmapDao;
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
public final class UserLocalDataSourceImpl_Factory implements Factory<UserLocalDataSourceImpl> {
  private final Provider<ProfileDao> profileDaoProvider;

  private final Provider<RoadmapDao> roadmapDaoProvider;

  private UserLocalDataSourceImpl_Factory(Provider<ProfileDao> profileDaoProvider,
      Provider<RoadmapDao> roadmapDaoProvider) {
    this.profileDaoProvider = profileDaoProvider;
    this.roadmapDaoProvider = roadmapDaoProvider;
  }

  @Override
  public UserLocalDataSourceImpl get() {
    return newInstance(profileDaoProvider.get(), roadmapDaoProvider.get());
  }

  public static UserLocalDataSourceImpl_Factory create(Provider<ProfileDao> profileDaoProvider,
      Provider<RoadmapDao> roadmapDaoProvider) {
    return new UserLocalDataSourceImpl_Factory(profileDaoProvider, roadmapDaoProvider);
  }

  public static UserLocalDataSourceImpl newInstance(ProfileDao profileDao, RoadmapDao roadmapDao) {
    return new UserLocalDataSourceImpl(profileDao, roadmapDao);
  }
}
