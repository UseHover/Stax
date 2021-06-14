package com.hover.stax.balances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.FragmentBalanceBinding;
import com.hover.stax.home.MainActivity;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.staxcardstack.StaxCardStackView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BalancesFragment extends Fragment implements NavigationInterface {
    final public static String TAG = "BalanceFragment";
    final private String GREEN_BG = "#46E6CC";
    final private String BLUE_BG = "#04CCFC";
    private boolean SHOW_ADD_ANOTHER_ACCOUNT = false;
    final private static int STACK_OVERLAY_GAP = 10;
    final private static int ROTATE_UPSIDE_DOWN = 180;

    private BalancesViewModel balancesViewModel;

    private CardView addChannelLink;
    private TextView balanceTitle;
    private boolean balancesVisible = false;

    private RecyclerView balancesRecyclerView;

    private FragmentBalanceBinding binding;
    private StaxCardStackView balanceStack;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        balancesViewModel = new ViewModelProvider(requireActivity()).get(BalancesViewModel.class);
        binding = FragmentBalanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        balanceStack = binding.stackBalanceCards;
        setUpBalances();
        setUpLinkNewAccount();
    }

    private void setUpBalances() {
        initBalanceCard();
        balancesViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), this::updateServices);
    }

    private void setUpLinkNewAccount() {
        addChannelLink = binding.newAccountLink;
        addChannelLink.setOnClickListener(v -> navigateToChannelsListFragment(NavHostFragment.findNavController(this), false));
    }

    private void initBalanceCard() {
        balanceTitle = binding.homeCardBalances.balanceHeaderTitleId;
        balanceTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(balancesVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off, 0, 0, 0);
        balanceTitle.setOnClickListener(view -> {
            showBalanceCards(!balancesVisible);
        });

        balancesRecyclerView = binding.homeCardBalances.balancesRecyclerView;
        balancesRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(getContext()));
        balancesRecyclerView.setHasFixedSize(true);
    }

    private void showBalanceCards(boolean status) {
        toggleLink(status);
        balanceTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(status ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off, 0, 0, 0);

        if (status) {
            balanceStack.setVisibility(GONE);
            binding.homeCardBalances.balancesMl.transitionToEnd();
        } else {
            balanceStack.setVisibility(VISIBLE);
            binding.homeCardBalances.balancesMl.transitionToStart();
        }

        balancesVisible = status;
    }

    private void updateServices(List<Channel> channels) {
        SHOW_ADD_ANOTHER_ACCOUNT = channels != null && !Channel.hasDummy(channels) && channels.size() > 1;
        addDummyChannelsIfRequired(channels);
        BalanceAdapter balanceAdapter = new BalanceAdapter(channels, (MainActivity) getActivity());
        balancesRecyclerView.setAdapter(balanceAdapter);
        balanceAdapter.showBalanceAmounts(true);

        showBalanceCards(Channel.areAllDummies(channels));
        updateStackCard(channels);
        showBubbleIfRequired(channels);


    }
    private void showBubbleIfRequired(List<Channel> channels) {
        int title  = 0;
        int desc = 0;
        if(Channel.areAllDummies(channels)) {
            title = R.string.onboard_addaccounthead;
            desc = R.string.onboard_addaccountdesc;
        }
        else if(Channel.hasDummy(channels)) {
            title = R.string.onboard_addaccount_greatwork_head;
            desc = R.string.onboard_addaccount_greatwork_desc;
        }
        Timber.i("It's supposed to show bubble");
        new ShowcaseExecutor(requireActivity(), NavHostFragment.findNavController(this), binding).showcaseAddAccount(title, desc);

    }
    private void updateStackCard(List<Channel> channels) {
        if(channels !=null) {
            List<Channel> tempChannels = new ArrayList<>(channels);
            Collections.reverse(tempChannels);

            BalanceCardStackAdapter cardStackAdapter = new BalanceCardStackAdapter(requireContext());
            balanceStack.setAdapter (cardStackAdapter);
            cardStackAdapter.updateData(tempChannels);
            balanceStack.setOverlapGaps(STACK_OVERLAY_GAP);
            balanceStack.setRotationX(ROTATE_UPSIDE_DOWN);
            balanceStack.setOnClickListener(view -> showBalanceCards(!balancesVisible));
            updateBalanceCardStackHeight(tempChannels.size());
        }
    }

    private void updateBalanceCardStackHeight(int numOfItems) {
        ViewGroup.LayoutParams params = balanceStack.getLayoutParams();
        params.height = 20 * numOfItems;
        balanceStack.setLayoutParams(params);
    }

    private void addDummyChannelsIfRequired(@Nullable List<Channel> channels) {
        if (channels != null && channels.size() == 0) {
            channels.add(new Channel().dummy(getString(R.string.your_main_account), GREEN_BG));
            channels.add(new Channel().dummy(getString(R.string.your_other_account), BLUE_BG));
        } else if (channels != null && channels.size() == 1)
            channels.add(new Channel().dummy(getString(R.string.your_other_account), BLUE_BG));
    }

    public void toggleLink(boolean show) {
        if(SHOW_ADD_ANOTHER_ACCOUNT) {
            addChannelLink.setVisibility(show ? VISIBLE : GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
