package com.example.kontaktiotask.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class GroupServiceException extends ResponseStatusException {
    public GroupServiceException(HttpStatusCode status, String reason) {
        super(status, reason);
    }
}
