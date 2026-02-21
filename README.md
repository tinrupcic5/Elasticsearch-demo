# Elasticsearch Demo ☕

A centralized log server that receives logs from other applications, stores them in Elasticsearch, and allows querying via a REST API. The application has no built-in web UI; use Kibana (Docker) to view logs in Elasticsearch.

## Configuration (env)

All URLs and endpoints are provided only via environment variables; there are no hardcoded values in config files. You must use a `.env` file.


Variables in `.env`:

| Variable | Description | Example |
|----------|-------------|---------|
| `SERVER_PORT` | Port the log server listens on | `8080` |
| `ELASTICSEARCH_URL` | Elasticsearch URL | `http://localhost:9200` |
| `LOG_ENDPOINT_PATH` | API path for logs | `/api/logs` |
| `LOG_INDEX_NAME` | Elasticsearch index name | `application-logs` |
| `KIBANA_URL` | Kibana URL (printed at startup) | `http://localhost:5601` |

Load `.env` before running:


In IntelliJ: Run → Edit Configurations → select the application → Env file: point to `.env`.

## Profiles

- **default** – Uses only values from env / `.env`.
- **dev** – Same config source (env / `.env`), plus DEBUG log level for `com.example.elasticsearch`. For local development, set localhost values in `.env` as in `.env.example`.

Activate the dev profile:

Or set env: `SPRING_PROFILES_ACTIVE=dev`. In IntelliJ: Run → Edit Configurations → Active profiles: `dev`.

## Running

### 1. Elasticsearch and Kibana (Docker)

From the project root:

```bash
docker compose up -d
```

This starts:
- **Elasticsearch** at `http://localhost:9200`
- **Kibana** at `http://localhost:5601`

Wait about one minute for the services to be ready.

### 2. Elasticsearch Demo application

After startup, the Kibana URL is printed in the log (e.g. `http://localhost:5601`).

## Accessing the web UI (Kibana)

1. Open **http://localhost:5601** in your browser (or the value of `KIBANA_URL` from `.env`).
2. No login is required (security is disabled in Docker Compose).
3. Go to **Discover** (left) → **Create data view**.
4. **Index pattern:** `application-logs` (or your `LOG_INDEX_NAME` value).
5. **Timestamp field:** `timestamp` → **Save**.
6. In **Discover**, select that data view to browse and filter logs.

## REST API

Base URL: `http://localhost:8080` (or `http://localhost:${SERVER_PORT}` if you changed the port). Log path: value of `LOG_ENDPOINT_PATH` (e.g. `/api/logs`).

- **POST** `{LOG_ENDPOINT_PATH}`  
  Accepts a JSON log (required: `timestamp`, `level`, `service`, `message`; optional: `exception`, `traceId`, `host`).  
  Returns **201 Created** or 400/500.

- **GET** `{LOG_ENDPOINT_PATH}`  
  Optional query params: `level`, `service`, `traceId`, `from`, `to` (ISO datetime), `page` (default 0), `size` (default 50).  
  Returns paginated JSON (`content`, `page`, `size`, `totalElements`).

- **GET** `/actuator/health`  
  Health check.

## Stopping

```bash
docker compose down
```

Elasticsearch data is kept (volume). To remove volumes as well: `docker compose down -v`.
