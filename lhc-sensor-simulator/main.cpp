#include <iostream>
#include <string>
#include <curl/curl.h>
#include <chrono>
#include <thread>
#include <random>
#include <cstdlib>
#include <cstdio>
#include <iomanip>
#include <sstream>

size_t WriteCallback(void* contents, size_t size, size_t nmemb, void* userp) {
    ((std::string*)userp)->append((char*)contents, size * nmemb);
    return size * nmemb;
}

class TelemetryClient {
public:
    std::string getStatus() {
        CURL* curl = curl_easy_init();
        std::string readBuffer;
        if(curl) {
            const char* env_base = std::getenv("API_BASE_URL");
            std::string url = (env_base != nullptr) ? std::string(env_base) + "/api/get-lhc-status/" : "http://glance-backend:8000/api/get-lhc-status/";
            curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
            curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
            curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
            curl_easy_perform(curl);
            curl_easy_cleanup(curl);
        }
        return readBuffer;
    }

    void sendEvent(const std::string& jsonPayload) {
        CURL* curl = curl_easy_init();
        if(curl) {
            struct curl_slist *headers = NULL;
            headers = curl_slist_append(headers, "Content-Type: application/json");
            const char* env_url = std::getenv("INGEST_URL");
            std::string target_url = (env_url != nullptr) ? env_url : "http://telemetry-service:8080/api/v1/telemetry/ingest";

            curl_easy_setopt(curl, CURLOPT_URL, target_url.c_str());
            curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
            curl_easy_setopt(curl, CURLOPT_POSTFIELDS, jsonPayload.c_str());

            CURLcode res = curl_easy_perform(curl);
            if(res != CURLE_OK) {
                std::cerr << "Uplink Error: " << curl_easy_strerror(res) << std::endl;
            }

            curl_slist_free_all(headers);
            curl_easy_cleanup(curl);
        }
    }
};

int main() {
    TelemetryClient client;
    std::default_random_engine generator;
    std::uniform_real_distribution<double> jitter(-0.06e11, 0.06e11);

    double currentEnergy = 0.0;

    while(true) {
        std::string statusResponse = client.getStatus();
        double intensity = 0;
        std::string mode = "NO BEAM";

        if (statusResponse.find("STABLE") != std::string::npos) {
            intensity = 1.14e11 + jitter(generator);
            currentEnergy = 6800.0;
            mode = "STABLE BEAMS";
        }
        else if (statusResponse.find("RAMP") != std::string::npos) {
            intensity = 1.12e11 + jitter(generator);
            mode = "RAMP";
            if (currentEnergy < 450.0) currentEnergy = 450.0;
            if (currentEnergy < 6800.0) currentEnergy += 250.0;
        }
        else {
            intensity = 0.0;
            currentEnergy = 0.0;
            mode = "NO BEAM";
        }

        std::stringstream ss;
        ss << std::fixed << std::setprecision(2);
        ss << "{"
           << "\"sensorId\": \"BCTDC-P5\","
           << "\"eventType\": \"BEAM_INTENSITY_DATA\","
           << "\"fillNumber\": 9412,"
           << "\"accelerator\": \"LHC\","
           << "\"status\": \"" << mode << "\","
           << "\"energy\": " << currentEnergy << ","
           << "\"value\": " << intensity
           << "}";

        std::string payload = ss.str();
        std::cout << "[C++ SENSOR] Sending: " << payload << std::endl;

        client.sendEvent(payload);
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }
    return 0;
}