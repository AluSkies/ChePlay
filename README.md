# CHEPLAY – Recomendador Social de Películas y Música

CHEPLAY es una aplicación Spring Boot + Neo4j que demuestra cómo los 
algoritmos clásicos (BFS, Dijkstra, Prim, …) pueden impulsar 
recomendaciones del mundo real.

---
## Inicio Rápido (JVM local)
```bash
# prerequisitos: JDK 21 + Maven 3.9+
export NEO4J_URI=neo4j+s://<tu-aura>.databases.neo4j.io
export NEO4J_USER=neo4j
export NEO4J_PASSWORD=<contraseña>

./mvnw verify            # ejecuta tests unitarios + integración Aura
./mvnw -DskipTests package
java -jar target/ChePlay-0.0.1-SNAPSHOT.jar
```
El servidor inicia en `http://localhost:8080`.

---
## Build con Docker (ejecuta tests dentro del contenedor)
Un archivo compose helper monta el código fuente, tu caché de Maven e 
inyecta las credenciales de Aura:
```bash
docker compose -f compose.test.yaml up --build
```

---
## Interfaz Frontend

Abrí `http://localhost:8080` para acceder a la interfaz web moderna con:

### 🎵 Pestaña Música
- **Recomendación de Amigos**: Recomendaciones basadas en red usando 
  algoritmos de grafos
- **Recomendación de Canciones**: Algoritmo Prim MST para descubrir música
- **Generador de Playlist**: Programación dinámica para playlists óptimas
- **Trending**: Canciones populares en tiempo real

### 🎬 Pestaña Películas
- **Recomendaciones de Películas**: Elegí entre BFS, DFS, Kruskal o MergeSort
- **Planificador de Maratón**: DP o Branch & Bound para maratones óptimos
- **Películas en Tendencia**: Global, por género o más influyentes

### 📊 Pestaña Analytics
- **Comparación de Algoritmos**: Análisis de rendimiento lado a lado
- **Estadísticas del Sistema**: Insights de datos en tiempo real

**Características:**
- Visualizaciones interactivas con Chart.js
- Diseño responsive con Tailwind CSS
- Arquitectura JavaScript modular ES6
- Notificaciones toast y animaciones suaves

Mirá `src/main/resources/static/FRONTEND_README.md` para documentación 
detallada del frontend.

---
## Endpoints de la API REST

### Recomendaciones de Amigos y Canciones
```
GET /api/recommendations/users
GET /api/recommendations/closest?user={user}&k={k}
GET /api/recommendations/songs?user={user}&k={k}
```

### Recomendaciones de Películas
```
GET /api/movies/recommendations/{algorithm}?user={user}&k={k}
  algorithms: bfs | dfs | diverse | sorted | by-genre
```

### Planificación de Maratón
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

### Generación de Playlist
```
GET /api/playlist/generate?user={user}&size={n}&uniqueArtist={bool}&minDuration={sec}&maxDuration={sec}
```

Para documentación completa de la API, mirá `MOVIE_RECOMMENDATIONS_README.md`.

---
## Referencia de Algoritmos
| Algoritmo | Propósito en el pipeline |
|-----------|-------------------------|
| BFS / DFS | Explorar vecindario N-hop alrededor de las semillas |
| Dijkstra  | Rankear por distancia de similitud |
| Prim / Kruskal | Calcular subgrafo de conexión mínima |
| Greedy top-K | Mantener los top-K items más populares de la frontera |
| MergeSort / QuickSort | Ordenar candidatos por puntaje compuesto |
| Dynamic (Knapsack) | Ajustar selecciones en la duración de sesión |
| Backtracking / Branch-and-Bound | Fallback de subconjunto exhaustivo |

---
## Testing
* `RecommendationControllerTest` – Test HTTP con MockMvc
* `RecommendationAuraIntegrationTest` – Contexto Spring completo contra Aura

Configurá `NEO4J_URI/USER/PASSWORD` para ejecutar tests de integración 
localmente. Se saltean en otro caso, para que CI sea rápido y seguro para 
repos públicos.

---
## Carga de Datos (Seeding)

Para poblar Neo4j Aura con datos de ejemplo:

```bash
# Configurá tus credenciales de Neo4j
export NEO4J_URI=neo4j+s://<tu-aura>.databases.neo4j.io
export NEO4J_USER=neo4j
export NEO4J_PASSWORD=<contraseña>

# Ejecutá el seeder (requiere el JAR compilado)
java -cp target/ChePlay-0.0.1-SNAPSHOT.jar \
     org.cheplay.neo4j.MovieDataSeeder
```

Esto crea:
* 20 películas de múltiples géneros
* 3+ usuarios de prueba (u1, u2, u3)
* Relaciones WATCHED y RATED
* Relaciones SIMILAR_TO entre películas
* Redes de amistad entre usuarios
* Historial de escucha de canciones

**Nota:** Descomentá `createMovies()` y `createMovieSimilarities()` en 
`MovieDataSeeder.java` si las películas aún no existen.

---
## Notas de Desarrollo

### Arquitectura Frontend
* **Tailwind CSS 3.x** vía CDN para estilos modernos
* **Chart.js 4.x** para visualizaciones de datos interactivas
* **date-fns 3.x** para manipulación de fechas
* **Módulos ES6**: JavaScript modular (`api.js`, `ui.js`, `charts.js`, 
  `tabs.js`)
* **Sin proceso de build**: Carga directa, listo para producción

### Backend
* Estilo de código: funcional donde sea posible, máx 80 columnas
* Manejo de errores: excepciones específicas, try/catch solo en llamadas 
  externas
* Comillas simples en JS/TS, comillas dobles en Python

### Documentación
* Guía frontend: `src/main/resources/static/FRONTEND_README.md`
* Detalles de implementación: `FRONTEND_ENHANCEMENT_SUMMARY.md`
* Características de películas: `MOVIE_RECOMMENDATIONS_README.md`

