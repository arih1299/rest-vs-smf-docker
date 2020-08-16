
This demo intends to compare a full REST flow with a REST + Solace messaging flow, in terms of throughput, latency, and especially resource consumptions.
The services are built with Spring Boot.
Performance tests are done using K6.io

Note:
- Docker environment must have enough resources to run the containers. Mine is set with 4 CPUs and 10 GBs RAM.

```
mvn clean package
docker build -t rest-svc1 .
docker build -t rest-svc2 .
docker build -t eda-svc3 .
docker build -t eda-svc4 .

docker network create solace-net
docker run -d --network solace-net -p 8080:8080 -p 55555:55555 --shm-size=2g --env username_admin_globalaccesslevel=admin --env username_admin_password=admin --env system_scaling_maxconnectioncount=1000 --name=sol96 solace/solace-pubsub-standard
docker run -p 8081:8080 --network=solace-net --env BACKEND_URL=http://rest-svc2:8082/ --name=rest-svc1 rest-svc1
docker run  --network=solace-net --name=rest-svc2 rest-svc2
docker run -p 8083:8083 --env SOL_URL=tcp://sol96:55555 --network=solace-net --name=eda-svc3 eda-svc3
docker run  --env SOL_URL=tcp://sol96:55555 --network=solace-net --name=eda-svc4  eda-svc4

k6 run --vus 10 --duration 100s load-eda.ts
k6 run --vus 10 --duration 100s load-rest.ts

docker stats

```
