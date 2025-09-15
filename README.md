Set-Location C:\Users\bbdnet10280\monitoring-POC
Set-Content -Path .\README.md -Value @'
# Monitoring POC — Spring Boot + OTel + Tempo + Loki + Prometheus/Grafana/Alertmanager (kind)

> Windows PowerShell steps (no file blobs, just commands).

## 0) Prereqs
- Docker Desktop, kubectl, helm, kind
- Check: `kubectl version --client`, `helm version`, `kind version`

## 1) Create cluster
- `kind create cluster --name monitoring-demo-rushi`
- `kubectl config use-context kind-monitoring-demo-rushi`
- `kubectl get nodes`

## 2) Add Helm repos
- `helm repo add prometheus-community https://prometheus-community.github.io/helm-charts`
- `helm repo add grafana https://grafana.github.io/helm-charts`
- `helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts`
- `helm repo update`

## 3) Install kube-prometheus-stack (Prometheus + Grafana + Alertmanager)
- `helm install mon prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace --set grafana.adminPassword=admin123`
- Verify: `kubectl -n monitoring get pods`

## 4) Install Loki + Promtail + Tempo
- Install charts into `monitoring` namespace (your chosen commands)
- Verify pods: `loki-0`, `loki-promtail-*`, `tempo-0`

## 5) Grafana datasources (ConfigMap)
- Ensure a ConfigMap `grafana-datasources` in `monitoring` with 3 datasources:
  - Prometheus → `http://mon-kube-prometheus-stack-prometheus.monitoring.svc:9090`
  - Loki → `http://loki.monitoring.svc:3100`
  - **Tempo → `http://tempo.monitoring.svc:3200` (important!)**
- Apply & restart Grafana:
  - `kubectl apply -f .\grafana-datasources.yaml`
  - `kubectl -n monitoring delete pod -l app.kubernetes.io/name=grafana,app.kubernetes.io/instance=mon`

## 6) OpenTelemetry Collector (Helm)
- Values must enable receivers `otlp.grpc` and `otlp.http`
- Export traces to Tempo OTLP/HTTP `:4318`
- Install:
  - `helm upgrade --install otel open-telemetry/opentelemetry-collector --namespace monitoring -f .\otel-collector-values.yaml`
- Verify: `kubectl -n monitoring get pods`

## 7) Build & load Spring Boot app image
- `cd .\spring-demo`
- `docker build -t spring-demo:1.0 .`
- `kind load docker-image spring-demo:1.0 --name monitoring-demo-rushi`

## 8) Deploy app + Service + ServiceMonitor
- `kubectl apply -f .\k8s-app.yaml`
- Deployment env must include:
  - `OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-opentelemetry-collector.monitoring.svc.cluster.local:4318`
  - `OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf`
  - `OTEL_METRICS_EXPORTER=none` (Prometheus scrapes Actuator)
- Verify: `kubectl -n app get pods`

## 9) Generate traffic
- Terminal A: `kubectl -n app port-forward deploy/spring-demo 8080:8080`
- Terminal B: `curl http://localhost:8080/hello` and `curl http://localhost:8080/error500`

## 10) Verify UIs
- **Grafana**: `kubectl -n monitoring port-forward svc/mon-grafana 3000:80`
  - Login: admin / admin123
  - Loki (LogQL): `{app="spring-demo"}`
  - Tempo (TraceQL): `{ resource.service.name = "spring-demo" }`
- **Prometheus**: `kubectl -n monitoring port-forward svc/mon-kube-prometheus-stack-prometheus 9090:9090`
  - Queries: `http_server_requests_seconds_count{service="spring-demo"}`, `up{namespace="app"}`

## 11) Alerts
- Apply rules: `kubectl apply -f .\prom-rule.yaml`
- Alertmanager UI: `kubectl -n monitoring port-forward svc/mon-kube-prometheus-stack-alertmanager 9093:9093`
- (Optional) webhook receiver: apply `alert-webhook.yaml` + `amconfig-webhook.yaml`

## 12) Push repo to GitHub (HTTPS)
- `git init && git add . && git commit -m "Initial commit: Monitoring POC"`
- `git branch -M main`
- `git config --global credential.helper manager-core`
- `git remote add origin https://github.com/<YOUR-USERNAME>/monitoring-POC.git`
- `git push -u origin main` (username = your GH user, password = PAT)

## 13) Troubleshooting quick hits
- Grafana Tempo 502 → datasource must use `:3200`
- OTel agent protocol error → `OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf`
- Boot jar won’t start → ensure `spring-boot:repackage` in Docker build
- ServiceMonitor not scraping → label `release: mon` must match Helm release
- Tempo HTTP check → `kubectl -n monitoring port-forward svc/tempo 3200:3200` then open `http://localhost:3200/ready`

## 14) Clean up
- `kind delete cluster --name monitoring-demo-rushi`
'@
notepad .\README.md
