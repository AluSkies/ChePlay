package org.cheplay.model.trending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cheplay.algorithm.greedy.GreedyExamples;
import org.cheplay.dto.SongPlay;
import org.cheplay.neo4j.DbConnector;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Service;

@Service
public class TrendingGlobalService {
    private final DbConnector db;

    // Si usás Spring, inyectá un bean DbConnector; si no, crealo a mano en main.
    public TrendingGlobalService(DbConnector db) {
        this.db = db;
    }

    /**
     * Top-K canciones más escuchadas en toda la plataforma (Greedy).
     */
    public List<SongPlay> topGlobalTrending(int k, List<String> platforms) {
        if (k <= 0) return List.of();

        final String cypher = """
            MATCH (s:Song)<-[l:LISTENED]-(:User)
            OPTIONAL MATCH (s)-[:AVAILABLE_ON]->(svc:StreamingService)
            WITH s, sum(l.count) AS plays, collect(DISTINCT svc.name) AS svcs, $platforms AS pfs
            WHERE size(pfs) = 0 OR any(p IN pfs WHERE p IN svcs)
            RETURN s.id AS id, s.title AS title, s.artist AS artist, plays
            """;

        Map<String, Object> params = Map.of("platforms", platforms == null ? List.of() : platforms);

        // leer todo y preparar map <songId, plays>
        Map<String,Integer> songPlays = new HashMap<>();
        Map<String, SongPlay> index = new HashMap<>();

        List<Record> rows = db.readList(cypher, params, r -> r); // devolveme los Record sin mapear
        for (Record row : rows) {
            String id = row.get("id").asString();
            String title = row.get("title").asString("");
            String artist = row.get("artist").asString("");
            int plays = row.get("plays").asInt(0);
            songPlays.put(id, plays);
            index.put(id, new SongPlay(id, title, artist, plays));
        }

        if (songPlays.isEmpty()) return List.of();

        // Greedy Top-K
        List<Map.Entry<String,Integer>> topK = GreedyExamples.topKGreedy(songPlays, k);

        // Mapear al DTO final, manteniendo orden DESC
        List<SongPlay> out = new ArrayList<>(topK.size());
        for (var e : topK) {
            SongPlay base = index.get(e.getKey());
            out.add(new SongPlay(
                e.getKey(),
                base != null ? base.title  : "",
                base != null ? base.artist : "",
                e.getValue()
            ));
        }
        return out;
    }
}
