package com.ijruiz.cs126.iouapplication;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by isabelruiz on 5/2/17.
 * Class for testing the methods of Validator
 */
public class ValidatorTest {
    @Test
    public void validateEmail() throws Exception {
        assertTrue(Validator.validateEmail("tinman@forest.com"));
        assertTrue(Validator.validateEmail("nothing@nowhere.com"));

        assertFalse(Validator.validateEmail("nothing"));
        assertFalse(Validator.validateEmail("nothing@"));
        assertFalse(Validator.validateEmail("nothing@.com"));
        assertFalse(Validator.validateEmail("nothing@nowhere."));
        assertFalse(Validator.validateEmail("@."));
        assertFalse(Validator.validateEmail(null));
        assertFalse(Validator.validateEmail(""));
    }

    @Test
    public void validatePassword() throws Exception {
        assertTrue(Validator.validatePassword("123456"));
        assertTrue(Validator.validatePassword("abcdef"));
        assertTrue(Validator.validatePassword("%^&(*$"));

        assertFalse(Validator.validatePassword(null));
        assertFalse(Validator.validatePassword(""));
        assertFalse(Validator.validatePassword("123"));
        assertFalse(Validator.validatePassword("abc"));
    }

    @Test
    public void validateInputTransaction() throws Exception {

        boolean valid1 = Validator.validateInputTransaction("02/16/2017", "03/04/2017", "Dorothy Gale", "5.99", "Cocomero", "Bought bubble tea");
        assertTrue(valid1);

        boolean invalidEmpty = Validator.validateInputTransaction("", "", "", "", "", "");
        assertFalse(invalidEmpty);

        boolean invalidNull = Validator.validateInputTransaction(null, null, null, null, null, null);
        assertFalse(invalidNull);

        boolean invalid1 = Validator.validateInputTransaction("022/164/2021317", "03/04/2017", "Dorothy Gale", "5.99", "Cocomero", "Bought bubble tea");
        assertFalse(invalid1);

        boolean invalid2 = Validator.validateInputTransaction("02/16/2017", "030/041/290017", "Dorothy Gale", "5.99", "Cocomero", "Bought bubble tea");
        assertFalse(invalid2);

        boolean invalid3 = Validator.validateInputTransaction("02/16/2017", "03/04/2017", "Dorothy.Gale", "5.99", "Cocomero", "Bought bubble tea");
        assertFalse(invalid3);

        boolean invalid4 = Validator.validateInputTransaction("02/16/2017", "03/04/2017", "Dorothy Gale", "HELLO", "Cocomero", "Bought bubble tea");
        assertFalse(invalid4);

        boolean invalid5 = Validator.validateInputTransaction("02/16/2017", "03/04/2017", "Dorothy Gale", "5.99", "", "Bought bubble tea");
        assertFalse(invalid5);

        boolean invalid6 = Validator.validateInputTransaction("02/16/2017", "03/04/2017", "Dorothy.Gale", "5.99", "Cocomero", "Bought [bubble] tea");
        assertFalse(invalid6);


    }
}