package ch.uzh.ifi.seal.soprafs16.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by soyabeen on 29.03.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResource implements Serializable {

        private static final long serialVersionUID = 1L;
        private String status;
        private String message;

        protected ErrorResource() {
            /**
             * Here just for jackson.
             */
        }

        public ErrorResource(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
