package com.ijruiz.cs126.iouapplication;

/**
 * Created by isabelruiz on 5/1/17.
 * Class that represents transactions between users in the application.
 */
public class Transaction {

    private String amount;
    private String date;
    private String payByDate;
    private String location;
    private String notes;

    private String lenderDisplayName;
    private String borrowerDisplayName;

    //constructors

    public Transaction() {}

    public Transaction(String amount, String date, String payByDate, String location, String notes,
                       String lenderDisplayName, String borrowerDisplayName) {
        this.amount = amount;
        this.date = date;
        this.payByDate = payByDate;
        this.location = location;
        this.notes = notes;
        this.lenderDisplayName = lenderDisplayName;
        this.borrowerDisplayName = borrowerDisplayName;
    }

    //getters

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getPayByDate() {
        return payByDate;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }

    public String getLenderDisplayName() {
        return lenderDisplayName;
    }

    public String getBorrowerDisplayName() {
        return borrowerDisplayName;
    }

    //public instance methods

    public String getDescription() {
        String description = this.lenderDisplayName + " lent $" + this.amount + " to " + this.borrowerDisplayName + ".";
        return description;
    }

    public String getDescription(String userDisplayName) {
        String description = "";
        if (this.lenderDisplayName.equals(userDisplayName)) {
            description =  "You lent $" + this.amount + " to " + this.borrowerDisplayName + ".";
        } else {
            description = "You borrowed $" + this.amount + " from " + this.lenderDisplayName + ".";
        }
        return description;
    }
}
