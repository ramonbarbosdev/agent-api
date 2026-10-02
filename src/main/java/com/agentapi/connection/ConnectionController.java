package com.agentapi.connection;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.web.ConnectionResponse;
import com.agentapi.web.ConnectionTestResponse;
import com.agentapi.web.CreateCursorConnectionRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConnectionResponse> list() {
        return connectionService.listMine();
    }

    @PostMapping(value = "/cursor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ConnectionResponse createCursor(@Valid @RequestBody CreateCursorConnectionRequest request) {
        return connectionService.createCursor(request);
    }

    @PostMapping(value = "/{id}/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConnectionTestResponse test(@PathVariable UUID id) {
        return connectionService.test(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        connectionService.delete(id);
    }
}
