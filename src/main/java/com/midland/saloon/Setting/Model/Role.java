package com.midland.saloon.Setting.Model;

import com.midland.saloon.Uaa.Model.Permission;
import com.midland.saloon.Utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "category")
    private String category;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission",
            joinColumns=@JoinColumn(name = "role_uid"),
            inverseJoinColumns = @JoinColumn(name = "permission_uid")
    )
    private List<Permission> permission;

}
