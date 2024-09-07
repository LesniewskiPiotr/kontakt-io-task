package com.example.kontaktiotask.model.dto;

import com.example.kontaktiotask.model.Asset;

public record AssetDTO(String name, String type, String description) {
    public static AssetDTO fromEntity(Asset asset) {
        return new AssetDTO(asset.getName(), asset.getType(), asset.getDescription());
    }
}
