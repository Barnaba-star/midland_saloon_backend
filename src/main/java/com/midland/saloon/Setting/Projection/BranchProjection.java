package com.midland.saloon.Setting.Projection;

import jakarta.persistence.Column;

public interface BranchProjection {
    String getUid();
    String getBranchCode();
    String getBranchCategory();
    String getName();

    String getCode();

    String getDescription();
    String getStatus();
    String getCategory();
}
