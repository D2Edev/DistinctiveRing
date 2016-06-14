package io.github.d2edev.distinctivering;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class MainListFragment extends Fragment {



    public MainListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_list, container, false);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_list_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_main_add_item:{
                Toast.makeText(getActivity(),getString(R.string.action_main_add_title),Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.action_main_delete_item:{
                Toast.makeText(getActivity(),getString(R.string.action_menu_main_delete_title),Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.action_main_add_item_manual:{
                Toast.makeText(getActivity(),getString(R.string.action_add_manual_title),Toast.LENGTH_SHORT).show();
                DialogFragment manualAddDialog = new ManualAddDialog();
                manualAddDialog.show(getActivity().getSupportFragmentManager(), "manual");
                break;
            }
            case R.id.action_help:{
                Toast.makeText(getActivity(),getString(R.string.action_menu_main_help_title),Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }




}
