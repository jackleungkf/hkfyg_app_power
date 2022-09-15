package com.hkfyg.camp.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter

class TabViewPagerAdapter: FragmentStatePagerAdapter{
    var fragmentStackMap: MutableMap<Int, ArrayList<Fragment>> = mutableMapOf()

    constructor(fragmentManager: FragmentManager, fragmentStackMap: MutableMap<Int, ArrayList<Fragment>>): super(fragmentManager){
        this.fragmentStackMap = fragmentStackMap
    }

    override fun getCount(): Int{
        return this.fragmentStackMap.size
    }

    override fun getItem(position: Int): Fragment{
        val fragmentList = this.fragmentStackMap.get(position)
        if(fragmentList != null){
            return fragmentList.get(fragmentList.size-1)
        }
        return Fragment()
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}