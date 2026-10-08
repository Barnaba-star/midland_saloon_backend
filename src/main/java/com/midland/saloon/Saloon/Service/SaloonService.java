package com.midland.saloon.Saloon.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Notification.Service.NotificationService;
import com.midland.saloon.Saloon.Dto.*;
import com.midland.saloon.Saloon.Model.*;
import com.midland.saloon.Saloon.Projection.*;
import com.midland.saloon.Saloon.Repository.*;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.midland.saloon.Utils.Exceptions.BusinessException;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import com.midland.saloon.Utils.Responses.ResponsePage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;


@Service
@Log
@RequiredArgsConstructor
public class SaloonService {

  /** The payment methods a payout may go by - the same as a bill's. */
  private static final java.util.Set<String> PAYOUT_METHODS = java.util.Set.of("cash", "mpesa", "tigopesa", "airtelmoney", "halopesa", "bank");

  /** A payout's method, lower-case; cash when none (or an unknown one) was given. */
  private static String payoutMethod(String method) {
      String m = method == null ? "" : method.trim().toLowerCase();
      return PAYOUT_METHODS.contains(m) ? m : "cash";
  }
  private final SaloonServiceRepository saloonServiceRepository;
  private final OtherCommissionService otherCommissionService;
  private final UserRepository userRepository;
  private final CommissionRepository commissionRepository;
  private final SaloonStaffRepository saloonStaffRepository;
  private final SaloonSalesRepository saloonSalesRepository;
  private final SaloonReportsRepository saloonReportsRepository;
  private final SalesOpenedRepository salesOpenedRepository;
  private final StaffCommissionsRepository staffCommissionsRepository;
  private final StoreRepository storeRepository;
  private final OpenStoreRepository openStoreRepository;
  private final ServiceAndStoreReportRepository serviceAndStoreReportRepository;
  private final IncomeExpensesRepository incomeExpensesRepository;
  private final IncomeExpensesDescriptionRepository incomeExpensesDescriptionRepository;
  private final StockAndPurchaseRepository stockAndPurchaseRepository;
  private final StockAndPurchaseDescriptionsRepository stockAndPurchaseDescriptionsRepository;
  private final NotificationService notificationService;

    /*
   SALOON SERVICE METHODS
    */
  public Response<SaloonServiceEntity> saveSaloonService(SaloonServiceDTO saloonServiceDTO){
      log.info(LoggerUser.getEmail() + "Is Saving Saloon Services");
      if(saloonServiceDTO == null)
          return new Response<>("Provide Data for Saloon Service");
      SaloonServiceEntity serviceEntity = null;
      if(saloonServiceDTO.getUid() != null){
          Optional<SaloonServiceEntity> optionalSaloonServiceEntity = saloonServiceRepository.findById(saloonServiceDTO.getUid());
          if(optionalSaloonServiceEntity.isEmpty())
              return new Response<>("Service Not Found");
          serviceEntity = optionalSaloonServiceEntity.get();
          serviceEntity.update();
      }else{
          serviceEntity = new SaloonServiceEntity();
      }
      serviceEntity.setServiceCode(saloonServiceDTO.getServiceCode());
      serviceEntity.setServiceName(saloonServiceDTO.getServiceName());
      serviceEntity.setDescription(saloonServiceDTO.getDescription());
      serviceEntity.setStatus(saloonServiceDTO.getStatus());
      serviceEntity.setPrice(saloonServiceDTO.getPrice());
      serviceEntity.setDuration(saloonServiceDTO.getDuration());
      serviceEntity.setUsageType(saloonServiceDTO.getUsageType());
      try {
          return new Response<>(saloonServiceRepository.save(serviceEntity));
      } catch (Exception e) {
          e.printStackTrace();
          return new Response<>("Error in Saving New Service");
      }
  }
  public Response<SaloonServiceEntity> findSaloonServiceByUID(String saloonServiceUID){
      log.info(LoggerUser.getEmail() + " Is Accessing Saloon Service");
      if(saloonServiceUID == null)
          return new Response<>("Provide Saloon Service Ref");
      Optional<SaloonServiceEntity> optionalSaloonServiceEntity =  saloonServiceRepository.findSaloonServiceByUID(saloonServiceUID, LoggerUser.getBranchUID());
      return  optionalSaloonServiceEntity.map(Response::new).orElseGet(()->new Response<>("Service Not Found"));
  }
  public Response<SaloonServiceEntity> deleteServiceSaloonByUID(String saloonServiceUID){
      log.info(LoggerUser.getEmail() + "Is Deleting Saloon Service");
      if(saloonServiceUID == null)
          return new Response<>("Provide Saloon service Ref");
      Optional<SaloonServiceEntity> optionalSaloonServiceEntity=saloonServiceRepository.findSaloonServiceByUID(saloonServiceUID,LoggerUser.getBranchUID());
      if (optionalSaloonServiceEntity.isEmpty())
          return new Response<>("Service Not Found");
      SaloonServiceEntity serviceEntity = optionalSaloonServiceEntity.get();
      try{
          saloonServiceRepository.delete(optionalSaloonServiceEntity.get());
          return new Response<>(serviceEntity);
      } catch (Exception e) {
          e.printStackTrace();
          return new Response<>("Error in Deleting Service");
      }
  }
  public ResponseList<SaloonProjection> findSaloonServiceList(){
      log.info(LoggerUser.getEmail() + "Is Accessing Saloon Service");
      try{
          return new ResponseList<>(saloonServiceRepository.findAllSaloonServiceList(LoggerUser.getBranchUID()));
      }catch (Exception e){
          e.printStackTrace();
          return new ResponseList<>("Error in Accessing Services");
      }
  }
  public ResponsePage<SaloonProjection> findSaloonServicePage(Integer page, Integer size){
        log.info(LoggerUser.getEmail() + " Is Accessing Saloon Services");
        Pageable pageable = PageRequest.of(page, size);
        try{
            Page<SaloonProjection> response = saloonServiceRepository.findSaloonServicePage(pageable, LoggerUser.getBranchUID());
            return new ResponsePage<>(response);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponsePage<>("Error in Accessing Saloon Service");
        }
  }
  public ResponsePage<User>  findUserPageByBranch(Integer page, Integer size){
      log.info(LoggerUser.getEmail() + " Is Accessing User by using Branch");
      Pageable pageable = PageRequest.of(page, size);
      return new ResponsePage<>(userRepository.findUserPageByBranch(pageable, LoggerUser.getBranchUID()));
  }

  /*
  SALOON COMMISSIONS METHODS
   */

    public Response<Commission> saveCommissions(CommissionDTO commissionDTO){
        log.info(LoggerUser.getEmail() + "Is Saving Saloon Commissions");
        if(commissionDTO == null)
            return new Response<>("Provide Commissions Data");
        Commission commission = null;
        if(commissionDTO.getUid() !=null){
            Optional<Commission> optionalCommission = commissionRepository.findCommissionByUID(commissionDTO.getUid(), LoggerUser.getBranchUID());
            if(optionalCommission.isEmpty())
                return new Response<>("Commission Not Found");
            commission = optionalCommission.get();
        }else{
            commission=new Commission();
        }
        if(commissionDTO.getSaloonServiceUID() != null) {
            Optional<SaloonServiceEntity> optionalSaloonServiceEntity = saloonServiceRepository.findById(commissionDTO.getSaloonServiceUID());
            if(optionalSaloonServiceEntity.isEmpty())
                return new Response<>("Service Not Found");
            commission.setSaloonService(optionalSaloonServiceEntity.get());
        }
        commission.setEmergencyPercent(commissionDTO.getEmergencyPercent());
        commission.setMaintenancePercent(commissionDTO.getMaintenancePercent());
        commission.setStaffPercent(commissionDTO.getStaffPercent());
        commission.setOwnerPercent(commissionDTO.getOwnerPercent());
        commission.setOtherPercent(commissionDTO.getOtherPercent());
        commission.setTraPercent(commissionDTO.getTraPercent());
        commission.setTotalPercent(commissionDTO.getTotalPercent());
        commission.setLoanPercent(commissionDTO.getLoanPercent());
        commission.setLukuPercent(commissionDTO.getLukuPercent());
        commission.setWaterPercent(commissionDTO.getWaterPercent());
        commission.setRentPercent(commissionDTO.getRentPercent());
        commission.setStockPurchasePercent(commissionDTO.getStockPurchasePercent());
        try{
            return new Response<>(commissionRepository.save(commission));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("Error in Saving new Commission");
        }

    }
    public ResponseList<CommissionProjection> findCommissionList(){
        log.info(LoggerUser.getEmail() + "Is accessing Commissions");
        return new ResponseList<>(commissionRepository.findCommissionList(LoggerUser.getBranchUID()));
    }
    public ResponsePage<CommissionProjection> findCommissionPage(Integer page, Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(commissionRepository.findCommissionPage(pageable, LoggerUser.getBranchUID()));
    }
    public Response<Commission> deleteCommission(String commissionUID){
        log.info(LoggerUser.getEmail() + "Is deleting Commission");
        if(commissionUID == null)
            return new Response<>("Provide Commission Ref UID");
        Optional<Commission> optionalCommission = commissionRepository.findCommissionByUID(commissionUID, LoggerUser.getBranchUID());
        if(optionalCommission.isEmpty())
            return new Response<>("Commission Not Found");
        Commission commission = optionalCommission.get();
        try{
            commissionRepository.delete(optionalCommission.get());
            return new Response<>(commission);
        } catch (Exception e) {
            return new Response<>("Error in Deleting Commission");
        }
    }
    public Response<Commission> findCommissionByUID(String commissionUID){
        log.info(LoggerUser.getEmail() + "Is Accessing Commission");
        if(commissionUID == null)
            return new Response<>("Provide Commission REF");
        Optional<Commission> optionalCommission=commissionRepository.findCommissionByUID(commissionUID, LoggerUser.getBranchUID());
        return optionalCommission.map(Response::new).orElseGet(()->new Response<>("Commission Not Found"));
    }

    /*
  SALOON STAFF METHODS
 */
    public Response<SaloonStaff> saveSaloonStaff(SaloonStaffDTO saloonStaffDTO){
        log.info(LoggerUser.getEmail() + "is Saving Staff");
        if(saloonStaffDTO ==null)
            return new Response<>("Provide Staff Data");
        SaloonStaff saloonStaff = null;
        if(saloonStaffDTO.getUid() != null){
            Optional<SaloonStaff> optionalSaloonStaff = saloonStaffRepository.findSaloonStaffByUID(saloonStaffDTO.getUid(), LoggerUser.getBranchUID());
            if(optionalSaloonStaff.isEmpty())
                return new Response<>("Service Not Found");
            saloonStaff = optionalSaloonStaff.get();
            saloonStaff.update();
        }else{
            saloonStaff = new SaloonStaff();
        }

        saloonStaff.setDateOfBirth(saloonStaffDTO.getDateOfBirth());
        saloonStaff.setFirstName(saloonStaffDTO.getFirstName());
        saloonStaff.setMiddleName(saloonStaffDTO.getMiddleName());
        saloonStaff.setLastName(saloonStaffDTO.getLastName());
        saloonStaff.setPhoneNumber(saloonStaffDTO.getPhoneNumber());
        saloonStaff.setSaloonCategory(saloonStaffDTO.getSaloonCategory());
        saloonStaff.setDescription(saloonStaffDTO.getDescription());
        saloonStaff.setGender(saloonStaffDTO.getGender());
        try{
            return new Response<>(saloonStaffRepository.save(saloonStaff));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("Error in Saving Staff");
        }

    }
    public Response<SaloonStaff> findSaloonStaffByUID(String saloonStaffUID){
        log.info(LoggerUser.getEmail() + "is accessing Staff");
        Optional<SaloonStaff> optionalSaloonStaff = saloonStaffRepository.findSaloonStaffByUID(saloonStaffUID, LoggerUser.getBranchUID());
        return optionalSaloonStaff.map(Response::new).orElseGet(()-> new Response<>("Staff Not Found"));
    }
    public ResponseList<SaloonProjection> findSaloonStaffList(){
        log.info(LoggerUser.getEmail() + "Is Accessing Saloon Staff");
        return new ResponseList<>(saloonStaffRepository.findSaloonStaffList(LoggerUser.getBranchUID()));
    }
    public ResponsePage<SaloonProjection> findSaloonStaffPage(Integer page, Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(saloonStaffRepository.findSaloonStaffPage(pageable, LoggerUser.getBranchUID()));
    }
    public Response<SaloonStaff> deleteSaloonStaff(String saloonStaffUID){
        log.info(LoggerUser.getEmail() + "Is Deleting Staff");
        Optional<SaloonStaff> optionalSaloonStaff = saloonStaffRepository.findSaloonStaffByUID(saloonStaffUID, LoggerUser.getBranchUID());
        if(optionalSaloonStaff.isEmpty())
            return new Response<>("Staff Not Found");
        SaloonStaff saloonStaff = optionalSaloonStaff.get();
        try{
            saloonStaffRepository.delete(optionalSaloonStaff.get());
            return new Response<>(saloonStaff);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("Error in Deleting Staff");
        }
    }


