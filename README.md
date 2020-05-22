# sbb-tsp

## Run

```bash
docker-compose up
```

Use the run configurations from IntelliJ

## Publish on Heroku

```bash
./gradlew :server:shadowJar
heroku container:push web
heroku container:release web
```
