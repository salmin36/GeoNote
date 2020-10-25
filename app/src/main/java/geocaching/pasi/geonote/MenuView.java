/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */

package geocaching.pasi.geonote;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

/**
 * Created by Pasi on 24/11/2016.
 */

public class MenuView extends DialogFragment {

    private User myUser;

    static MenuView newInstance(User user) {
        MenuView menuObject = new MenuView();
        // Supply argument.
        Bundle args = new Bundle();
        args.putString("username", user.getUsername());
        args.putString("password", user.getMyPassword());
        menuObject.setArguments(args);
        return menuObject;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.menu_layout, null));

        final AlertDialog ad = builder.create();
        return ad;
    }


    @Override
    public void onStart(){
        super.onStart();
        setupUiHandlers();
    }

    private void setupUiHandlers() {

        ((Button)getDialog().findViewById(R.id.menu_cancel_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ((Button)getDialog().findViewById(R.id.menu_update_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
