package com.example.gek.pizza.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.gek.pizza.R;
import com.example.gek.pizza.adapters.DeliveriesAdapter;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeliveriesActivity extends BaseActivity {

    private static final String TAG = "DeliveriesActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // Хранит все наши вкладки с переключателем
    private ViewPager mViewPager;

    public static boolean activeDeliveriesActivity;


    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activeDeliveriesActivity = false;

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_deliveries, null, false);
        mDrawer.addView(contentView, 0);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_orders);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // Создаем адаптер, который будет возвращать фрагменты для каждой из трех секций активити
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Указываем ViewPager-у наш адаптер, который обеспечит контентом вкладки
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Указываем переключателю вкладок вью, которое будет отображать контент
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    /** Фрагмент, содержащий контент наших вкладок */
    public static class PlaceholderFragment extends Fragment {

        // Через эту переменную будет указывать номер выбранной вкладки
        private static final String ARG_SECTION_NUMBER = "section_number";
        private ArrayList<Delivery> listDeliveries = new ArrayList<>();
        private RecyclerView rv;
        public PlaceholderFragment() {
        }

        // Создаем фрагмент и передаем ему параметр с номером, соответствующим вкладке
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        // Собственно отрисовка фрагмента с заполнением вью контентом по полученному номеру вкладки
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_deliveries, container, false);

            rv = (RecyclerView) rootView.findViewById(R.id.rv);
            rv.setLayoutManager(new LinearLayoutManager(container.getContext()));

            final int num = getArguments().getInt(ARG_SECTION_NUMBER);

            // В зависимости от номера вкладки ставим слушатель на нужный нам раздел в БД
            // и создаем адаптер с соответствующим параметром
            ValueEventListener deliveriesListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    listDeliveries.clear();
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        Delivery delivery = child.getValue(Delivery.class);
                        listDeliveries.add(delivery);
                    }
                    switch (num) {
                        case 1:
                            rv.setAdapter(new DeliveriesAdapter(
                                    listDeliveries,
                                    getActivity(),
                                    Const.CHILD_DELIVERIES_NEW));
                            break;
                        case 2:
                            rv.setAdapter(new DeliveriesAdapter(
                                    listDeliveries,
                                    getContext(),
                                    Const.CHILD_DELIVERIES_COOKING));
                            break;
                        case 3:
                            rv.setAdapter(new DeliveriesAdapter(
                                    listDeliveries,
                                    getContext(),
                                    Const.CHILD_DELIVERIES_TRANSPORT));
                            break;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadDeliveries:onCancelled", databaseError.toException());
                }
            };

            switch (num){
                case 1:
                    Const.db.child(Const.CHILD_DELIVERIES_NEW).addValueEventListener(deliveriesListener);
                    break;
                case 2:
                    Const.db.child(Const.CHILD_DELIVERIES_COOKING).addValueEventListener(deliveriesListener);
                    break;
                case 3:
                    Const.db.child(Const.CHILD_DELIVERIES_TRANSPORT).addValueEventListener(deliveriesListener);
                    break;
            }

            return rootView;
        }
    }


    /** Фрагмент адаптер, который хранит наши фрагменты в памяти и обеспечивает к ним доступ */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_orders_new);
                case 1:
                    return getResources().getString(R.string.tab_orders_cook);
                case 2:
                    return getResources().getString(R.string.tab_orders_in_transit);
            }
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP) {
            menu.add(0, Const.ACTION_ARCHIVE, 0, R.string.action_archive);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Const.ACTION_ARCHIVE:
                startActivity(new Intent(this, ArchiveDeliveriesActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        activeDeliveriesActivity = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        activeDeliveriesActivity = false;
        super.onStop();
    }
}
