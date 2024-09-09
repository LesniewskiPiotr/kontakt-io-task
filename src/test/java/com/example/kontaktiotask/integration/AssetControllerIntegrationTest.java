package com.example.kontaktiotask.integration;

import com.example.kontaktiotask.model.Asset;
import com.example.kontaktiotask.model.command.CreateAssetCommand;
import com.example.kontaktiotask.model.command.UpdateAssetCommand;
import com.example.kontaktiotask.repository.AssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AssetControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Asset asset1, asset2;
    private CreateAssetCommand createAssetCommand;

    @BeforeEach
    void setup() {
        assetRepository.deleteAll();

        asset1 = Asset.builder().name("Asset 1").type("Type 1").description("Description 1").build();
        asset2 = Asset.builder().name("Asset 2").type("Type 2").description("Description 2").build();

        createAssetCommand = new CreateAssetCommand("New Asset", "New Type", "New Description");
    }

    @Test
    void shouldReturnAllAssets() throws Exception {
        //given
        assetRepository.saveAllAndFlush(List.of(asset1, asset2));

        //when
        mockMvc.perform(get("/api/v1/assets"))
                .andExpect(status().isOk());

        //then
        mockMvc.perform(get("/api/v1/assets"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Asset 1"))
                .andExpect(jsonPath("$[1].name").value("Asset 2"));
    }

    @Test
    void shouldReturnAssetById() throws Exception {
        //given
        Asset savedAsset = assetRepository.saveAndFlush(asset1);

        //when
        mockMvc.perform(get("/api/v1/assets/" + savedAsset.getId()))
                .andExpect(status().isOk());

        //then
        mockMvc.perform(get("/api/v1/assets/" + savedAsset.getId()))
                .andExpect(jsonPath("$.name").value("Asset 1"))
                .andExpect(jsonPath("$.description").value("Description 1"));
    }

    @Test
    void shouldReturn404WhenAssetNotFound() throws Exception {
        //when
        mockMvc.perform(get("/api/v1/assets/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateAssetSuccessfully() throws Exception {
        //given
        String jsonPayload = objectMapper.writeValueAsString(createAssetCommand);

        //when
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());

        //then
        assertTrue(assetRepository.findAll().stream()
                .anyMatch(asset -> asset.getName().equals("New Asset")));
    }

    @Test
    void shouldFailWhenCreatingAssetWithoutName() throws Exception {
        //given
        CreateAssetCommand invalidCommand = new CreateAssetCommand("", "Type", "Description");
        String jsonPayload = objectMapper.writeValueAsString(invalidCommand);

        //when
        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteAssetSuccessfully() throws Exception {
        //given
        Asset savedAsset = assetRepository.saveAndFlush(asset1);

        //when
        mockMvc.perform(delete("/api/v1/assets/" + savedAsset.getId()))
                .andExpect(status().isNoContent());

        //then
        assertTrue(assetRepository.findById(savedAsset.getId()).isEmpty());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingAsset() throws Exception {
        //when
        mockMvc.perform(delete("/api/v1/assets/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateAssetSuccessfully() throws Exception {
        //given
        Asset savedAsset = assetRepository.saveAndFlush(asset1);
        UpdateAssetCommand updateCommand = new UpdateAssetCommand("Updated Name", "Updated Type", "Updated Description", savedAsset.getVersion());
        String jsonPayload = objectMapper.writeValueAsString(updateCommand);

        //when
        mockMvc.perform(put("/api/v1/assets/" + savedAsset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        //then
        Asset updatedAsset = assetRepository.findById(savedAsset.getId()).orElseThrow();
        assertEquals("Updated Name", updatedAsset.getName());
        assertEquals(1, updatedAsset.getVersion());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAsset() throws Exception {
        //given
        UpdateAssetCommand updateCommand = new UpdateAssetCommand("Updated Name", "Updated Type", "Updated Description", 1);
        String jsonPayload = objectMapper.writeValueAsString(updateCommand);

        //when
        mockMvc.perform(put("/api/v1/assets/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound());
    }
}
