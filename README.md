### AudioPlayer - Vanilla+ Radio Bridge

A lightweight **server-side/singleplayer** Fabric addon that connects **AudioPlayer** and **Vanilla+ Radio**.

With this addon, players can broadcast their custom music discs and uploaded audio tracks across the entire server using the radio network!

### 📻 How it works

1.  Place a **Vanilla+ Radio** block directly **on top** of a **Jukebox**.
2.  Set the radio mode to **Broadcast** by right-clicking it.
3.  Insert any custom music disc created via **AudioPlayer** into the Jukebox.
4.  The audio stream is automatically broadcasted to all active radio receivers tuned into the same Redstone frequency across the world!

### 🛠️ Installation

*   **Server-side only:** This mod only needs to be installed on the **server**. Players do not need to download it to join and listen to the music!
*   **Dependencies:** Requires *Simple Voice Chat*, *AudioPlayer*, and *Vanilla+ Radio* installed on the server.

### ⚠ IMPORTANT ⚠

*   **Redstone Control Changes:** To prevent conflicts (since working Jukeboxes can emit redstone signals), **the Radio transmitter block will ONLY read its channel from the 4 horizontal sides (North, South, East, West)**. Redstone inputs from the **top** and **bottom** are completely ignored. Make sure your redstone wire or levers are connected to the sides of the Radio block to switch channels successfully!
*   **Custom Music Discs Only:** This addon strictly intercepts the digital Opus audio stream. Therefore, it will **only** work with custom audio tracks or uploaded music discs from the **AudioPlayer** mod. Vanilla Minecraft music discs will play normally in the Jukebox but will not be broadcasted over the radio.
