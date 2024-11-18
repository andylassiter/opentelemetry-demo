# Telemetry Demo

## Run

```bash
docker image build . -t awlassit/spring-boot-demo
helm upgrade --install spring-boot-demo spring-boot-demo 
helm uninstall spring-boot-demo
```