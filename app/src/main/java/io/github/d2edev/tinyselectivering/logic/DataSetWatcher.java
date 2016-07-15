package io.github.d2edev.tinyselectivering.logic;

/**
 * Created by d2e on 17.06.16.
 * interface to pass result from asynctask modifying records in db
 * to ui view
 */

public interface DataSetWatcher {

//    invoked on any attempt of data change passing result as parameter
    void dataSetChanged(boolean success);
}
