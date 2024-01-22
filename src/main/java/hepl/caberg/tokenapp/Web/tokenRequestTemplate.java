package hepl.caberg.tokenapp.Web;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class tokenRequestTemplate implements Serializable {
    private String bankNumber;
    private Date date;

    public tokenRequestTemplate() {
    }

    public tokenRequestTemplate(String bankNumber, Date date) {
        this.bankNumber = bankNumber;
        this.date = date;
    }

    public String getBankNumber() { return bankNumber; }
    public void setBankNumber(String bankNumber) { this.bankNumber = bankNumber; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}
