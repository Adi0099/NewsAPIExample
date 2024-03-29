package com.example.newsapiexample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;


import com.example.newsapiexample.R;
import com.example.newsapiexample.adaptor.NewsAdaptor;
import com.example.newsapiexample.model.News;
import com.example.newsapiexample.networks.APIClient;
import com.example.newsapiexample.networks.APIService;
import com.example.newsapiexample.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsListActivity extends AppCompatActivity {
    APIService mAPIService;

    RecyclerView newsRecyclerView;

    NewsAdaptor newsAdaptor;

    Call<News> newsCall;

    List<News.Article> newsList;

    String newsType;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        progressBar = findViewById(R.id.newsListProgressbar);
        progressBar.setIndeterminate(true);
        mAPIService = APIClient.getClient().create(APIService.class);
        Intent receivedIntent = getIntent();
        newsType = receivedIntent.getStringExtra(Constants.NEWS_TYPE);
        newsRecyclerView = findViewById(R.id.newsListRecyclerView);
        newsList = new ArrayList<>();
        newsAdaptor = new NewsAdaptor(this, newsList, newsType);
        newsRecyclerView.setAdapter(newsAdaptor);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        loadData();
    }

    private void loadData() {
        if (newsType.equals("top_news")) {
            newsCall = mAPIService.
                    topNews(getResources().getString(R.string.country), getResources().getString(R.string.api_key));
            newsCall.enqueue(new Callback<News>() {
                @Override
                public void onResponse(Call<News> call, Response<News> response) {
                    if (!response.isSuccessful()) {
                        newsCall = call.clone();
                        newsCall.enqueue(this);
                        return;
                    }
                    progressBar.setVisibility(View.GONE);
                    if (response.body() == null) return;
                    newsList.addAll(response.body().getArticles());
                    newsAdaptor.notifyDataSetChanged();

                }

                @Override
                public void onFailure(Call<News> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                }
            });

        } else {
            newsCall = mAPIService.topNewsCategory(getResources().getString(R.string.country), newsType, getResources().getString(R.string.api_key));
            newsCall.enqueue(new Callback<News>() {
                @Override
                public void onResponse(Call<News> call, Response<News> response) {
                    if (!response.isSuccessful()) {
                        newsCall = call.clone();
                        newsCall.enqueue(this);
                        return;
                    }
                    progressBar.setVisibility(View.GONE);

                    if (response.body() == null) return;
                    newsList.addAll(response.body().getArticles());
                    newsAdaptor.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<News> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newsCall != null) newsCall.cancel();
    }
}
