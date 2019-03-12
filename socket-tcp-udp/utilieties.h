//
// Created by Kamil on 2019-03-11.
//

#ifndef SOCKET_TCP_UDP_UTILIETIES_H
#define SOCKET_TCP_UDP_UTILIETIES_H

#define TCP 1
#define UDP 2

#define BUSY 3
#define FREE 4
#define TOKEN 5
#define CONNECTION_TOKEN 6

typedef struct Configuration{
    char userId[128];
    int listeningPort;
    char neighbour_ip_address[32];
    int neighbour_port;
    int starts_with_token;
    int protocol;
}Configuration;

typedef struct Token {
    int type;
    int status;
    char receiver_name[128]; // client ID
    char msg[1024];
//    access_record ac_rec;
    int listening_port;
} Token;

struct Configuration read_args(int argc, char *argv[]);

#endif //SOCKET_TCP_UDP_UTILIETIES_H
