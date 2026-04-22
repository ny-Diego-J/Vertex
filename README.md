# Vertex

Vertex is a node based file explorer that is based on [lwjgl](https://www.lwjgl.org/).

## Table of content

- [Infos](#infos)
- [Features](#features)

## Infos

Vertex uses Nodes to display files and directories. This means if a directory has 200 files Vertex will display 201 Nodes.
This means that i can get a bit laggy if you open the System32 folder. The team around Vertex (me) is always working to improve perfomance.

## Features

### physics display

Once you start the program you will see your C drive with its subdirectories. The directories will be arranged in a circle around the C directory.
If you drag your C drive (or your current focused folder) the other nodes will drag along. If you move your current directory against a subdirectory
it will bounce away. This also happens with the nodes if the collide against each other.

### directory navigation

If you click on a node it will open it. If it's a file it gets executed in the default program. If the node is a directory it will be the new current directory and
its subdirectories will be displayed.

### Idle mode

If you don't interact with the program for a minute the program will activate the Idle mode.
In this mode every node will just move in a direction and collide with the window border and the other nodes.
The current directory will also move and is still draggable. It will also show lines to all nodes that it is checking for collisions.
