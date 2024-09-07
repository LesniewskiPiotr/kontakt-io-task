package com.example.kontaktiotask.controller;

import com.example.kontaktiotask.model.command.CreateGroupCommand;
import com.example.kontaktiotask.model.dto.AssetDTO;
import com.example.kontaktiotask.model.dto.GroupDTO;
import com.example.kontaktiotask.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @GetMapping
    public List<GroupDTO> findAll() {
        return groupService.findAll()
                .stream()
                .map(GroupDTO::fromEntity)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDTO create(@RequestBody CreateGroupCommand command) {
        return GroupDTO.fromEntity(groupService.create(command));
    }

    @GetMapping("/{id}/assets")
    public List<AssetDTO> findGroupAssets(@PathVariable Long id) {
        return groupService.findGroupAssets(id).
                stream()
                .map(AssetDTO::fromEntity)
                .toList();
    }

    @PostMapping("/{groupId}/assets/{assetId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAsset(@PathVariable Long groupId, @PathVariable Long assetId) {
        groupService.addAsset(groupId, assetId);
    }

    @DeleteMapping("/{groupId}/assets/{assetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAsset(@PathVariable Long groupId, @PathVariable Long assetId) {
        groupService.removeAsset(groupId, assetId);
    }

}
