package com.hover.stax.ui.moveMoney;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.hover.stax.R;
import com.hover.stax.ui.moveMoney.betweenServices.BetweenServicesActivity;
import com.hover.stax.ui.moveMoney.requestMoney.RequestMoneyActivity;
import com.hover.stax.ui.moveMoney.toSomeoneEsle.ToSomeElseActivity;

public class MoveMoneyFragment extends Fragment {

public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View root = inflater.inflate(R.layout.fragment_movemoney, container, false);
	root.findViewById(R.id.betweenServicesText).setOnClickListener(view-> {
	startActivity(new Intent(getActivity(), BetweenServicesActivity.class));
	});

	root.findViewById(R.id.toSomeElseText).setOnClickListener(view-> {
	startActivity(new Intent(getActivity(), ToSomeElseActivity.class));
	});

	root.findViewById(R.id.requestMoneyText).setOnClickListener(view-> {
	startActivity(new Intent(getActivity(), RequestMoneyActivity.class));
	});

	return root;
}
}
