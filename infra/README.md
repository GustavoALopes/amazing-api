# Local infrastructure

Start the infrastructure from the repository root:

```shell
docker compose up -d
```

Run the Spring Boot API on port `8080`. Prometheus scrapes its metrics at
`host.docker.internal:8080/actuator/prometheus` every 15 seconds.

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (default credentials: `admin` / `admin`)

Grafana automatically provisions Prometheus as its default datasource and loads
the **Spring Boot 3.x Statistics** dashboard (Grafana dashboard ID 19004) into the
**Spring Boot** folder.
