package com.example.androideatit.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.example.androideatit.Adapter.MyBestDealsAdapter;
import com.example.androideatit.Adapter.MyPopularCategoriesAdapter;
import com.example.androideatit.R;

import java.util.List;

import Model.PopularCategoryModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    Unbinder unbinder;

    @BindView(R.id.recycler_popular)
    RecyclerView recycler_popular;

    @BindView(R.id.viewpager)
    LoopingViewPager viewPager;

    LayoutAnimationController layoutAnimationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        unbinder = ButterKnife.bind(this, root);
        init(); // this function call can be replaced by the lines inside function.
//        recycler_popular.setHasFixedSize(true);
//        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        homeViewModel.getPopularList().observe(this, popularCategoryModels->{

            //creating adapter
            MyPopularCategoriesAdapter adapter = new MyPopularCategoriesAdapter(getContext(), (List<PopularCategoryModel>) popularCategoryModels);
            recycler_popular.setAdapter(adapter);
            recycler_popular.setLayoutAnimation(layoutAnimationController);
        });

        homeViewModel.getBestDealList().observe(this, bestDealModels -> {
            MyBestDealsAdapter adapter = new MyBestDealsAdapter(getContext(), bestDealModels, true);
            viewPager.setAdapter(adapter);
            viewPager.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void init() {
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        recycler_popular.setHasFixedSize(true);
        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        viewPager.pauseAutoScroll();
        super.onPause();
    }
}
