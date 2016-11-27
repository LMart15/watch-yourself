package mapd.android.phoenix.watchyourself;

/**
 * Team Phoenix
 */


import android.content.Context;
import android.content.res.Resources;

import java.util.List;
import java.util.Vector;

public class ImagesList {

    public static final String PRODUCT_INDEX = "PRODUCT_INDEX";

    private static List<WImages> catalog;
    private static List<WImages> cart;
    private static Context context;

    public static List<WImages> getCatalog(Resources res){
        if(catalog == null) {
            catalog = new Vector<WImages>();
            catalog.add(new WImages(context.getString(R.string.record_video), R.drawable.camera256,"Record Video"));
            catalog.add(new WImages("Record Voice", R.drawable.mic256,"Record Audio"));
            catalog.add(new WImages("Msg Emergency Contacts", R.drawable.msg128,"Send Message"));
            catalog.add(new WImages("Call Emergency Contacts", R.drawable.contact128,"Call Now"));
        }

        return catalog;
    }

    public static List<WImages> getCart() {
        if(cart == null) {
            cart = new Vector<WImages>();
        }

        return cart;
    }


}
