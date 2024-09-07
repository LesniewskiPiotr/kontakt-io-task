package com.example.kontaktiotask.unit;

import com.example.kontaktiotask.exception.GroupServiceException;
import com.example.kontaktiotask.model.Asset;
import com.example.kontaktiotask.model.Group;
import com.example.kontaktiotask.model.command.CreateGroupCommand;
import com.example.kontaktiotask.repository.GroupRepository;
import com.example.kontaktiotask.service.AssetService;
import com.example.kontaktiotask.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private GroupService groupService;

    private Group group;
    private Asset asset;
    private CreateGroupCommand createGroupCommand;

    private ArgumentCaptor<Group> groupCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        groupCaptor = ArgumentCaptor.forClass(Group.class);

        group = Group.builder()
                .id(1L)
                .name("Test Group")
                .description("Test Description")
                .assets(new HashSet<>())
                .build();

        asset = Asset.builder()
                .id(1L)
                .name("Test Asset")
                .groups(new HashSet<>())
                .build();

        createGroupCommand = new CreateGroupCommand("New Group", "Group Description");
    }

    @Test
    void shouldCreateGroupSuccessfully() {
        //given
        when(groupRepository.saveAndFlush(any(Group.class))).thenReturn(group);

        //when
        groupService.create(createGroupCommand);

        //then
        verify(groupRepository, times(1)).saveAndFlush(groupCaptor.capture());
        Group capturedGroup = groupCaptor.getValue();

        assertEquals("New Group", capturedGroup.getName());
        assertEquals("Group Description", capturedGroup.getDescription());
    }

    @Test
    void shouldFindAllGroupsSuccessfully() {
        //given
        when(groupRepository.findAll()).thenReturn(List.of(group));

        //when
        var groups = groupService.findAll();

        //then
        assertEquals(1, groups.size());
        verify(groupRepository, times(1)).findAll();
    }

    @Test
    void shouldAddAssetToGroupSuccessfully() {
        //given
        when(groupRepository.findByIdWithAssets(1L)).thenReturn(Optional.of(group));
        when(assetService.findByIdWithGroups(1L)).thenReturn(asset);

        //when
        groupService.addAsset(1L, 1L);

        //then
        verify(groupRepository, times(1)).saveAndFlush(groupCaptor.capture());
        Group capturedGroup = groupCaptor.getValue();

        assertTrue(capturedGroup.getAssets().contains(asset));
        assertTrue(asset.getGroups().contains(capturedGroup));
    }

    @Test
    void shouldThrowConflictWhenAssetAlreadyInGroup() {
        //given
        group.getAssets().add(asset);
        when(groupRepository.findByIdWithAssets(1L)).thenReturn(Optional.of(group));
        when(assetService.findByIdWithGroups(1L)).thenReturn(asset);

        //when
        GroupServiceException exception = assertThrows(GroupServiceException.class, () -> {
            groupService.addAsset(1L, 1L);
        });

        //then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Asset with id 1 already exists in assets", exception.getReason());
    }

    @Test
    void shouldRemoveAssetFromGroupSuccessfully() {
        //given
        group.getAssets().add(asset);
        asset.getGroups().add(group);
        when(groupRepository.findByIdWithAssets(1L)).thenReturn(Optional.of(group));
        when(assetService.findByIdWithGroups(1L)).thenReturn(asset);

        //when
        groupService.removeAsset(1L, 1L);

        //then
        verify(groupRepository, times(1)).saveAndFlush(groupCaptor.capture());
        Group capturedGroup = groupCaptor.getValue();

        assertFalse(capturedGroup.getAssets().contains(asset));
        assertFalse(asset.getGroups().contains(capturedGroup));
    }

    @Test
    void shouldThrowNotFoundWhenRemovingNonExistingAssetFromGroup() {
        //given
        when(groupRepository.findByIdWithAssets(1L)).thenReturn(Optional.of(group));
        when(assetService.findByIdWithGroups(1L)).thenReturn(asset);

        //when
        GroupServiceException exception = assertThrows(GroupServiceException.class, () -> {
            groupService.removeAsset(1L, 1L);
        });

        //then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Asset with id 1 not found in assets", exception.getReason());
    }

    @Test
    void shouldThrowNotFoundWhenGroupDoesNotExist() {
        //given
        when(groupRepository.findByIdWithAssets(1L)).thenReturn(Optional.empty());

        //when
        GroupServiceException exception = assertThrows(GroupServiceException.class, () -> {
            groupService.addAsset(1L, 1L);
        });

        //then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Group with id 1 not found", exception.getReason());
    }
}
