package io.github.d2edev.distinctivering.db;

import android.provider.BaseColumns;

/**
 * Created by d2e on 10.06.16.
 */

public class DataContract {

    public static final class Person implements BaseColumns{
        public static final String TABLE_NAME="person";
        public static final String COLUMN_FIRST_NAME="first_name";
        public static final String COLUMN_LAST_NAME="last_name";
        public static final String COLUMN_PIC_PATH="last_name";
    }

    public static final class PhoneNumber implements BaseColumns{
        public static final String TABLE_NAME="phone_number";
        public static final String COLUMN_NUMBER ="number";
        public static final String COLUMN_KEY_PERSON="id_person";
        public static final String COLUMN_KEY_NUMBER_TYPE="id_numbertype";
    }

    public static final class PhoneNumberType implements BaseColumns{
        public static final String TABLE_NAME="phone_number_type";
        public static final String COLUMN_NUMBER_TYPE="number_type";

    }
}
