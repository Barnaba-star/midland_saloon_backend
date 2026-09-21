package com.midland.saloon.Saloon.Service;

import com.midland.saloon.Saloon.Projection.CommissionTotalProjection;
import com.midland.saloon.Saloon.Projection.SaloonProjection;
import com.midland.saloon.Utils.Responses.ResponsePage;

public class ServiceAndStoreReportResponse {

    private ResponsePage<SaloonProjection> data;
    private CommissionTotalProjection commissionTotals;

    public ServiceAndStoreReportResponse(
            ResponsePage<SaloonProjection> data,
            CommissionTotalProjection commissionTotals
    ) {
        this.data = data;
        this.commissionTotals = commissionTotals;
    }

    public ResponsePage<SaloonProjection> getData() {
        return data;
    }

    public CommissionTotalProjection getCommissionTotals() {
        return commissionTotals;
    }
}
