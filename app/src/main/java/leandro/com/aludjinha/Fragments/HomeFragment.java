package leandro.com.aludjinha.Fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.gson.Gson;

import leandro.com.aludjinha.Activities.CategoriaActivity;
import leandro.com.aludjinha.Activities.DetalhesProduto;
import leandro.com.aludjinha.Adapters.BannerSliderAdapter;
import leandro.com.aludjinha.Adapters.CategoriasAdapter;
import leandro.com.aludjinha.Adapters.ProdutoAdapter;
import leandro.com.aludjinha.Helpers.ConstantesHelper;
import leandro.com.aludjinha.Model.Banner;
import leandro.com.aludjinha.Model.Categorias;
import leandro.com.aludjinha.Model.Produto;
import leandro.com.aludjinha.Model.RequestModel.RetornoBanner;
import leandro.com.aludjinha.Model.RequestModel.RetornoCategorias;
import leandro.com.aludjinha.Model.RequestModel.RetornoProdutos;
import leandro.com.aludjinha.R;
import leandro.com.aludjinha.Service.JsonRequest;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class HomeFragment extends Fragment implements JsonRequest.PostCommentResponseListener, BannerSliderAdapter.IBannerEvent, CategoriasAdapter.ICategoriasEvent, ProdutoAdapter.IProdutoClick {


    Activity mActivity;
    Gson gson = new Gson();
    ProgressBar progressBar;
    BannerSliderAdapter bannerSliderAdapter;
    CategoriasAdapter categoriasAdapter;
    ProdutoAdapter produtosAdapter;
    ViewPager viewPager;
    TextView txtIndicator, txtCategoriasNull;
    RecyclerView recyclerViewCategorias, recyclerViewProdutos;
    LinearLayoutManager linearLayoutManager;

    boolean scroll = false;
    int limite = 5;
    int offset = 0;



    @SuppressLint("ValidFragment")
    public HomeFragment(Activity activity) {
        mActivity = activity;
    }



    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        getViews(view);

        configureRecyclerCategorias(view);
        configureRecyclerProdutos();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(bannerSliderAdapter.getCount() > 0){
                    txtIndicator.setText(setTextIndicator(bannerSliderAdapter.getCount(), position));
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new Thread(() -> {
            String urlBanner = ConstantesHelper.BASE_URL + ConstantesHelper.BANNER;
            makeRequest(urlBanner);
        }).start();
        return view;
    }

    private void configureRecyclerProdutos() {
        linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerViewProdutos.setLayoutManager(linearLayoutManager);
        recyclerViewProdutos.setNestedScrollingEnabled(false);
        recyclerViewProdutos.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scroll = true;

                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if(offset == lastVisiblePosition + 1){
                    offset +=5;
                    requestProdutos();
                }
            }
        });
    }

    private void configureRecyclerCategorias(View view) {
        recyclerViewCategorias.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
    }

    private void getViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        viewPager = view.findViewById(R.id.banner_slider);
        txtIndicator = view.findViewById(R.id.indicator);
        txtCategoriasNull = view.findViewById(R.id.txtCategoriasNull);
        recyclerViewCategorias = view.findViewById(R.id.recyclerCategorias);
        recyclerViewProdutos = view.findViewById(R.id.recyclerMaisVendidos);
    }

    private void makeRequest(String url) {
         JsonRequest.jsonObjectRequest(mActivity, Request.Method.GET, url, null, null, HomeFragment.this);
    }


    @Override
    public void requestCompleted(String json, String request, int method) {
        if(request.contains(ConstantesHelper.BANNER)){
            fillBanner(json);
            requestCategorias();
        }

        if(request.contains(ConstantesHelper.CATEGORIA)){
            fillCategories(json);
            requestProdutos();
        }

        if(request.contains(ConstantesHelper.PRODUTO_BASE)){
            fillProdutos(json);
        }
        mActivity.runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
    }



    private void requestProdutos() {
        String url_produtos = ConstantesHelper.BASE_URL + ConstantesHelper.PRODUTOS_MAIS_VENDIDOS;
        makeRequest(url_produtos);
    }

    private void requestCategorias() {
        String urlCategorias = ConstantesHelper.BASE_URL + ConstantesHelper.CATEGORIA;
        makeRequest(urlCategorias);
    }

    private void fillCategories(String json) {
        try {
            RetornoCategorias retornoCategorias = gson.fromJson(json, RetornoCategorias.class);
            categoriasAdapter = new CategoriasAdapter(retornoCategorias.getData(), HomeFragment.this);
            if(categoriasAdapter.getItemCount() > 0){
                recyclerViewCategorias.setAdapter(categoriasAdapter);
            } else{
                throw new Exception();
            }
        } catch (Exception ex){
            recyclerViewCategorias.setVisibility(View.GONE);
            txtCategoriasNull.setVisibility(View.VISIBLE);
        }
    }

    private void fillBanner(String json) {
        try{
            RetornoBanner retornoBanner = gson.fromJson(json, RetornoBanner.class);
            bannerSliderAdapter = new BannerSliderAdapter(retornoBanner.getData(), mActivity, HomeFragment.this);
            viewPager.setAdapter(bannerSliderAdapter);
            if(bannerSliderAdapter.getCount() > 0){
                txtIndicator.setText(setTextIndicator(bannerSliderAdapter.getCount(), 0));
            }
        } catch (Exception ex){
        }
    }

    private void fillProdutos(String json) {
        try {
            RetornoProdutos retornoProdutos = gson.fromJson(json, RetornoProdutos.class);
            if(!scroll){
                produtosAdapter = new ProdutoAdapter(retornoProdutos.getData(), HomeFragment.this);
                recyclerViewProdutos.setAdapter(produtosAdapter);
            } else{
                if(retornoProdutos.getData().size() > 0){
                    for(Produto produto : retornoProdutos.getData()){
                        produtosAdapter.add(produto);
                    }
                }
            }
        } catch (Exception ex){

        }
    }

    @Override
    public void requestError(String error, String request) {
        Toast.makeText(mActivity, error, Toast.LENGTH_SHORT).show();
        mActivity.runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
    }

    @Override
    public void onBannerClick(Banner banner) {
        String url = banner.getLinkUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        mActivity.startActivity(intent);
    }

    private String setTextIndicator(int count, int position){
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i <count; i++){
            if(position==i){
                stringBuilder.append(getString(R.string.indicator_full));
            } else {
                stringBuilder.append(getString(R.string.indicator_empty));
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public void onCategoriaClick(Categorias categorias) {
        Intent intent = new Intent(mActivity, CategoriaActivity.class);
        intent.putExtra(mActivity.getString(R.string.id), categorias.getId());
        intent.putExtra(mActivity.getString(R.string.categoria_name), categorias.getDescricao());
        startActivity(intent);
    }

    @Override
    public void onProdutoClick(Produto produto) {
        int id = produto.getId();
        Intent intent = new Intent(mActivity, DetalhesProduto.class);
        intent.putExtra(mActivity.getString(R.string.id), id);
        startActivity(intent);

    }
}
