package com.midland.saloon.Uaa.Model;

import com.midland.saloon.Utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.util.stream.Stream;

@Entity
// A change here may change who a signed-in user is - see PrincipalCache.
@jakarta.persistence.EntityListeners(com.midland.saloon.Config.Security.PrincipalCache.Evict.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "permissions", indexes = {
        // Seeding looks every permission up by name on each startup.
        @Index(name = "idx_permissions_name", columnList = "name")
})
public class Permission extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "group_name")
    private String group;

    @Column(name = "module")
    private String module;

}
