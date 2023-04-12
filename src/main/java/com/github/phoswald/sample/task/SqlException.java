package com.github.phoswald.sample.task;

public class SqlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    SqlException(Throwable cause) {
        super(cause);
    }

    SqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
