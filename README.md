# Monitoring-observability-project
# Monitoring POC — Spring Boot + OpenTelemetry + Tempo + Loki + Prometheus/Grafana/Alertmanager in k8s cluster

This repository spins up a local, full-stack observability lab on **Kubernetes (kind)** and deploys a small **Spring Boot** app instrumented via the **OpenTelemetry Java agent**. You’ll get:

- **Metrics**: Prometheus (scrapes Spring Actuator) visualized in Grafana  
- **Logs**: Promtail → Loki visualized in Grafana  
- **Traces**: OTel Java agent → OTel Collector → Tempo visualized in Grafana  
- **Alerts**: PrometheusRule → Alertmanager (UI + optional webhook)

> This guide uses **Windows PowerShell** commands and a kind cluster named **`monitoring-demo-rushi`**. Adjust if you’re on macOS/Linux.

---

## Prerequisites

- Docker Desktop
- `kubectl`, `helm`, `kind`
- Internet access for pulling images/charts

```powershell
kubectl version --client
helm version
kind version
