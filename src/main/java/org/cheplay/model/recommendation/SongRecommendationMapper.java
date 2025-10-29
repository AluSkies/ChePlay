package org.cheplay.model.recommendation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.cheplay.neo4j.DbConnector;
import org.springframework.stereotype.Component;

@Component
public class SongRecommendationMapper {

    private final DbConnector db;

    public SongRecommendationMapper(DbConnector db) {
        this.db = Objects.requireNonNull(db);
    }

    public List<Map<String, Object>> toRecommendationList(Map<String, Double> scores, int limit) {
        if (scores == null || scores.isEmpty()) return List.of();

        int effectiveLimit = limit <= 0 ? Integer.MAX_VALUE : limit;
        List<Map.Entry<String, Double>> ordered = scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(effectiveLimit)
                .collect(Collectors.toList());

        Set<String> ids = ordered.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, SongMetadata> metadata = fetchSongMetadata(ids);
        List<Map<String, Object>> out = new ArrayList<>(ordered.size());

        for (Map.Entry<String, Double> entry : ordered) {
            String id = entry.getKey();
            double score = entry.getValue();
            SongMetadata data = metadata.get(id);

            Map<String, Object> row = new HashMap<>();
            row.put("id", id);
            row.put("score", score);

            if (data != null) {
                if (!data.artist().isEmpty()) row.put("artist", data.artist());
                if (!data.title().isEmpty()) row.put("title", data.title());
                String label = buildDisplayLabel(data);
                if (!label.isEmpty()) row.put("label", label);
            }
            out.add(row);
        }

        return out;
    }

    private Map<String, SongMetadata> fetchSongMetadata(Collection<String> songIds) {
        if (songIds == null || songIds.isEmpty()) return Map.of();

        String cypher = """
            MATCH (s:Song)
            WHERE coalesce(s.id,s.name,s.title) IN $ids
            RETURN coalesce(s.id,s.name,s.title) AS songId,
                   coalesce(s.artist, s.band, '') AS artist,
                   coalesce(s.title,s.name,s.id) AS title
            """;

        Map<String, Object> params = Map.of("ids", new ArrayList<>(songIds));

        List<SongMetadata> rows = db.readList(cypher, params, record -> {
            if (record == null || record.get("songId").isNull()) {
                return null;
            }
            String id = record.get("songId").asString("").trim();
            if (id.isEmpty()) {
                return null;
            }
            String artist = record.get("artist").asString("").trim();
            String title = record.get("title").asString("").trim();
            return new SongMetadata(id, artist, title);
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());

        Map<String, SongMetadata> meta = new HashMap<>(rows.size());
        for (SongMetadata item : rows) {
            meta.putIfAbsent(item.id(), item);
        }
        return meta;
    }

    private String buildDisplayLabel(SongMetadata meta) {
        if (meta == null) return "";
        String artist = meta.artist();
        String title = meta.title();
        if (!artist.isBlank() && !title.isBlank()) {
            return artist + " - " + title;
        }
        if (!title.isBlank()) return title;
        if (!artist.isBlank()) return artist;
        return "";
    }

    private record SongMetadata(String id, String artist, String title) { }
}
