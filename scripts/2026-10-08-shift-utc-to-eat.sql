-- One-off (2026-10-08): until 18:08:32 EAT on 2026-10-08 the Saloon backend
-- ran on UTC, so every time it stamped was 3 hours early. Everything written
-- before then is moved 3 hours on; anything written after is already
-- Tanzania time and is left alone. The cut is 16:00 as stored: old values
-- stop by about 15:08 (UTC), new ones start at 18:08, so nothing
-- falls on the wrong side even if the old server ran a few seconds longer.
--
-- Safe to run only once: a marker table refuses a second run.
-- Run: psql "<database URL>" -v ON_ERROR_STOP=1 -f <this file>

BEGIN;

CREATE TABLE tz_shift_2026_10_08 (done_at timestamp NOT NULL DEFAULT now());
INSERT INTO tz_shift_2026_10_08 DEFAULT VALUES;

-- Days first, while the old times still say which rows fell between 00:00
-- and 03:00 Tanzania time (21:00-24:00 UTC): those were filed under the
-- previous day.
UPDATE saloon_sales SET created_at = (sold_at + interval '3 hours')::date
 WHERE sold_at < timestamp '2026-10-08 16:00' AND created_at = sold_at::date AND sold_at::time >= '21:00';
UPDATE work_shifts SET created_at = (opened_at + interval '3 hours')::date
 WHERE opened_at < timestamp '2026-10-08 16:00' AND created_at = opened_at::date AND opened_at::time >= '21:00';
UPDATE stock_takes SET created_at = (taken_at + interval '3 hours')::date
 WHERE taken_at < timestamp '2026-10-08 16:00' AND created_at = taken_at::date AND taken_at::time >= '21:00';
UPDATE stock_receipts SET created_at = (received_at + interval '3 hours')::date
 WHERE received_at < timestamp '2026-10-08 16:00' AND created_at = received_at::date AND received_at::time >= '21:00';

-- Then every date-and-time column in the schema.
DO $$
DECLARE
    c record;
    n bigint;
BEGIN
    FOR c IN
        SELECT table_name, column_name
          FROM information_schema.columns
         WHERE table_schema = 'public'
           AND data_type = 'timestamp without time zone'
           AND table_name <> 'tz_shift_2026_10_08'
         ORDER BY 1, 2
    LOOP
        EXECUTE format(
            'UPDATE %I SET %I = %I + interval ''3 hours'' WHERE %I < timestamp ''2026-10-08 16:00''',
            c.table_name, c.column_name, c.column_name, c.column_name);
        GET DIAGNOSTICS n = ROW_COUNT;
        IF n > 0 THEN
            RAISE NOTICE '%.%: % rows', c.table_name, c.column_name, n;
        END IF;
    END LOOP;
END $$;

COMMIT;
