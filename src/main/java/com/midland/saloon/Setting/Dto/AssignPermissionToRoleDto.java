package com.midland.saloon.Setting.Dto;

import com.midland.saloon.Uaa.Model.Permission;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignPermissionToRoleDto {
    private String roleUID;
    private List<Permission> permissions;
}
