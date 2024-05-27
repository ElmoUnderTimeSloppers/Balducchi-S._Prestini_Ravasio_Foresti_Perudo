package controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TerminalController {

    @GetMapping("/execute")
    public String executeCommand(@RequestParam String command) {
        String result = executeSocketCommand(command);
        return result;
    }

    private String executeSocketCommand(String command) {
        return "Risultato del comando: " + command;
    }
}
