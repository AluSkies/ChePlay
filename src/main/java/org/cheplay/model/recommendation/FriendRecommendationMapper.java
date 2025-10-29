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
public class FriendRecommendationMapper {

    private final DbConnector db;

    public FriendRecommendationMapper(DbConnector db) {
        this.db = Objects.requireNonNull(db);
    }

    public List<Map<String, Object>> decorateRankedUsers(List<Map.Entry<String, Double>> ranked, String scoreKey) {
        if (ranked == null || ranked.isEmpty()) return List.of();

        List<Map.Entry<String, Double>> ordered = new ArrayList<>(ranked);
        Set<String> ids = ordered.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, UserMetadata> metadata = fetchUserMetadata(ids);
        List<Map<String, Object>> out = new ArrayList<>(ordered.size());
        for (Map.Entry<String, Double> entry : ordered) {
            String id = entry.getKey();
            double score = entry.getValue();
            UserMetadata meta = metadata.get(id);

            Map<String, Object> row = new HashMap<>();
            row.put("id", id);
            row.put(scoreKey, score);
            if (meta != null) {
                if (!meta.displayName().isEmpty()) row.put("name", meta.displayName());
                if (!meta.username().isEmpty()) row.put("username", meta.username());
                String label = buildLabel(meta);
                if (!label.isEmpty()) row.put("label", label);
            }
            out.add(row);
        }
        return out;
    }

    public Map<String, Object> decorateUser(String userId) {
        if (userId == null) return null;
        Map<String, UserMetadata> meta = fetchUserMetadata(List.of(userId));
        UserMetadata data = meta.get(userId);
        if (data == null) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", userId);
            row.put("label", userId);
            return row;
        }
        Map<String, Object> row = new HashMap<>();
        row.put("id", data.id());
        if (!data.displayName().isEmpty()) row.put("name", data.displayName());
        if (!data.username().isEmpty()) row.put("username", data.username());
        String label = buildLabel(data);
        if (!label.isEmpty()) row.put("label", label);
        return row;
    }

    public Map<String, String> resolveLabels(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        Map<String, UserMetadata> metadata = fetchUserMetadata(userIds);
        Map<String, String> out = new HashMap<>(metadata.size());
        for (String id : userIds) {
            if (id == null) continue;
            UserMetadata meta = metadata.get(id);
            if (meta == null) {
                out.put(id, id);
                continue;
            }
            String label = buildLabel(meta);
            out.put(id, label.isEmpty() ? meta.id() : label);
        }
        return out;
    }

    public List<Map<String, Object>> decorateUsers(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        List<String> ordered = new ArrayList<>(userIds.size());
        for (String id : userIds) {
            if (id != null && !id.isBlank()) {
                ordered.add(id);
            }
        }
        if (ordered.isEmpty()) return List.of();

        Map<String, UserMetadata> metadata = fetchUserMetadata(ordered);
        List<Map<String, Object>> out = new ArrayList<>(ordered.size());
        for (String id : ordered) {
            UserMetadata meta = metadata.get(id);
            Map<String, Object> row = new HashMap<>();
            String resolvedId = meta != null ? meta.id() : id;
            row.put("id", resolvedId);
            if (meta != null) {
                if (!meta.displayName().isEmpty()) row.put("name", meta.displayName());
                if (!meta.username().isEmpty()) row.put("username", meta.username());
                String label = buildLabel(meta);
                row.put("label", label.isEmpty() ? resolvedId : label);
            } else {
                row.put("label", id);
            }
            out.add(row);
        }

        return out;
    }

    private Map<String, UserMetadata> fetchUserMetadata(Collection<String> userKeys) {
        if (userKeys == null || userKeys.isEmpty()) return Map.of();
        String cypher = """
            MATCH (p:User)
            WHERE coalesce(p.id,p.nombre,p.name) IN $keys
            RETURN coalesce(p.id,p.nombre,p.name) AS key,
                   coalesce(p.name,p.nombre,'') AS displayName,
                   coalesce(p.username,'') AS username
            """;

        Map<String, Object> params = Map.of("keys", new ArrayList<>(userKeys));
        List<UserMetadata> rows = db.readList(cypher, params, record -> {
            String id = record.get("key").asString("").trim();
            if (id.isEmpty()) return null;
            String name = record.get("displayName").asString("").trim();
            String username = record.get("username").asString("").trim();
            return new UserMetadata(id, name, username);
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());

        Map<String, UserMetadata> meta = new HashMap<>(rows.size());
        for (UserMetadata item : rows) {
            meta.putIfAbsent(item.id(), item);
        }
        return meta;
    }

    private String buildLabel(UserMetadata meta) {
        if (meta == null) return "";
        String id = meta.id();
        String name = meta.displayName();
        if (!name.isBlank()) return id + " - " + name;
        if (!meta.username().isBlank()) return id + " - " + meta.username();
        return id;
    }

    private record UserMetadata(String id, String displayName, String username) {}
}
