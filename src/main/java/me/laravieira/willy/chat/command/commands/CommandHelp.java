package me.laravieira.willy.chat.command.commands;

import me.laravieira.willy.chat.command.CommandListener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CommandHelp implements CommandListener {
    public static final String COMMAND = "help";

    public void execute(@NotNull Logger console, int count, String[] args) {
        console.info("---------------------------------------------------------------");
        console.info("---                        Help Page                        ---");
        console.info("---------------------------------------------------------------");
        console.info("status           : Show Willy general status.");
        console.info("stop             : Close all connections and stop Willy.");
        console.info("talk             : Send a message to Willy, response will be printed.");
        console.info("talk debug       : Send a message to Willy, full debug response will be printed.");
        console.info("contexts         : List all contexts existent.");
        console.info("help             : Show all know commands and their descriptions.");
        console.info("player add       : Add on queue and play music on default channel.");
        console.info("player play      : Play music on player instance.");
        console.info("player resume    : Play music on player instance.");
        console.info("player pause     : Pause music on player instance.");
        console.info("player stop      : Stop music on player instance.");
        console.info("player next      : Go to next music on queue.");
        console.info("player clear     : Clear queue of player instance.");
        console.info("player destroy   : Destroy the player instance.");
        console.info("player info      : See info about the player instance.");
        console.info("user             : Show all info about an Discord user.");
        console.info("noadm ban        : Ban a member.");
        console.info("noadm unban      : Unban a member.");
        console.info("noadm op         : Give OP to a member.");
        console.info("noadm deop       : Remove OP of a member.");
        console.info("noadm mperms     : Show all member permissions.");
        console.info("noadm rperms     : Show all role permissions.");
        console.info("noadm cmperms    : Show all member permissions overwrite on channel.");
        console.info("noadm crperms    : Show all role permissions overwrite on channel.");
        console.info("whats connect    : Connect to WhatsApp and get the QR-Code if needed.");
        console.info("whats disconnect : Disconnected from WhatsApp.");
        console.info("whats reconnect  : Disconnect and reconnect again to WhatsApp.");
        console.info("whats logout     : Logout from a WhatsApp device.");
        console.info("whats chats      : Lists all WhatsApp chats.");
        console.info("---------------------------------------------------------------");
    }
}