    /***
     SALOON_SALES_METHODS
     */
    @Transactional
    public ResponseList<SaloonSales> saveSaloonSales(SaloonSalesDTO saloonSalesDTO) {
        log.info("{} is saving saloon sales"+ LoggerUser.getEmail());
        if (saloonSalesDTO == null) {
            return new ResponseList<>("Weka Taarifa za Mauzo");
        }
        if (saloonSalesDTO.getSaloonStaffUID() == null || saloonSalesDTO.getSaloonStaffUID().isBlank()) {
            return new ResponseList<>("Weka Staff REF");
        }

        if (saloonSalesDTO.getSaloonServiceUID() == null || saloonSalesDTO.getSaloonServiceUID().isEmpty()) {
            return new ResponseList<>("Weka REF ya Hududma");
        }

        if (saloonSalesDTO.getSalesOpenedUID() == null || saloonSalesDTO.getSalesOpenedUID().isBlank()) {
            return new ResponseList<>("Provide Open Sale REF");
        }

        String branchUID = LoggerUser.getBranchUID();

        // ============================
        // FIND STAFF
        // ============================

        Optional<SaloonStaff> optionalSaloonStaff = saloonStaffRepository.findSaloonStaffByUID(saloonSalesDTO.getSaloonStaffUID(), branchUID);
        if (optionalSaloonStaff.isEmpty()) {
            log.info("Staff not found. Staff UID: {}, Branch: {}"+ saloonSalesDTO.getSaloonStaffUID());
            return new ResponseList<>("Staff Hapatikani");
        }

        // ============================
        // FIND OPEN SALE
        // ============================

        Optional<SalesOpened> optionalSalesOpened = salesOpenedRepository.findById(saloonSalesDTO.getSalesOpenedUID());
        if (optionalSalesOpened.isEmpty()) {
            log.info("Open sale not found. UID: {}"+ saloonSalesDTO.getSalesOpenedUID());
            return new ResponseList<>("Open Sale Not Found");
        }
        SaloonStaff staff = optionalSaloonStaff.get();
        SalesOpened salesOpened = optionalSalesOpened.get();

        // ============================
        // FIND SERVICES
        // ============================

        List<SaloonServiceEntity> saloonServiceEntities = saloonServiceRepository.findAllById(saloonSalesDTO.getSaloonServiceUID());

        if (saloonServiceEntities == null || saloonServiceEntities.isEmpty()) {
            log.info("Services not found. Requested services: {}"+ saloonSalesDTO.getSaloonServiceUID());
            return new ResponseList<>("Services Not Found");
        }
        log.info("Services found: {}"+ saloonServiceEntities.size());

        // ============================
        // CREATE SALES
        // ============================
        List<Integer> bills = new ArrayList<>();
        List<Integer> staffBill = new ArrayList<>();
        List<SaloonSales> sales = new ArrayList<>();
        List<Commission> commissions = new ArrayList<>();
        List<StockAndPurchase> stockAndPurchases = new ArrayList<>();
        Map<String, Commission> commissionsByService = loadCommissionsByService(saloonServiceEntities);
        for (SaloonServiceEntity service : saloonServiceEntities) {
            if (service == null) {log.info("Skipping null service");
                continue;
            }
            log.info("Preparing sale for service: {}, price: {}"+ service.getPrice());
            if (service.getPrice() == null) {
                log.info("Service price is null. Service: {}"+ service.getUid());
                return new ResponseList<>("Price Not Found For:  " + service.getServiceName());
            }


            Commission commission = commissionsByService.get(service.getUid());
            if(commission == null)
                return new ResponseList<>("No Commission Found ");
            staffBill.add(commission.getStaffPercent()* (service.getPrice()/100));
            bills.add(service.getPrice());
            SaloonSales sale = new SaloonSales();
            sale.setSoldAt(java.time.LocalDateTime.now());
            sale.setSaloonStaff(staff);
            sale.setSaloonServiceEntity(service);
            sale.setSalesOpened(salesOpened);
            commissions.add(commission);
            sales.add(sale);

        }
// ============================
// CALCULATE BILL
// ============================

        int currentBill = salesOpened.getBill() != null
                ? salesOpened.getBill()
                : 0;

        int totalBills = bills.stream()
                .mapToInt(Integer::intValue)
                .sum();

        salesOpened.setBill(currentBill + totalBills);

        if (sales.isEmpty()) {
            log.info("No sales to save");
            return new ResponseList<>("Hakuna Mauzo yoyote ya kusave");
        }

        // ============================
        // SAVE SALES
        // ============================

        try {
            log.info("Saving {} saloon sales..."+ sales.size());
            salesOpenedRepository.save(salesOpened);
            List<SaloonSales> savedSaloonSales = saloonSalesRepository.saveAll(sales);
            if (savedSaloonSales == null || savedSaloonSales.isEmpty()) {
                log.info("Mauzo hayaja Hifadhiwa");

                throw new BusinessException("Sales were not saved");

            }


            log.info(
                    "SALES SAVED SUCCESSFULLY. Count: {}"+
                    savedSaloonSales.size()
            );

            // ============================
            // CREATE REPORTS
            // ============================

            log.info(
                    "Starting report creation for {} sales..."+
                    savedSaloonSales.size()
            );

            ResponseList<SaloonReports> reportsResponse = saveSaloonReport(savedSaloonSales, staff, commissionsByService);

            // ============================
            // CHECK REPORT RESPONSE
            // ============================

            if (reportsResponse == null) {

                log.info(
                        "Report response is NULL"
                );

                throw new BusinessException(
                        "Failed to create saloon reports"
                );
            }

            if (reportsResponse.getMessage() != null &&
                    !reportsResponse.getMessage().isBlank()) {

                log.info(
                        "Report creation failed: {}"+
                        reportsResponse.getMessage()
                );
                throw new BusinessException(reportsResponse.getMessage());
            }

            log.info(
                    "SALON SALES AND REPORTS SAVED SUCCESSFULLY"
            );
            ResponseList<ServiceAndStoreReport> serviceAndStoreReports = saveServiceAndStoreReport(reportsResponse);
            if(serviceAndStoreReports.getMessage() !=null)
                throw new BusinessException(serviceAndStoreReports.getMessage());
            if(serviceAndStoreReports.getData() == null || serviceAndStoreReports.getData().isEmpty())
                throw new BusinessException("Error in Serving Service and Report");

            StaffCommissions staffCommissions = addStaffCommission(saloonSalesDTO.getSaloonStaffUID(), LoggerUser.getBranchUID(), LocalDate.now(), staffBill);
            ResponseList<IncomeExpenses> savedIncome = saveIncomeAndExpenses(commissions);

            if (savedIncome.getData() == null ||
                    savedIncome.getData().isEmpty()) {

                throw new BusinessException(
                        "Error in saving Income and Expenses"
                );
            }
            ResponseList<StockAndPurchase> andPurchaseResponseList = saveStockAndPurchase(saloonServiceEntities, commissionsByService);
            if (andPurchaseResponseList.getData() == null ||
                    andPurchaseResponseList.getData().isEmpty()) {

                throw new BusinessException(
                        "Error in saving Stock And Purchase"
                );
            }

            return new ResponseList<>(savedSaloonSales);

        } catch (Exception e) {

            log.info(
                    "Error while saving saloon sales and reports"+
                    e
            );

            throw e;
        }
    }
    public Response<SaloonProjection> findSaloonSalesByUID(String saloonSalesUID){
        log.info(LoggerUser.getEmail() + "Is accessing sales");
        if(saloonSalesUID == null)
            return new Response<>("Provide sales REF");
        Optional<SaloonProjection> optionalSaloonProjection = saloonSalesRepository.findSaloonSalesByUID(saloonSalesUID, LoggerUser.getBranchUID());
        return optionalSaloonProjection.map(Response::new).orElseGet(()->new Response<>("Sales Not Found"));
    }
    public ResponseList<SaloonProjection> findSaloonSalesList(String saloonOpenUID){
        log.info(LoggerUser.getEmail() + "Is accessing Sales");
        return new ResponseList<>(saloonSalesRepository.findSaloonSalesList(LoggerUser.getBranchUID(), saloonOpenUID));
    }
    public ResponseList<SaloonProjection> findSaloonSalesListActiveTrue(){
        log.info(LoggerUser.getEmail() + "Is accessing Sales");
        return new ResponseList<>(saloonSalesRepository.findSaloonSalesListActiveTrue(LoggerUser.getBranchUID(), LocalDate.now()));
    }
    public ResponsePage<SaloonProjection> findSaloonSalesPage(Integer page, Integer size){
        log.info(LoggerUser.getEmail() + "is Accessing Saloon Sales");
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(saloonSalesRepository.findSaloonSalesPage(pageable, LoggerUser.getBranchUID()));
    }
    public Response<SaloonSales> deleteSaloonSales(String saloonSalesUID){
        log.info(LoggerUser.getEmail() + "Is Deleting Sales");
        Optional<SaloonSales> optionalSaloonSales = saloonSalesRepository.findSalesByUID(saloonSalesUID, LoggerUser.getBranchUID());
        if(optionalSaloonSales.isEmpty())
            return new Response<>("Sales Not Found");
        SaloonSales sales=optionalSaloonSales.get();
        try{
            saloonSalesRepository.delete(optionalSaloonSales.get());
            return new Response<>(sales);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("Error in deleting sales");
        }
    }
    /**
     * Removes a bill opened by mistake - only while nothing is on it, so no sale or
     * money goes with it. Cancelled and soft-deleted rather than erased.
     */
    @org.springframework.transaction.annotation.Transactional
    public Response<SalesOpened> deleteEmptyBill(String billUid) {
        SalesOpened bill = salesOpenedRepository.findById(billUid)
                .filter(b -> LoggerUser.getBranchUID() != null && LoggerUser.getBranchUID().equals(b.getBranchUid()))
                .filter(b -> b.getIsActive() == null || b.getIsActive())
                .orElse(null);
        if (bill == null || !"PENDING".equals(bill.getPaymentStatus()))
            return new Response<>("That bill is not open any more");
        if (saloonSalesRepository.countLines(billUid) > 0 || (bill.getBill() != null && bill.getBill() > 0))
            return new Response<>("Only an empty bill can be deleted - bill " + bill.getSalesCode() + " has services on it");
        bill.setPaymentStatus("CANCELLED");
        bill.delete();
        return new Response<>(salesOpenedRepository.save(bill));
    }

