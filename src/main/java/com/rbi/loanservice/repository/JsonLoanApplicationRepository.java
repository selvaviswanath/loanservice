package com.rbi.loanservice.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rbi.loanservice.domain.ApplicationStatus;
import com.rbi.loanservice.domain.LoanApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON-file-backed repository.
 * Reads the entire file on each query (negligible cost for a demo), writes atomically on save.
 * Thread-safe: all mutating operations are synchronized.
 */
@Repository
public class JsonLoanApplicationRepository {

    private final File dbFile;
    private final ObjectMapper mapper;

    public JsonLoanApplicationRepository(@Value("${app.json-db-path}") String dbPath) {
        this.dbFile = new File(dbPath);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Ensure parent directories exist
        if (dbFile.getParentFile() != null) {
            dbFile.getParentFile().mkdirs();
        }
    }

    // ——— Write ———

    public synchronized LoanApplication save(LoanApplication app) {
        List<LoanApplication> all = readAll();

        if (app.getId() == null) {
            // New record — assign ID and timestamp
            app.setId(UUID.randomUUID());
            app.setCreatedAt(LocalDateTime.now());
            all.add(app);
        } else {
            // Update existing record
            all.replaceAll(existing ->
                    existing.getId().equals(app.getId()) ? app : existing);
        }

        writeAll(all);
        return app;
    }

    // ——— Read ———

    public Optional<LoanApplication> findById(UUID id) {
        return readAll().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    /**
     * Filtered, sorted list — sorted newest first.
     * Returns a subpage of results based on page/size.
     * Returns: [filteredPage, totalCount]
     */
    public List<LoanApplication> findWithFilters(
            ApplicationStatus status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size) {

        List<LoanApplication> filtered = readAll().stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> from == null || !a.getCreatedAt().isBefore(from))
                .filter(a -> to == null || !a.getCreatedAt().isAfter(to))
                .sorted(Comparator.comparing(LoanApplication::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        return start >= filtered.size() ? List.of() : filtered.subList(start, end);
    }

    public long countWithFilters(
            ApplicationStatus status,
            LocalDateTime from,
            LocalDateTime to) {

        return readAll().stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> from == null || !a.getCreatedAt().isBefore(from))
                .filter(a -> to == null || !a.getCreatedAt().isAfter(to))
                .count();
    }

    // ——— Internal ———

    private List<LoanApplication> readAll() {
        if (!dbFile.exists() || dbFile.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(mapper.readValue(dbFile, new TypeReference<>() {}));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON database: " + dbFile.getAbsolutePath(), e);
        }
    }

    private void writeAll(List<LoanApplication> apps) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, apps);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON database: " + dbFile.getAbsolutePath(), e);
        }
    }
}
