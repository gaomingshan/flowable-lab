package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.GroupDTO;
import com.flowable.lab.common.dto.UserDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.IdentityMapper;
import com.flowable.lab.service.identity.IdentityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityService identityService;
    private final IdentityMapper mapper;

    public IdentityController(IdentityService identityService, IdentityMapper mapper) {
        this.identityService = identityService;
        this.mapper = mapper;
    }

    @GetMapping("/user")
    public Result<List<UserDTO>> listUsers() {
        return Result.success(mapper.toUserDtoList(identityService.listUsers()));
    }

    @PostMapping("/user")
    public Result<UserDTO> createUser(@RequestBody Map<String, String> body) {
        return Result.success(mapper.toUserDto(identityService.createUser(
                body.get("id"), body.get("firstName"), body.get("lastName"), body.get("email"))));
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable String id) {
        identityService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/group")
    public Result<List<GroupDTO>> listGroups() {
        return Result.success(mapper.toGroupDtoList(identityService.listGroups()));
    }

    @PostMapping("/group")
    public Result<GroupDTO> createGroup(@RequestBody Map<String, String> body) {
        return Result.success(mapper.toGroupDto(identityService.createGroup(
                body.get("id"), body.get("name"), body.get("type"))));
    }

    @DeleteMapping("/group/{id}")
    public Result<Void> deleteGroup(@PathVariable String id) {
        identityService.deleteGroup(id);
        return Result.success();
    }

    @PostMapping("/membership")
    public Result<Void> addMembership(@RequestBody Map<String, String> body) {
        identityService.addMembership(body.get("userId"), body.get("groupId"));
        return Result.success();
    }

    @DeleteMapping("/membership")
    public Result<Void> removeMembership(@RequestBody Map<String, String> body) {
        identityService.removeMembership(body.get("userId"), body.get("groupId"));
        return Result.success();
    }

    @GetMapping("/group/{id}/users")
    public Result<List<UserDTO>> getGroupUsers(@PathVariable String id) {
        return Result.success(mapper.toUserDtoList(identityService.getUsersInGroup(id)));
    }
}
