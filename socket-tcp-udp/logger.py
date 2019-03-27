#!/usr/bin/env python3

import socket
import datetime
import sys
import time

MULTICAST_IP = "224.0.0.1"
MULTICAST_PORT = 9010
buff = []
log_filename = "log.txt"

if len(sys.argv) > 1:
    log_filename = sys.argv[1]

server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

server_socket.bind((MULTICAST_IP, MULTICAST_PORT))

print('PYTHON LOGGER IS RUNNING')

while True:
    buff = server_socket.recv(1024)
    log_time = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
    print("%s | %s\n" % (log_time, buff.decode('utf-8')))
    with open(log_filename, "a+") as log_file:
        log_file.write("%s | %s\n" % (log_time, buff.decode('utf-8')))
