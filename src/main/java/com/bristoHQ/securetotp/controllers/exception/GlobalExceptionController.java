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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(NoHandlerFoundException ex) {
        System.out.println("NoHandlerFoundException: " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO> handleException(Exception e) {
         System.out.println("Exception e.getMessage(): " + e.getMessage());
          System.out.println("Exception e: " + e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponseDTO(false, e.getMessage(), new Date()));
    }
}
