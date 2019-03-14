//
// Created by Kamil on 2019-03-11.
//

#ifndef SOCKET_TCP_UDP_CLIENT_H
#define SOCKET_TCP_UDP_CLIENT_H


int establish_socket_server(int port,char* ip);
int establish_socket_client(int port);
int accept_in_connection(int socket,int port);
Token create_connection_token();
void send_connection_token(char* userID);
void send_token(Token token);
int is_token_for_me(Token token);
int has_message_to_send();
void emulate_work();
void set_client_id(char *userID);
int neighbour_connection_port();
void set_message_to_send(char* message,char* receiver,int flag);
char* get_message_to_send();
void join_token(int port);


#endif //SOCKET_TCP_UDP_CLIENT_H
