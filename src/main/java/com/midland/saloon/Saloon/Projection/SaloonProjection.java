package com.midland.saloon.Saloon.Projection;

import com.midland.saloon.Saloon.Model.SaloonSales;
import com.midland.saloon.Saloon.Model.SaloonServiceEntity;
import com.midland.saloon.Saloon.Model.SaloonStaff;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SaloonProjection {

    /*
     * ==========================================================
     * SERVICE PROJECTION
     * Used to retrieve Saloon Service information.
     * ==========================================================
     */

    String getServiceName();

    String getServiceCode();

    String getDescription();

    Integer getPrice();

    Integer getDuration();

    Integer getCommissionValue();

    String getCommissionType();

    String getStatus();

    String getUid();
    String getUsageType();

    LocalDate getSalesTime();

    LocalDate getWeekDate();

    /*
     * ==========================================================
     * PAYMENT METHOD PROJECTION
     * Used to retrieve Payment Method information.
     * ==========================================================
     */
    String getPaymentName();

    Integer getPaymentCode();


    /*
     * ==========================================================
     * STAFF METHOD PROJECTION
     * Used to retrieve Staff Method information.
     * ==========================================================
     */

    String getFirstName();
    String getMiddleName();
    String getLastName();
    LocalDate getDateOfBirth();
    String getPhoneNumber();
    String getGender();
    String getSaloonCategory();
    Boolean getActive();

    /*
     * ==========================================================
     * SERVICES BOOKED METHOD PROJECTION
     * Used to retrieve Staff Method information.
     * ==========================================================
     */

    String getCustomerName();
    BigDecimal getTotalAmount();
    BigDecimal getPaidAmount();
    BigDecimal getRemainingAmount();
    LocalDate getBookingDate();
    String getServiceUID();


    /*
     * ==========================================================
     * REPORTS METHOD PROJECTION
     * Used to retrieve Staff Method information.
     * ==========================================================
     */
    Integer getTraAmount();
    Integer getEmergencyAmount();
    Integer getOthersAmount();
    Integer getOwnerAmount();
    Integer getStaffAmount();
    Integer getMaintenanceAmount();
    String getPaymentMethod();
    Integer getLoanAmount();
    Integer getRentAmount();
    Integer getWaterAmount();
    Integer getLukuAmount();
    Integer getStockPurchaseAmount();
    Integer getSharedAmount();

    /*
     * ==========================================================
     * SALES METHOD PROJECTION
     * Used to retrieve Payment Method information.
     * ==========================================================
     */
     String getSalesCode();


     String getNameOfStore();

     String getCodeOfStore();

     Integer getQuantity();

     String getSaloonServiceEntityUID();

    /*
     * ==========================================================
     * STORE METHOD PROJECTION
     * Used to retrieve Store Method information.
     * ==========================================================
     */
    LocalDate getClosedDate();
    LocalDate getOpenedDate();
    Integer getBuyingPrice();
    Integer getTotalQuantityPrice();
    Integer getUsedQuantity();
    Integer getNotUsedQuantity();
    String getOpenStoreCode();
    Integer getOpenQuantity();


    String getStaffCommissionUID();
    LocalDate getDate();

}
