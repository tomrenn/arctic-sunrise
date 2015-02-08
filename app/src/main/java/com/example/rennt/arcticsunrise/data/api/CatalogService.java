package com.example.rennt.arcticsunrise.data.api;

import com.example.rennt.arcticsunrise.data.api.models.Catalog;

import rx.Observable;


public interface CatalogService {

    Observable<Catalog> getCatalogObservable();

    Observable<Catalog> getCatalogObservable(final boolean useCache);
}
