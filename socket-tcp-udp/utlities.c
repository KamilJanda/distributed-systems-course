//
// Created by Kamil on 2019-03-11.
//

#include <stdio.h>
#include <string.h>
#include "utilieties.h"



struct Configuration read_args(int argc, char *argv[]){
    Configuration result = {};
    char protocol[3];

    sscanf(argv[1], "%s", result.userId);
    sscanf(argv[2], "%d", &result.listeningPort);
    sscanf(argv[3], "%s", result.neighbour_ip_address);
    sscanf(argv[4], "%d", &result.neighbour_port);
    sscanf(argv[5], "%d", &result.starts_with_token);
    sscanf(argv[6], "%s", protocol);

    if(strcmp(protocol,"TCP") == 0)
        result.protocol = TCP;
    else
        result.protocol = UDP;

    return result;
}


