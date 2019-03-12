#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include "utilieties.h"
#include "client.h"
#include <netinet/in.h>
#include <zconf.h>
#include <mach/boolean.h>

int SOCKET_IN;
int SOCKET_OUT;
Configuration PROGRAM_CONFIG;
int HAS_TOKEN = FALSE;


void graceful_exit(int signum) {
    printf("exit\n");
    shutdown(SOCKET_IN, SHUT_RDWR);
    shutdown(SOCKET_OUT, SHUT_RDWR);
    exit(EXIT_SUCCESS);
}

int main(int argc, char *argv[]) {
    signal(SIGINT, graceful_exit);

    PROGRAM_CONFIG = read_args(argc, argv);

    if (PROGRAM_CONFIG.starts_with_token) {
        Token token;
        HAS_TOKEN = TRUE;

        establish_socket_server(PROGRAM_CONFIG.listeningPort);

        int socket_fd = accept_in_connection(PROGRAM_CONFIG.listeningPort);

        read(socket_fd, &token, sizeof(Token));

        if (token.type == CONNECTION_TOKEN) {
            PROGRAM_CONFIG.neighbour_port = token.listening_port;
            printf("Connection token received\n");
        }

        Token connection_token = {
                .type = CONNECTION_TOKEN,
                .listening_port = PROGRAM_CONFIG.listeningPort
        };

//        sleep(1);

        establish_socket_client(PROGRAM_CONFIG.neighbour_port);

        send_connection_token(connection_token);

    } else {
        sleep(1);

        Token connection_token = {
                .type = CONNECTION_TOKEN,
                .listening_port = PROGRAM_CONFIG.listeningPort
        };

        establish_socket_client(PROGRAM_CONFIG.neighbour_port);

        send_connection_token(connection_token);

        establish_socket_server(PROGRAM_CONFIG.listeningPort);
    }

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
    while (1) {
        if (PROGRAM_CONFIG.starts_with_token) {
            Token token = {
                    .type = TOKEN,
                    .status = FREE
            };

            printf("Emulate work\n");
            sleep(1);

            PROGRAM_CONFIG.starts_with_token = FALSE;
            HAS_TOKEN = FALSE;
            send_token(token);

            printf("block");

        } else {
            Token tmp_token;
            int socket_fd = accept_in_connection(PROGRAM_CONFIG.listeningPort);

            read(socket_fd, &tmp_token, sizeof(Token));

            if (tmp_token.type == CONNECTION_TOKEN) {
                printf("Connection token received 1\n");
            } else if (tmp_token.type == TOKEN) {
                HAS_TOKEN = TRUE;
                printf("Normal token received\n");
            } else{
                printf("else");
            }

            close(socket_fd);

        }
    }
#pragma clang diagnostic pop


    return 0;
}

void establish_socket_server(int port) {
    SOCKET_IN = socket(AF_INET, SOCK_STREAM, 0);

    if (SOCKET_IN == -1) {
        perror("Can't create socket");
        exit(EXIT_FAILURE);
    }

    int opt = 1;
    if (setsockopt(SOCKET_IN, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }

    struct sockaddr_in address;
    address.sin_family = AF_INET;
    address.sin_port = htons((uint16_t) port);
    address.sin_addr.s_addr = htonl(INADDR_ANY);

    if ((bind(SOCKET_IN, (struct sockaddr *) &address, sizeof(address))) != 0) {
        perror("socket bind");
        exit(EXIT_FAILURE);
    }

    if ((listen(SOCKET_IN, 10)) != 0) {
        perror("listen");
        exit(EXIT_FAILURE);
    }
}

int accept_in_connection(int port) {
    int new_socket;

    struct sockaddr_in address;
    address.sin_family = AF_INET;
    address.sin_port = htons((uint16_t) port);
    address.sin_addr.s_addr = htonl(INADDR_ANY);

    int address_len = sizeof(address);

    printf("1\n");
    if ((new_socket = accept(SOCKET_IN, (struct sockaddr *) &address, (socklen_t *) &address_len)) < 0) {
        perror("accept");
        exit(EXIT_FAILURE);
    }
    printf("2\n");

    return new_socket;
}

void establish_socket_client(int port) {

    SOCKET_OUT = socket(AF_INET, SOCK_STREAM, 0);
    if (SOCKET_OUT == -1) {
        perror("Can't create socket");
        exit(EXIT_FAILURE);
    }

    struct sockaddr_in address;
    address.sin_family = AF_INET;
    address.sin_port = htons((uint16_t) port);
    address.sin_addr.s_addr = htonl(INADDR_ANY);

    if (connect(SOCKET_OUT, (struct sockaddr *) &address, sizeof(address)) < 0) {
        perror("\nConnection Failed \n");
        exit(EXIT_FAILURE);
    }
}

Token create_connection_token() {
    Token connection_token = {
            .type = CONNECTION_TOKEN,
            .listening_port = PROGRAM_CONFIG.listeningPort
    };
    return connection_token;
}

void send_token(Token token) {
    write(SOCKET_OUT, &token, sizeof(token));
    printf("Normal token sent %d\n",SOCKET_OUT);
}

void send_connection_token(Token token) {
    write(SOCKET_OUT, &token, sizeof(token));
    printf("Connection token sent %d\n",SOCKET_OUT);
}

//void handle_connection(int socket) {
//    int client;
//    if ((client = accept(socket, NULL, NULL)) == -1) {
//        perror("\nError : Could not accept new client\n");
//        exit(EXIT_FAILURE);
//    }
//
//    struct epoll_event event;
//    event.events = EPOLLIN | EPOLLPRI;
//    event.data.fd = client;
//
//    if (epoll_ctl(epoll, EPOLL_CTL_ADD, client, &event) == -1) {
//        perror("\nError : Could not add new client to epoll\n");
//        exit(EXIT_FAILURE);
//    }
//}
