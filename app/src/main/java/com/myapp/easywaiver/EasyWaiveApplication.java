package com.myapp.easywaiver;

import android.app.Application;

import com.myapp.easywaiver.billing.BillingDataSource;


public class EasyWaiveApplication extends Application {
    public AppContainer appContainer;

    // Container of objects shared across the whole app
    public class AppContainer {
        final BillingDataSource billingDataSource = BillingDataSource.getInstance(
                EasyWaiveApplication.this,
                EasyWaiveRepository.INAPP_SKUS,
                EasyWaiveRepository.SUBSCRIPTION_SKUS,
                EasyWaiveRepository.AUTO_CONSUME_SKUS);
        final public EasyWaiveRepository easyWaiveRepository = new EasyWaiveRepository(
                billingDataSource);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer();
    }
}
