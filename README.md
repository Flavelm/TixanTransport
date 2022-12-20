# TixanTransport
I'm working on a project for myself.

The project is intended for data exchange between servers and proxies.
For example: synchronization of configs, protection from entering the backend, chat synchronization in etc.


## Features
- [ ] Protocol
  - [X] Token
  - [X] Server and client names
  - [ ] Timeout check
  - [ ] Forward message
  - [ ] Encryption
- [ ] Bukkit side
  - [X] Config
    - [X] Server address
    - [X] Server port
    - [X] Token
    - [X] Client name
    - [ ] Timeout
    - [X] Proxy name
    - [ ] Separator the protocol parts of the message
  - [ ] API
    - [X] Send message to server
    - [ ] Event
      - [X] Message
      - [X] Response
      - [ ] Packet type
- [ ] Proxy side
  - [ ] Config
    - [ ] Server port
    - [ ] Tokens
    - [X] Server names
    - [ ] Timeout
    - [ ] Proxy name
    - [ ] Separator the protocol parts of the message
    - [ ] Backlog
  - [ ] API
    - [X] Send message to client
    - [ ] Event
      - [X] Message
      - [X] Response
      - [ ] Packet type
- [X] Log
  - [X] Info
  - [X] Debug

