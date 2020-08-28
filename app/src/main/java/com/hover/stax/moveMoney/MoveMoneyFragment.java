package com.hover.stax.moveMoney;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hover.stax.R;
import com.hover.stax.moveMoney.betweenServices.BetweenServicesActivity;
import com.hover.stax.moveMoney.requestMoney.RequestMoneyActivity;
import com.hover.stax.moveMoney.toSomeoneEsle.ToSomeElseActivity;

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
