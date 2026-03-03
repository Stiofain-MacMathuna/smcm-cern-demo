FROM ubuntu:22.04 AS builder
RUN apt-get update && apt-get install -y g++ cmake libcurl4-openssl-dev
WORKDIR /app
COPY . .
RUN cmake . && make

FROM ubuntu:22.04
RUN apt-get update && apt-get install -y libcurl4
WORKDIR /app
COPY --from=builder /app/lhc_sensor_simulator .

ENV INGEST_URL="http://telemetry-service:8080/api/v1/telemetry/ingest"
ENTRYPOINT ["./lhc_sensor_simulator"]
