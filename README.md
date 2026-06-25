# 🏗️ Infra Monitoring Project

A full-stack infrastructure monitoring solution using **Docker Compose** that includes a Spring Boot application, PostgreSQL with replication, HAProxy load balancer, ELK stack for logging, and Prometheus + Grafana for metrics monitoring.

---

## 📐 Architecture

```
                        ┌─────────────┐
                        │   HAProxy   │  :8088
                        │ Load Balancer│
                        └──────┬──────┘
                    ┌──────────┴──────────┐
                    ▼                     ▼
             ┌────────────┐        ┌────────────┐
             │  Tomcat 1  │        │  Tomcat 2  │
             │  :8082     │        │  :8083     │
             └─────┬──────┘        └─────┬──────┘
                   └──────────┬──────────┘
                              ▼
                        ┌───────────┐
                        │  PgPool   │  :9999
                        └─────┬─────┘
                   ┌──────────┴──────────┐
                   ▼                     ▼
          ┌──────────────┐     ┌──────────────────┐
          │  PostgreSQL  │     │  PostgreSQL       │
          │  Primary     │────▶│  Replica         │
          │  :5432       │     │  :5432           │
          └──────────────┘     └──────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    MONITORING STACK                      │
│                                                         │
│  Node Exporter → Prometheus → Grafana                   │
│     :9100          :9090       :3000                    │
│                                                         │
│  HAProxy Exporter → Prometheus                          │
│     :9101                                               │
│                                                         │
│  rsyslog → Elasticsearch → Kibana                       │
│   :514        :9200          :5601                      │
└─────────────────────────────────────────────────────────┘
```

---

## 🧰 Tech Stack

| Component | Technology | Port |
|-----------|-----------|------|
| Application | Spring Boot 3.x + Tomcat 10 | 8082, 8083 |
| Load Balancer | HAProxy | 8088 |
| Database Primary | PostgreSQL 16 | 5432 |
| Database Replica | PostgreSQL 16 (Streaming Replication) | 5432 |
| Connection Pooling | PgPool 4.6 | 9999 |
| Metrics Collection | Prometheus | 9090 |
| Metrics Visualization | Grafana | 3000 |
| Node Metrics | Node Exporter | 9100 |
| HAProxy Metrics | HAProxy Exporter | 9101 |
| Log Collection | rsyslog | 514 |
| Log Storage | Elasticsearch 8.14 | 9200 |
| Log Visualization | Kibana 8.14 | 5601 |

---

## 📁 Project Structure

```
infra-monitoring-project/
├── docker-compose.yml
├── README.md
│
├── app/
│   └── employee-app/
│       ├── pom.xml
│       └── src/
│           └── main/
│               ├── java/com/company/employeeapp/
│               │   ├── controller/
│               │   ├── entity/
│               │   ├── repository/
│               │   └── service/
│               ├── resources/
│               │   └── application.properties
│               └── webapp/
│                   └── WEB-INF/jsp/
│
├── config/
│   ├── Dockerfile.rsyslog
│   └── rsyslog.conf
│
├── haproxy/
│   └── haproxy.cfg
│
└── prometheus/
    └── prometheus.yml
```

