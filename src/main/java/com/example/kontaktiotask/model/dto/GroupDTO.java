package com.example.kontaktiotask.model.dto;

import com.example.kontaktiotask.model.Group;

public record GroupDTO(String name, String description) {
    public static GroupDTO fromEntity(Group group) {
        return new GroupDTO(group.getName(), group.getDescription());
    }
}
