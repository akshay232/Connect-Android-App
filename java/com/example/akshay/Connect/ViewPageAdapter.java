package com.example.akshay.Connect;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Akshay on 12/9/2017.
 */

class ViewPageAdapter extends FragmentPagerAdapter {
    public ViewPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0:
                return "Request";
            case 1:
                return "Chat";
            case 2:
                return "Friends";
            default:
                return null;
        }

    }
    @Override
    public int getCount() {
        return 3;
    }
}
