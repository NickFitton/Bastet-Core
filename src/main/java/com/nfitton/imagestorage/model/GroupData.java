package com.nfitton.imagestorage.model;

import com.nfitton.imagestorage.entity.Group;
import com.nfitton.imagestorage.entity.GroupCamera;
import com.nfitton.imagestorage.entity.UserGroup;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupData {
  private final Group group;
  private final List<UUID> userIds;
  private final List<UUID> cameraIds;

  public GroupData(Group group, List<UUID> userIds, List<UUID> cameraIds) {
    this.group = group;
    this.userIds = userIds;
    this.cameraIds = cameraIds;
  }

  public Group getGroup() {
    return group;
  }

  public List<UUID> getUserIds() {
    return userIds;
  }

  public List<UUID> getCameraIds() {
    return cameraIds;
  }

  public boolean isInGroup(UUID userId) {
    return userIds.contains(userId);
  }

  public static final class Builder {
    private Group group;
    private List<UUID> userIds;
    private List<UUID> cameraIds;

    private Builder() {

    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public Builder withGroup(Group var) {
      this.group = var;
      return this;
    }

    public Builder withUserGroups(List<UserGroup> var) {
      this.userIds = var.stream().map(UserGroup::getUserId).collect(Collectors.toList());
      return this;
    }

    public Builder withGroupCameras(List<GroupCamera> var) {
      this.cameraIds = var.stream().map(GroupCamera::getCameraId).collect(Collectors.toList());
      return this;
    }

    public GroupData build() {
      return new GroupData(group, userIds, cameraIds);
    }
  }
}
