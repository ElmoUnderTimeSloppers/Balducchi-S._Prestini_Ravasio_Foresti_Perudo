package giocoClientServer.server;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/terminal")
    @SendTo("/topic/output")
    public String handleCommand(String command) {
        String result = executeSocketCommand(command);
        return result;
    }

    private String executeSocketCommand(String command) {
        return "Risultato del comando: " + command;
    }
}