    public Response<SalesOpened> saveOpenSale(SaleOpenedDTO saleOpenedDTO) {
        log.info(LoggerUser.getEmail() + " is Opening Sale");
        if (saleOpenedDTO == null) {
            return new Response<>("Provide Data For Opening new sale");
        }
        SalesOpened salesOpened;
        boolean wasAlreadyPaid = false;
        if (saleOpenedDTO.getUid() != null) {
            Optional<SalesOpened> optionalSalesOpened = salesOpenedRepository.findById(saleOpenedDTO.getUid());
            if (optionalSalesOpened.isEmpty()) {
                return new Response<>("Open Sale Not Found");
            }
            salesOpened = optionalSalesOpened.get();
            wasAlreadyPaid = "PAID".equals(salesOpened.getPaymentStatus());
            salesOpened.update();
        } else {
            salesOpened = new SalesOpened();
        }
        if(saleOpenedDTO.getPaymentMethod() != null)
            salesOpened.setPaymentMethod(saleOpenedDTO.getPaymentMethod());
        if(saleOpenedDTO.getPaidAmount() != null)
            salesOpened.setPaidAmount(saleOpenedDTO.getPaidAmount());
        if(saleOpenedDTO.getPaymentStatus()!=null)
            salesOpened.setPaymentStatus(saleOpenedDTO.getPaymentStatus());
        if(saleOpenedDTO.getSalesCode() != null)
            salesOpened.setSalesCode(saleOpenedDTO.getSalesCode());
        if(saleOpenedDTO.getStatus() != null)
            salesOpened.setStatus(saleOpenedDTO.getStatus());

        // Who took the money and when - a cash-up counts it in that cashier's takings.
        if (!wasAlreadyPaid && "PAID".equals(salesOpened.getPaymentStatus())) {
            salesOpened.setPaidBy(LoggerUser.getEmail());
            salesOpened.setPaidAt(java.time.LocalDateTime.now());
        }

        try {
            SalesOpened savedSale = salesOpenedRepository.save(salesOpened);

            // Notify the branch only the moment a sale first becomes
            // PAID — not on every subsequent edit of an already-paid sale.
            if (!wasAlreadyPaid && "PAID".equals(savedSale.getPaymentStatus())) {
                try {
                    notificationService.notifySaleCompleted(savedSale);
                } catch (Exception notifyError) {
                    log.warning("Failed to send sale-completed notification: " + notifyError.getMessage());
                }
            }

            return new Response<>(savedSale);
        } catch (Exception e) {
            return new Response<>(
                    "Error in Opening new sale: " + e.getMessage()
            );
        }
    }
    public ResponseList<SalesOpened> salesOpenedList(){
        log.info(LoggerUser.getEmail() + " Is Accessing opened Sales");
        return new ResponseList<>(salesOpenedRepository.salesOpenedList(LoggerUser.getBranchUID(), LocalDate.now()));
    }
    public ResponseList<SalesOpened> salesOpenedListByStatus(String filter) {

        log.info(LoggerUser.getEmail() + " is accessing Sales");

        log.info("Filter" + filter);
        LocalDate today = LocalDate.now();

        LocalDate startDate;
        LocalDate endDate;

        if (filter == null || filter.isBlank()) {

            // Default = leo
            startDate = today;
            endDate = today.plusDays(1);

        } else {

            switch (filter.toUpperCase()) {

                case "DAY":
                    // Leo
                    startDate = today;
                    endDate = today.plusDays(1);
                    break;
                case "YESTERDAY":
                    // Jana
                    startDate = today.minusDays(1);
                    endDate = today;
                    break;

                case "WEEK":
                    // Wiki hii
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    );
                    endDate = startDate.plusWeeks(1);
                    break;

                case "LAST_WEEK":
                    // Wiki iliyopita
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    ).minusWeeks(1);

                    endDate = startDate.plusWeeks(1);
                    break;

                case "MONTH":
                    // Mwezi huu
                    startDate = today.withDayOfMonth(1);
                    endDate = startDate.plusMonths(1);
                    break;

                case "LAST_MONTH":
                    // Mwezi uliopita
                    startDate = today.withDayOfMonth(1).minusMonths(1);
                    endDate = startDate.plusMonths(1);
                    break;

                case "THIS_YEAR":
                    // Mwaka huu
                    startDate = LocalDate.of(today.getYear(), 1, 1);
                    endDate = startDate.plusYears(1);
                    break;
                case "LAST_YEAR":
                    // Mwaka uliopita
                    startDate = today
                            .with(TemporalAdjusters.firstDayOfYear())
                            .minusYears(1);

                    endDate = today
                            .with(TemporalAdjusters.firstDayOfYear());

                    break;

                default:
                    try {
                        DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern("yyyy-MM-dd");

                        startDate = LocalDate.parse(filter, formatter);
                        endDate = startDate.plusDays(1);

                    } catch (DateTimeParseException e) {

                        throw new IllegalArgumentException(
                                "Filter must be DAY, WEEK, LAST_WEEK, MONTH, " +
                                        "LAST_MONTH, THIS_YEAR or date in format yyyy-MM-dd"
                        );
                    }
            }
        }

        return new ResponseList<>(
                salesOpenedRepository.salesOpenedListByStatus(
                        LoggerUser.getBranchUID(),
                        startDate,
                        endDate
                )
        );
    }
    private ResponseList<IncomeExpenses> saveIncomeAndExpenses(List<Commission> commissions) {

        log.info(
                "{} is serving income and expenses"+
                LoggerUser.getEmail()
        );

        if (commissions == null || commissions.isEmpty()) {
            throw new BusinessException("Empty Commissions");
        }

        List<IncomeExpenses> result = new ArrayList<>();
        // This week's pots per branch, read once and kept up to date here.
        // Each of the eleven buckets of every service on the sale used to look
        // its pot up with a query of its own (each forcing a flush first).
        Map<String, Map<String, IncomeExpenses>> pots = new HashMap<>();

        /*
         * Leo
         */
        LocalDate today = LocalDate.now();

        /*
         * Wiki inaanza Monday
         *
         * Mfano:
         * Sunday 13/09/2026
         *
         * weekStartDate:
         * Monday 07/09/2026
         */
        LocalDate weekStartDate = today.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY
                )
        );

        for (Commission commission : commissions) {

            if (commission == null) {
                continue;
            }

            /*
             * Hakikisha commission ina service
             */
            if (commission.getSaloonService() == null) {
                continue;
            }

            /*
             * Amount ya service
             */
            BigDecimal amount = BigDecimal.valueOf(
                    commission
                            .getSaloonService()
                            .getPrice()
            );

            /*
             * Branch
             */
            String branchUID = commission.getBranchUid();

            /*
             * =========================
             * STAFF
             * =========================
             */
            addIncomeExpense(
                    "Staff",
                    amount,
                    commission.getStaffPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * OWNER
             * =========================
             */
            addIncomeExpense(
                    "Owner",
                    amount,
                    commission.getOwnerPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * TRA
             * =========================
             */
            addIncomeExpense(
                    "TRA",
                    amount,
                    commission.getTraPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * EMERGENCY
             * =========================
             */
            addIncomeExpense(
                    "Emergency",
                    amount,
                    commission.getEmergencyPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * MAINTENANCE
             * =========================
             */
            addIncomeExpense(
                    "Maintenance",
                    amount,
                    commission.getMaintenancePercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * OTHER
             * =========================
             */
            addOtherIncome(amount, commission.getOtherPercent(), branchUID, weekStartDate, result, today, pots);

            /*
             * =========================
             * LUKU
             * =========================
             */
            addIncomeExpense(
                    "LUKU",
                    amount,
                    commission.getLukuPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * WATER
             * =========================
             */
            addIncomeExpense(
                    "Water",
                    amount,
                    commission.getWaterPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * RENT
             * =========================
             */
            addIncomeExpense(
                    "Rent",
                    amount,
                    commission.getRentPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * LOAN
             * =========================
             */
            addIncomeExpense(
                    "Loan",
                    amount,
                    commission.getLoanPercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );

            /*
             * =========================
             * STOCK PURCHASE
             * =========================
             */
            addIncomeExpense(
                    "Stock Purchase",
                    amount,
                    commission.getStockPurchasePercent(),
                    branchUID,
                    weekStartDate,
                    result,
                    pots
            );
        }

        return new ResponseList<>(
                result
        );
    }


    /**
     * The Other bucket. With the branch's own list set (POS Setting > Other),
     * its amount is shared across those items, each into a pot of its own;
     * without one it stays the single "Other" pot it always was.
     */
    private void addOtherIncome(BigDecimal amount, Integer percent, String branchUID, LocalDate weekStartDate, List<IncomeExpenses> result, LocalDate day,
                                Map<String, Map<String, IncomeExpenses>> pots) {
        if (percent == null || percent <= 0) {
            return;
        }
        BigDecimal other = amount.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
        java.util.Map<String, BigDecimal> parts = otherCommissionService.split(other, branchUID);
        if (parts.isEmpty()) {
            addIncomeExpense("Other", amount, percent, branchUID, weekStartDate, result, pots);
            return;
        }
        // Each share is already in shillings - taken at 100% it lands as it is.
        parts.forEach((pot, share) -> addIncomeExpense(pot, share, 100, branchUID, weekStartDate, result, pots));
        otherCommissionService.record(parts, day);
    }

    private void addIncomeExpense(String name, BigDecimal amount, Integer percent, String branchUID, LocalDate weekStartDate, List<IncomeExpenses> result,
                                  Map<String, Map<String, IncomeExpenses>> pots) {

        /*
         * Kama percentage haipo
         * au ni 0, hakuna cha kufanya.
         */
        if (percent == null || percent <= 0) {
            return;
        }

        /*
         * Calculate percentage.
         *
         * Mfano:
         *
         * amount = 100,000
         * percent = 20
         *
         * calculatedAmount = 20,000
         */
        BigDecimal calculatedAmount = amount
                .multiply(
                        BigDecimal.valueOf(percent)
                )
                .divide(
                        BigDecimal.valueOf(100)
                );

        /*
         * Search:
         *
         * name
         * branch
         * week
         */
        Map<String, IncomeExpenses> weekPots = pots.computeIfAbsent(
                branchUID + "|" + weekStartDate,
                key -> loadWeekPots(branchUID, weekStartDate)
        );
        Optional<IncomeExpenses> existing = Optional.ofNullable(weekPots.get(name));

        /*
         * ============================
         * RECORD IPO
         * ============================
         */
        if (existing.isPresent()) {

            IncomeExpenses incomeExpenses =
                    existing.get();

            /*
             * Existing income
             */
            BigDecimal currentIncome =
                    incomeExpenses.getIncome() == null
                            ? BigDecimal.ZERO
                            : incomeExpenses.getIncome();

            /*
             * Add new amount
             */
            incomeExpenses.setIncome(
                    currentIncome.add(
                            calculatedAmount
                    )
            );

            /*
             * Update description
             */
            incomeExpenses.setDescriptions(
                    name + " commission"
            );

            /*
             * Save update
             */
            IncomeExpenses updated =
                    incomeExpensesRepository.save(
                            incomeExpenses
                    );
            weekPots.put(name, updated);

            /*
             * Add kwenye response
             */
            result.add(updated);

        }

        /*
         * ============================
         * RECORD HAIPO
         * ============================
         */
        else {

            IncomeExpenses incomeExpenses =
                    new IncomeExpenses();

            /*
             * Name
             */
            incomeExpenses.setName(name);

            /*
             * Income
             */
            incomeExpenses.setIncome(
                    calculatedAmount
            );

            /*
             * Expenses
             */
            incomeExpenses.setExpenses(
                    BigDecimal.ZERO
            );

            /*
             * Description
             */
            incomeExpenses.setDescriptions(
                    name + " commission"
            );

            /*
             * Week
             */
            incomeExpenses.setWeekStartDate(
                    weekStartDate
            );

            /*
             * Branch
             */
            incomeExpenses.setBranchUid(
                    branchUID
            );

            /*
             * Save
             */
            IncomeExpenses saved =
                    incomeExpensesRepository.save(
                            incomeExpenses
                    );
            weekPots.put(name, saved);

            /*
             * Add kwenye response
             */
            result.add(saved);
        }
    }
    /** A branch's pots for one week, by name - what findByNameAndWeek answered one name at a time. */
    private Map<String, IncomeExpenses> loadWeekPots(String branchUID, LocalDate weekStartDate) {
        Map<String, IncomeExpenses> byName = new HashMap<>();
        for (IncomeExpenses pot : incomeExpensesRepository.findByBranchAndWeek(branchUID, weekStartDate)) {
            byName.putIfAbsent(pot.getName(), pot);
        }
        return byName;
    }
    public ResponseList<IncomeExpenses> getIncomeExpenses(String filter) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        switch (filter.toUpperCase()) {

            case "THIS_WEEK":

                startDate = today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );

                endDate = startDate.plusDays(6);

                break;


            case "LAST_WEEK":

                startDate = today
                        .with(
                                TemporalAdjusters.previousOrSame(
                                        DayOfWeek.MONDAY
                                )
                        )
                        .minusWeeks(1);

                endDate = startDate.plusDays(6);

                break;


            case "THIS_MONTH":

                startDate = today.with(
                        TemporalAdjusters.firstDayOfMonth()
                );

                endDate = today.with(
                        TemporalAdjusters.lastDayOfMonth()
                );

                break;


            case "LAST_MONTH":

                LocalDate lastMonth = today.minusMonths(1);

                startDate = lastMonth.with(
                        TemporalAdjusters.firstDayOfMonth()
                );

                endDate = lastMonth.with(
                        TemporalAdjusters.lastDayOfMonth()
                );

                break;


            case "THIS_YEAR":

                startDate = today.with(
                        TemporalAdjusters.firstDayOfYear()
                );

                endDate = today.with(
                        TemporalAdjusters.lastDayOfYear()
                );

                break;


            case "LAST_YEAR":

                LocalDate lastYear = today.minusYears(1);

                startDate = lastYear.with(
                        TemporalAdjusters.firstDayOfYear()
                );

                endDate = lastYear.with(
                        TemporalAdjusters.lastDayOfYear()
                );

                break;


            default:

                throw new BusinessException(
                        "Invalid income and expenses filter: " + filter
                );
        }

        return new ResponseList<>(incomeExpensesRepository.findByBranchAndWeekRange(
                LoggerUser.getBranchUID(),
                startDate,
                endDate));
    }
    @Transactional
    public Response<IncomeExpenses> addSpend(SpendDTO spendDTO) {

        log.info(LoggerUser.getEmail() + " is Saving Spend");

        if (spendDTO == null) {
            return new Response<>("Provide Spend Data");
        }

        if (spendDTO.getUid() == null) {
            return new Response<>("Provide Spend REF");
        }

        if (spendDTO.getAmount() == null || spendDTO.getAmount() <= 0) {
            return new Response<>("Provide valid Spend Amount");
        }

        if (spendDTO.getDescription() == null ||
                spendDTO.getDescription().trim().isEmpty()) {
            return new Response<>("Provide Spend Description");
        }

        if (spendDTO.getMethod() != null && !spendDTO.getMethod().isBlank() && !PAYOUT_METHODS.contains(spendDTO.getMethod().trim().toLowerCase())) {
            return new Response<>("Choose how it was paid");
        }


        // ==============================
        // FIND INCOME EXPENSE
        // ==============================

        // Only a pot of the user's own branch - a uid from another branch is not found.
        Optional<IncomeExpenses> optionalIncomeExpenses =
                incomeExpensesRepository.findById(spendDTO.getUid())
                        .filter(pot -> java.util.Objects.equals(pot.getBranchUid(), LoggerUser.getBranchUID()));

        if (optionalIncomeExpenses.isEmpty()) {
            return new Response<>("Income Expenses Not Found");
        }

        IncomeExpenses incomeExpenses =
                optionalIncomeExpenses.get();


        // ==============================
        // CHECK AVAILABLE BALANCE
        // ==============================

        BigDecimal currentExpenses =
                incomeExpenses.getExpenses() != null
                        ? incomeExpenses.getExpenses()
                        : BigDecimal.ZERO;

        BigDecimal income =
                incomeExpenses.getIncome() != null
                        ? incomeExpenses.getIncome()
                        : BigDecimal.ZERO;

        BigDecimal spendAmount =
                BigDecimal.valueOf(spendDTO.getAmount());


        BigDecimal availableAmount =
                income.subtract(currentExpenses);


        if (spendAmount.compareTo(availableAmount) > 0) {
            return new Response<>(
                    "Spend amount cannot exceed available amount"
            );
        }


        // ==============================
        // UPDATE EXPENSES
        // ==============================

        incomeExpenses.setExpenses(
                currentExpenses.add(spendAmount)
        );


        // ==============================
        // CREATE SPEND HISTORY
        // ==============================

        IncomeExpensesDescription spend =
                new IncomeExpensesDescription();
        spend.setPaidBy(LoggerUser.getEmail());
        spend.setPaidAt(java.time.LocalDateTime.now());
        spend.setMethod(payoutMethod(spendDTO.getMethod()));

        spend.setDescription(
                spendDTO.getDescription().trim()
        );

        spend.setDescriptionDate(
                LocalDate.now()
        );

        spend.setSpendAmount(
                spendAmount
        );

        // Who recorded the payment, for the spending history.
        com.midland.saloon.Uaa.Model.User payer = LoggerUser.getUser();
        if (payer != null) {
            String fullName = ((payer.getFirstName() == null ? "" : payer.getFirstName()) + " "
                    + (payer.getLastName() == null ? "" : payer.getLastName())).trim();
            spend.setStaffName(fullName.isEmpty() ? payer.getUsername() : fullName);
        }

        spend.setIncomeExpenses(
                incomeExpenses
        );


        // ==============================
        // SAVE SPEND HISTORY
        // ==============================

        incomeExpensesDescriptionRepository.save(spend);
        return new Response<>(incomeExpensesRepository.save(incomeExpenses));
    }
    public Response<IncomeExpenses> findIncomeExpensesAndDescription(String incomeUID){
        if(incomeUID == null)
            return new Response<>("Provide Income REF");
        Optional<IncomeExpenses> optionalIncomeExpenses = incomeExpensesRepository.findIncomeExpensesAndDescription(incomeUID, LoggerUser.getBranchUID());
        return optionalIncomeExpenses.map(Response::new).orElseGet(()->new Response<>("Income Expenses Not Found"));
    }

    /***
     SALOON_REPORT_METHODS
     */
    // Commissions arrive already looked up by the caller, keyed on service UID.
    private Map<String, Commission> loadCommissionsByService(List<SaloonServiceEntity> services) {
        List<String> serviceUids = services.stream()
                .filter(Objects::nonNull)
                .map(SaloonServiceEntity::getUid)
                .toList();
        if (serviceUids.isEmpty()) {
            return Map.of();
        }
        Map<String, Commission> byService = new HashMap<>();
        for (Commission commission : commissionRepository.findCommissionsByServices(serviceUids, LoggerUser.getBranchUID())) {
            if (commission.getSaloonService() != null) {
                byService.put(commission.getSaloonService().getUid(), commission);
            }
        }
        return byService;
    }

    @Transactional
    private ResponseList<SaloonReports> saveSaloonReport(List<SaloonSales> sales, SaloonStaff staff, Map<String, Commission> commissionsByService) {
        log.info("{} is saving saloon reports"+ LoggerUser.getEmail());
        log.info("Staff received for reports: {}"+ staff);
        List<String> errors = new ArrayList<>();
        List<SaloonReports> saloonReports = new ArrayList<>();
        // ============================
        // VALIDATION
        // ============================
        if (sales == null || sales.isEmpty()) {
            log.info("No sales found to create reports");
            throw new BusinessException("No sales found to create reports");
        }

        if (staff == null) {
            log.info("Staff is null");
            throw new BusinessException("Staff not found");
        }

        String branchUID = LoggerUser.getBranchUID();
        log.info("Creating reports for branch: {}"+ branchUID);
        // ============================
        // LOOP SALES
        // ============================

        for (SaloonSales sale : sales) {

            if (sale == null) {

                log.info(
                        "Invalid sale data: sale is null"
                );

                errors.add("Invalid sale data");
                continue;
            }

            log.info(
                    "Processing sale: {}"+
                    sale.getUid()
            );

            // ============================
            // GET SERVICE
            // ============================

            SaloonServiceEntity service =
                    sale.getSaloonServiceEntity();

            if (service == null) {

                log.info(
                        "Service not found for sale: {}"+
                        sale.getUid()
                );

                errors.add(
                        "Service not found for sale: "
                                + sale.getUid()
                );

                continue;
            }

            log.info(
                    "Service found. Service UID: {}"+
                    service.getUid()
            );

            // ============================
            // SERVICE PRICE
            // ============================

            Integer serviceValue = service.getPrice();

            if (serviceValue == null) {

                log.info(
                        "Service price not found. Service: {}"+
                        service.getUid()
                );

                errors.add(
                        "Service price not found for service: "
                                + service.getUid()
                );

                continue;
            }

            log.info(
                    "Service price: {}"+
                    serviceValue
            );

            // ============================
            // FIND COMMISSION
            // ============================

            log.info(
                    "Searching commission. Service: {}, Branch: {}"+
                    service.getUid()
            );

            Commission commission =
                    commissionsByService.get(service.getUid());

            if (commission == null) {

                log.info(
                        "COMMISSION NOT FOUND. Service: {}, Branch: {}"+
                        service.getUid()
                );

                errors.add(
                        "Commission not found for service: "
                                + service.getUid()
                );

                continue;
            }

            log.info(
                    "COMMISSION FOUND. Service: {}"+
                    service.getUid()
            );

            // ============================
            // CREATE REPORT
            // ============================

            SaloonReports saloonReport =
                    new SaloonReports();

            saloonReport.setSaloonStaff(staff);
            saloonReport.setSaloonSales(sale);
            saloonReport.setSaloonServiceEntity(service);

            // ============================
            // COMMISSION AMOUNTS
            // ============================

            saloonReport.setEmergencyAmount(
                    calculatePercentage(
                            commission.getEmergencyPercent(),
                            serviceValue
                    )
            );

            saloonReport.setOwnerAmount(
                    calculatePercentage(
                            commission.getOwnerPercent(),
                            serviceValue
                    )
            );

            saloonReport.setOthersAmount(
                    calculatePercentage(
                            commission.getOtherPercent(),
                            serviceValue
                    )
            );

            saloonReport.setMaintenanceAmount(
                    calculatePercentage(
                            commission.getMaintenancePercent(),
                            serviceValue
                    )
            );

            saloonReport.setStaffAmount(
                    calculatePercentage(
                            commission.getStaffPercent(),
                            serviceValue
                    )
            );

            saloonReport.setLoanAmount(
                    calculatePercentage(
                            commission.getLoanPercent(),
                            serviceValue
                    )
            );
            saloonReport.setRentAmount(
                    calculatePercentage(
                            commission.getRentPercent(),
                            serviceValue
                    )
            );
            saloonReport.setWaterAmount(
                    calculatePercentage(
                            commission.getWaterPercent(),
                            serviceValue
                    )
            );
            saloonReport.setLukuAmount(
                    calculatePercentage(
                            commission.getLukuPercent(),
                            serviceValue
                    )
            );
            saloonReport.setStockPurchaseAmount(
                    calculatePercentage(
                            commission.getStockPurchasePercent(),
                            serviceValue
                    )
            );
            saloonReport.setTraAmount(
                    calculatePercentage(
                            commission.getTraPercent(),
                            serviceValue
                    )
            );

            // ============================
            // LOG CALCULATED AMOUNTS
            // ============================
            saloonReports.add(saloonReport);
        }

        // ============================
        // NOTHING TO SAVE
        // ============================

        if (saloonReports.isEmpty()) {

            log.info(
                    "NO SALOON REPORTS WERE PREPARED"
            );

            if (!errors.isEmpty()) {

                log.info(
                        "Report errors: {}" +
                        errors
                );

                return new ResponseList<>(
                        null,
                        errors
                );
            }

            return new ResponseList<>(
                    "No saloon reports to save"
            );
        }

        // ============================
        // SAVE REPORTS
        // ============================

        try {

            log.info(
                    "Saving {} saloon reports..."+
                    saloonReports.size()
            );

            List<SaloonReports> savedReports =
                    saloonReportsRepository.saveAll(
                            saloonReports
                    );

            if (savedReports == null ||
                    savedReports.isEmpty()) {

                log.info(
                        "SALON REPORTS WERE NOT SAVED"
                );

                throw new IllegalStateException(
                        "Saloon reports were not saved"
                );
            }

            log.info(
                    "SALON REPORTS SAVED SUCCESSFULLY. Count: {}"+
                    savedReports.size()
            );

            // ============================
            // PARTIAL ERRORS
            // ============================

            if (!errors.isEmpty()) {

                log.info(
                        "Some reports were skipped: {}"+
                        errors
                );
            }

            return new ResponseList<>(
                    savedReports
            );

        } catch (Exception e) {

            log.info(
                    "ERROR WHILE SAVING SALOON REPORTS"+
                    e
            );

            throw e;
        }
    }
    private ResponseList<ServiceAndStoreReport> saveServiceAndStoreReport(
            ResponseList<SaloonReports> reports) {

        log.info("{} Is Saving Service and Store Report"+ LoggerUser.getEmail());

        if (reports == null ||
                reports.getData() == null ||
                reports.getData().isEmpty()) {

            throw new BusinessException("Provide Saloon Reports");
        }

        List<ServiceAndStoreReport> serviceAndStoreReports = new ArrayList<>();

        Map<String, List<StoreOpen>> openStoresByService =
                getOpenStoresByService(reports.getData());

        for (SaloonReports report : reports.getData()) {
            Integer storeOpened=0;
            List<StoreOpen> storeOpens =
                    openStoresByService.getOrDefault(
                            report.getSaloonServiceEntity().getUid(),
                            List.of()
                    );

            if (storeOpens.isEmpty()) {
                throw new BusinessException(
                        "No Open Store Found For Service:  " + report.getSaloonServiceEntity().getServiceName()
                );
            }

            storeOpened=storeOpens.size();
            for (StoreOpen storeOpen : storeOpens) {

                ServiceAndStoreReport storeReport =
                        new ServiceAndStoreReport();

                storeReport.setSaloonReports(report);
                storeReport.setStoreOpen(storeOpen);
                storeReport.setSharedAmount(report.getSaloonServiceEntity().getPrice()/storeOpened);
                serviceAndStoreReports.add(storeReport);
            }
        }

        if (serviceAndStoreReports.isEmpty()) {

            throw new BusinessException("No Service And Store Report To Save");
        }

        List<ServiceAndStoreReport> savedReports =
                serviceAndStoreReportRepository.saveAll(
                        serviceAndStoreReports
                );

        if (savedReports == null || savedReports.isEmpty()) {

            throw new BusinessException("Error in saving Service and Store Report");
        }



        return new ResponseList<>(savedReports);
    }

    // Open stores for every service in a batch of reports, grouped by service UID.
    private Map<String, List<StoreOpen>> getOpenStoresByService(List<SaloonReports> reports) {
        List<String> serviceUids = reports.stream()
                .map(SaloonReports::getSaloonServiceEntity)
                .filter(Objects::nonNull)
                .map(SaloonServiceEntity::getUid)
                .distinct()
                .toList();
        if (serviceUids.isEmpty()) {
            return Map.of();
        }
        Map<String, List<StoreOpen>> byService = new HashMap<>();
        for (StoreOpen storeOpen : openStoreRepository.findOpenStoreListByServices(
                LoggerUser.getBranchUID(),
                serviceUids
        )) {
            SaloonServiceEntity service = storeOpen.getStore() != null
                    ? storeOpen.getStore().getSaloonServiceEntity()
                    : null;
            if (service != null) {
                byService.computeIfAbsent(service.getUid(), uid -> new ArrayList<>()).add(storeOpen);
            }
        }
        return byService;
    }

    private Integer calculatePercentage(Integer percentage, Integer amount) {
        if (percentage == null || amount == null) {
            return 0;
        }
        return (percentage * amount) / 100;
    }
    public Response<SaloonReports> findSaloonReportByUID(String saloonReportUID){
        log.info(LoggerUser.getEmail() + "Is Accessing Saloon Reports");
        Optional<SaloonReports> optionalSaloonReports = saloonReportsRepository.findSaloonReportByUID(saloonReportUID, LoggerUser.getBranchUID());
        return optionalSaloonReports.map(Response::new).orElseGet(()->new Response<>("Report Not Found"));
    }
    public ResponsePage<SaloonProjection> findSaloonReportsPage(Integer page, Integer size, LocalDate date){
        log.info(LoggerUser.getEmail() + "Is Accessing Reports");
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(saloonReportsRepository.findSaloonReportsPage(pageable, LoggerUser.getBranchUID(), date));
    }
    public Response<SaloonProjection> findSaloonRevenueReport(LocalDate date){
        log.info(LoggerUser.getEmail() + "is accessing Commissions ");
        Optional<SaloonProjection> optionalSaloonProjection=saloonReportsRepository.findSaloonRevenueReport(LoggerUser.getBranchUID(), date);
        return optionalSaloonProjection.map(Response::new).orElseGet(() -> new Response<>("No Report Found"));
    }
    public Response<SaloonProjection> findCurrentSaloonRevenueReport(String filter) {

        LocalDate today = LocalDate.now();

        LocalDate startDate;
        LocalDate endDate;

        if (filter == null || filter.isBlank()) {

            // Default = leo
            startDate = today;
            endDate = today.plusDays(1);

        } else {

            switch (filter.toUpperCase()) {

                case "DAY":
                    // Leo
                    startDate = today;
                    endDate = today.plusDays(1);
                    break;
                case "YESTERDAY":
                    // Jana
                    startDate = today.minusDays(1);
                    endDate = today;
                    break;

                case "WEEK":
                    // Wiki hii
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    );
                    endDate = startDate.plusWeeks(1);
                    break;

                case "LAST_WEEK":
                    // Wiki iliyopita
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    ).minusWeeks(1);

                    endDate = startDate.plusWeeks(1);
                    break;

                case "MONTH":
                    // Mwezi huu
                    startDate = today.withDayOfMonth(1);
                    endDate = startDate.plusMonths(1);
                    break;

                case "LAST_MONTH":
                    // Mwezi uliopita
                    startDate = today.withDayOfMonth(1).minusMonths(1);
                    endDate = startDate.plusMonths(1);
                    break;

                case "THIS_YEAR":
                    // Mwaka huu
                    startDate = LocalDate.of(today.getYear(), 1, 1);
                    endDate = startDate.plusYears(1);
                    break;
                case "LAST_YEAR":
                    // Mwaka uliopita
                    startDate = today
                            .with(TemporalAdjusters.firstDayOfYear())
                            .minusYears(1);

                    endDate = today
                            .with(TemporalAdjusters.firstDayOfYear());

                    break;

                default:
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        startDate = LocalDate.parse(filter, formatter);
                        endDate = startDate.plusDays(1);
                    } catch (DateTimeParseException e) {

                        throw new IllegalArgumentException(
                                "Filter must be DAY, WEEK, LAST_WEEK, MONTH, " +
                                        "LAST_MONTH, THIS_YEAR or date in format dd-MM-yyyy"
                        );
                    }
            }
        }Optional<SaloonProjection> optionalSaloonProjection =
                saloonReportsRepository.findCurrentSaloonRevenueReport(
                        LoggerUser.getBranchUID(),
                        startDate,
                        endDate
                );

        return optionalSaloonProjection
                .map(Response::new)
                .orElseGet(() -> new Response<>("Not found"));

    }
    public ResponseList<SaloonServiceRevenueProjection> findSaloonRevenueByService(LocalDate date) {

        List<SaloonServiceRevenueProjection> data = saloonReportsRepository.findSaloonRevenueByService(LoggerUser.getBranchUID(), date);
        ResponseList<SaloonServiceRevenueProjection> response = new ResponseList<>();
        response.setData(data);
        return response;
    }
    public ResponsePage<SaloonProjection> findCurrentSaloonReportsPage(int page, int size, String filter) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        if (filter == null || filter.isBlank()) {
            startDate = today;
            endDate = today.plusDays(1);
        } else {
            switch (filter.toUpperCase()) {
                case "DAY":
                    startDate = today;
                    endDate = today.plusDays(1);
                    break;
                case "YESTERDAY":
                    // Jana
                    startDate = today.minusDays(1);
                    endDate = today;
                    break;
                case "WEEK":
                    startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    endDate = startDate.plusWeeks(1);
                    break;
                case "LAST_WEEK":
                    startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
                    endDate = startDate.plusWeeks(1);
                    break;
                case "MONTH":
                    startDate = today.withDayOfMonth(1);
                    endDate = startDate.plusMonths(1);
                    break;
                case "LAST_MONTH":
                    startDate = today.withDayOfMonth(1).minusMonths(1);
                    endDate = startDate.plusMonths(1);
                    break;
                case "THIS_YEAR":
                    startDate = LocalDate.of(today.getYear(), 1, 1);
                    endDate = startDate.plusYears(1);
                    break;
                case "LAST_YEAR":
                    // Mwaka uliopita
                    startDate = today
                            .with(TemporalAdjusters.firstDayOfYear())
                            .minusYears(1);

                    endDate = today
                            .with(TemporalAdjusters.firstDayOfYear());

                    break;
                default:
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        startDate = LocalDate.parse(filter, formatter);
                        endDate = startDate.plusDays(1);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Filter must be DAY, WEEK, LAST_WEEK, MONTH, " +
                                "LAST_MONTH, THIS_YEAR or date in format dd-MM-yyyy");
                    }
            }
        }
        return new ResponsePage<>(saloonReportsRepository.findCurrentSaloonReportsPage(pageable, LoggerUser.getBranchUID(), startDate, endDate));
    }
    public ResponseList<SaloonServiceRevenueProjection> findCurrentSaloonRevenueByService(String filter) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        if (filter == null || filter.isBlank()) {
            startDate = today;
            endDate = today.plusDays(1);
        } else {
            switch (filter.toUpperCase()) {
                case "DAY":
                    startDate = today;
                    endDate = today.plusDays(1);
                    break;
                case "YESTERDAY":
                    // Jana
                    startDate = today.minusDays(1);
                    endDate = today;
                    break;

                case "WEEK":
                    // Wiki hii
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    );
                    endDate = startDate.plusWeeks(1);
                    break;

                case "LAST_WEEK":
                    // Wiki iliyopita
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    ).minusWeeks(1);

                    endDate = startDate.plusWeeks(1);
                    break;

                case "MONTH":
                    // Mwezi huu
                    startDate = today.withDayOfMonth(1);
                    endDate = startDate.plusMonths(1);
                    break;

                case "LAST_MONTH":
                    // Mwezi uliopita
                    startDate = today.withDayOfMonth(1).minusMonths(1);
                    endDate = startDate.plusMonths(1);
                    break;

                case "THIS_YEAR":
                    // Mwaka huu
                    startDate = LocalDate.of(today.getYear(), 1, 1);
                    endDate = startDate.plusYears(1);
                    break;
                case "LAST_YEAR":
                    // Mwaka uliopita
                    startDate = today
                            .with(TemporalAdjusters.firstDayOfYear())
                            .minusYears(1);

                    endDate = today
                            .with(TemporalAdjusters.firstDayOfYear());

                    break;

                default:
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        startDate = LocalDate.parse(filter, formatter);
                        endDate = startDate.plusDays(1);
                    } catch (DateTimeParseException e) {

                        throw new IllegalArgumentException(
                                "Filter must be DAY, WEEK, LAST_WEEK, MONTH, " +
                                        "LAST_MONTH, THIS_YEAR or date in format dd-MM-yyyy"
                        );
                    }
            }
        }

        return new ResponseList<>(saloonReportsRepository.findCurrentSaloonRevenueByService(LoggerUser.getBranchUID(), startDate, endDate));
    }
    /***
     STAFF_COMMISSION_METHODS
     */
    @Transactional
    private StaffCommissions addStaffCommission(String staffUid, String branchUid, LocalDate date, List<Integer> values) {
        int value = values.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        Optional<StaffCommissions> existing = staffCommissionsRepository.findCommission(staffUid, branchUid, date);
        StaffCommissions commission;
        if (existing.isPresent()) {
            commission = existing.get();
            int currentTotal = commission.getTotalAmount() == null ? 0 : commission.getTotalAmount();
            int currentRemaining = commission.getRemainingAmount() == null ? 0 : commission.getRemainingAmount();
            commission.setTotalAmount(currentTotal + value);
            commission.setRemainingAmount(currentRemaining + value);
        } else {
            commission = new StaffCommissions();
            SaloonStaff staff = new SaloonStaff();
            staff.setUid(staffUid);
            commission.setSaloonStaff(staff);
            commission.setBranchUid(branchUid);
            commission.setCreatedAt(date);
            commission.setPaymentStatus("NOT PAID");
            commission.setPayedAmount(0);
            commission.setTotalAmount(value);
            commission.setRemainingAmount(value);
        }
        return staffCommissionsRepository.save(commission);
    }
    public ResponsePage<SaloonProjection> findStaffCommissionPage(String range, Integer page, Integer size) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        Pageable pageable = PageRequest.of(page, size);
        // Specific date: 05-8-2026, 5-8-2026, 05-08-2026, etc.
        if (range.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
            try {
                LocalDate date = LocalDate.parse(
                        range,
                        DateTimeFormatter.ofPattern("d-M-yyyy")
                );
                startDate = date;
                endDate = date;

            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Invalid date format: " + range +
                                ". Expected format: dd-M-yyyy"
                );
            }

        }  else {

            switch (range.toUpperCase()) {
                case "TODAY" -> {
                    startDate = today;
                    endDate = today;
                }

                case "YESTERDAY" -> {
                    startDate = today.minusDays(1);
                    endDate = today.minusDays(1);
                }
                case "THIS_WEEK" -> {
                    startDate = today.with(DayOfWeek.MONDAY);
                    endDate = startDate.plusDays(6);
                }
                case "LAST_WEEK" -> {
                    startDate = today
                            .with(DayOfWeek.MONDAY)
                            .minusWeeks(1);
                    endDate = startDate.plusDays(6);
                }
                case "THIS_MONTH" -> {
                    startDate = today.withDayOfMonth(1);
                    endDate = today.withDayOfMonth(today.lengthOfMonth());
                }

                case "LAST_MONTH" -> {
                    LocalDate lastMonth = today.minusMonths(1);
                    startDate = lastMonth.withDayOfMonth(1);
                    endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                }

                case "THIS_YEAR" -> {
                    startDate = today.withDayOfYear(1);
                    endDate = today.withDayOfYear(today.lengthOfYear());
                }

                case "LAST_YEAR" -> {
                    LocalDate lastYear = today.minusYears(1);
                    startDate = lastYear.withDayOfYear(1);
                    endDate = lastYear.withDayOfYear(lastYear.lengthOfYear());
                }

                default -> throw new IllegalArgumentException("Invalid date range: " + range);
            }
        }

        return new ResponsePage<>(staffCommissionsRepository.findStaffCommissionPage(
                pageable,
                LoggerUser.getBranchUID(),
                startDate,
                endDate
        ));
    }
    public Response<StaffCommissions> payStaffCommission(StaffCommissionDTO staffCommissionDTO){
        log.info(LoggerUser.getEmail() + "Is Paying Staff Commission");
        if(staffCommissionDTO == null)
            return new Response<>("Weka Details za Malipo");
        StaffCommissions staffCommissions =null;
        if(staffCommissionDTO.getUid() ==null)
            return new Response<>("Weka Commission REF");
        Optional<StaffCommissions> optionalStaffCommissions = staffCommissionsRepository.findById(staffCommissionDTO.getUid());
        if(optionalStaffCommissions.isEmpty())
            return new Response<>("Commission ya staff Haipo");
        staffCommissions = optionalStaffCommissions.get();
        staffCommissions.update();
        if(staffCommissionDTO.getAmount() == null)
            return new Response<>("Weka Kiasi kinacholipwa");
        staffCommissions.setPayedAmount(staffCommissionDTO.getAmount() + staffCommissions.getPayedAmount());
        staffCommissions.setRemainingAmount(staffCommissions.getRemainingAmount() - staffCommissionDTO.getAmount());
        List<IncomeExpenses> incomeExpenses = getIncomeExpensesFilter(staffCommissionDTO.getFilter(), staffCommissionDTO.getWeekDate());
        StaffCommissions savedStaffCommission = null;
        try{
            savedStaffCommission = staffCommissionsRepository.save(staffCommissions);
            incomeExpenses = getIncomeExpensesFilter(staffCommissionDTO.getFilter(), staffCommissionDTO.getWeekDate());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("Error wakati wa kulipa");
        }
        if(incomeExpenses.isEmpty())
            throw new BusinessException("Income Expenses Not Found");
        int number = incomeExpenses.size();
        List<IncomeExpenses> saveIncomeAndExpenses= new ArrayList<>();
        List<IncomeExpensesDescription> incomeExpensesDescriptions = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.valueOf(
                staffCommissionDTO.getAmount()
        );

        BigDecimal numberOfRecords = BigDecimal.valueOf(number);
        BigDecimal baseAmount = totalAmount.divide(
                numberOfRecords,
                0,
                RoundingMode.DOWN
        );

        BigDecimal remainder = totalAmount.remainder(numberOfRecords);
        for (int i = 0; i < incomeExpenses.size(); i++) {
            IncomeExpenses expenses = incomeExpenses.get(i);
            IncomeExpensesDescription incomeExpensesDescription = new IncomeExpensesDescription();
            incomeExpensesDescription.setPaidBy(LoggerUser.getEmail());
            incomeExpensesDescription.setPaidAt(java.time.LocalDateTime.now());
            incomeExpensesDescription.setMethod(payoutMethod(staffCommissionDTO.getMethod()));
            expenses.setDescriptions("Staff Commission");
            BigDecimal amountPerExpense = baseAmount;
            if (i == incomeExpenses.size() - 1) {
                amountPerExpense = baseAmount.add(remainder);
            }
            BigDecimal currentExpenses = expenses.getExpenses();
            if (currentExpenses == null) {
                currentExpenses = BigDecimal.ZERO;
            }
            expenses.setExpenses(currentExpenses.add(amountPerExpense));
            saveIncomeAndExpenses.add(expenses);
            incomeExpensesDescription.setSpendAmount(amountPerExpense);
            incomeExpensesDescription.setStaffName(savedStaffCommission.getSaloonStaff().getFirstName() + "  " +savedStaffCommission.getSaloonStaff().getLastName());
            incomeExpensesDescription.setDescription(staffCommissionDTO.getDescriptions());

                    incomeExpensesDescription.setDescriptionDate(LocalDate.now());
            incomeExpensesDescription.setIncomeExpenses(expenses);
            incomeExpensesDescriptions.add(incomeExpensesDescription);
        }
        try{
            incomeExpensesRepository.saveAll(saveIncomeAndExpenses);
            incomeExpensesDescriptionRepository.saveAll(incomeExpensesDescriptions);
        }catch(Exception e){
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
        return new Response<>(savedStaffCommission);
    }
    public List<IncomeExpenses> getIncomeExpensesFilter(String filter, LocalDate weekDate) {
        log.info("FILTER" + filter);
        LocalDate today = LocalDate.now();

        LocalDate startDate;
        LocalDate endDate;

        switch (filter) {

            case "TODAY" -> {
                startDate = getWeekStart(today);
                endDate = startDate.plusDays(6);
            }


            case "YESTERDAY" -> {
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
            }

            case "THIS_WEEK" -> {
                startDate = getWeekStart(today);
                endDate = startDate.plusDays(6);
            }

            case "LAST_WEEK" -> {
                startDate = getWeekStart(today).minusWeeks(1);
                endDate = startDate.plusDays(6);
            }

            case "THIS_MONTH" -> {
                startDate = today.withDayOfMonth(1);
                endDate = today.withDayOfMonth(
                        today.lengthOfMonth()
                );
            }

            case "LAST_MONTH" -> {
                LocalDate lastMonth = today.minusMonths(1);

                startDate = lastMonth.withDayOfMonth(1);
                endDate = lastMonth.withDayOfMonth(
                        lastMonth.lengthOfMonth()
                );
            }

            case "THIS_YEAR" -> {
                startDate = today.withDayOfYear(1);
                endDate = today.withDayOfYear(
                        today.lengthOfYear()
                );
            }

            case "LAST_YEAR" -> {
                LocalDate lastYear = today.minusYears(1);

                startDate = lastYear.withDayOfYear(1);
                endDate = lastYear.withDayOfYear(
                        lastYear.lengthOfYear()
                );
            }

            default -> throw new BusinessException(
                    "Invalid filter: " + filter
            );
        }

        return incomeExpensesRepository.findByBranchAndWeekStartDateBetween(
                LoggerUser.getBranchUID(),
                weekDate
        );
    }
    private LocalDate getWeekStart(LocalDate date) {

        int daysFromSaturday =
                (date.getDayOfWeek().getValue() + 1) % 7;

        return date.minusDays(daysFromSaturday);
    }
    private LocalDate[] getDateRange(String filterDate) {

        LocalDate today = LocalDate.now();

        LocalDate startDate;
        LocalDate endDate;

        switch (filterDate) {

            case "TODAY":
                startDate = today;
                endDate = today.plusDays(1);
                break;

            case "YESTERDAY":
                startDate = today.minusDays(1);
                endDate = today;
                break;

            case "THIS_WEEK":
                startDate = today.with(
                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                );
                endDate = startDate.plusWeeks(1);
                break;

            case "LAST_WEEK":
                startDate = today
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .minusWeeks(1);

                endDate = startDate.plusWeeks(1);
                break;

            default:
                startDate = today;
                endDate = today.plusDays(1);
                break;
        }

        return new LocalDate[]{startDate, endDate};
    }
    /***
     SALOON_STORE_METHODS
     */
    public Response<Store> saveStore(StoreDTO storeDTO){
        log.info(LoggerUser.getEmail() + "Is Saving Store Items");
        if(storeDTO == null)
            return new Response<>("Provide Store Details");
        Store store = null;
        if(storeDTO.getUid() !=null){
            Optional<Store> optionalStore = storeRepository.findById(storeDTO.getUid());
            if(optionalStore.isEmpty())
                return new Response<>("Store Not Found");
            store = optionalStore.get();
            store.update();
        }else{
            store = new Store();
        }
        store.setCodeOfStore(generateCode(storeDTO.getNameOfStore()));
        if(storeDTO.getNameOfStore() == null)
            return new Response<>("Provide Store Item Name");
        store.setNameOfStore(storeDTO.getNameOfStore());
        if(storeDTO.getDescription() == null)
            return new Response<>("Provide Descriptions For Store Item");
        store.setDescription(storeDTO.getDescription());
        if(storeDTO.getQuantity() == null)
            return new Response<>("Provide Quantity For Store");
        store.setQuantity(storeDTO.getQuantity());
        if(storeDTO.getSaloonServiceEntityUID() == null)
            return new Response<>("provide Service");
        Optional<SaloonServiceEntity> optionalSaloonServiceEntity = saloonServiceRepository.findById(storeDTO.getSaloonServiceEntityUID());
        if(optionalSaloonServiceEntity.isEmpty())
            return new Response<>("Service Not Found");
        store.setSaloonServiceEntity(optionalSaloonServiceEntity.get());
        // The price per item, the price of the whole lot, or both: the
        // missing one comes from the quantity.
        Integer unit = storeDTO.getBuyingPrice();
        Integer total = storeDTO.getTotalPrice();
        if (unit == null && total == null)
            return new Response<>("Provide Buying Price For Store");
        if (storeDTO.getQuantity() <= 0)
            return new Response<>("Quantity must be more than zero");
        if (unit == null)
            unit = Math.round((float) total / storeDTO.getQuantity());
        if (total == null)
            total = unit * storeDTO.getQuantity();
        store.setBuyingPrice(unit);
        store.setTotalQuantityPrice(total);
        store.setNotUsedQuantity(storeDTO.getQuantity());

        try{
            return new Response<>(storeRepository.save(store));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("Error in saving Store");
        }
    }
    public ResponseList<SaloonProjection> findStoreList(){
        log.info(LoggerUser.getEmail() + "Access saloon store");
        return new ResponseList<>(storeRepository.findStoreList(LoggerUser.getBranchUID()));
    }
    public ResponsePage<SaloonProjection> findStorePage(Integer page, Integer size, String search){
        log.info(LoggerUser.getEmail() + "Is accessing Saloon Store");
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(storeRepository.findStorePage(pageable, LoggerUser.getBranchUID(), like(search)));
    }
    public Response<Store> deleteSaloonStore(String storeUID){
        log.info(LoggerUser.getEmail() + "is deleting saloon store item");
        if(storeUID == null)
            return new Response<>("Provide Store Item REF");
        Optional<Store> optionalStore = storeRepository.findById(storeUID);
        if(optionalStore.isEmpty())
            return new Response<>("Store Not Found");
        Store store = optionalStore.get();
        storeRepository.delete(store);
        return new Response<>(optionalStore.get());
    }
    private String generateCode(String nameOfStore) {
        if (nameOfStore == null || nameOfStore.trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty");
        }
        String cleanName = nameOfStore
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();
        return cleanName.substring(0, Math.min(3, cleanName.length()));
    }
    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    public Response<Store> addQuantityToStore(StoreDTO storeDTO){
        log.info(LoggerUser.getEmail() + "Is adding quantity to the store items");
        if(storeDTO == null)
            return new Response<>("Provide Data For Adding Quantity");
        Store store= null;
        if(storeDTO.getUid() != null){
            Optional<Store> optionalStore = storeRepository.findById(storeDTO.getUid());
            if(optionalStore.isEmpty())
                return new Response<>("Store Not Found");
            store = optionalStore.get();
        }else{
            store = new Store();
        }
        if (storeDTO.getQuantity() == null || storeDTO.getQuantity() <= 0)
            return new Response<>("Provide Quantity To Add");
        // This delivery's cost: its whole-lot price, or its price per item,
        // or (neither given) the item's current price per item.
        int added = storeDTO.getQuantity();
        int lotCost = storeDTO.getTotalPrice() != null
                ? storeDTO.getTotalPrice()
                : added * (storeDTO.getBuyingPrice() != null ? storeDTO.getBuyingPrice() : nz(store.getBuyingPrice()));
        store.setQuantity(nz(store.getQuantity()) + added);
        store.setNotUsedQuantity(nz(store.getNotUsedQuantity()) + added);
        store.setTotalQuantityPrice(nz(store.getTotalQuantityPrice()) + lotCost);
        // Price per item becomes the average over everything bought.
        if (store.getQuantity() > 0)
            store.setBuyingPrice(Math.round((float) store.getTotalQuantityPrice() / store.getQuantity()));
        try{
            return new Response<>(storeRepository.save(store));
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>("Error in Adding Quantity");
        }
    }
    public Response<Store> openStore(StoreDTO storeDTO){
        log.info(LoggerUser.getEmail() + "Is Opening Store for use");
        if(storeDTO.getUid() == null)
            return new Response<>("Provide Store REF");
        Optional<Store> optionalStore = storeRepository.findById(storeDTO.getUid());
        if(optionalStore.isEmpty())
            return new Response<>("Store Not Found");
        StoreOpen storeOpen = new StoreOpen();
        storeOpen.setStore(optionalStore.get());
        storeOpen.setOpenQuantity(1);
        storeOpen.setOpenStoreCode(generateOpenStoreCode(optionalStore.get().getCodeOfStore(),
                        openStoreRepository.countByStoreNameAndBranchUid(
                                optionalStore.get().getNameOfStore(),
                                LoggerUser.getBranchUID()
                        )
                )
        );
        try{
            openStoreRepository.save(storeOpen);
            Store store = optionalStore.get();
            store.setQuantity(optionalStore.get().getQuantity());
            store.setNotUsedQuantity(optionalStore.get().getNotUsedQuantity()-1);
            store.setUsedQuantity(optionalStore.get().getUsedQuantity() + 1);
            return new Response<>(storeRepository.save(store));
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>("Error in Opening Store");
        }
    }
    private String generateOpenStoreCode(String code, Long number) {
        LocalDate now = LocalDate.now();

        String month = String.format("%02d", now.getMonthValue());
        String year = String.valueOf(now.getYear());

        long nextNumber = (number == null || number <= 0)
                ? 1L
                : number + 1;

        return String.format(
                "%s-%s-%s-%03d",
                code.toUpperCase(),
                month,
                year,
                nextNumber
        );
    }
    public ResponsePage<SaloonProjection> findOpenStorePage(Integer page, Integer size, String searchKey, String search){
        log.info(LoggerUser.getEmail() + "is Accessing Stores Open");
        if(page == null || size == null)
            return new ResponsePage<>("Either Page or Size must no be Null");
        Pageable pageable = PageRequest.of(page, size);
        // The "search" is the status wanted (OPEN or CLOSED). Without one the
        // query matched nothing, so opened items never showed - default to OPEN.
        String status = (searchKey == null || searchKey.isBlank()) ? "OPEN" : searchKey.trim().toUpperCase();
        return new ResponsePage<>(openStoreRepository.findOpenStorePage(pageable, LoggerUser.getBranchUID(), status, like(search)));
    }

    /** A search box's text for a LIKE: lower-case, trimmed; empty matches everything. */
    private static String like(String search) {
        return search == null ? "" : search.trim().toLowerCase();
    }
    public Response<StoreOpen> closeOpenStore(StoreDTO storeDTO){
        log.info(LoggerUser.getEmail() + "Is closing open store");
        if(storeDTO == null)
            return new Response<>("Weka Taarifa za kufunga Store");
        if(storeDTO.getOpenStoreUID() == null)
            return new Response<>("Provide Open Store REF");
        Optional<StoreOpen> optionalStoreOpen = openStoreRepository.findById(storeDTO.getOpenStoreUID());
        if(optionalStoreOpen.isEmpty())
            return new Response<>("Open Store Not Found");
        StoreOpen storeOpen = optionalStoreOpen.get();
        storeOpen.setUpdatedAt(LocalDate.now());
        storeOpen.setStatus("CLOSED");
        try {
            return new Response<>(openStoreRepository.save(storeOpen));
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>("Error in Closing Open Store");
        }
    }
    public ResponseList<SaloonProjection> findServiceEntityUIDList(String status){
        log.info(LoggerUser.getEmail() + "is Accessing Service Entity");
        if(status == null)
            return new ResponseList<>("Provide Status");
        return new ResponseList<>(openStoreRepository.findServiceEntityUIDList(LoggerUser.getBranchUID(), status));
    }
    public ResponsePage<StoreReportSummaryProjection> findServiceAndStoreReportPage(Integer page, Integer size, String filter) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        if (filter == null || filter.isBlank()) {
            startDate = today;
            endDate = today.plusDays(1);
        } else {
            switch (filter.toUpperCase()) {
                case "DAY":
                    startDate = today;
                    endDate = today.plusDays(1);
                    break;
                case "YESTERDAY":
                    startDate = today.minusDays(1);
                    endDate = today;
                    break;
                case "WEEK":
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    );
                    endDate = startDate.plusWeeks(1);
                    break;
                case "LAST_WEEK":
                    startDate = today.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    ).minusWeeks(1);
                    endDate = startDate.plusWeeks(1);
                    break;
                case "MONTH":
                    startDate = today.withDayOfMonth(1);
                    endDate = startDate.plusMonths(1);
                    break;
                case "LAST_MONTH":
                    startDate = today
                            .withDayOfMonth(1)
                            .minusMonths(1);
                    endDate = startDate.plusMonths(1);
                    break;
                case "THIS_YEAR":
                    startDate = LocalDate.of(
                            today.getYear(),
                            1,
                            1
                    );
                    endDate = startDate.plusYears(1);
                    break;
                case "LAST_YEAR":
                    startDate = LocalDate.of(
                            today.getYear() - 1,
                            1,
                            1
                    );
                    endDate = startDate.plusYears(1);
                    break;

                default:
                    try {
                        DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        startDate = LocalDate.parse(
                                filter,
                                formatter
                        );
                        endDate = startDate.plusDays(1);
                    } catch (DateTimeParseException e) {
                        throw new BusinessException(
                                "Filter must be DAY, YESTERDAY, WEEK, " +
                                        "LAST_WEEK, MONTH, LAST_MONTH, " +
                                        "THIS_YEAR or date in format yyyy-MM-dd"
                        );
                    }
            }
        }

        String branchUID = LoggerUser.getBranchUID();

        Page<StoreReportSummaryProjection> result =
                serviceAndStoreReportRepository
                        .findStoreReportSummaryPage(
                                pageable,
                                branchUID,
                                startDate,
                                endDate
                        );

        return new ResponsePage<>(result);
    }
    /***
     SALOON_STOCK_AND_PURCHASE_METHODS
     */

    public Optional<StockAndPurchase> getCurrentWeekByService(String serviceUid) {
        LocalDate weekDate = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return stockAndPurchaseRepository.findCurrentWeekByService(
                LoggerUser.getBranchUID(),
                serviceUid,
                weekDate
        );
    }

    // Same week-row lookup as above, but for every service on a sale at once.
    // Rows come back newest first, so the first one seen per service wins.
    private Map<String, StockAndPurchase> getCurrentWeekByServices(List<SaloonServiceEntity> services) {
        List<String> serviceUids = services.stream()
                .filter(Objects::nonNull)
                .map(SaloonServiceEntity::getUid)
                .toList();
        if (serviceUids.isEmpty()) {
            return Map.of();
        }
        LocalDate weekDate = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<String, StockAndPurchase> byService = new HashMap<>();
        for (StockAndPurchase purchase : stockAndPurchaseRepository.findCurrentWeekByServices(
                LoggerUser.getBranchUID(),
                serviceUids,
                weekDate
        )) {
            if (purchase.getSaloonService() != null) {
                byService.putIfAbsent(purchase.getSaloonService().getUid(), purchase);
            }
        }
        return byService;
    }
    public ResponseList<StockAndPurchase> saveStockAndPurchase(List<SaloonServiceEntity> saloonServiceEntities,
                                                               Map<String, Commission> commissionsByService) {
        log.info(LoggerUser.getEmail() + " Is saving Stock And Purchase");
        if (saloonServiceEntities.isEmpty()) {
            throw new BusinessException("Saloon Service is Empty");
        }

        Map<String, StockAndPurchase> currentWeekByService =
                getCurrentWeekByServices(saloonServiceEntities);

        List<StockAndPurchase> stockAndPurchases = new ArrayList<>();
        for (SaloonServiceEntity entity : saloonServiceEntities) {
            Commission commission = commissionsByService.get(entity.getUid());
            if (commission == null) {
                throw new BusinessException("Commission Not Found For: " + entity.getServiceName());
            }

            StockAndPurchase stockAndPurchase =
                    currentWeekByService.get(entity.getUid());

            int stockPurchaseAmount =
                    entity.getPrice()
                            * commission.getStockPurchasePercent()
                            / 100;

            StockAndPurchase purchase;

            if (stockAndPurchase == null) {

                purchase = new StockAndPurchase();

                purchase.setSaloonService(entity);
                purchase.setCommission(commission);

                purchase.setTotalAmount(stockPurchaseAmount);
                purchase.setRemainingAmount(stockPurchaseAmount);

            } else {

                purchase = stockAndPurchase;

                purchase.setTotalAmount(
                        purchase.getTotalAmount() + stockPurchaseAmount
                );

                purchase.setRemainingAmount(
                        purchase.getRemainingAmount() + stockPurchaseAmount
                );
            }

            stockAndPurchases.add(purchase);
        }

        try {

            return new ResponseList<>(
                    stockAndPurchaseRepository.saveAll(stockAndPurchases)
            );

        } catch (Exception e) {

          e.printStackTrace();
            throw new BusinessException(
                    "Error In Saving Stock And Purchase: " + e.getMessage()
            );
        }
    }
    public ResponseList<StockAndPurchaseProjection> getStockAndPurchaseByFilter(String filter) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        switch (filter.toUpperCase()) {
            case "THIS_WEEK" -> {startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = startDate;
            }

            case "LAST_WEEK" -> {

                startDate = today.with(
                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                ).minusWeeks(1);

                endDate = startDate;
            }

            case "THIS_MONTH" -> {

                startDate = today.with(
                        TemporalAdjusters.firstDayOfMonth()
                );

                endDate = today.with(
                        TemporalAdjusters.lastDayOfMonth()
                );
            }

            case "LAST_MONTH" -> {

                LocalDate lastMonth = today.minusMonths(1);

                startDate = lastMonth.with(
                        TemporalAdjusters.firstDayOfMonth()
                );

                endDate = lastMonth.with(
                        TemporalAdjusters.lastDayOfMonth()
                );
            }

            case "THIS_YEAR" -> {

                startDate = today.with(
                        TemporalAdjusters.firstDayOfYear()
                );

                endDate = today.with(
                        TemporalAdjusters.lastDayOfYear()
                );
            }

            case "LAST_YEAR" -> {

                LocalDate lastYear = today.minusYears(1);

                startDate = lastYear.with(
                        TemporalAdjusters.firstDayOfYear()
                );

                endDate = lastYear.with(
                        TemporalAdjusters.lastDayOfYear()
                );
            }

            default -> throw new BusinessException(
                    "Invalid filter: " + filter
            );
        }


        List<StockAndPurchaseProjection> stockAndPurchases =
                stockAndPurchaseRepository.findByWeekDateRange(
                        LoggerUser.getBranchUID(),
                        startDate,
                        endDate
                );

        return new ResponseList<>(stockAndPurchases);
    }
    @Transactional
    public Response<StockAndPurchase> payStockAndPurchase(PayStockAndPurchaseDTO dto) {

        log.info(LoggerUser.getEmail() + " Is Paying Stock and Purchase");


        // ==============================
        // VALIDATION
        // ==============================

        if (dto == null) {
            return new Response<>("Provide Data For Pay Stock");
        }

        if (dto.getUid() == null || dto.getUid().isBlank()) {
            return new Response<>("Provide Stock And Pay REF");
        }

        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            return new Response<>("Provide Valid Payment Amount");
        }


        // ==============================
        // FIND STOCK
        // ==============================

        Optional<StockAndPurchase> optionalStockAndPurchase =
                stockAndPurchaseRepository.findById(dto.getUid());

        if (optionalStockAndPurchase.isEmpty()) {
            return new Response<>("Pay And Stock Not Found");
        }

        log.info(" Service Imepatikana " + optionalStockAndPurchase.get().getSaloonService().getServiceName());

        StockAndPurchase stockAndPurchase =
                optionalStockAndPurchase.get();


        // ==============================
        // FIND INCOME EXPENSE
        // ==============================

        Optional<IncomeExpenses> optionalIncomeExpenses =
                incomeExpensesRepository.findIncomeAndExpensesByWeekDate(
                        LoggerUser.getBranchUID(),
                        dto.getWeekDate()
                );

        if (optionalIncomeExpenses.isEmpty()) {
            return new Response<>("No Income Recorded This Week");
        }

        log.info(" Income And Expenses  :" + optionalIncomeExpenses.get().getName());

        IncomeExpenses incomeExpenses = optionalIncomeExpenses.get();
        // ==============================
        // PAYMENT AMOUNT
        // ==============================

        BigDecimal amount =
                BigDecimal.valueOf(dto.getAmount());


        // ==============================
        // CHECK REMAINING
        // ==============================

        if (dto.getAmount() >
                stockAndPurchase.getRemainingAmount()) {

            return new Response<>(
                    "Payment Amount Is Greater Than Remaining Amount"
            );
        }


        // ==============================
        // UPDATE INCOME EXPENSE
        // ==============================

        BigDecimal currentExpenses =
                incomeExpenses.getExpenses() != null
                        ? incomeExpenses.getExpenses()
                        : BigDecimal.ZERO;

        incomeExpenses.setExpenses(
                currentExpenses.add(amount)
        );

        incomeExpensesRepository.save(incomeExpenses);


        // ==============================
        // UPDATE STOCK
        // ==============================

        Integer currentPaid =
                stockAndPurchase.getPayedAmount() != null
                        ? stockAndPurchase.getPayedAmount()
                        : 0;

        Integer currentRemaining =
                stockAndPurchase.getRemainingAmount() != null
                        ? stockAndPurchase.getRemainingAmount()
                        : 0;


        Integer paymentAmount =
                dto.getAmount();


        Integer newPaid =
                currentPaid + paymentAmount;

        Integer newRemaining =
                currentRemaining - paymentAmount;


        stockAndPurchase.setPayedAmount(newPaid);

        stockAndPurchase.setRemainingAmount(newRemaining);


        // ==============================
        // PAYMENT STATUS
        // ==============================

        if (newRemaining <= 0) {

            stockAndPurchase.setRemainingAmount(0);

            stockAndPurchase.setPaymentStatus("PAID");

        } else {

            stockAndPurchase.setPaymentStatus("PARTIAL");

        }


        // ==============================
        // SAVE STOCK
        // ==============================

        StockAndPurchase savedStock =
                stockAndPurchaseRepository.save(
                        stockAndPurchase
                );


        // ==============================
        // SAVE PAYMENT DESCRIPTION
        // ==============================

        IncomeExpensesDescription incomeExpensesDescription=new IncomeExpensesDescription();
        incomeExpensesDescription.setPaidBy(LoggerUser.getEmail());
        incomeExpensesDescription.setPaidAt(java.time.LocalDateTime.now());
        incomeExpensesDescription.setMethod(payoutMethod(dto.getMethod()));

        log.info(" Stock And Purchase NAME  :" + savedStock.getSaloonService().getServiceName());

        incomeExpensesDescription.setIncomeExpenses(incomeExpenses);

        incomeExpensesDescription.setDescription(
                dto.getDescription()
        );

        incomeExpensesDescription.setSpendAmount(
                BigDecimal.valueOf(dto.getAmount())
        );

        incomeExpensesDescription.setDescriptionDate(
                LocalDate.now()
        );


        IncomeExpensesDescription incomeExpensesDescription1 =
                incomeExpensesDescriptionRepository.save(
                        incomeExpensesDescription
                );

        log.info(" Stock And Purchase Descriptions UID :" + incomeExpensesDescription1.getUid());
        return new Response<>(savedStock);
    }
    public ResponseList<StockAndPurchaseDescriptions> findStockPurchaseByUid(String stockUID){
        log.info(LoggerUser.getEmail() + "Is Accessing Stock And Purchase     " + stockUID);
        log.info(LoggerUser.getEmail() + "Is Accessing Stock And Purchase     " + LoggerUser.getBranchUID());
        if(stockUID == null)
            return new ResponseList<>("Provide Stock And Purchase REF");
        return new ResponseList<>(stockAndPurchaseDescriptionsRepository.findByStockPurchaseUid(LoggerUser.getBranchUID(), stockUID));
    }



    /***
     POS_HOME_DASHBOARD
     */

    /** The four headline numbers for the signed-in user's own branch. */
    public Response<DashboardSummaryDTO> findBranchDashboard() {
        String branchUID = LoggerUser.getBranchUID();
        LocalDate today = LocalDate.now();

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        // Already on the principal - the auth filter fetches the branch with
        // the user - so naming it here costs no extra query.
        User currentUser = LoggerUser.getUser();
        if (currentUser != null && currentUser.getBranch() != null) {
            summary.setBranchName(currentUser.getBranch().getBranchName());
        }
        summary.setTodayRevenue(saloonReportsRepository.totalRevenueOn(branchUID, today));
        summary.setYesterdayRevenue(saloonReportsRepository.totalRevenueOn(branchUID, today.minusDays(1)));
        summary.setServicesSoldToday(saloonReportsRepository.countServicesSoldOn(branchUID, today));
        summary.setPendingBillsCount(salesOpenedRepository.countPendingBills(branchUID));
        summary.setPendingBillsAmount(salesOpenedRepository.pendingBillsAmount(branchUID));
        summary.setOpenStores(openStoreRepository.countOpenStores(branchUID));
        summary.setTotalStores(storeRepository.countStores(branchUID));
        return new Response<>(summary);
    }

    /**
     * Revenue per day for the home page's trend line. Days the branch took
     * nothing have no report rows at all, so they are filled in as zero here -
     * otherwise the line would join Friday straight to Monday and read as if
     * the weekend never happened.
     */
    public ResponseList<DailyRevenueDTO> findRevenueTrend(int days) {
        int span = days < 1 ? 30 : Math.min(days, 365);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(span - 1L);

        Map<LocalDate, Long> byDate = new HashMap<>();
        for (RevenueTrendProjection point : saloonReportsRepository.revenueTrend(LoggerUser.getBranchUID(), start)) {
            byDate.put(point.getDate(), point.getAmount() == null ? 0L : point.getAmount());
        }

        List<DailyRevenueDTO> trend = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            trend.add(new DailyRevenueDTO(date, byDate.getOrDefault(date, 0L)));
        }
        return new ResponseList<>(trend);
    }


    /** Staff ranked by what they earned the branch this month. */
    public ResponseList<StaffEarningsProjection> findStaffEarnings() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        return new ResponseList<>(
                saloonReportsRepository.staffEarningsSince(LoggerUser.getBranchUID(), monthStart)
        );
    }

}
