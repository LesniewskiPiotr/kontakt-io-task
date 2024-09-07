package com.example.kontaktiotask.integration;

import com.example.kontaktiotask.model.Asset;
import com.example.kontaktiotask.model.Group;
import com.example.kontaktiotask.model.command.CreateGroupCommand;
import com.example.kontaktiotask.repository.AssetRepository;
import com.example.kontaktiotask.repository.GroupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(TestContainersInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = TestContainersInitializer.class)
public class GroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Group group1;
    private Asset asset1;
    private CreateGroupCommand createGroupCommand;

    @BeforeEach
    void setup() {
        groupRepository.deleteAll();
        assetRepository.deleteAll();

        group1 = Group.builder().name("Test Group 1").description("Description 1").build();
        asset1 = Asset.builder().name("Test Asset 1").type("Type 1").build();

        createGroupCommand = new CreateGroupCommand("New Group", "New Group Description");
    }

    @Test
    void shouldReturnAllGroups() throws Exception {
        //given
        groupRepository.saveAllAndFlush(List.of(group1));

        //when
        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isOk());

        //then
        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Group 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"));
    }

    @Test
    void shouldCreateGroupSuccessfully() throws Exception {
        //given
        String jsonPayload = objectMapper.writeValueAsString(createGroupCommand);

        //when
        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());

        //then
        assertTrue(groupRepository.findAll().stream()
                .anyMatch(group -> group.getName().equals("New Group")));
    }

    @Test
    void shouldFailWhenCreatingGroupWithoutName() throws Exception {
        //given
        CreateGroupCommand invalidCommand = new CreateGroupCommand("", "Group without name");
        String jsonPayload = objectMapper.writeValueAsString(invalidCommand);

        //when
        mockMvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnGroupAssets() throws Exception {
        //given
        Group savedGroup = groupRepository.saveAndFlush(group1);

        //when
        mockMvc.perform(get("/api/v1/groups/" + savedGroup.getId() + "/assets"))
                .andExpect(status().isOk());

        //then
        mockMvc.perform(get("/api/v1/groups/" + savedGroup.getId() + "/assets"))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldAddAssetToGroup() throws Exception {
        //given
        Group savedGroup = groupRepository.saveAndFlush(group1);
        Asset savedAsset = assetRepository.saveAndFlush(asset1);

        //when
        mockMvc.perform(post("/api/v1/groups/" + savedGroup.getId() + "/assets/" + savedAsset.getId()))
                .andExpect(status().isCreated());

        //then
        Group updatedGroup = groupRepository.findByIdWithAssets(savedGroup.getId()).orElseThrow();
        assertTrue(updatedGroup.getAssets().contains(savedAsset));
    }

    @Test
    void shouldReturn404IfAddingAssetToNonExistingGroup() throws Exception {
        //given
        Asset savedAsset = assetRepository.saveAndFlush(asset1);

        //when
        mockMvc.perform(post("/api/v1/groups/9999/assets/" + savedAsset.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404IfAddingNonExistingAssetToGroup() throws Exception {
        //given
        Group savedGroup = groupRepository.saveAndFlush(group1);

        //when
        mockMvc.perform(post("/api/v1/groups/" + savedGroup.getId() + "/assets/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRemoveAssetFromGroup() throws Exception {
        //given
        Group savedGroup = groupRepository.saveAndFlush(group1);
        Asset savedAsset = assetRepository.saveAndFlush(asset1);

        mockMvc.perform(post("/api/v1/groups/" + savedGroup.getId() + "/assets/" + savedAsset.getId()))
                .andExpect(status().isCreated());

        //when
        mockMvc.perform(delete("/api/v1/groups/" + savedGroup.getId() + "/assets/" + savedAsset.getId()))
                .andExpect(status().isOk());

        //then
        Group updatedGroup = groupRepository.findByIdWithAssets(savedGroup.getId()).orElseThrow();
        assertTrue(updatedGroup.getAssets().isEmpty());
    }

    @Test
    void shouldReturn404IfRemovingAssetFromNonExistingGroup() throws Exception {
        //given
        Asset savedAsset = assetRepository.saveAndFlush(asset1);

        //when
        mockMvc.perform(delete("/api/v1/groups/9999/assets/" + savedAsset.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404IfRemovingNonExistingAssetFromGroup() throws Exception {
        //given
        Group savedGroup = groupRepository.saveAndFlush(group1);

        //when
        mockMvc.perform(delete("/api/v1/groups/" + savedGroup.getId() + "/assets/9999"))
                .andExpect(status().isNotFound());
    }
}
