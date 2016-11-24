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
