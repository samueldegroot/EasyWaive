package com.myapp.easywaiver;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/*
    This is used for any business logic, as well as to echo LiveData from the BillingRepository.
 */
public class HomeActivityViewModel extends ViewModel {
    //static final String TAG = GameViewModel.class.getSimpleName();
    private final EasyWaiveRepository tdr;

    public HomeActivityViewModel(EasyWaiveRepository trivialDriveRepository) {
        tdr = trivialDriveRepository;
    }

    public LifecycleObserver getBillingLifecycleObserver() {
        return tdr.getBillingLifecycleObserver();
    }

    public static class HomeActivityViewModelFactory implements
            ViewModelProvider.Factory {
        private final EasyWaiveRepository trivialDriveRepository;

        public HomeActivityViewModelFactory(EasyWaiveRepository tdr) {
            trivialDriveRepository = tdr;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(HomeActivityViewModel.class)) {
                return (T) new HomeActivityViewModel(trivialDriveRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}