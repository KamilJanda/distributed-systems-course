//
// Created by Kamil on 2019-03-11.
//

#ifndef SOCKET_TCP_UDP_CLIENT_H
#define SOCKET_TCP_UDP_CLIENT_H


void establish_socket_server(int port);
void establish_socket_client(int port);
int accept_in_connection(int port);
Token create_connection_token();
void send_connection_token(Token token);
void send_token(Token token);



#endif //SOCKET_TCP_UDP_CLIENT_H
