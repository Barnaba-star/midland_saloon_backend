package com.midland.saloon.Saloon.Service;

import com.midland.saloon.Saloon.Repository.InsightRepository;
import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Utils.Responses.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The owner's view of what the saloon earns and holds:
 * - sales per service and what sells most (a service's buying cost is not kept,
 *   so profit and margin are reported as unknown rather than guessed);
 * - store items lying unopened;
 * - when it sells, by weekday and hour;
 * - every pot's balance since the branch began.
 */
@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository repository;

    public Response<Map<String, Object>> products(String filter) {
        LocalDateTime[] range = ReportRange.of(filter);
        LocalDate from = range[0].toLocalDate(), to = range[1].toLocalDate();
        String branchUID = LoggerUser.getBranchUID();

        List<Map<String, Object>> rows = new ArrayList<>();
        long revenue = 0, units = 0;
        for (Object[] r : repository.salesByService(branchUID, from, to)) {
            long qty = num(r[3]), rev = num(r[4]);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("uid", r[0]);
            row.put("serviceName", r[1]);
            row.put("serviceCode", r[2]);
            row.put("units", qty);
            row.put("revenue", rev);
            row.put("cost", null);
            row.put("profit", null);
            row.put("marginPercent", null);
            row.put("costKnown", false);
            rows.add(row);
            revenue += rev;
            units += qty;
        }

        List<Map<String, Object>> slow = new ArrayList<>();
        long idleValue = 0;
        for (Object[] r : repository.idleStoreItems(branchUID, from, to)) {
            long stock = num(r[3]), buying = num(r[4]);
            long value = stock * buying;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("uid", r[0]);
            row.put("serviceName", r[1]);
            row.put("serviceCode", r[2]);
            row.put("stock", stock);
            row.put("stockValue", value);
            row.put("lastSold", r[5]);
            slow.add(row);
            idleValue += value;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("products", rows);
        body.put("revenue", revenue);
        body.put("units", units);
        body.put("revenueWithCost", 0L);
        body.put("cost", 0L);
        body.put("profit", 0L);
        body.put("marginPercent", null);
        body.put("costKnown", false);
        body.put("slowMovers", slow);
        body.put("idleStockValue", idleValue);
        return new Response<>(body);
    }

    public Response<Map<String, Object>> peakHours(String filter) {
        LocalDateTime[] range = ReportRange.of(filter);
        List<Map<String, Object>> cells = new ArrayList<>();
        Map<Integer, Long> byHour = new TreeMap<>();
        Map<Integer, Long> byDay = new TreeMap<>();
        for (Object[] r : repository.salesByHour(LoggerUser.getBranchUID(), range[0].toLocalDate(), range[1].toLocalDate())) {
            int dow = (int) num(r[0]), hour = (int) num(r[1]);
            long amount = num(r[2]), units = num(r[3]);
            Map<String, Object> cell = new LinkedHashMap<>();
            cell.put("day", dow);
            cell.put("hour", hour);
            cell.put("amount", amount);
            cell.put("units", units);
            cells.add(cell);
            byHour.merge(hour, amount, Long::sum);
            byDay.merge(dow, amount, Long::sum);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cells", cells);
        body.put("byHour", byHour);
        body.put("byDay", byDay);
        return new Response<>(body);
    }

    /** Every pot since the branch began - collected, spent, left - with this week beside it. */
    public Response<Map<String, Object>> potsLedger() {
        String branchUID = LoggerUser.getBranchUID();
        LocalDateTime[] week = ReportRange.of("WEEK");
        Map<String, long[]> weekly = new LinkedHashMap<>();
        for (Object[] r : repository.potsInWeeks(branchUID, week[0].toLocalDate(), week[1].toLocalDate()))
            weekly.put(String.valueOf(r[0]), new long[]{num(r[1]), num(r[2])});

        List<Map<String, Object>> pots = new ArrayList<>();
        long in = 0, out = 0;
        for (Object[] r : repository.potsAllTime(branchUID)) {
            String name = String.valueOf(r[0]);
            long collected = num(r[1]), spent = num(r[2]);
            long[] w = weekly.getOrDefault(name, new long[]{0, 0});
            Map<String, Object> pot = new LinkedHashMap<>();
            pot.put("name", name);
            pot.put("collected", collected);
            pot.put("spent", spent);
            pot.put("balance", collected - spent);
            pot.put("weekCollected", w[0]);
            pot.put("weekSpent", w[1]);
            pots.add(pot);
            in += collected;
            out += spent;
        }
        pots.sort((a, b) -> Long.compare((long) b.get("balance"), (long) a.get("balance")));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pots", pots);
        body.put("collected", in);
        body.put("spent", out);
        body.put("balance", in - out);
        return new Response<>(body);
    }

    private static long num(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return Math.round(n.doubleValue());
        return Math.round(Double.parseDouble(String.valueOf(o)));
    }
}
