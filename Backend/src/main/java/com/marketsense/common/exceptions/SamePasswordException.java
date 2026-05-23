package com.marketsense.common.exceptions;

public class SamePasswordException extends RuntimeException{
    public SamePasswordException(String message){super(message);}
}
