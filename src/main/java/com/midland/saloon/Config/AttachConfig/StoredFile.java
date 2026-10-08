package com.midland.saloon.Config.AttachConfig;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A copy of an uploaded picture (profile photo, company logo) in Postgres.
 * The upload folder is only a cache: on Render's free plan it lives in /tmp
 * and every deploy empties it, so the database copy is what survives.
 */
@Entity
@Table(name = "stored_files")
@Getter
@Setter
@NoArgsConstructor
public class StoredFile {

    /** The stored name the rest of the system keeps (e.g. profile-<uuid>.png). */
    @Id
    @Column(length = 200)
    private String name;

    @Column(length = 100)
    private String contentType;

    @Column(columnDefinition = "bytea", nullable = false)
    private byte[] data;

    private LocalDateTime createdAt = LocalDateTime.now();
}
