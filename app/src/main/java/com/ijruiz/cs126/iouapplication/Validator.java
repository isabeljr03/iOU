package com.ijruiz.cs126.iouapplication;

import android.util.Log;

/**
 * Created by isabelruiz on 4/11/17.
 * A class that validates email and password strings.
 */
public class Validator {

    /**
     * Validates an email and password string
     * @param email     String representing an email in the form of "name@domain.ext"
     * @param password  String represeting a password
     * @return true if email has a valid format and if the password has at least 6 characters,
     * false otherwise
     */
    public static boolean validateCredentials(String email, String password) {
        return validateEmail(email) && validatePassword(password);
    }

    /**
     * Validates an email string.
     * @param email     String representing an email in the form of "name@domain.ext"
     * @return true if email has a valid format, false otherwise
     */
    public static boolean validateEmail(String email){
        if (email == null || email.length() == 0) {
            return false;
        }

        if (!email.contains("@")) {
            return false;
        }

        String[] nameAndDomain = email.split("@");
        if (nameAndDomain.length != 2) {
            return false;
        }

        String name = nameAndDomain[0];
        if (name.length() == 0) {
            return false;
        }

        String domain = nameAndDomain[1];
        if (domain.length() == 0 || !domain.contains(".")) {
            return false;
        }

        String[] secondDomainAndExt = domain.split("\\.");
        if (secondDomainAndExt.length != 2) {
            return false;
        }

        String secondDomain = secondDomainAndExt[0];
        if (secondDomain.length() == 0) {
            return false;
        }

        String ext = secondDomainAndExt[1];
        if (ext.length() == 0) {
            return false;
        }

        return true;
    }

    /**
     * Validates a password string
     * @param password  String representing a password
     * @return true if the password has at least 6 characters, false otherwise
     */
    public static boolean validatePassword(String password) {
        if (password == null || password.length() == 0) {
            return false;
        }
        return (password.length() >= 6);
    }

    /**
     * Validates a display name
     * @param name  String representing a display name
     * @return true if the display name is not null and not empty
     */
    public static boolean validateDisplayName(String name) {

        if (name == null) {
            return false;
        }
        if (name.length() == 0) {
            return false;
        }

        if (name.contains(".") || name.contains("$") || name.contains("[") || name.contains("]") || name.contains("#")) {
            return false;
        }

        return true;
    }

    /**
     * Validates the fields of a Transaction object.
     * @param date string representing date of transaction
     * @param payByDate string representing date that borrower need to pay by
     * @param displayName string representing display name of friend
     * @param amount string representing amount lent
     * @param location string representing location of where the transaction took place
     * @param notes string representing unique notes
     * @return true if all fields are valid, false otherwise
     */
    public static boolean validateInputTransaction(String date, String payByDate, String displayName,
                                                   String amount, String location, String notes) {
        return validateDate(date) && validateDate(payByDate) && validateDisplayName(displayName) &&
                validateAmount(amount) && validateLocation(location) && validateNotes(notes);

    }

    /**
     * Validates that a string representing a date is formatted MM/MM/YYYY
     * @param date string representing date
     * @return true if date is properly formatted and all elements can be parsed as ints
     */
    private static boolean validateDate(String date) {
        if (date == null) {
            return false;
        }
        if (date.length() == 0) {
            return false;
        }

        String[] dateElements = date.split("/");
        if (dateElements.length != 3) {
            return false;
        }
        if (dateElements[0].length() != 2 || dateElements[1].length() != 2 || dateElements[2].length() != 4) {
            return false;
        }
        try {
            int month = Integer.parseInt(dateElements[0]);
            if (month > 12 || month < 1) {
                return false;
            }

            //TODO: Check for individual months
            int day = Integer.parseInt(dateElements[1]);
            if (day > 31 || day < 1) {
                return false;
            }

            int year = Integer.parseInt(dateElements[2]);
            if (year > 2017 || year < 0) {
                return false;
            }



        } catch (NumberFormatException nfe) {
            Log.d("VALIDATOR", "NumberFormatException for date");
            return false;
        }

        return true;
    }

    /**
     * Validates that the string representing an amount is formatted as an int or double and can
     * be parsed as a double.
     * @param amount string representing amount
     * @return true if amount can be parsed as a double, false otherwise
     */
    private static boolean validateAmount(String amount) {
        if (amount == null) {
            return false;
        }
        if (amount.length() == 0) {
            return false;
        }
        try {
            double dollarAmount = Double.parseDouble(amount);
            if (dollarAmount < 0.0) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            Log.d("VALIDATOR", "NumberFormatException for amount.");
            return false;
        }
        return true;
    }

    /**
     * Validates that the string representing a location is not empty and not null/
     * @param location string representing a location
     * @return
     */
    private static boolean validateLocation(String location) {
        return (location != null && location.length() != 0);
    }

    /**
     * Validates a string representing notes about a transaction
     * @param notes string representing notes
     * @return true if notes is not null, not empty, and does not contain the characters .$[]#
     */
    private static boolean validateNotes(String notes) {
        if (notes == null) {
            return false;
        }
        if (notes.length() == 0) {
            return false;
        }

        if (notes.contains(".") || notes.contains("$") || notes.contains("[") || notes.contains("]") || notes.contains("#")) {
            return false;
        }

        return true;
    }

}
