package com.example.kontaktiotask.service;

import com.example.kontaktiotask.exception.AssetServiceException;
import com.example.kontaktiotask.model.Asset;
import com.example.kontaktiotask.model.command.CreateAssetCommand;
import com.example.kontaktiotask.model.command.UpdateAssetCommand;
import com.example.kontaktiotask.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<Asset> findAll() {
        log.info("Fetching all assets");
        return assetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Asset findById(Long id) {
        log.info("Fetching asset with ID: {}", id);
        return assetRepository.findById(id)
                .orElseThrow(() -> new AssetServiceException(HttpStatus.NOT_FOUND, String.format("Asset with id %s not found", id)));
    }

    @Transactional
    public Asset create(@NonNull CreateAssetCommand command) {
        Asset asset = Asset.builder()
                .name(command.name())
                .type(command.type())
                .description(command.description())
                .groups(new HashSet<>())
                .build();

        log.info("Creating new asset: {}", asset.getName());
        return assetRepository.save(asset);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting asset with ID: {}", id);
        if (!existsById(id)) {
            log.warn("Asset with ID: {} not found", id);
            throw new AssetServiceException(HttpStatus.NOT_FOUND, String.format("Asset with id %s not found", id));
        }
        assetRepository.deleteById(id);
        log.info("Asset with ID: {} deleted successfully", id);
    }

    @Transactional
    public Asset update(Long id, @NonNull UpdateAssetCommand command) {
        log.info("Updating asset with ID: {}", id);
        Asset asset = assetRepository.findByIdWithLock(id)
                .orElseThrow(() -> new AssetServiceException(HttpStatus.NOT_FOUND, String.format("Asset with id %s not found", id)));

        Asset updatedAsset = Asset.builder()
                .id(asset.getId())
                .name(command.name())
                .type(command.type())
                .description(command.description())
                .groups(asset.getGroups())
                .build();

        log.info("Asset with ID: {} updated successfully", id);
        return assetRepository.saveAndFlush(updatedAsset);
    }

    public Asset findByIdWithGroups(Long id) {
        log.info("Fetching asset with groups for ID: {}", id);
        return assetRepository.findByIdWithGroups(id)
                .orElseThrow(() -> new AssetServiceException(HttpStatus.NOT_FOUND, String.format("Asset with id %s not found", id)));
    }

    private boolean existsById(Long id) {
        log.info("Checking if asset with ID: {} exists", id);
        return assetRepository.existsById(id);
    }
}
