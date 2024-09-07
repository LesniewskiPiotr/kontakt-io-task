package com.example.kontaktiotask.service;

import com.example.kontaktiotask.exception.GroupServiceException;
import com.example.kontaktiotask.model.Asset;
import com.example.kontaktiotask.model.Group;
import com.example.kontaktiotask.model.command.CreateGroupCommand;
import com.example.kontaktiotask.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class GroupService {

    private final GroupRepository groupRepository;
    private final AssetService assetService;

    @Transactional(readOnly = true)
    public List<Group> findAll() {
        log.info("Fetching all groups");
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Asset> findGroupAssets(Long groupId) {
        log.info("Fetching assets for group with ID: {}", groupId);
        return groupRepository.findByIdWithAssets(groupId)
                .map(Group::getAssets)
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Transactional
    public Group create(@NonNull CreateGroupCommand command) {
        Group group = Group.builder()
                .name(command.name())
                .description(command.description())
                .build();
        log.info("Creating new group: {}", group.getName());
        return groupRepository.saveAndFlush(group);
    }

    @Transactional
    public void addAsset(Long groupId, Long assetId) {
        log.info("Adding asset with ID: {} to group with ID: {}", assetId, groupId);
        Group group = findByIdWithAssets(groupId);
        Asset asset = assetService.findByIdWithGroups(assetId);

        addAsset(group, asset);
        log.info("Asset added successfully to group");
        groupRepository.saveAndFlush(group);
    }

    @Transactional
    public void removeAsset(Long groupId, Long assetId) {
        log.info("Removing asset with ID: {} from group with ID: {}", assetId, groupId);
        Group group = findByIdWithAssets(groupId);
        Asset asset = assetService.findByIdWithGroups(assetId);

        removeAsset(group, asset);
        log.info("Asset removed successfully from group");
        groupRepository.saveAndFlush(group);
    }

    private Group findByIdWithAssets(Long id) {
        log.info("Fetching group with assets for ID: {}", id);
        return groupRepository.findByIdWithAssets(id)
                .orElseThrow(() -> new GroupServiceException(HttpStatus.NOT_FOUND, String.format("Group with id %s not found", id)));
    }

    private void addAsset(Group group, Asset asset) {
        if (group.getAssets().contains(asset)) {
            log.warn("Asset with ID: {} already exists in group", asset.getId());
            throw new GroupServiceException(HttpStatus.CONFLICT, String.format("Asset with id %s already exists in assets", asset.getId()));
        }
        group.getAssets().add(asset);
        asset.getGroups().add(group);
        log.info("Asset with ID: {} added to group", asset.getId());
    }

    private void removeAsset(Group group, Asset asset) {
        if (!group.getAssets().contains(asset)) {
            log.warn("Asset with ID: {} not found in group", asset.getId());
            throw new GroupServiceException(HttpStatus.NOT_FOUND, String.format("Asset with id %s not found in assets", asset.getId()));
        }
        group.getAssets().remove(asset);
        asset.getGroups().remove(group);
        log.info("Asset with ID: {} removed from group", asset.getId());
    }
}
