package com.jaygengi.gank.ui.fragment.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.jaygengi.gank.R
import com.jaygengi.gank.base.BaseFragment
import com.jaygengi.gank.mvp.contract.CategoryContract
import com.jaygengi.gank.mvp.model.bean.CategoryEntity
import com.jaygengi.gank.mvp.presenter.CategoryPresenter
import com.jaygengi.gank.net.exception.ErrorStatus
import com.jaygengi.gank.showToast
import com.jaygengi.gank.ui.adapter.ToDayAndroidAdapter
import kotlinx.android.synthetic.main.fragment_home_common_type.*

/**
   * @description: android
   * @author JayGengi
   * @date  2018/11/7 0007 下午 5:00
   * @email jaygengiii@gmail.com
   */

class CategoryFragment : BaseFragment(), CategoryContract.View {

    //category 后面可接受参数 all | Android | iOS | 休息视频 | 福利 | 拓展资源 | 前端 | 瞎推荐 | App
    lateinit var key: String

    private val mPresenter by lazy { CategoryPresenter() }

    private var androidList = ArrayList<CategoryEntity.ResultsBean>()

    private val mAdapter by lazy { activity?.let { ToDayAndroidAdapter( androidList) } }



    override fun getLayoutId(): Int = R.layout.fragment_home_common_type

    override fun lazyLoad() {
        key = arguments!!.getString("key")
        loadData()
    }
    override fun initView() {
        mPresenter.attachView(this)
        mLayoutStatusView = multipleStatusView
        mRefreshLayout.setOnRefreshListener {
            CURRENT_PAGE =1
            loadData()
        }
        mRefreshLayout.setOnLoadMoreListener {
            CURRENT_PAGE ++
            loadData()
        }
        recycler.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter!!.setOnItemClickListener { adapter, _, position ->
            val item :CategoryEntity.ResultsBean = adapter.getItem(position) as CategoryEntity.ResultsBean
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            val contentUrl = Uri.parse(item.url)
            intent.data = contentUrl
            startActivity(intent)
        }


    }
    private fun loadData(){
        mPresenter.requestCategoryInfo(key,PAGE_CAPACITY,CURRENT_PAGE)
    }
    override fun getCategoryInfo(todayInfo: CategoryEntity) {
        mAdapter?.run {
            if (todayInfo.results != null && todayInfo.results!!.isNotEmpty()) {
                setNewData(todayInfo.results)
            } else {
                multipleStatusView?.showEmpty()
            }
        }
    }
    override fun showError(msg: String, errorCode: Int) {
        showToast(msg)
        if (errorCode == ErrorStatus.NETWORK_ERROR) {
            multipleStatusView?.showNoNetwork()

        } else {
            multipleStatusView?.showError()
        }
    }

    /**
     * 显示 Loading
     */
    override fun showLoading() {
        mLayoutStatusView?.showLoading()
    }

    /**
     * 隐藏 Loading
     */
    override fun dismissLoading() {
        multipleStatusView?.showContent()
        if(mRefreshLayout!=null && mRefreshLayout.isLoading){
            mRefreshLayout.finishRefresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    companion object {
        fun newInstance(key: String): CategoryFragment {
            val args = Bundle()
            args.putString("key", key)
            val fragment = CategoryFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
