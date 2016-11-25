package mapd.android.phoenix.watchyourself;

/**
 * Team Phoenix
 */


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class ImageAdapter extends BaseAdapter{

    private List<WImages> wyimages;
    private LayoutInflater mInflater;

    public ImageAdapter(List<WImages> list, LayoutInflater inflater) {
        wyimages = list;
        mInflater = inflater;
    }

    @Override
    public int getCount() {
        return wyimages.size();
    }

    @Override
    public Object getItem(int position) {
        return wyimages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewItem item;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.catalog_list_view,
                    null);
            item = new ViewItem();

            item.productImageView = (ImageView) convertView
                    .findViewById(R.id.ImageViewItem);

            item.productTitle = (TextView) convertView.findViewById(R.id.TextViewItem);

            convertView.setTag(item);
        } else {
            item = (ViewItem) convertView.getTag();
        }

        WImages curProduct = wyimages.get(position);

        item.productImageView.setImageResource(curProduct.wyImagePath);
        item.productTitle.setText(curProduct.title);

        return convertView;
    }


    private class ViewItem {
        ImageView productImageView;
        TextView productTitle;
    }
    
}
