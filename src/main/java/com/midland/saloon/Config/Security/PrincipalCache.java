package com.midland.saloon.Config.Security;

import com.midland.saloon.Uaa.Model.User;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The signed-in user behind a token, kept for a few seconds.
 *
 * Every request used to rebuild the principal from the database - the user
 * with branch and roles, then the roles' permissions, then the extra branches:
 * three statements before the endpoint itself ran its one. A screen of the app
 * fires several requests at once, and the heartbeat one every thirty seconds,
 * so most of what the database did was this. The permissions that guard the
 * endpoints come from the token, not from here; the user only answers "who"
 * and "which branch".
 *
 * Kept per username and branch claim, already pointed at that branch, and
 * never changed after it is stored - two devices working in two branches get
 * two entries. Any change to a user, branch, role or permission row (see
 * {@link Evict}) drops everything, so a block, a delete, a role or a branch
 * change is seen on the very next request; the short TTL covers what changes
 * behind Hibernate's back (bulk updates, JDBC).
 */
public final class PrincipalCache {

    private static final long TTL_MILLIS = 20_000;
    private static final int MAX_ENTRIES = 2_000;

    private record Entry(User user, long loadedAt) {}

    private static final ConcurrentHashMap<String, Entry> CACHE = new ConcurrentHashMap<>();

    private PrincipalCache() {}

    /** The cached user for this key, or the loader's answer (cached only when not null). */
    public static User get(String username, String branchClaim, Supplier<User> loader) {
        String key = username + '\u0000' + (branchClaim == null ? "" : branchClaim);
        long now = System.currentTimeMillis();
        Entry hit = CACHE.get(key);
        if (hit != null && now - hit.loadedAt() < TTL_MILLIS)
            return hit.user();
        User user = loader.get();
        if (user != null) {
            if (CACHE.size() >= MAX_ENTRIES)
                CACHE.clear();
            CACHE.put(key, new Entry(user, now));
        } else {
            CACHE.remove(key);
        }
        return user;
    }

    /** Forget every cached principal - after anything that may change who someone is or where they work. */
    public static void evictAll() {
        CACHE.clear();
    }

    /** Entity listener: any write to a user, branch, role or permission row empties the cache. */
    public static class Evict {
        @PostPersist
        @PostUpdate
        @PostRemove
        void changed(Object entity) {
            evictAll();
        }
    }
}
