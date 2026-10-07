package com.midland.saloon.Saloon.Controller;

import com.midland.saloon.Saloon.Dto.*;
import com.midland.saloon.Saloon.Model.*;
import com.midland.saloon.Saloon.Projection.*;
import com.midland.saloon.Saloon.Service.SaloonService;
import com.midland.saloon.Saloon.Service.ServiceAndStoreReportResponse;
import com.midland.saloon.Uaa.Dto.AssignUserRoleDTO;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Service.UserService;
import com.midland.saloon.Utils.PageableParam;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import com.midland.saloon.Utils.Responses.ResponsePage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/saloon")
@RequiredArgsConstructor
public class SaloonController {
    private final SaloonService saloonService;
    private final com.midland.saloon.Saloon.Service.InsightService insightService;
    private final com.midland.saloon.Saloon.Service.CashUpService cashUpService;
    private final com.midland.saloon.Saloon.Service.StockTakeService stockTakeService;
    private final com.midland.saloon.Saloon.Service.OtherCommissionService otherCommissionService;
    private final UserService userService;

    /***
     METHODS FOR SALOON SETTING
     ***/
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_COMMISSION')")
    @PostMapping("/saveCommissions")
    public Response<Commission> saveCommissions(@RequestBody CommissionDTO commissionDTO){
        return saloonService.saveCommissions(commissionDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_COMMISSION')")
    @GetMapping("/findCommissionByUID/{commissionUID}")
    public Response<Commission> findCommissionByUID(@PathVariable String commissionUID){
        return saloonService.findCommissionByUID(commissionUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_COMMISSION')")
    @PostMapping("/findCommissionList")
    public ResponseList<CommissionProjection> findCommissionList(){
        return saloonService.findCommissionList();
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_COMMISSION')")
    @PostMapping("/findCommissionPage")
    public ResponsePage<CommissionProjection> findCommissionPage(@RequestBody PageableParam pageableParam){
        return saloonService.findCommissionPage(pageableParam.getPage(), pageableParam.getSize());
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_COMMISSION')")
    @PostMapping("/deleteCommission/{commissionUID}")
    public Response<Commission> deleteCommission(@PathVariable String commissionUID){
        return saloonService.deleteCommission(commissionUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_USER')")
    @PostMapping("/findUserPageByBranch")
    public ResponsePage<User> findUserPageByBranch(@RequestBody PageableParam pageableParam){
        return saloonService.findUserPageByBranch(pageableParam.getPage(), pageableParam.getSize());
    }
    /**
     * The POS side of revoking a branch user's access. Same rule as the
     * settings one - blocked, not deleted, because their uid is what the
     * commission report and every payment record point at.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('ENABLE_OR_DISABLE_USER')")
    @PostMapping("/setUserBlocked/{userUID}/{blocked}")
    public Response<String> setUserBlocked(@PathVariable String userUID, @PathVariable Boolean blocked){
        return userService.setAccountBlocked(userUID, blocked);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('ASSIGN_USER_ROLE')")
    @PostMapping("/assignOrUnAssignUserRoleByBranch")
    public Response<User> assignOrUnAssignUserRoleByBranch(@RequestBody AssignUserRoleDTO assignUserRoleDTO){
        return userService.assignOrUnAssignUserRole(assignUserRoleDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_USER')")
    @PostMapping("/deleteUserByBranch/{userUID}")
    public Response<User> deleteUserByBranch(@PathVariable String userUID) {
        return userService.deleteUser(userUID);
    }

    /***
     METHODS FOR SALOON SERVICE
     ***/
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_SERVICE')")
    @PostMapping("/saveSaloonEntity")
    public Response<SaloonServiceEntity> saveSaloonEntity(@Valid @RequestBody SaloonServiceDTO saloonServiceDTO){
        return saloonService.saveSaloonService(saloonServiceDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SERVICE')")
    @GetMapping("/findSaloonServiceByUID/{saloonServiceUID}")
    public Response<SaloonServiceEntity> findSaloonServiceByUID(@PathVariable String saloonServiceUID){
        return saloonService.findSaloonServiceByUID(saloonServiceUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_SERVICE')")
    @PostMapping("/deleteServiceSaloonByUID/{saloonServiceUID}")
    public Response<SaloonServiceEntity> deleteServiceSaloonByUID(@PathVariable String saloonServiceUID){
        return saloonService.deleteServiceSaloonByUID(saloonServiceUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SERVICE')")
    @GetMapping("/findSaloonServiceList")
    public ResponseList<SaloonProjection> findSaloonServiceList(){
        return saloonService.findSaloonServiceList();
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SERVICE')")
    @PostMapping("/findSaloonServicePage")
    public ResponsePage<SaloonProjection> findSaloonServicePage(@RequestBody PageableParam pageableParam){
        return saloonService.findSaloonServicePage(pageableParam.getPage(), pageableParam.getSize());
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SERVICE')")
    @GetMapping("/findServiceEntityUIDList/{status}")
    public ResponseList<SaloonProjection> findServiceEntityUIDList(@PathVariable String status){
        return saloonService.findServiceEntityUIDList(status);
    }

    /***
     METHODS FOR SALOON STAFFS
     ***/
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STAFF')")
    @PostMapping("/saveSaloonStaff")
    public Response<SaloonStaff> saveSaloonStaff(@RequestBody SaloonStaffDTO saloonStaffDTO){
        return saloonService.saveSaloonStaff(saloonStaffDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_SALOON')")
    @PostMapping("/deleteSaloonStaff/{saloonStaffUID}")
    public Response<SaloonStaff> deleteSaloonStaff(@PathVariable String saloonStaffUID){
        return saloonService.deleteSaloonStaff(saloonStaffUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STAFF')")
    @GetMapping("/findSaloonStaffByUID/{saloonStaffUID}")
    public Response<SaloonStaff> findSaloonStaffByUID(@PathVariable String saloonStaffUID){
        return saloonService.findSaloonStaffByUID(saloonStaffUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STAFF')")
    @GetMapping("/findSaloonStaffList")
    public ResponseList<SaloonProjection> findSaloonStaffList(){
        return saloonService.findSaloonStaffList();
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STAFF')")
    @PostMapping("/findSaloonStaffPage")
    public ResponsePage<SaloonProjection> findSaloonStaffPage(@RequestBody PageableParam pageableParam){
        return saloonService.findSaloonStaffPage(pageableParam.getPage(), pageableParam.getSize());
    }

    /***
     METHODS FOR SALOON SALES
     ***/
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_SALES')")
    @PostMapping("/saveSaloonSales")
    public ResponseList<SaloonSales> saveSaloonSales(@RequestBody SaloonSalesDTO saloonSalesDTO){
        return saloonService.saveSaloonSales(saloonSalesDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SALES)")
    @GetMapping("/findSaloonSalesByUID/{saloonSalesUID}")
    public Response<SaloonProjection> findSaloonSalesByUID(@PathVariable String saloonSalesUID){
        return saloonService.findSaloonSalesByUID(saloonSalesUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SALES')")
    @GetMapping("/findSaloonSalesList/{saloonOpenUID}")
    public ResponseList<SaloonProjection> findSaloonSalesList(@PathVariable String saloonOpenUID){
        return saloonService.findSaloonSalesList(saloonOpenUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SALES')")
    @GetMapping("/findSaloonSalesListActiveTrue")
    public ResponseList<SaloonProjection> findSaloonSalesListActiveTrue(){
        return saloonService.findSaloonSalesListActiveTrue();
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SALES')")
    @GetMapping("/findSaloonSalesPage")
    public ResponsePage<SaloonProjection> findSaloonSalesPage(@RequestBody PageableParam pageableParam){
        return saloonService.findSaloonSalesPage(pageableParam.getPage(), pageableParam.getSize());
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_SALES')")
    @PostMapping("/deleteSaloonSales/{saloonSalesUID}")
    public Response<SaloonSales> deleteSaloonSales(@PathVariable String saloonSalesUID){
        return saloonService.deleteSaloonSales(saloonSalesUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SALES')")
    @GetMapping("/salesOpenedList")
    public ResponseList<SalesOpened> salesOpenedList(){
        return saloonService.salesOpenedList();
    }

    /** An empty bill opened by mistake - whoever may open a bill may take an empty one away. */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_SALES')")
    @PostMapping("/deleteEmptyBill/{billUid}")
    public Response<SalesOpened> deleteEmptyBill(@PathVariable String billUid){
        return saloonService.deleteEmptyBill(billUid);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_SALES')")
    @PostMapping("/saveOpenSale")
    public Response<SalesOpened> saveOpenSale(@RequestBody SaleOpenedDTO saleOpenedDTO){
        return saloonService.saveOpenSale(saleOpenedDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SALES')")
    @GetMapping("/salesOpenedListByStatus/{filter}")
    public ResponseList<SalesOpened> salesOpenedListByStatus(@PathVariable String filter) {
        return saloonService.salesOpenedListByStatus(filter);
    }

    /***
     METHODS FOR REPORT PERMISSION
     ***/
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT)")
    @GetMapping("/findSaloonReportByUID/{saloonReportUID}")
    public Response<SaloonReports> findSaloonReportByUID(@PathVariable String saloonReportUID){
        return saloonService.findSaloonReportByUID(saloonReportUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @PostMapping("/findSaloonReportsPage")
    public ResponsePage<SaloonProjection> findSaloonReportsPage(@RequestBody PageableParam pageableParam){
        return saloonService.findSaloonReportsPage(pageableParam.getPage(), pageableParam.getSize(), pageableParam.getDate());
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @PostMapping("/findCurrentSaloonReportsPage")
    public ResponsePage<SaloonProjection> findCurrentSaloonReportsPage(@RequestBody PageableParam pageableParam) {
        return saloonService.findCurrentSaloonReportsPage(pageableParam.getPage(), pageableParam.getSize(), pageableParam.getFilter());
    }


    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/findSaloonRevenueReport/{date}")
    public Response<SaloonProjection> findSaloonRevenueReport(@PathVariable LocalDate date){
        return saloonService.findSaloonRevenueReport(date);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/findCurrentSaloonRevenueReport/{filter}")
    public Response<SaloonProjection> findCurrentSaloonRevenueReport(@PathVariable String filter){
        return saloonService.findCurrentSaloonRevenueReport(filter);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/findSaloonRevenueByService/{date}")
    public ResponseList<SaloonServiceRevenueProjection> findSaloonRevenueByService(@PathVariable LocalDate date) {
        return saloonService.findSaloonRevenueByService(date);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/findCurrentSaloonRevenueByService/{filter}")
    public ResponseList<SaloonServiceRevenueProjection> findCurrentSaloonRevenueByService(@PathVariable String filter) {
        return saloonService.findCurrentSaloonRevenueByService(filter);
    }


    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @PostMapping("/findStaffCommissionPage")
    public ResponsePage<SaloonProjection> findStaffCommissionPage(@RequestBody PageableParam pageableParam) {
        return saloonService.findStaffCommissionPage(pageableParam.getFilter(), pageableParam.getPage(), pageableParam.getSize());
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('PAY_STAFF')")
    @PostMapping("/payStaffCommission")
    public Response<StaffCommissions> payStaffCommission(@RequestBody  StaffCommissionDTO staffCommissionDTO){
        return saloonService.payStaffCommission(staffCommissionDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @PostMapping("/findServiceAndStoreReportPage")
    public ResponsePage<StoreReportSummaryProjection> findServiceAndStoreReportPage(@RequestBody PageableParam pageableParam) {
        return saloonService.findServiceAndStoreReportPage(pageableParam.getPage(), pageableParam.getSize(), pageableParam.getSearchParam());
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/findIncomeExpenses/{filter}")
    public ResponseList<IncomeExpenses> findIncomeExpenses(@PathVariable String filter){
        return saloonService.getIncomeExpenses(filter);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_EXPENSES')")
    @PostMapping("/addSpend")
    public Response<IncomeExpenses> addSpend(@RequestBody SpendDTO spendDTO) {
        return saloonService.addSpend(spendDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_EXPENSES')")
    @GetMapping("/findIncomeExpensesAndDescription/{incomeRef}")
    public Response<IncomeExpenses> findIncomeExpensesAndDescription(@PathVariable String incomeRef){
        return saloonService.findIncomeExpensesAndDescription(incomeRef);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/getStockAndPurchaseByFilter/{weekFilter}")
    public ResponseList<StockAndPurchaseProjection> getStockAndPurchaseByFilter(@PathVariable  String weekFilter){
        return saloonService.getStockAndPurchaseByFilter(weekFilter);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STOCK_AND_PURCHASE')")
    @PostMapping("/payStockAndPurchase")
    public Response<StockAndPurchase> payStockAndPurchase(@RequestBody  PayStockAndPurchaseDTO payStockAndPurchaseDTO){
        return saloonService.payStockAndPurchase(payStockAndPurchaseDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STOCK_AND_PURCHASE')")
    @GetMapping("/findStockPurchaseByUid/{stockUID}")
    public ResponseList<StockAndPurchaseDescriptions> findStockPurchaseByUid(@PathVariable  String stockUID){
        return saloonService.findStockPurchaseByUid(stockUID);
    }

    /***
     METHODS FOR SALOON SETTING
     ***/
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STORE')")
    @PostMapping("/saveStore")
    public Response<Store> saveStore(@RequestBody StoreDTO storeDTO){
        return saloonService.saveStore(storeDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STORE')")
    @PostMapping("/openStore")
    public Response<Store> openStore(@RequestBody StoreDTO storeDTO){
        return saloonService.openStore(storeDTO);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STORE')")
    @PostMapping("/addQuantityToStore")
    public Response<Store> addQuantityToStore(@RequestBody StoreDTO storeDTO){
        return saloonService.addQuantityToStore(storeDTO);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STORE')")
    @GetMapping("/findSaloonStoreList")
    public ResponseList<SaloonProjection> findSaloonStoreList(){
        return saloonService.findStoreList();
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STORE')")
    @PostMapping("/findSaloonStorePage")
    public ResponsePage<SaloonProjection> findSaloonStorePage(@RequestBody PageableParam pageableParam){
        return saloonService.findStorePage(pageableParam.getPage(), pageableParam.getSize());
    }


    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STORE')")
    @PostMapping("/findOpenStorePage")
    public ResponsePage<SaloonProjection> findOpenStorePage(@RequestBody PageableParam pageableParam){
        return saloonService.findOpenStorePage(pageableParam.getPage(), pageableParam.getSize(), pageableParam.getSearchParam());
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_STORE')")
    @PostMapping("/deleteStore/{storeUID}")
    public Response<Store> deleteStore(@PathVariable String storeUID){
        return saloonService.deleteSaloonStore(storeUID);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STORE')")
    @PostMapping("/closeOpenStore")
    public Response<StoreOpen> closeOpenStore(@RequestBody StoreDTO storeDTO){
        return saloonService.closeOpenStore(storeDTO);
    }

    // ---- POS home dashboard ----

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_SALES')")
    @GetMapping("/findBranchDashboard")
    public Response<DashboardSummaryDTO> findBranchDashboard() {
        return saloonService.findBranchDashboard();
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/findRevenueTrend/{days}")
    public ResponseList<DailyRevenueDTO> findRevenueTrend(@PathVariable int days) {
        return saloonService.findRevenueTrend(days);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/findStaffEarnings")
    public ResponseList<StaffEarningsProjection> findStaffEarnings() {
        return saloonService.findStaffEarnings();
    }

    /** What the branch's Other commission pays for - the CEO's list. */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('MANAGE_OTHER_COMMISSION')")
    @GetMapping("/otherCommissionItems")
    public ResponseList<com.midland.saloon.Saloon.Model.OtherCommissionItem> otherCommissionItems(){
        return otherCommissionService.findItems();
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('MANAGE_OTHER_COMMISSION')")
    @PostMapping("/saveOtherCommissionItems")
    public ResponseList<com.midland.saloon.Saloon.Model.OtherCommissionItem> saveOtherCommissionItems(@RequestBody java.util.List<com.midland.saloon.Saloon.Dto.OtherCommissionItemDTO> items){
        return otherCommissionService.saveItems(items);
    }

    /** How the Other commission was split over a period - the table under Other in Reports. */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/otherSplitReport/{filter}")
    public Response<java.util.Map<String, Object>> otherSplitReport(@PathVariable String filter){
        return otherCommissionService.report(filter);
    }

    /*
     CASH-UP - closing a shift. Whoever marks bills paid (SAVE_SALES) closes their
     own; the list follows VIEW_REPORT, narrowed to a cashier's own unless a manager.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_SALES')")
    @GetMapping("/cashUp/preview")
    public Response<java.util.Map<String, Object>> cashUpPreview(){
        return cashUpService.preview();
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_SALES')")
    @PostMapping("/cashUp/submit")
    public Response<com.midland.saloon.Saloon.Model.CashUp> cashUpSubmit(@RequestBody com.midland.saloon.Saloon.Dto.CashUpDTO dto){
        return cashUpService.submit(dto);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/cashUp/list/{filter}")
    public ResponseList<com.midland.saloon.Saloon.Model.CashUp> cashUps(@PathVariable String filter){
        return cashUpService.findClosed(filter);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/cashUp/{uid}/lines")
    public ResponseList<com.midland.saloon.Saloon.Model.CashUpLine> cashUpLines(@PathVariable String uid){
        return cashUpService.findLines(uid);
    }

    /*
     STOCK TAKE - counting the store's unopened items at once (SAVE_STORE);
     its history and the variance by item follow VIEW_STORE.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STORE')")
    @GetMapping("/stockTake/items")
    public ResponseList<java.util.Map<String, Object>> stockTakeItems(){
        return stockTakeService.countable();
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_STORE')")
    @PostMapping("/stockTake/submit")
    public Response<com.midland.saloon.Saloon.Model.StockTake> stockTakeSubmit(@RequestBody com.midland.saloon.Saloon.Dto.StockTakeDTO dto){
        return stockTakeService.submit(dto);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STORE')")
    @GetMapping("/stockTake/list/{filter}")
    public ResponseList<com.midland.saloon.Saloon.Model.StockTake> stockTakes(@PathVariable String filter){
        return stockTakeService.findTaken(filter);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STORE')")
    @GetMapping("/stockTake/{uid}/lines")
    public ResponseList<com.midland.saloon.Saloon.Model.StockTakeLine> stockTakeLines(@PathVariable String uid){
        return stockTakeService.findLines(uid);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_STORE')")
    @GetMapping("/stockTake/variance/{filter}")
    public ResponseList<java.util.Map<String, Object>> stockVariance(@PathVariable String filter){
        return stockTakeService.varianceByProduct(filter);
    }

    /*
     INSIGHTS - sales per service, best sellers, idle store items, peak hours,
     and every pot's balance since the start. Report readers (VIEW_REPORT).
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/insights/products/{filter}")
    public Response<java.util.Map<String, Object>> insightProducts(@PathVariable String filter){
        return insightService.products(filter);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/insights/peak/{filter}")
    public Response<java.util.Map<String, Object>> insightPeak(@PathVariable String filter){
        return insightService.peakHours(filter);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_REPORT')")
    @GetMapping("/insights/ledger")
    public Response<java.util.Map<String, Object>> insightLedger(){
        return insightService.potsLedger();
    }
}
