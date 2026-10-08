package com.midland.saloon.Config.AttachConfig;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {
}
