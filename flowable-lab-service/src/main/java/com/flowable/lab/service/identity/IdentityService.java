package com.flowable.lab.service.identity;

import org.flowable.idm.api.Group;
import org.flowable.idm.api.IdmIdentityService;
import org.flowable.idm.api.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IdentityService {

    private final IdmIdentityService idmIdentityService;

    public IdentityService(IdmIdentityService idmIdentityService) {
        this.idmIdentityService = idmIdentityService;
    }

    public User createUser(String id, String firstName, String lastName, String email) {
        User user = idmIdentityService.newUser(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        idmIdentityService.saveUser(user);
        return user;
    }

    public void deleteUser(String userId) {
        idmIdentityService.deleteUser(userId);
    }

    public User getUser(String userId) {
        return idmIdentityService.createUserQuery().userId(userId).singleResult();
    }

    public List<User> listUsers() {
        return idmIdentityService.createUserQuery().list();
    }

    public Group createGroup(String id, String name, String type) {
        Group group = idmIdentityService.newGroup(id);
        group.setName(name);
        group.setType(type);
        idmIdentityService.saveGroup(group);
        return group;
    }

    public void deleteGroup(String groupId) {
        idmIdentityService.deleteGroup(groupId);
    }

    public Group getGroup(String groupId) {
        return idmIdentityService.createGroupQuery().groupId(groupId).singleResult();
    }

    public List<Group> listGroups() {
        return idmIdentityService.createGroupQuery().list();
    }

    public void addMembership(String userId, String groupId) {
        idmIdentityService.createMembership(userId, groupId);
    }

    public void removeMembership(String userId, String groupId) {
        idmIdentityService.deleteMembership(userId, groupId);
    }

    public List<User> getUsersInGroup(String groupId) {
        return idmIdentityService.createUserQuery().memberOfGroup(groupId).list();
    }
}
