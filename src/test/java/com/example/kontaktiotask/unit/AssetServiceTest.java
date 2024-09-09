package com.example.kontaktiotask.unit;

import com.example.kontaktiotask.exception.AssetServiceException;
import com.example.kontaktiotask.model.Asset;
import com.example.kontaktiotask.model.command.CreateAssetCommand;
import com.example.kontaktiotask.model.command.UpdateAssetCommand;
import com.example.kontaktiotask.repository.AssetRepository;
import com.example.kontaktiotask.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset asset;
    private CreateAssetCommand createAssetCommand;
    private UpdateAssetCommand updateAssetCommand;

    private ArgumentCaptor<Asset> assetCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        assetCaptor = ArgumentCaptor.forClass(Asset.class);

        asset = Asset.builder()
                .id(1L)
                .name("Test Asset")
                .type("Test Type")
                .description("Test Description")
                .groups(Set.of())
                .build();

        createAssetCommand = new CreateAssetCommand("New Asset", "New Type", "New Description");
        updateAssetCommand = new UpdateAssetCommand("Updated Asset", "Updated Type", "Updated Description", 1);
    }

    @Test
    void shouldCreateAssetSuccessfully() {
        //given
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);

        //when
        assetService.create(createAssetCommand);

        //then
        verify(assetRepository, times(1)).save(assetCaptor.capture());
        Asset capturedAsset = assetCaptor.getValue();

        assertEquals("New Asset", capturedAsset.getName());
        assertEquals("New Type", capturedAsset.getType());
        assertEquals("New Description", capturedAsset.getDescription());
    }

    @Test
    void shouldUpdateAssetSuccessfully() {
        //given
        when(assetRepository.findByIdWithLock(1L)).thenReturn(Optional.of(asset));
        when(assetRepository.saveAndFlush(any(Asset.class))).thenReturn(asset);

        //when
        assetService.update(1L, updateAssetCommand);

        //then
        verify(assetRepository, times(1)).saveAndFlush(assetCaptor.capture());
        Asset capturedUpdatedAsset = assetCaptor.getValue();

        assertEquals("Updated Asset", capturedUpdatedAsset.getName());
        assertEquals("Updated Type", capturedUpdatedAsset.getType());
        assertEquals("Updated Description", capturedUpdatedAsset.getDescription());
    }

    @Test
    void shouldFetchAllAssets() {
        //given
        when(assetRepository.findAll()).thenReturn(List.of(asset));

        //when
        List<Asset> assets = assetService.findAll();

        //then
        assertEquals(1, assets.size());
        verify(assetRepository, times(1)).findAll();
    }

    @Test
    void shouldFindAssetById() {
        //given
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        //when
        Asset foundAsset = assetService.findById(1L);

        //then
        assertEquals(asset.getId(), foundAsset.getId());
        assertEquals(asset.getName(), foundAsset.getName());
        verify(assetRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenAssetNotFoundById() {
        //given
        when(assetRepository.findById(1L)).thenReturn(Optional.empty());

        //when
        AssetServiceException exception = assertThrows(AssetServiceException.class, () -> {
            assetService.findById(1L);
        });

        //then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Asset with id 1 not found", exception.getReason());
    }

    @Test
    void shouldDeleteAssetById() {
        //given
        when(assetRepository.existsById(1L)).thenReturn(true);
        doNothing().when(assetRepository).deleteById(1L);

        //when
        assetService.deleteById(1L);

        //then
        verify(assetRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingAsset() {
        //given
        when(assetRepository.existsById(1L)).thenReturn(false);

        //when
        AssetServiceException exception = assertThrows(AssetServiceException.class, () -> {
            assetService.deleteById(1L);
        });

        //then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Asset with id 1 not found", exception.getReason());
    }

    @Test
    void shouldFindAssetByIdWithGroups() {
        //given
        when(assetRepository.findByIdWithGroups(1L)).thenReturn(Optional.of(asset));

        //when
        Asset foundAsset = assetService.findByIdWithGroups(1L);

        //then
        assertEquals(asset.getId(), foundAsset.getId());
        assertEquals(asset.getName(), foundAsset.getName());
        verify(assetRepository, times(1)).findByIdWithGroups(1L);
    }

    @Test
    void shouldThrowExceptionWhenFindingAssetWithGroupsNotFound() {
        //given
        when(assetRepository.findByIdWithGroups(1L)).thenReturn(Optional.empty());

        //when
        AssetServiceException exception = assertThrows(AssetServiceException.class, () -> {
            assetService.findByIdWithGroups(1L);
        });

        //then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Asset with id 1 not found", exception.getReason());
    }
}
