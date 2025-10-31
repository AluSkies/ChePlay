# CHEPLAY – Social-Based Movie & Music Recommender

CHEPLAY is a Spring Boot + Neo4j application that demonstrates how classic
algorithms (BFS, Dijkstra, Prim, …) can drive real-world recommendations.

This README focuses on running the new *Recommendation Pipeline* added in
October 2025.

---
## Quick Start (local JVM)
```bash
# prerequisites: JDK 21 + Maven 3.9+
export NEO4J_URI=neo4j+s://<your-aura>.databases.neo4j.io
export NEO4J_USER=neo4j
export NEO4J_PASSWORD=<password>

./mvnw verify            # runs unit + Aura integration tests
./mvnw -DskipTests package
java -jar target/ChePlay-0.0.1-SNAPSHOT.jar
```
Server starts on `http://localhost:8080`.

---
## Docker Build (runs tests inside container)
A helper compose file mounts the source, your Maven cache and injects Aura
credentials:
```bash
docker compose -f compose.test.yaml up --build
```

---
## Frontend Interface

Open `http://localhost:8080` to access the modern web interface with:

### 🎵 Music Tab
- **Friend Recommendations**: Network-based recommendations using graph 
  algorithms
- **Song Recommendations**: Prim MST algorithm for music discovery
- **Playlist Generator**: Dynamic programming for optimal playlists
- **Trending**: Real-time popular songs

### 🎬 Movies Tab
- **Movie Recommendations**: Choose from BFS, DFS, Kruskal, or MergeSort
- **Marathon Planner**: DP or Branch & Bound for optimal movie marathons
- **Trending Movies**: Global, by genre, or most influential

### 📊 Analytics Tab
- **Algorithm Comparison**: Side-by-side performance analysis
- **System Statistics**: Real-time data insights

**Features:**
- Interactive Chart.js visualizations
- Responsive Tailwind CSS design
- Modular ES6 JavaScript architecture
- Toast notifications and smooth animations

See `src/main/resources/static/FRONTEND_README.md` for detailed frontend 
documentation.

---
## REST API Endpoints

### Friend & Song Recommendations
```
GET /api/recommendations/users
GET /api/recommendations/closest?user={user}&k={k}
GET /api/recommendations/songs?user={user}&k={k}
```

### Movie Recommendations
```
GET /api/movies/recommendations/{algorithm}?user={user}&k={k}
  algorithms: bfs | dfs | diverse | sorted | by-genre
```

### Marathon Planning
```
GET /api/movies/marathon/quick?user={user}&totalMinutes={min}&minRating={rating}&algorithm={algo}
  algorithms: dp | branchandbound
```

### Trending
```
GET /api/movies/trending/{type}?k={k}
  types: global | by-genre | influential
GET /api/trending/global?k={k}
```

### Playlist Generation
```
GET /api/playlist/generate?user={user}&size={n}&uniqueArtist={bool}&minDuration={sec}&maxDuration={sec}
```

For complete API documentation, see `MOVIE_RECOMMENDATIONS_README.md`.

---
## Algorithm Reference
| Algorithm | Purpose in pipeline |
|-----------|--------------------|
| BFS / DFS | Explore N-hop neighbourhood around seeds |
| Dijkstra  | Rank by similarity distance |
| Prim / Kruskal | Compute minimal connecting sub-graph |
| Greedy top-K | Keep top-K most popular frontier items |
| MergeSort / QuickSort | Sort candidates by composite score |
| Dynamic (Knapsack) | Fit picks into session length |
| Backtracking / Branch-and-Bound | Exhaustive subset fallback |

---
## Testing
* `RecommendationControllerTest` – MockMvc HTTP test
* `RecommendationAuraIntegrationTest` – full Spring context against Aura

Set `NEO4J_URI/USER/PASSWORD` to run integration tests locally. Skipped
otherwise, so CI remains fast and public-repo safe.

---
## Data Seeding

To populate Neo4j Aura with sample data:

```bash
# Set your Neo4j credentials
export NEO4J_URI=neo4j+s://<your-aura>.databases.neo4j.io
export NEO4J_USER=neo4j
export NEO4J_PASSWORD=<password>

# Run the seeder (requires compiled JAR)
java -cp target/ChePlay-0.0.1-SNAPSHOT.jar \
     org.cheplay.neo4j.MovieDataSeeder
```

This creates:
* 20 movies across multiple genres
* 3+ test users (u1, u2, u3)
* WATCHED and RATED relationships
* SIMILAR_TO relationships between movies
* User friendship networks
* Song listening history

**Note:** Uncomment `createMovies()` and `createMovieSimilarities()` 
in `MovieDataSeeder.java` if movies don't exist yet.

---
## Development Notes

### Frontend Architecture
* **Tailwind CSS 3.x** via CDN for modern styling
* **Chart.js 4.x** for interactive data visualizations  
* **date-fns 3.x** for date manipulation
* **ES6 Modules**: Modular JavaScript (`api.js`, `ui.js`, `charts.js`, 
  `tabs.js`)
* **No build process**: Direct loading, production-ready

### Backend
* Code style: functional where feasible, max 80 columns
* Error handling: specific exceptions, try/catch only around external calls
* Single quotes in JS/TS, double quotes in Python

### Documentation
* Frontend guide: `src/main/resources/static/FRONTEND_README.md`
* Implementation details: `FRONTEND_ENHANCEMENT_SUMMARY.md`
* Movie features: `MOVIE_RECOMMENDATIONS_README.md`

