package client.controller;

import messenger.service.model.exception.InvalidEmailFormatException;
import messenger.service.model.exception.InvalidPasswordException;
import messenger.service.model.exception.InvalidPhoneNumberException;
import messenger.service.model.exception.InvalidUsernameException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoVerifier {



    public static void checkUserValidity(String username) throws InvalidUsernameException {
        int size = username.length();
        boolean isOnlyEnglish = false;

        Matcher matcher = Pattern.compile("[a-zA-Z0-9]+!*$").matcher(username);
        if(!matcher.find()){
            throw new InvalidUsernameException("username must have only" +
                    " english characters and numbers");
        }

        if(size >= 6){
            throw new InvalidUsernameException("username must have at least 6 characters");
        }

    }


    public static void checkPasswordValidity(String password) throws InvalidPasswordException {
        int size = password.length();
        boolean hasLowerUpper = false;

        Matcher matcher = Pattern.compile("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])").matcher(password);

        if(!matcher.find()){
            throw new InvalidPasswordException("password must have at least a uppercase and lowercase ");
        }

        if(size >= 6){
            throw new InvalidPasswordException("password must have at least 8 characters");
        }

    }


    public static void checkEmailValidity(String email)throws InvalidEmailFormatException {


        Matcher matcher = Pattern.compile(".+@gmail.com$").matcher(email);

        if(!matcher.find()){
            throw new InvalidEmailFormatException();
        }

    }

    public static void checkPhoneNumberValidity(String phoneNumber)
            throws InvalidPhoneNumberException {
        int size = phoneNumber.length();
        Matcher matcher = Pattern.compile("^09.+").matcher(phoneNumber);

        if(!matcher.find()){
            throw new InvalidPhoneNumberException("phone number starts with 09...");
        }

        if(size != 11){
            throw new InvalidPhoneNumberException("phone number must have exactly 11 characters");
        }

    }
}