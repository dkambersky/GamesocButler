# Gamesoc Butler
A bot written for the University of Edinburgh's Gaming society's Discord ([link]), mostly grows organically with what we need

## Features
The bot uses modules which can be turned on and off at will and developed / configured independently. Some notable modules:

### Janitor
Pokecord is fun, but is spammy and results in conversations constantly broken with images which take a lot of vertical space. The `janitor` module deletes any Pokecord messages and the players' dialogue, apart from the final confirmation message, when a mon is caught.

### Game roles
Finding other people which play a specific game can be a pain on a larger server. the `game-role` module enables 'matchmaking' with the bot adding roles for specific games, assigning players, listing /pinging active players for a game etc. use `-help` for more.


### Moo
Module made when the soc wanted to merge Janitor and Moobot into a single Gamesoc bot, implements moobot features (other than fox's NLP stuff :p). `fortune`, `harambe`,`f` etc. Meme stuff. 

## Architecture
The bot is written in Kotlin, using Discord4J and coroutines for multithreaded stuff.

### Adding modules 
I'll write this up later.

 The TL;DR  is:
 
 - create a new class that extends Module
 
 - in the constructor, pass a name that'll be used in the config to enable the module

 - implement the process(event) method to process arbitrary events

 - the config / db APIs are an unholy mess, but for now, mimic what `game-role` uses. 


## TODOs

- game-role
  - make process clearer
  - reaction assigns?

- moo 
  - cowsay for fortunes (reqs linux)

- janitor
  - ash ketchum mode (catch everything that spawns; taunt opposition)

- tournament module
  - use challonge or sth similar for local tourneys .D