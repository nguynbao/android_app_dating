package com.example.myapplication.network;

import com.example.myapplication.model.City;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ProvinceApi {
    @GET("api/")
    Call<List<City>> getProvinces();
}
