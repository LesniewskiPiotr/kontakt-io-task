package com.example.kontaktiotask.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class AssetServiceException extends ResponseStatusException {
    public AssetServiceException(HttpStatusCode status, String reason) {
        super(status, reason);
    }
}
