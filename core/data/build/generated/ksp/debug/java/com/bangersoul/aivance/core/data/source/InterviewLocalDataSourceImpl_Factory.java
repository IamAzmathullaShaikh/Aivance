package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.database.dao.InterviewDao;
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
public final class InterviewLocalDataSourceImpl_Factory implements Factory<InterviewLocalDataSourceImpl> {
  private final Provider<InterviewDao> interviewDaoProvider;

  private InterviewLocalDataSourceImpl_Factory(Provider<InterviewDao> interviewDaoProvider) {
    this.interviewDaoProvider = interviewDaoProvider;
  }

  @Override
  public InterviewLocalDataSourceImpl get() {
    return newInstance(interviewDaoProvider.get());
  }

  public static InterviewLocalDataSourceImpl_Factory create(
      Provider<InterviewDao> interviewDaoProvider) {
    return new InterviewLocalDataSourceImpl_Factory(interviewDaoProvider);
  }

  public static InterviewLocalDataSourceImpl newInstance(InterviewDao interviewDao) {
    return new InterviewLocalDataSourceImpl(interviewDao);
  }
}
