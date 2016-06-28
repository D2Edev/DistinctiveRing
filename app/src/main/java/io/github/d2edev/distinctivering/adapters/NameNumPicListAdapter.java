package io.github.d2edev.distinctivering.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 15.06.16.
 */

public class NameNumPicListAdapter extends CursorAdapter {
    public static final String TAG = "TAG_" + NameNumPicListAdapter.class.getSimpleName();

    //FirstName+SecondName if true, SecondName+FirstName otherwise
    private boolean mNameNativeOrder = true;

    private List<Integer> selectedNumIDs = new LinkedList<>();


    public NameNumPicListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_numbers_allowed_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    public List<Integer> getSelectedNumIDs() {
        return selectedNumIDs;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //Cursor should contain following columns in following order
        //FistName,SecondName,PicPath,Number
        String name = "";
//        Log.d(TAG, "bindView: cursor " + cursor.getString(0) +" " +cursor.getString(1)+ " "
//                + cursor.getString(2)+ " " + cursor.getString(3) );
        if (selectedNumIDs.contains(Integer.valueOf(cursor.getInt(5)))) {
            view.setBackgroundColor(Color.parseColor(context.getString(R.color.my_accent_light)));
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        name = formattedNameLine(cursor.getString(0), cursor.getString(1), mNameNativeOrder);
        viewHolder.tvFullName.setText(name);
        viewHolder.tvNumber.setText(cursor.getString(3));
        //TODO make image load at nonUI thread, enable caching
        Utility.setImage(viewHolder.ivUserPic, cursor.getString(2), R.drawable.ic_person_green);


    }

    public static class ViewHolder {
        public final ImageView ivUserPic;
        public final TextView tvFullName;
        public final TextView tvNumber;

        public ViewHolder(View view) {
            ivUserPic = (ImageView) view.findViewById(R.id.allowed_list_entry_pic);
            tvFullName = (TextView) view.findViewById(R.id.allowed_list_entry_name);
            tvNumber = (TextView) view.findViewById(R.id.allowed_list_entry_phone);
        }


    }

    public boolean isNameNativeOrder() {
        return mNameNativeOrder;
    }

    public void setNameNativeOrder(boolean mNameNativeOrder) {
        this.mNameNativeOrder = mNameNativeOrder;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }

    private String formattedNameLine(String first, String second, boolean nativeOrder) {
        String result = null;

        if (TextUtils.isEmpty(first)) return second;
        if (TextUtils.isEmpty(second)) return first;
        if (nativeOrder) {
            result = first + " " + second;
        } else {
            result = second + ", " + first;
        }
        return result;
    }


}