---

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose installed
- Java 21 + Maven (for building the app)
- Minimum 8GB RAM recommended

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/infra-monitoring-project.git
cd infra-monitoring-project
```

### 2. Build the Spring Boot Application

```bash
cd app/employee-app
mvn clean package -DskipTests -Dspring-boot.repackage.skip=true
cd ../..
```

### 3. Start All Services

```bash
docker-compose up -d --build
```

### 4. Verify All Containers are Running

```bash
docker ps
```

Expected containers:
```
postgres-primary
postgres-replica
pgpool
tomcat1
tomcat2
haproxy
haproxy-exporter
prometheus
grafana
node-exporter
rsyslog
elasticsearch
kibana
```

---

## 🌐 Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8088 | - |
| Tomcat 1 (direct) | http://localhost:8082 | - |
| Tomcat 2 (direct) | http://localhost:8083 | - |
| HAProxy Stats | http://localhost:8404/stats | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin / admin |
| Kibana | http://localhost:5601 | - |
| Elasticsearch | http://localhost:9200 | - |
| Node Exporter | http://localhost:9100/metrics | - |
| PgPool | localhost:9999 | admin / admin123 |

---

## 📊 Grafana Setup

### Add Prometheus Datasource

```
Grafana → Configuration → Data Sources → Add data source
Type: Prometheus
URL: http://prometheus:9090
Save & Test
```

### Import Dashboard

```
Grafana → Dashboards → Import
Upload: grafana-dashboard.json
Select Prometheus datasource → Import
```

Dashboard includes:
- ✅ CPU Utilisation (Gauge + Time Series)
- ✅ RAM Usage (Gauge + Detailed breakdown)
- ✅ Disk Usage (Gauge + I/O)
- ✅ Network Traffic (In/Out)
- ✅ System Load Average (1m/5m/15m)
- ✅ System Info (Uptime, Cores, Total RAM, Disk)

---

## 📋 Kibana Setup

### Create Index Pattern

```
Kibana → Stack Management → Data Views → Create data view
Name: syslog
Index pattern: syslog*
Timestamp field: timestamp
Save
```

### View Logs

```
Kibana → Discover → Select "syslog" → Set time range
```

### Generate Test Logs

```bash
# Single log
logger -n 127.0.0.1 -P 514 --udp "Test message"

# Continuous log generation
while true; do
  logger -n 127.0.0.1 -P 514 --udp "[INFO] Service health check OK at $(date)"
  logger -n 127.0.0.1 -P 514 --udp "[ERROR] Connection timeout at $(date)"
  sleep 2
done
```

---

## 🗄️ Database Setup

### PostgreSQL Primary

```
Host: localhost:9999 (via PgPool)
Database: employeedb
Username: admin
Password: admin123
```

### Check Replication Status

```bash
docker exec postgres-primary psql -U admin -c "SELECT * FROM pg_stat_replication;"
```

### Check PgPool Nodes

```bash
docker exec pgpool psql -h localhost -U admin -p 5432 -c "SHOW pool_nodes;" employeedb
```

---

## 🔍 Prometheus Targets

Access: http://localhost:9090/targets

| Job | Target | Status |
|-----|--------|--------|
| node-exporter | node-exporter:9100 | ✅ UP |
| haproxy | haproxy-exporter:9101 | ✅ UP |
| postgres-primary | postgres-exporter-primary:9187 | ✅ UP |
| postgres-replica | postgres-exporter-replica:9187 | ✅ UP |

---

## 🔧 Useful Commands

```bash
# View all container logs
docker-compose logs -f

# View specific container logs
docker logs <container-name> --tail 50

# Restart a service
docker-compose restart <service-name>

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Check Elasticsearch indices
curl http://localhost:9200/_cat/indices?v

# Check log count
curl http://localhost:9200/syslog/_count

# Check HAProxy stats via curl
curl http://localhost:8404/stats
```

---

## ⚙️ Configuration Files

### rsyslog.conf
Collects system logs and forwards to Elasticsearch via `omelasticsearch` plugin.

### haproxy.cfg
Load balances traffic between Tomcat1 and Tomcat2 with health checks.

### prometheus.yml
Scrapes metrics from Node Exporter, HAProxy Exporter, and Postgres Exporters every 15 seconds.

### application.properties
Spring Boot app connects to PostgreSQL via PgPool with Actuator and Prometheus metrics enabled.

---

## 🐛 Troubleshooting

### rsyslog not sending logs to Elasticsearch
```bash
docker logs rsyslog --tail 20
docker exec rsyslog curl -s http://elasticsearch:9200/_cat/health
```

### Tomcat not starting
```bash
docker logs tomcat1 --tail 30
```

### PostgreSQL replication not working
```bash
docker logs postgres-replica --tail 20
docker exec postgres-primary psql -U admin -c "SELECT * FROM pg_stat_replication;"
```

### Prometheus targets down
```bash
curl http://localhost:9090/targets
docker logs prometheus --tail 20
```

---

## 👨‍💻 Author

**Sonu**  
Infrastructure Monitoring Project  
Built with ❤️ using Docker, Spring Boot, ELK Stack, and Prometheus
