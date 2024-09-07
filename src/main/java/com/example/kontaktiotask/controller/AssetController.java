package com.example.kontaktiotask.controller;

import com.example.kontaktiotask.model.command.CreateAssetCommand;
import com.example.kontaktiotask.model.command.UpdateAssetCommand;
import com.example.kontaktiotask.model.dto.AssetDTO;
import com.example.kontaktiotask.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public List<AssetDTO> findAll() {
        return assetService.findAll()
                .stream()
                .map(AssetDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public AssetDTO findById(@PathVariable Long id) {
        return AssetDTO.fromEntity(assetService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetDTO create(@RequestBody CreateAssetCommand command) {
        return AssetDTO.fromEntity(assetService.create(command));
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        assetService.deleteById(id);
    }

    @PutMapping("/{id}")
    public AssetDTO update(@PathVariable Long id, @RequestBody UpdateAssetCommand command) {
        return AssetDTO.fromEntity(assetService.update(id, command));
    }

}
