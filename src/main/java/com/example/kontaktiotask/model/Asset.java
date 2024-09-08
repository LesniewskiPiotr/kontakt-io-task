package com.example.kontaktiotask.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "groups")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Asset name cannot be empty")
    private String name;

    @NotEmpty(message = "Asset type cannot be empty")
    private String type;

    private String description;

    @Version
    private int version;

    @ManyToMany(mappedBy = "assets", fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();

    @PreRemove
    private void removeGroupAssociations() {
        this.groups.forEach(group -> group.getAssets().remove(this));
    }
}
