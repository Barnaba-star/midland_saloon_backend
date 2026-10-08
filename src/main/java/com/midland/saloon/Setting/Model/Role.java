package com.midland.saloon.Setting.Model;

import com.midland.saloon.Uaa.Model.Permission;
import com.midland.saloon.Utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;

@Entity
// A change here may change who a signed-in user is - see PrincipalCache.
@jakarta.persistence.EntityListeners(com.midland.saloon.Config.Security.PrincipalCache.Evict.class)
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

    // Batched so loading a user's roles costs one extra select for all of them
    // together, not one per role.
    @ManyToMany(fetch = FetchType.EAGER)
    @BatchSize(size = 100)
    @JoinTable(
            name = "role_permission",
            joinColumns=@JoinColumn(name = "role_uid"),
            inverseJoinColumns = @JoinColumn(name = "permission_uid"),
            // No index before: loading a role's permissions scanned the table.
            indexes = @Index(name = "idx_role_permission_role", columnList = "role_uid")
    )
    private List<Permission> permission;

}
