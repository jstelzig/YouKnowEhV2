package stelztech.youknowehv4.helper;

import android.app.Activity;
import android.app.ListActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import stelztech.youknowehv4.model.Deck;

/**
 * Created by alex on 2017-04-10.
 */

public class Helper {

    private static Helper instance;
    private Context context;

    private Helper(){

    }

    public static Helper getInstance(){
        if(instance == null)
            instance = new Helper();
        return instance;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void hideKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public ArrayList CompareArrayList(List<Deck> a, List<Deck> b)
    {
        ArrayList output = new ArrayList();
        for (int i = 0; i < a.size(); i++)
        {
            String str = (String) a.get(i).getDeckId();
            if (!b.contains(str))
            {
                if(!output.contains(str)) // check for dupes
                    output.add(str);
            }
        }
        return output;
    }

}