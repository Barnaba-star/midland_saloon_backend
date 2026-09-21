package com.midland.saloon.Uaa.Projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface UserProjection {

    String getUid();

    String getUsername();

    String getFirstName();

    String getMiddleName();

    String getLastName();

    String getEmail();

    String getPhone();

    String getGender();

    LocalDate getDateOfBirth();

    String getAddress();

    Boolean getIsRoot();

    Boolean getIsBlocked();

    Boolean getIsActive();

    LocalDate getCreatedAt();

    LocalDate getUpdatedAt();

    String getBranchUid();

    String getBranchName();

    String getBranchCode();

    String getRoleName();

    String getRoleUID();

    String getRoleCode();

    LocalDateTime getLastSeen();

}
