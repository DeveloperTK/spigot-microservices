# Spigot Microservices
A microservice runtime for [SpigotMC](https://www.spigotmc.org/) and [PaperMC](https://papermc.io/) Minecraft-Servers.

## Requirements and Installation

You will need...

- Java SE ≥8
- Spigot or Paper for Minecraft 1.8.x (more version support is planned)

The plugin will create a folder called `services` inside your plugin data folder.
This is the place, where you should put all of your microservice JARs.

## Developing your own microservices 

Developing your own microservices with the SpigotMS API is pretty staight forward
if you are already used to Spigot plugin development. You start by creating a class that
extends `SpigotService`. Here you can create your own `onLoad`, `onEnable` and `onDisable` functions. 

Commands and event handlers are created as usual, with the small difference, that they can be
registered using `SpigotService#registerCommand` and `SpigotService#registerEvents` respectively.

_More documentation coming soon..._

## Contributing

_More documentation coming soon..._
