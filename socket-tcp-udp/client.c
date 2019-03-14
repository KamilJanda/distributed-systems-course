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

        SOCKET_IN = establish_socket_server(PROGRAM_CONFIG.listeningPort, PROGRAM_CONFIG.neighbour_ip_address);

        int socket_fd = accept_in_connection(SOCKET_IN, PROGRAM_CONFIG.listeningPort);

        TMP_FD = socket_fd;
        read(socket_fd, &token, sizeof(Token));
        close(socket_fd);

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
        read(TMP_FD, &token, sizeof(Token));
        close(TMP_FD);

        if (token.type == CONNECTION_TOKEN) {
            PROGRAM_CONFIG.neighbour_port = token.listening_port;
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

            read(TMP_FD, &tmp_token, sizeof(Token));

            if (tmp_token.type == CONNECTION_TOKEN) {
                set_client_id(tmp_token.senderID);

                printf("PREVIOUS CLIENT %s\n",PREVIOUS_CLIENT_ID);

                char msg[16];
                sprintf(msg, "%d", tmp_token.listening_port);
                set_message_to_send(msg, PREVIOUS_CLIENT_ID, JOIN_TOKEN);


            } else if (tmp_token.type == TOKEN) {
                HAS_TOKEN = TRUE;
//                printf("Normal token received\n");
                printf("Normal token RECEIVED| flag: %d, Receiver: %s, msg: %s \n",tmp_token.flag,tmp_token.receiverID,tmp_token.msg);


                emulate_work();

                if (is_token_for_me(tmp_token)){
                    printf("It is for me!\n");

                    char senderID[128];
                    char receiverID[128];
                    char msg[1024];

                    strcpy(senderID,tmp_token.senderID);
                    strcpy(receiverID,tmp_token.receiverID);
                    strcpy(msg,tmp_token.msg);
                    int flag = tmp_token.flag;

                    if(flag == JOIN_TOKEN){
                        int port = atoi(msg);
                        join_token(port);
                    } else{
                        printf("Message received\n");
                    }

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

                    printf("has msg %d\n",has_message_to_send());

                    strcpy(tmp_token.senderID, PROGRAM_CONFIG.userId);
                    strcpy(tmp_token.receiverID, RECEIVER);
                    strcpy(tmp_token.msg, MESSAGE);
                    tmp_token.flag = JOIN_TOKEN;
                    tmp_token.TTL = 10;
                    tmp_token.status = BUSY;

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


    return 0;
}

int establish_socket_server(int port, char *ip) {
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
//    address.sin_addr.s_addr = htonl(INADDR_ANY);
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
}

int accept_in_connection(int socket, int port) {
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

int establish_socket_client(int port) {

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
//    printf("Connecting to: %d\n", port);

    if (connect(socket_fd, (struct sockaddr *) &address, sizeof(address)) < 0) {
        perror("\nConnection Failed \n");
        exit(EXIT_FAILURE);
    }
    return socket_fd;
}

Token create_connection_token() {
    Token connection_token = {
            .type = CONNECTION_TOKEN,
            .listening_port = PROGRAM_CONFIG.listeningPort
    };
    return connection_token;
}

void send_token(Token token) {
    if (token.status == BUSY) {
        token.TTL--;
        if (token.TTL == 0) {
            printf("DESTINATION UNREACHABLE\n");
            token.status = FREE;
        }
    }

    write(SOCKET_OUT, &token, sizeof(token));
    printf("Normal token sent| sender: %s, Receiver: %s, msg: %s \n",token.senderID,token.receiverID,token.msg);
    close(SOCKET_OUT);
}

void send_connection_token(char *userID) {
    Token token = {
            .type = CONNECTION_TOKEN,
            .listening_port = PROGRAM_CONFIG.listeningPort,
    };

    strcpy(token.senderID, userID);

    write(SOCKET_OUT, &token, sizeof(token));
//    write(CONNECTION_PORT_OUT, &token, sizeof(token));
    printf("Connection token sent\n");
    close(SOCKET_OUT);
}

int is_token_for_me(Token token) {
    if (strcmp(token.receiverID, PROGRAM_CONFIG.userId)==0){
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

char *get_message_to_send() {
    return MESSAGE;
}

void emulate_work() {
    printf("Emulate work\n");
    sleep(1);
}

void set_client_id(char *userID) {
    strcpy(PREVIOUS_CLIENT_ID,CLIENT_ID);
    strcpy(CLIENT_ID, userID);
    printf("Connection token received from: %s\n", CLIENT_ID);
}

int neighbour_connection_port() {
    return PROGRAM_CONFIG.neighbour_port + 100;
}

void join_token(int port){
    printf("PORT: %d\n",port);
    close(SOCKET_OUT);
    shutdown(SOCKET_OUT, SHUT_RDWR);

    PROGRAM_CONFIG.neighbour_port = port;

    SOCKET_OUT = establish_socket_client(port);

    send_connection_token(PROGRAM_CONFIG.userId);
}