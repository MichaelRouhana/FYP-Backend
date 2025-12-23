package com.example.AzureTestProject.Api.Function;


public interface CostFunc<P, V> {
    V execute(P p);
}
