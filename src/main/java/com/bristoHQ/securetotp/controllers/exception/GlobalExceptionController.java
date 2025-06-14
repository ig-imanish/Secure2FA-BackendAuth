package com.bristoHQ.securetotp.controllers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.bristoHQ.securetotp.dto.MessageResponseDTO;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionController {

    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<String> exception(Exception ex) {
    //     System.out.println("Exception: " + ex.getMessage());
    //     return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    // }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(NoHandlerFoundException ex) {
        System.out.println("NoHandlerFoundException: " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO> handleException(Exception e) {
         System.out.println("Exception 1: " + e.getMessage());
          System.out.println("Exception 2: " + e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
    }
}
