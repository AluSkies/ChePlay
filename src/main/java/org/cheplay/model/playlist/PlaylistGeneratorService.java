package org.cheplay.model.playlist;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.cheplay.neo4j.DbConnector;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Service;

/**
 * Orchestrates playlist generation: it fetches candidate songs from Neo4j and delegates the combination step to the backtracking helper.
 */
@Service
public class PlaylistGeneratorService {

    private final DbConnector dbConnector;
    private final PlaylistBacktrackingGenerator generator;

    public PlaylistGeneratorService(DbConnector dbConnector) {
        this.dbConnector = Objects.requireNonNull(dbConnector, "dbConnector");
        this.generator = new PlaylistBacktrackingGenerator();
    }

    public Optional<GeneratedPlaylist> generatePlaylist(String userId, PlaylistConstraints constraints) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(constraints, "constraints");

        List<PlaylistSong> candidates = fetchCandidateSongs(userId, constraints.getTargetSize() * 5);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        GeneratedPlaylist playlist = generator.generate(candidates, constraints);
        return Optional.ofNullable(playlist);
    }

    private List<PlaylistSong> fetchCandidateSongs(String userId, int limit) {
        String cypher = """
            MATCH (u:User)
            WHERE toLower(coalesce(u.id,u.nombre,u.name)) = toLower($userId)
            MATCH (u)-[:LISTENED]->(s:Song)
            RETURN coalesce(s.id,s.name,s.title) AS id,
                   coalesce(s.title,s.name,s.id) AS title,
                   coalesce(s.artist,s.band,'Unknown') AS artist,
                   coalesce(s.duration, 0) AS duration,
                   coalesce(s.playCount,s.plays,0) AS popularity
            ORDER BY popularity DESC, title ASC
            LIMIT $limit
            """;

    return dbConnector.readList(cypher, java.util.Map.of("userId", userId, "limit", limit), this::mapSong)
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    private PlaylistSong mapSong(Record record) {
        String id = record.get("id").asString("").trim();
        if (id.isEmpty()) {
            return null;
        }
        String title = record.get("title").asString("").trim();
        String artist = record.get("artist").asString("").trim();
        Integer duration = record.get("duration").isNull() ? null : record.get("duration").asInt();
        Integer popularity = record.get("popularity").isNull() ? null : record.get("popularity").asInt();
        return new PlaylistSong(id, title.isEmpty() ? id : title, artist.isEmpty() ? null : artist, duration, popularity);
    }
}
