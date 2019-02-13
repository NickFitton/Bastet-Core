package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.GroupV1;
import com.nfitton.imagestorage.entity.Group;
import com.nfitton.imagestorage.model.GroupData;
import com.nfitton.imagestorage.util.ValidationUtil;
import java.util.UUID;
import javax.validation.Validator;

public class GroupMapper {

  public static GroupData newGroup(GroupV1 v1, UUID owner, Validator validator) {
    ValidationUtil.validate(v1, validator);

    Group newGroup = new Group(null, owner, v1.getName());
    return GroupData.Builder
        .newBuilder()
        .withGroup(newGroup)
        .build();
  }

  public static GroupV1 toV1(GroupData groupData) {
    final Group group = groupData.getGroup();
    return new GroupV1(
        group.getId(), group.getOwnerId(), group.getName(), groupData.getUserIds(),
        groupData.getCameraIds());
  }

  public static GroupV1 toV1(Group group) {
    return new GroupV1(group.getId(), group.getOwnerId(), group.getName(), null, null);
  }
}
