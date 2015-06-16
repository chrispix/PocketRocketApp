package com.liebwerks.PocketRocketApp;

/**
 * Created by klieberman on 5/25/15.
 */
public class RgbCommand {
    String name;
    byte[] command;

    RgbCommand(String name, byte[] command) {
        this.name = name;
        this.command = command;
        fixChecksum(this.command);
    }

    void fixChecksum(byte[] command) {
        if(command.length == 8) {
            this.command[7] = (byte) (command[0] + command[1] + command[2] + command[3] + command[4] + command[5] + command[6]);
        }
    }
}
