package unimelb.dix1.fuzzylocation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.app.DialogFragment;
import android.widget.Button;


public class HelpFragment extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            View view = inflater.inflate(R.layout.fragment_help, container);

            Button ok = (Button)view.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });

            Button toAboutPage = (Button)view.findViewById(R.id.toAboutPage);
            toAboutPage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), About.class);

                    startActivity(intent);
                    dismiss();
                }
            });
            return view;
        }
}
