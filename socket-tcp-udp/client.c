#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include "utilieties.h"
#include "client.h"
#include <netinet/in.h>
#include <zconf.h>
#include <mach/boolean.h>
#include <sys/event.h>
#include <err.h>
#include <string.h>
#include <arpa/inet.h>

int SOCKET_IN;
int SOCKET_OUT;
Configuration PROGRAM_CONFIG;
int HAS_TOKEN = FALSE;
int HAS_MESSAGE_TO_SEND = FALSE;
char CLIENT_ID[128];
char PREVIOUS_CLIENT_ID[128];
char *MESSAGE = NULL;
char *RECEIVER = NULL;
int FLAG = 0;

int CONNECTION_SOCKET;

int TMP_FD;
int EXPECT_CONFIRMATION = FALSE;

int MULTICAST_SOCKET;
char *MULTICAST_IP = "224.0.0.1";
int MULTICAST_PORT = 9010;


void graceful_exit(int signum) {
    printf("exit\n");
    shutdown(SOCKET_IN, SHUT_RDWR);
    shutdown(SOCKET_OUT, SHUT_RDWR);
    exit(EXIT_SUCCESS);
}

int main(int argc, char *argv[]) {
    signal(SIGINT, graceful_exit);

    PROGRAM_CONFIG = read_args(argc, argv);

    init_multicast();

    if (PROGRAM_CONFIG.starts_with_token) {
        Token token;
        HAS_TOKEN = TRUE;

        SOCKET_IN = establish_socket_server(PROGRAM_CONFIG.listeningPort, PROGRAM_CONFIG.neighbour_ip_address);

        int socket_fd = accept_in_connection(SOCKET_IN, PROGRAM_CONFIG.listeningPort);

        TMP_FD = socket_fd;

        if (PROGRAM_CONFIG.protocol == TCP) {
            read(socket_fd, &token, sizeof(Token));
            close(socket_fd);
        } else {
            udp_recv(socket_fd, &token);
            close(socket_fd);
        }

        if (token.type == CONNECTION_TOKEN) {
            PROGRAM_CONFIG.neighbour_port = token.listening_port;
            set_client_id(token.senderID);
        }

        SOCKET_OUT = establish_socket_client(PROGRAM_CONFIG.neighbour_port);

        send_connection_token(PROGRAM_CONFIG.userId);

    } else {
        SOCKET_IN = establish_socket_server(PROGRAM_CONFIG.listeningPort, PROGRAM_CONFIG.neighbour_ip_address);

        SOCKET_OUT = establish_socket_client(PROGRAM_CONFIG.neighbour_port);

        send_connection_token(PROGRAM_CONFIG.userId);

        TMP_FD = accept_in_connection(SOCKET_IN, PROGRAM_CONFIG.listeningPort);
        Token token;

        if (PROGRAM_CONFIG.protocol == TCP) {
            read(TMP_FD, &token, sizeof(Token));
            close(TMP_FD);
        } else {
            udp_recv(TMP_FD, &token);
            close(TMP_FD);
        }


        if (token.type == CONNECTION_TOKEN) {
            set_client_id(token.senderID);
        }
    }

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
    while (1) {
        if (PROGRAM_CONFIG.starts_with_token) {
            Token token = {
                    .type = TOKEN,
                    .status = FREE
            };

            emulate_work();

            PROGRAM_CONFIG.starts_with_token = FALSE;
            HAS_TOKEN = FALSE;
            SOCKET_OUT = establish_socket_client(PROGRAM_CONFIG.neighbour_port);
            send_token(token);
        } else {
            Token tmp_token;
            TMP_FD = accept_in_connection(SOCKET_IN, PROGRAM_CONFIG.listeningPort);

            if (PROGRAM_CONFIG.protocol == TCP) {
                read(TMP_FD, &tmp_token, sizeof(Token));
            } else {
                udp_recv(TMP_FD, &tmp_token);
            }


            if (tmp_token.type == CONNECTION_TOKEN) {
                set_client_id(tmp_token.senderID);

                printf("PREVIOUS CLIENT %s\n", PREVIOUS_CLIENT_ID);

                char msg[16];
                sprintf(msg, "%d", tmp_token.listening_port);
                set_message_to_send(msg, PREVIOUS_CLIENT_ID, JOIN_TOKEN);


            } else if (tmp_token.type == TOKEN) {
                HAS_TOKEN = TRUE;
                printf("Normal token received\n");

                //logger
                send_multicast(PROGRAM_CONFIG.userId, sizeof(PROGRAM_CONFIG.userId));

                emulate_work();

                if (is_token_for_me(tmp_token)) {
                    printf("It is for me!\n");

                    char senderID[128];
                    char receiverID[128];
                    char msg[1024];

                    strcpy(senderID, tmp_token.senderID);
                    strcpy(receiverID, tmp_token.receiverID);
                    strcpy(msg, tmp_token.msg);
                    int flag = tmp_token.flag;

                    if (flag == JOIN_TOKEN) {
                        int port = atoi(msg);
                        join_token(port);
                    } else {
                        printf("Message received %s\n",msg);
                    }

                    tmp_token.status = FREE;

                    Token token = {
                            .type = TOKEN,
                            .status = FREE
                    };

                    SOCKET_OUT = establish_socket_client(PROGRAM_CONFIG.neighbour_port);

                    HAS_TOKEN = FALSE;
                    send_token(token);
                    close(TMP_FD);

                    continue;

                } else if (has_message_to_send() && tmp_token.status == FREE) {

                    printf("has msg %d\n", has_message_to_send());

                    strcpy(tmp_token.senderID, PROGRAM_CONFIG.userId);
                    strcpy(tmp_token.receiverID, RECEIVER);
                    strcpy(tmp_token.msg, MESSAGE);
                    tmp_token.flag = JOIN_TOKEN;
                    tmp_token.TTL = 10;
                    tmp_token.status = BUSY;

                    EXPECT_CONFIRMATION = TRUE;
                    HAS_MESSAGE_TO_SEND = FALSE;
                }

                SOCKET_OUT = establish_socket_client(PROGRAM_CONFIG.neighbour_port);

                HAS_TOKEN = FALSE;
                send_token(tmp_token);

            } else {
                printf("Something went wrong!\n");
            }

            close(TMP_FD);
        }
    }
#pragma clang diagnostic pop

}

