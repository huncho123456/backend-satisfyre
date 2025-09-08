package com.Satisfyre.app.exceptions;

public class NameValueRequiredException extends  RuntimeException{
    public NameValueRequiredException(String message){
        super(message);
    }
}
