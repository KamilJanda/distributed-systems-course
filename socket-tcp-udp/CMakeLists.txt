cmake_minimum_required(VERSION 3.13)
project(socket_tcp_udp C)

set(CMAKE_C_STANDARD 11)

add_executable(client client.c utlities.c utilieties.h tcp_utilieties.c tcp_utilieties.h client.h)