package io.github.d2edev.distinctivering.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 15.06.16.
 */

public class MainListAdapter extends CursorAdapter{
    public static final String TAG="TAG_"+MainListAdapter.class.getSimpleName();

    //FirstName+SecondName if true, SecondName+FirstName otherwise
    private boolean mNameNativeOrder=true;



    public MainListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view= LayoutInflater.from(context).inflate(R.layout.allowed_numbers_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return  view;
    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        //Cursor should contain following columns in following order
        //FistName,SecondName,PicPath,Number
        String name="";
        Log.d(TAG, "bindView: cursor " + cursor.getString(0) +" " +cursor.getString(1)+ " "
                + cursor.getString(2)+ " " + cursor.getString(3) );
        if (mNameNativeOrder){
            name=cursor.getString(0)+" "+ cursor.getString(1);
        }else{
            name=cursor.getString(1)+" "+ cursor.getString(0);
        }
        viewHolder.tvFullName.setText(name);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            viewHolder.tvNumber.setText("+"+PhoneNumberUtils.formatNumber(cursor.getString(3), Locale.getDefault().getCountry()));
        }else{
            viewHolder.tvNumber.setText("+"+PhoneNumberUtils.formatNumber(cursor.getString(3)));
        }
        Utility.setImage(viewHolder.ivUserPic,cursor.getString(2),R.drawable.ic_person_green);

    }

    public static class ViewHolder{
        public final ImageView ivUserPic;
        public final TextView tvFullName;
        public final TextView tvNumber;

        public ViewHolder(View view){
            ivUserPic= (ImageView) view.findViewById(R.id.allowed_list_entry_pic);
            tvFullName= (TextView) view.findViewById(R.id.allowed_list_entry_name);
            tvNumber= (TextView) view.findViewById(R.id.allowed_list_entry_phone);
        }


    }

    public boolean isNameNativeOrder() {
        return mNameNativeOrder;
    }

    public void setNameNativeOrder(boolean mNameNativeOrder) {
        this.mNameNativeOrder = mNameNativeOrder;
    }
}
