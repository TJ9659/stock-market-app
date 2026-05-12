package com.marketsense.exceptions;

public class SameAccountException extends RuntimeException{
    public SameAccountException(String message){
        super(message);
    }
}
