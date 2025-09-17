package com.github.phoswald.sample.task;

public class SqlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    SqlException(Throwable cause) {
        this(null, cause);
    }

    SqlException(String message, Throwable cause) {
        if(cause == null) {
            throw new IllegalArgumentException("no cause given");
        }
        super(message, cause);
    }
}
