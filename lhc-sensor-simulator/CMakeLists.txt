cmake_minimum_required(VERSION 3.20)
project(lhc_sensor_simulator)

set(CMAKE_CXX_STANDARD 17)

# Find the curl library on your system
find_package(CURL REQUIRED)

# This line tells CMake that main.cpp is the source code for your app
add_executable(lhc_sensor_simulator main.cpp)

# This links the curl library to your app
target_link_libraries(lhc_sensor_simulator PRIVATE CURL::libcurl)