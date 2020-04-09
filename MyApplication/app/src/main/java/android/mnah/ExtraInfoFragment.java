package android.mnah;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.mnah.R;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ExtraInfoFragment extends Fragment {

    private Spinner mConditionSpinner;
    private EditText mPrice;
    private Button mInformationButton;
    private SendData sendData;


    private int price;
    private String condition;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if (activity instanceof SendData) {
            sendData = (SendData) activity;
        } else {
            Log.e("Info", "Error sending information to parent activity");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_extra_info, container, false);

        mConditionSpinner = v.findViewById(R.id.dropdown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.item_conditions, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mConditionSpinner.setAdapter(adapter);
        mConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                condition = mConditionSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        mPrice = v.findViewById(R.id.price);

        mInformationButton = v.findViewById(R.id.information_button);
        mInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int price;
                if (mPrice.getText().toString().equals("")) {
                    price = 0;
                } else {
                    price = Integer.parseInt(mPrice.getText().toString());
                }

                sendData.setPrice(price);
                sendData.setCondition(condition);
                getParentFragmentManager().popBackStack();

            }
        });


        return v;

    }

    public interface SendData {
        void setCondition(String condition);
        void setPrice(int price);
    }

}
