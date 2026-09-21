package com.midland.saloon.Uaa.Model;

import com.midland.saloon.Utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.stream.Stream;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "group_name")
    private String group;

    @Column(name = "module")
    private String module;

}
