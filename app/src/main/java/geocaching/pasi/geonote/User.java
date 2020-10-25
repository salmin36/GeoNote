/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */


package geocaching.pasi.geonote;

/**
 * Created by Pasi on 24/11/2016.
 */

public class User {
    String myUsername;
    String myPassword;
    Boolean myValidCredential;

    public User(){
        myPassword = "";
        myUsername = "";
        myValidCredential = false;
    }

    public User(String username, String password){
        myUsername = username;
        myPassword = password;
        myValidCredential = true;
    }


    public void setUsername(String username){
        myUsername = username;
        if(myUsername.length() != 0 && myPassword.length() != 0){
            myValidCredential = true;
        }
        else {
            myValidCredential = false;
        }
    }

    public String getUsername() {
        return myUsername;
    }

    public void setPassword(String password){
        myPassword = password;
        if(myUsername.length() != 0 && myPassword.length() != 0){
            myValidCredential = true;
        }
        else {
            myValidCredential = false;
        }
    }

    public String getMyPassword(){
        return myPassword;
    }

    public void setValidCredential(Boolean valid){
        myValidCredential = valid;
    }

    public Boolean isValidCredentials(){
        return myValidCredential;
    }
}
