

```
mvn clean package
docker build -t rest-svc1 .
docker build -t rest-svc2 .
docker build -t eda-svc3 .
docker build -t eda-svc4 .

docker run -p 8081:8080 --network=solace-net --env BACKEND_URL=http://rest-svc2:8082/ --name=rest-svc1 rest-svc1
docker run  --network=solace-net --name=rest-svc2 rest-svc2

docker run -p 8083:8083 --env SOL_URL=tcp://sol96:55555 --network=solace-net --name=eda-svc3 eda-svc3
docker run  --env SOL_URL=tcp://sol96:55555 --network=solace-net --name=eda-svc4  eda-svc4

k6 run --vus 10 --duration 100s load-eda.ts
k6 run --vus 10 --duration 100s load-rest.ts

docker stats

```
