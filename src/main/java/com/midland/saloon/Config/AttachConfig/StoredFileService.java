package com.midland.saloon.Config.AttachConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Keeps uploaded pictures in the database as well as the upload folder, and
 * puts a picture back in the folder when a deploy has wiped it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoredFileService {

    private final StoredFileRepository repository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void save(String name, String contentType, byte[] data) {
        StoredFile file = new StoredFile();
        file.setName(name);
        file.setContentType(contentType);
        file.setData(data);
        repository.save(file);
    }

    public void delete(String name) {
        if (name != null && !name.isBlank() && repository.existsById(name)) {
            repository.deleteById(name);
        }
    }

    /**
     * Writes the picture back to the upload folder if it is missing there
     * but the database has it. Names with a path in them are refused.
     */
    public void restoreIfMissing(String name) {
        if (name == null || name.isBlank() || name.contains("/") || name.contains("\\") || name.contains("..")) {
            return;
        }
        Path target = Paths.get(uploadDir).resolve(name);
        if (Files.exists(target)) {
            return;
        }
        repository.findById(name).ifPresent(file -> {
            try {
                Files.createDirectories(target.getParent() == null ? Paths.get(uploadDir) : target.getParent());
                Files.write(target, file.getData());
            } catch (IOException e) {
                log.warn("Could not restore uploaded file {}", name, e);
            }
        });
    }
}