int establish_socket_server(int port, char *ip) {
    if (PROGRAM_CONFIG.protocol == TCP) {
        int socket_fd = socket(AF_INET, SOCK_STREAM, 0);

        if (socket_fd == -1) {
            perror("Can't create socket");
            exit(EXIT_FAILURE);
        }

        int opt = 1;
        if (setsockopt(socket_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
            perror("setsockopt");
            exit(EXIT_FAILURE);
        }

        struct sockaddr_in address;
        address.sin_family = AF_INET;
        address.sin_port = htons((uint16_t) port);
        address.sin_addr.s_addr = inet_addr(ip);

        if ((bind(socket_fd, (struct sockaddr *) &address, sizeof(address))) != 0) {
            perror("socket bind");
            exit(EXIT_FAILURE);
        }

        if ((listen(socket_fd, 10)) != 0) {
            perror("listen");
            exit(EXIT_FAILURE);
        }

        printf("listen on: %d\n", port);

        return socket_fd;
    } else {
        int sock_fd;
        struct sockaddr_in servaddr, cliaddr;

        if ((sock_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
            perror("socket creation failed");
            exit(EXIT_FAILURE);
        }

        memset(&servaddr, 0, sizeof(servaddr));
        memset(&cliaddr, 0, sizeof(cliaddr));

        // Filling server information
        servaddr.sin_family = AF_INET;
        servaddr.sin_addr.s_addr = inet_addr(ip);
        servaddr.sin_port = htons(port);

        if (bind(sock_fd, (const struct sockaddr *) &servaddr,
                 sizeof(servaddr)) < 0) {
            perror("bind failed");
            exit(EXIT_FAILURE);
        }

        return sock_fd;
    }
}

int accept_in_connection(int socket, int port) {
    if (PROGRAM_CONFIG.protocol == TCP) {
        int new_socket;

        struct sockaddr_in address;
        address.sin_family = AF_INET;
        address.sin_port = htons((uint16_t) port);
        address.sin_addr.s_addr = htonl(INADDR_ANY);

        int address_len = sizeof(address);

        if ((new_socket = accept(socket, (struct sockaddr *) &address, (socklen_t *) &address_len)) < 0) {
            perror("accept");
            exit(EXIT_FAILURE);
        }

        return new_socket;
    }
    return socket;
}

int establish_socket_client(int port) {
    if (PROGRAM_CONFIG.protocol == TCP) {
        int socket_fd;
        socket_fd = socket(AF_INET, SOCK_STREAM, 0);
        if (socket_fd == -1) {
            perror("Can't create socket");
            exit(EXIT_FAILURE);
        }

        struct sockaddr_in address;
        address.sin_family = AF_INET;
        address.sin_port = htons((uint16_t) port);
        address.sin_addr.s_addr = htonl(INADDR_ANY);

        sleep(1);

        if (connect(socket_fd, (struct sockaddr *) &address, sizeof(address)) < 0) {
            perror("\nConnection Failed \n");
            exit(EXIT_FAILURE);
        }
        return socket_fd;
    } else {
        int sock_fd;

        if ((sock_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
            perror("socket creation failed");
            exit(EXIT_FAILURE);
        }

        return sock_fd;
    }
}

void send_token(Token token) {
    if (PROGRAM_CONFIG.protocol == TCP) {
        if (token.status == BUSY) {
            token.TTL--;
            if (token.TTL == 0) {
                printf("DESTINATION UNREACHABLE\n");
                token.status = FREE;
            }
        }

        write(SOCKET_OUT, &token, sizeof(token));
        printf("Normal token sent| sender: %s, Receiver: %s, msg: %s \n", token.senderID, token.receiverID, token.msg);
        close(SOCKET_OUT);
    } else {
        struct sockaddr_in servaddr;
        // Filling server information
        servaddr.sin_family = AF_INET;
        servaddr.sin_port = htons(PROGRAM_CONFIG.neighbour_port);
        servaddr.sin_addr.s_addr = INADDR_ANY;

        sendto(SOCKET_OUT, &token, sizeof(token),
               0, (const struct sockaddr *) &servaddr,
               sizeof(servaddr));
        printf("UDP message sent to: %d\n", PROGRAM_CONFIG.neighbour_port);
        close(SOCKET_OUT);
    }
}

void send_connection_token(char *userID) {
    Token token = {
            .type = CONNECTION_TOKEN,
            .listening_port = PROGRAM_CONFIG.listeningPort,
    };

    strcpy(token.senderID, userID);

    if (PROGRAM_CONFIG.protocol == TCP) {

        write(SOCKET_OUT, &token, sizeof(token));
        printf("Connection token sent\n");
        close(SOCKET_OUT);
    } else {
        struct sockaddr_in servaddr;
        // Filling server information
        servaddr.sin_family = AF_INET;
        servaddr.sin_port = htons(PROGRAM_CONFIG.neighbour_port);
        servaddr.sin_addr.s_addr = INADDR_ANY;

        sendto(SOCKET_OUT, &token, sizeof(token),
               0, (const struct sockaddr *) &servaddr,
               sizeof(servaddr));
        printf("Connection token sent UDP to %d\n", PROGRAM_CONFIG.neighbour_port);
        close(SOCKET_OUT);
    }
}

int is_token_for_me(Token token) {
    if (strcmp(token.receiverID, PROGRAM_CONFIG.userId) == 0) {
        return TRUE;
    }
    return FALSE;
}

int is_token_from_me(Token token){
    if (strcmp(token.senderID, PROGRAM_CONFIG.userId) == 0) {
        return TRUE;
    }
    return FALSE;
}

int has_message_to_send() {
    return HAS_MESSAGE_TO_SEND;
}

void set_message_to_send(char *message, char *receiver, int flag) {
    HAS_MESSAGE_TO_SEND = TRUE;
    MESSAGE = message;
    RECEIVER = receiver;
    FLAG = flag;
}

void emulate_work() {
    printf("Emulate work\n");
    sleep(1);
}

void set_client_id(char *userID) {
    strcpy(PREVIOUS_CLIENT_ID, CLIENT_ID);
    strcpy(CLIENT_ID, userID);
    printf("Connection token received from: %s\n", CLIENT_ID);
}

void join_token(int port) {
    printf("PORT: %d\n", port);
    close(SOCKET_OUT);
    shutdown(SOCKET_OUT, SHUT_RDWR);

    PROGRAM_CONFIG.neighbour_port = port;

    SOCKET_OUT = establish_socket_client(port);

    send_connection_token(PROGRAM_CONFIG.userId);
}

void udp_recv(int socket_fd, Token *token) {
    struct sockaddr_in address = {};
    socklen_t len = sizeof(address);

    if (recvfrom(socket_fd, token, sizeof(token), 0, (struct sockaddr *) &address, &len) < 0) {
        perror("recvfrom() ERROR");
        exit(EXIT_FAILURE);
    }

}



void init_multicast() {
    if ((MULTICAST_SOCKET = socket(AF_INET, SOCK_DGRAM, 0)) == -1) {
        printf("Error during init_multicast()\n");
        exit(1);
    }
}

void send_multicast(char *message, size_t size) {
    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = inet_addr(MULTICAST_IP);
    addr.sin_port = htons(MULTICAST_PORT);

    if (sendto(MULTICAST_SOCKET, message, size, 0, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
        printf("Error during multicast send\n");
        exit(1);
    }
}