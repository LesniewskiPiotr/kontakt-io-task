package com.example.kontaktiotask.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@Table(name = "groups")
@EqualsAndHashCode(exclude = "assets")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Group name cannot be empty")
    private String name;
    private String description;

    @Version
    private int version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "asset_group",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id"))
    private Set<Asset> assets = new HashSet<>();

}
