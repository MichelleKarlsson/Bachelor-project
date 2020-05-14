package android.mnah;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Array;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ExtraInfoFragment extends Fragment {

    private Spinner mConditionSpinner;
    private EditText mPrice;
    private Button mInformationButton;
    private SendData sendData;
    private Spinner mModelSpinner;
    private TextView mTopDescription;
    private TextView mCurrencyText;


    private int price;
    private String condition;
    private String model;
    private String[] label;

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

        mCurrencyText = v.findViewById(R.id.currency);
        mCurrencyText.setText(getArguments().getString("currency"));
        //Description at the top of the screen
        String input = getArguments().getString("label");
        label = input.split("-");
        String capBrand = label[1].substring(0,1).toUpperCase()+label[1].substring(1);
        mTopDescription = v.findViewById(R.id.topdescription);
        mTopDescription.setText(String.format("Additional information for the description of your %s", capBrand + " " + label[0]));


        mConditionSpinner = v.findViewById(R.id.conditionspinner);
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

        mModelSpinner = v.findViewById(R.id.modelspinner);
        try {
            mModelSpinner.setAdapter(getAdapter());
        } catch (Exception e) {
            Log.e("ExtraInfoFragment", e.getMessage());
        }
        mModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                model = mModelSpinner.getSelectedItem().toString();
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
                sendData.setModel(model);
                getParentFragmentManager().popBackStack();

            }
        });


        return v;

    }

    private ArrayAdapter<CharSequence> getAdapter() {
        switch(label[1]) {
            case "dell":
                ArrayAdapter<CharSequence> adapterdell = ArrayAdapter.createFromResource(getContext(), R.array.dellmodels, R.layout.spinner_item);
                adapterdell.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                return adapterdell;
            case "acer":
                ArrayAdapter<CharSequence> adapteracer = ArrayAdapter.createFromResource(getContext(), R.array.acermodels, R.layout.spinner_item);
                adapteracer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                return adapteracer;
            case "lenovo":
                ArrayAdapter<CharSequence> adapterlenovo = ArrayAdapter.createFromResource(getContext(), R.array.lenovomodels, R.layout.spinner_item);
                adapterlenovo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                return adapterlenovo;
            case "apple":
                if (label[0].equals("phone")) {
                    ArrayAdapter<CharSequence> adapterapplephone = ArrayAdapter.createFromResource(getContext(), R.array.applephonemodels, R.layout.spinner_item);
                    adapterapplephone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    return adapterapplephone;
                } else {
                    ArrayAdapter<CharSequence> adapterapple = ArrayAdapter.createFromResource(getContext(), R.array.applemodels, R.layout.spinner_item);
                    adapterapple.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    return adapterapple;
                }
            case "android":
                ArrayAdapter<CharSequence> adapterandroid = ArrayAdapter.createFromResource(getContext(), R.array.androidbrands, R.layout.spinner_item);
                adapterandroid.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                return adapterandroid;
            default:
                break;
        }

        throw new IllegalStateException("No adapter created");
    }


    public interface SendData {
        void setCondition(String condition);
        void setPrice(int price);
        void setModel(String model);
    }

}
