package com.midland.saloon.Saloon.Service;

import com.midland.saloon.Saloon.Dto.StockTakeDTO;
import com.midland.saloon.Saloon.Model.StockTake;
import com.midland.saloon.Saloon.Model.StockTakeLine;
import com.midland.saloon.Saloon.Model.Store;
import com.midland.saloon.Saloon.Repository.StockTakeLineRepository;
import com.midland.saloon.Saloon.Repository.StockTakeRepository;
import com.midland.saloon.Saloon.Repository.StoreRepository;
import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Counting the store at one go. In the saloon the store is its consumables
 * (shampoo, oil, powder), counted in units: what the system holds for an item
 * is its unopened units (Store.notUsedQuantity - an item leaves that count when
 * it is opened for use). A count sets it to what is on the shelf and keeps the
 * difference, valued at the item's buying price, for the variance report.
 */
@Service
@RequiredArgsConstructor
public class StockTakeService {

    private final StoreRepository storeRepository;
    private final StockTakeRepository stockTakeRepository;
    private final StockTakeLineRepository lineRepository;

    /** What a count goes through: every active store item with its unopened units now. */
    public ResponseList<Map<String, Object>> countable() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Store s : storeRepository.findCountable(LoggerUser.getBranchUID())) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("uid", s.getUid());
            row.put("serviceName", s.getNameOfStore());
            row.put("serviceCode", s.getCodeOfStore());
            row.put("unit", null);
            row.put("packUnit", null);
            row.put("unitsPerPack", 1);
            row.put("stockQuantity", s.getNotUsedQuantity() == null ? 0 : s.getNotUsedQuantity());
            row.put("buyingPrice", s.getBuyingPrice() == null ? 0 : s.getBuyingPrice());
            rows.add(row);
        }
        return new ResponseList<>(rows);
    }

    @Transactional
    public Response<StockTake> submit(StockTakeDTO dto) {
        String branchUID = LoggerUser.getBranchUID();
        if (dto == null || dto.getLines() == null || dto.getLines().isEmpty())
            return new Response<>("Count at least one item");
        Set<String> seen = new HashSet<>();
        for (StockTakeDTO.Line line : dto.getLines()) {
            if (line.getBarServiceUID() == null || !seen.add(line.getBarServiceUID()))
                return new Response<>("Each item may be counted once");
            if (line.getCountedUnits() == null || line.getCountedUnits() < 0)
                return new Response<>("Enter a count of zero or more for every item");
        }

        LocalDateTime now = LocalDateTime.now();
        StockTake take = new StockTake();
        take.setTakenBy(LoggerUser.getEmail());
        take.setTakenByName(nameOf(LoggerUser.getUser()));
        take.setTakenAt(now);
        String note = dto.getNote() == null ? null : dto.getNote().trim();
        take.setNote(note == null || note.isEmpty() ? null : (note.length() > 500 ? note.substring(0, 500) : note));
        take.setItemsCounted(dto.getLines().size());
        take = stockTakeRepository.save(take);

        int different = 0;
        long loss = 0, gain = 0;
        for (StockTakeDTO.Line in : dto.getLines()) {
            Store item = storeRepository.findForUpdate(in.getBarServiceUID(), branchUID)
                    .filter(s -> s.getIsActive() == null || s.getIsActive())
                    .orElseThrow(() -> new IllegalArgumentException("An item on the count is not in this branch's store"));
            int before = item.getNotUsedQuantity() == null ? 0 : item.getNotUsedQuantity();
            int counted = in.getCountedUnits();
            int diff = counted - before;
            long value = (long) diff * (item.getBuyingPrice() == null ? 0 : item.getBuyingPrice());

            StockTakeLine line = new StockTakeLine();
            line.setStockTake(take);
            line.setStoreUid(item.getUid());
            line.setServiceName(item.getNameOfStore());
            line.setServiceCode(item.getCodeOfStore());
            line.setUnitsPerPack(1);
            line.setSystemUnits(before);
            line.setCountedUnits(counted);
            line.setDifferenceUnits(diff);
            line.setDifferenceValue(value);
            lineRepository.save(line);

            if (diff != 0) {
                different++;
                if (value < 0) loss += value; else gain += value;
                item.setNotUsedQuantity(counted);
                item.update();
                storeRepository.save(item);
            }
        }
        take.setItemsDifferent(different);
        take.setLossValue(loss);
        take.setGainValue(gain);
        return new Response<>(stockTakeRepository.save(take));
    }

    public ResponseList<StockTake> findTaken(String filter) {
        LocalDateTime[] range = ReportRange.of(filter);
        return new ResponseList<>(stockTakeRepository.findTaken(LoggerUser.getBranchUID(), range[0], range[1]));
    }

    public ResponseList<StockTakeLine> findLines(String takeUid) {
        if (stockTakeRepository.findInBranch(takeUid, LoggerUser.getBranchUID()).isEmpty())
            return new ResponseList<>("Stock take not found");
        return new ResponseList<>(lineRepository.findByTake(takeUid));
    }

    /** Per item over a period: how often counted, units missing or over, and what that was worth. */
    public ResponseList<Map<String, Object>> varianceByProduct(String filter) {
        LocalDateTime[] range = ReportRange.of(filter);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object[] r : lineRepository.varianceByProduct(LoggerUser.getBranchUID(), range[0], range[1])) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("barServiceUid", r[0]);
            row.put("serviceName", r[1]);
            row.put("serviceCode", r[2]);
            row.put("unit", r[3]);
            row.put("packUnit", r[4]);
            row.put("unitsPerPack", r[5]);
            row.put("counts", ((Number) r[6]).intValue());
            row.put("differenceUnits", r[7] == null ? 0 : ((Number) r[7]).intValue());
            row.put("differenceValue", r[8] == null ? 0 : ((Number) r[8]).longValue());
            rows.add(row);
        }
        return new ResponseList<>(rows);
    }

    private static String nameOf(User user) {
        if (user == null)
            return null;
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isEmpty() ? user.getUsername() : name;
    }
}
