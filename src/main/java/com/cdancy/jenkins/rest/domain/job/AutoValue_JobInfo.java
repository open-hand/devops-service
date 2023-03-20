package com.cdancy.jenkins.rest.domain.job;

import java.util.List;
import javax.annotation.Generated;

import com.cdancy.jenkins.rest.domain.queue.QueueItem;
import org.jclouds.javax.annotation.Nullable;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_JobInfo extends JobInfo {

  private final String description;

  private final String displayName;

  private final String displayNameOrNull;

  private final String name;

  private final String url;

  private final boolean buildable;

  private final List<BuildInfo> builds;

  private final String color;

  private final BuildInfo firstBuild;

  private final boolean inQueue;

  private final boolean keepDependencies;

  private final BuildInfo lastBuild;

  private final BuildInfo lastCompleteBuild;

  private final BuildInfo lastFailedBuild;

  private final BuildInfo lastStableBuild;

  private final BuildInfo lastSuccessfulBuild;

  private final BuildInfo lastUnstableBuild;

  private final BuildInfo lastUnsuccessfulBuild;

  private final int nextBuildNumber;
  private final QueueItem queueItem;
  private final boolean concurrentBuild;
  public List<ParametersDefinitionProperty> property;

  AutoValue_JobInfo(
          @Nullable String description,
          @Nullable String displayName,
          @Nullable String displayNameOrNull,
          String name,
          String url,
          boolean buildable,
          List<BuildInfo> builds,
          @Nullable String color,
          @Nullable BuildInfo firstBuild,
          boolean inQueue,
          boolean keepDependencies,
          @Nullable BuildInfo lastBuild,
          @Nullable BuildInfo lastCompleteBuild,
          @Nullable BuildInfo lastFailedBuild,
          @Nullable BuildInfo lastStableBuild,
          @Nullable BuildInfo lastSuccessfulBuild,
          @Nullable BuildInfo lastUnstableBuild,
          @Nullable BuildInfo lastUnsuccessfulBuild,
          int nextBuildNumber,
          List<ParametersDefinitionProperty> property,
          @Nullable QueueItem queueItem,
          boolean concurrentBuild) {
    this.description = description;
    this.displayName = displayName;
    this.displayNameOrNull = displayNameOrNull;
    if (name == null) {
      throw new NullPointerException("Null name");
    }
    this.name = name;
    if (url == null) {
      throw new NullPointerException("Null url");
    }
    this.url = url;
    this.buildable = buildable;
    if (builds == null) {
      throw new NullPointerException("Null builds");
    }
    this.builds = builds;
    this.color = color;
    this.firstBuild = firstBuild;
    this.inQueue = inQueue;
    this.keepDependencies = keepDependencies;
    this.lastBuild = lastBuild;
    this.lastCompleteBuild = lastCompleteBuild;
    this.lastFailedBuild = lastFailedBuild;
    this.lastStableBuild = lastStableBuild;
    this.lastSuccessfulBuild = lastSuccessfulBuild;
    this.lastUnstableBuild = lastUnstableBuild;
    this.lastUnsuccessfulBuild = lastUnsuccessfulBuild;
    this.nextBuildNumber = nextBuildNumber;
    this.queueItem = queueItem;
    this.concurrentBuild = concurrentBuild;
    this.property = property;
  }

  @Nullable
  @Override
  public String description() {
    return description;
  }

  @Nullable
  @Override
  public String displayName() {
    return displayName;
  }

  @Nullable
  @Override
  public String displayNameOrNull() {
    return displayNameOrNull;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String url() {
    return url;
  }

  @Override
  public boolean buildable() {
    return buildable;
  }

  @Override
  public List<BuildInfo> builds() {
    return builds;
  }

  @Nullable
  @Override
  public String color() {
    return color;
  }

  @Nullable
  @Override
  public BuildInfo firstBuild() {
    return firstBuild;
  }

  @Override
  public boolean inQueue() {
    return inQueue;
  }

  @Override
  public boolean keepDependencies() {
    return keepDependencies;
  }

  @Nullable
  @Override
  public BuildInfo lastBuild() {
    return lastBuild;
  }

  @Nullable
  @Override
  public BuildInfo lastCompleteBuild() {
    return lastCompleteBuild;
  }

  @Nullable
  @Override
  public BuildInfo lastFailedBuild() {
    return lastFailedBuild;
  }

  @Nullable
  @Override
  public BuildInfo lastStableBuild() {
    return lastStableBuild;
  }

  @Nullable
  @Override
  public BuildInfo lastSuccessfulBuild() {
    return lastSuccessfulBuild;
  }

  @Nullable
  @Override
  public BuildInfo lastUnstableBuild() {
    return lastUnstableBuild;
  }

  @Nullable
  @Override
  public BuildInfo lastUnsuccessfulBuild() {
    return lastUnsuccessfulBuild;
  }

  @Override
  public int nextBuildNumber() {
    return nextBuildNumber;
  }

  @Override
  public List<ParametersDefinitionProperty> property() {
    return property;
  }

  @Nullable
  @Override
  public QueueItem queueItem() {
    return queueItem;
  }

  @Override
  public boolean concurrentBuild() {
    return concurrentBuild;
  }

  @Override
  public String toString() {
    return "JobInfo{"
            + "description=" + description + ", "
            + "displayName=" + displayName + ", "
            + "displayNameOrNull=" + displayNameOrNull + ", "
            + "name=" + name + ", "
            + "url=" + url + ", "
            + "buildable=" + buildable + ", "
            + "builds=" + builds + ", "
            + "color=" + color + ", "
            + "firstBuild=" + firstBuild + ", "
            + "inQueue=" + inQueue + ", "
            + "keepDependencies=" + keepDependencies + ", "
            + "lastBuild=" + lastBuild + ", "
            + "lastCompleteBuild=" + lastCompleteBuild + ", "
            + "lastFailedBuild=" + lastFailedBuild + ", "
            + "lastStableBuild=" + lastStableBuild + ", "
            + "lastSuccessfulBuild=" + lastSuccessfulBuild + ", "
            + "lastUnstableBuild=" + lastUnstableBuild + ", "
            + "lastUnsuccessfulBuild=" + lastUnsuccessfulBuild + ", "
            + "nextBuildNumber=" + nextBuildNumber + ", "
            + "queueItem=" + queueItem + ", "
            + "concurrentBuild=" + concurrentBuild
            + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof JobInfo) {
      JobInfo that = (JobInfo) o;
      return (this.description == null ? that.description() == null : this.description.equals(that.description()))
              && (this.displayName == null ? that.displayName() == null : this.displayName.equals(that.displayName()))
              && (this.displayNameOrNull == null ? that.displayNameOrNull() == null : this.displayNameOrNull.equals(that.displayNameOrNull()))
              && this.name.equals(that.name())
              && this.url.equals(that.url())
              && this.buildable == that.buildable()
              && this.builds.equals(that.builds())
              && (this.color == null ? that.color() == null : this.color.equals(that.color()))
              && (this.firstBuild == null ? that.firstBuild() == null : this.firstBuild.equals(that.firstBuild()))
              && this.inQueue == that.inQueue()
              && this.keepDependencies == that.keepDependencies()
              && (this.lastBuild == null ? that.lastBuild() == null : this.lastBuild.equals(that.lastBuild()))
              && (this.lastCompleteBuild == null ? that.lastCompleteBuild() == null : this.lastCompleteBuild.equals(that.lastCompleteBuild()))
              && (this.lastFailedBuild == null ? that.lastFailedBuild() == null : this.lastFailedBuild.equals(that.lastFailedBuild()))
              && (this.lastStableBuild == null ? that.lastStableBuild() == null : this.lastStableBuild.equals(that.lastStableBuild()))
              && (this.lastSuccessfulBuild == null ? that.lastSuccessfulBuild() == null : this.lastSuccessfulBuild.equals(that.lastSuccessfulBuild()))
              && (this.lastUnstableBuild == null ? that.lastUnstableBuild() == null : this.lastUnstableBuild.equals(that.lastUnstableBuild()))
              && (this.lastUnsuccessfulBuild == null ? that.lastUnsuccessfulBuild() == null : this.lastUnsuccessfulBuild.equals(that.lastUnsuccessfulBuild()))
              && this.nextBuildNumber == that.nextBuildNumber()
              && (this.queueItem == null ? that.queueItem() == null : this.queueItem.equals(that.queueItem()))
              && this.concurrentBuild == that.concurrentBuild();
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= (description == null) ? 0 : description.hashCode();
    h$ *= 1000003;
    h$ ^= (displayName == null) ? 0 : displayName.hashCode();
    h$ *= 1000003;
    h$ ^= (displayNameOrNull == null) ? 0 : displayNameOrNull.hashCode();
    h$ *= 1000003;
    h$ ^= name.hashCode();
    h$ *= 1000003;
    h$ ^= url.hashCode();
    h$ *= 1000003;
    h$ ^= buildable ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= builds.hashCode();
    h$ *= 1000003;
    h$ ^= (color == null) ? 0 : color.hashCode();
    h$ *= 1000003;
    h$ ^= (firstBuild == null) ? 0 : firstBuild.hashCode();
    h$ *= 1000003;
    h$ ^= inQueue ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= keepDependencies ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= (lastBuild == null) ? 0 : lastBuild.hashCode();
    h$ *= 1000003;
    h$ ^= (lastCompleteBuild == null) ? 0 : lastCompleteBuild.hashCode();
    h$ *= 1000003;
    h$ ^= (lastFailedBuild == null) ? 0 : lastFailedBuild.hashCode();
    h$ *= 1000003;
    h$ ^= (lastStableBuild == null) ? 0 : lastStableBuild.hashCode();
    h$ *= 1000003;
    h$ ^= (lastSuccessfulBuild == null) ? 0 : lastSuccessfulBuild.hashCode();
    h$ *= 1000003;
    h$ ^= (lastUnstableBuild == null) ? 0 : lastUnstableBuild.hashCode();
    h$ *= 1000003;
    h$ ^= (lastUnsuccessfulBuild == null) ? 0 : lastUnsuccessfulBuild.hashCode();
    h$ *= 1000003;
    h$ ^= nextBuildNumber;
    h$ *= 1000003;
    h$ ^= (queueItem == null) ? 0 : queueItem.hashCode();
    h$ *= 1000003;
    h$ ^= concurrentBuild ? 1231 : 1237;
    return h$;
  }

}
