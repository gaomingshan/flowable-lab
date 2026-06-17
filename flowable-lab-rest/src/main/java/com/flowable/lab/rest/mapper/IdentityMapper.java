package com.flowable.lab.rest.mapper;

import com.flowable.lab.common.dto.GroupDTO;
import com.flowable.lab.common.dto.UserDTO;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IdentityMapper {

    UserDTO toUserDto(User user);

    @Mapping(target = "tenantId", constant = "")
    GroupDTO toGroupDto(Group group);

    List<UserDTO> toUserDtoList(List<? extends User> list);

    List<GroupDTO> toGroupDtoList(List<? extends Group> list);
}
