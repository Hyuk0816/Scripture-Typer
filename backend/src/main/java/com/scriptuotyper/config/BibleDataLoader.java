package com.scriptuotyper.config;

import com.scriptuotyper.repository.BibleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BibleDataLoader implements CommandLineRunner {

    private static final String CSV_FILE = "bible/BIBLE_202602082053.csv";
    private static final int BATCH_SIZE = 500;

    private final BibleRepository bibleRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        long count = bibleRepository.count();
        if (count > 0) {
            log.info("Bible data already loaded ({} rows). Skipping.", count);
            return;
        }

        log.info("Loading Bible CSV data...");
        long start = System.currentTimeMillis();

        try {
            loadCsvData();
            long elapsed = System.currentTimeMillis() - start;
            long total = bibleRepository.count();
            log.info("Bible data loaded: {} rows in {}ms", total, elapsed);
        } catch (Exception e) {
            log.error("Failed to load Bible CSV data", e);
        }
    }

    private void loadCsvData() throws Exception {
        var resource = new ClassPathResource(CSV_FILE);
        Map<Integer, String> bookNames = new HashMap<>();
        List<Object[]> batch = new ArrayList<>();
        int inserted = 0;

        try (var reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = parseCsvLine(line);
                if (cols.length < 4 || cols[0].isEmpty()) continue;

                int book = toInt(cols[0]);
                int chapter = toInt(cols[1]);
                int verse = toInt(cols[2]);
                String text = cols[3];

                // Metadata rows: BOOK=0
                if (book == 0) {
                    if (chapter == 10 && verse > 0) {
                        bookNames.put(verse, text);
                    }
                    continue;
                }

                String bookName = bookNames.getOrDefault(book, "Unknown");
                String testament = book <= 39 ? "OLD" : "NEW";

                batch.add(new Object[]{testament, bookName, book, chapter, verse, text});

                if (batch.size() >= BATCH_SIZE) {
                    insertBatch(batch);
                    inserted += batch.size();
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                insertBatch(batch);
                inserted += batch.size();
            }
        }

        log.info("Inserted {} Bible verses", inserted);
    }

    private void insertBatch(List<Object[]> batch) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO bible (testament, book_name, book_order, chapter, verse, content) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                batch
        );
    }

    private static int toInt(String floatStr) {
        return (int) Double.parseDouble(floatStr.trim());
    }

    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());

        return fields.toArray(new String[0]);
    }
}