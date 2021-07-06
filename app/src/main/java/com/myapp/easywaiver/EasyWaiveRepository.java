package com.myapp.easywaiver;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.myapp.easywaiver.billing.BillingDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The repository uses data from the Billing data source and the game state model together to give a
 * unified version of the state of the game to the ViewModel. It works closely with the
 * BillingDataSource to implement consumable items, premium items, etc.
 */
public class EasyWaiveRepository {
    // Source for all constants

    static final public String SKU_EASY_WAIVE_APP_SUBSCRIPTION = "easy_waive_app_subscription";
    static final public String SKU_EASY_WAIVE_APP_SUBSCRIPTION_YEARLY = "easy_waive_app_subscription_yearly";

    static final String TAG = "EasyWaive:" + EasyWaiveRepository.class.getSimpleName();
    static final String[] INAPP_SKUS = new String[]{};
    static final String[] SUBSCRIPTION_SKUS = new String[]{SKU_EASY_WAIVE_APP_SUBSCRIPTION};
    static final String[] AUTO_CONSUME_SKUS = new String[]{};

    final BillingDataSource billingDataSource;
    //final GameStateModel gameStateModel;
    //final SingleMediatorLiveEvent<Integer> gameMessages;
    //final SingleMediatorLiveEvent<Integer> allMessages = new SingleMediatorLiveEvent<>();
    final ExecutorService driveExecutor = Executors.newSingleThreadExecutor();

    public EasyWaiveRepository(BillingDataSource billingDataSource) {
        this.billingDataSource = billingDataSource;
        //this.gameStateModel = gameStateModel;

        //gameMessages = new SingleMediatorLiveEvent<>();
        setupMessagesSingleMediatorLiveEvent();

        // Since both are tied to application lifecycle
        /*
        billingDataSource.observeConsumedPurchases().observeForever(skuList -> {
            for ( String sku: skuList ) {
                if (sku.equals(SKU_GAS)) {
                    gameStateModel.incrementGas(GAS_TANK_MAX);
                }
            }
        });

         */
    }

    /**
     * Sets up the event that we can use to send messages up to the UI to be used in Snackbars. This
     * SingleMediatorLiveEvent observes changes in SingleLiveEvents coming from the rest of the game
     * and combines them into a single source with new purchase events from the BillingDataSource.
     * Since the billing data source doesn't know about our SKUs, it also transforms the known SKU
     * strings into useful String messages.
     */

    void setupMessagesSingleMediatorLiveEvent() {
        final LiveData<List<String>> billingMessages = billingDataSource.observeNewPurchases();
        /*
        allMessages.addSource(gameMessages, allMessages::setValue);
        allMessages.addSource(billingMessages,
                stringList -> {
                    // TOD0: Handle multi-line purchases better
                    for (String s: stringList) {
                        switch (s) {
                            case SKU_GAS:
                                allMessages.setValue(R.string.message_more_gas_acquired);
                                break;
                            case SKU_PREMIUM:
                                allMessages.setValue(R.string.message_premium);
                                break;
                            case SKU_INFINITE_GAS_MONTHLY:
                            case SKU_INFINITE_GAS_YEARLY:
                                // this makes sure that upgraded and downgraded subscriptions are
                                // reflected correctly in the app UI
                                billingDataSource.refreshPurchasesAsync();
                                allMessages.setValue(R.string.message_subscribed);
                                break;
                        }
                    }
                });

         */
    }



    /**
     * Drive the car (if we can). This is an asynchronous operation.
     */
    /*
    public void drive() {
        // We run this all on a background thread since we're not using a LiveData observable
        // to get the gas level and want to avoid doing database queries on the main thread.
        final LiveData<Integer> gasTankLevelLiveData = gasTankLevel();
        gasTankLevelLiveData.observeForever(
                new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer gasLevel) {
                        if ( null == gasLevel ) return;
                        switch (gasLevel) {
                            case EasyWaiveRepository.GAS_TANK_INFINITE:
                                // We never use gas in the tank if we have a subscription
                                sendMessage(R.string.message_infinite_drive);
                                break;
                            case EasyWaiveRepository.GAS_TANK_MIN:
                                sendMessage(R.string.message_out_of_gas);
                                break;
                            case EasyWaiveRepository.GAS_TANK_MIN + 1:
                                gameStateModel.decrementGas(GAS_TANK_MIN);
                                sendMessage(R.string.message_out_of_gas);
                                break;
                            default:
                                gameStateModel.decrementGas(GAS_TANK_MIN);
                                sendMessage(R.string.message_you_drove);
                                break;
                        }
                        gasTankLevelLiveData.removeObserver(this);
                    }
                });
    }

     */

    /**
     * Automatic support for upgrading/downgrading subscription.
     *
     * @param activity Needed by billing library to start the Google Play billing activity
     * @param sku the product ID to purchase
     */
    public void buySku(Activity activity, String sku) {
        String oldSku = null;
        switch (sku) {
            case SKU_EASY_WAIVE_APP_SUBSCRIPTION:
                oldSku = SKU_EASY_WAIVE_APP_SUBSCRIPTION_YEARLY;
                break;
            case SKU_EASY_WAIVE_APP_SUBSCRIPTION_YEARLY:
                oldSku = SKU_EASY_WAIVE_APP_SUBSCRIPTION;
                break;
        }
        if ( null != oldSku ) {
            billingDataSource.launchBillingFlow(activity, sku, oldSku);
        } else {
            billingDataSource.launchBillingFlow(activity, sku);
        }
    }

    /**
     * Return LiveData that indicates whether the sku is currently purchased.
     *
     * @param sku the SKU to get and observe the value for
     * @return LiveData that returns true if the sku is purchased.
     */
    public LiveData<Boolean> isPurchased(String sku) {
        return billingDataSource.isPurchased(sku);
    }

    /*
    private void combineGasAndCanPurchaseData(
            MediatorLiveData<Boolean> result,
            LiveData<Integer> gasTankLevel,
            LiveData<Boolean> canPurchase) {
        // don't emit until we have all of our data
        if (null == canPurchase.getValue() || null == gasTankLevel.getValue()) {
            return;
        }
        Log.d(TAG, "GetPurchase: " + canPurchase.getValue() + " GasTankLevel: "
                + gasTankLevel.getValue());
        result.setValue(canPurchase.getValue() && (gasTankLevel.getValue() < GAS_TANK_MAX));
    }

     */

    /*
     * We can buy if we have at least one unit of gas and a purchase isn't in progress. For other
     * skus, we can purchase them if they aren't already purchased. For subscriptions, only one of
     * the two should be held at a time, although that is only enforced by business logic.
     *
     * @param sku the product ID to get and observe the value for
     * @return LiveData that returns true if the sku can be purchased
     */
    /*
    public LiveData<Boolean> canPurchase(String sku) {
        switch (sku) {
            case SKU_GAS: {
                final MediatorLiveData<Boolean> result = new MediatorLiveData<>();
                final LiveData<Integer> gasTankLevel = gasTankLevel();
                final LiveData<Boolean> canPurchaseSku = billingDataSource.canPurchase(sku);
                result.addSource(gasTankLevel, level ->
                        combineGasAndCanPurchaseData(result, gasTankLevel, canPurchaseSku));
                result.addSource(canPurchaseSku, canPurchase ->
                        combineGasAndCanPurchaseData(result, gasTankLevel, canPurchaseSku));
                return result;
            }
            default:
                return billingDataSource.canPurchase(sku);
        }
    }

     */

    /*
    private void combineGasAndSubscriptionData(
            MediatorLiveData<Integer> result,
            LiveData<Integer> gasTankLevel,
            LiveData<Boolean> monthlySubscription,
            LiveData<Boolean> yearlySubscription
    ) {
        Boolean isMonthlySubscription = monthlySubscription.getValue();
        Boolean isYearlySubscription = yearlySubscription.getValue();
        if (
                null == isMonthlySubscription ||
                        null == isYearlySubscription
        ) return; // do not emit

        if (isMonthlySubscription || isYearlySubscription) {
            result.setValue(GAS_TANK_INFINITE);
        } else {
            Integer gasTankLevelValue = gasTankLevel.getValue();
            if (null == gasTankLevelValue) return;
            result.setValue(gasTankLevelValue);
        }
    }

     */

    /**
     * Combine the results from our subscription LiveData with our gas tank level to get our real
     * gas tank level.
     *
     * @return LiveData that represents the gasTankLevel by game logic.
     */
    /*
    public LiveData<Integer> gasTankLevel() {
        final MediatorLiveData<Integer> result = new MediatorLiveData<>();
        final LiveData<Integer> gasTankLevel = gameStateModel.gasTankLevel();
        final LiveData<Boolean> monthlySubPurchased = isPurchased(SKU_INFINITE_GAS_MONTHLY);
        final LiveData<Boolean> yearlySubPurchased = isPurchased(SKU_INFINITE_GAS_YEARLY);

        result.addSource(gasTankLevel, level ->
                combineGasAndSubscriptionData(result, gasTankLevel,
                        monthlySubPurchased, yearlySubPurchased));
        result.addSource(monthlySubPurchased, subPurchased ->
                combineGasAndSubscriptionData(result, gasTankLevel,
                        monthlySubPurchased, yearlySubPurchased));
        result.addSource(yearlySubPurchased, subPurchased ->
                combineGasAndSubscriptionData(result, gasTankLevel,
                        monthlySubPurchased, yearlySubPurchased));
        return result;
    }

     */

    public final void refreshPurchases() {
        billingDataSource.refreshPurchasesAsync();
    }

    public final LifecycleObserver getBillingLifecycleObserver() {
        return billingDataSource;
    }

    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc.
    public final LiveData<String> getSkuTitle(String sku) {
        LiveData<String> temp = billingDataSource.getSkuTitle(sku);
        return temp;
        //return billingDataSource.getSkuTitle(sku);
    }

    public final LiveData<String> getSkuPrice(String sku) {
        return billingDataSource.getSkuPrice(sku);
    }

    public final LiveData<String> getSkuDescription(String sku) {
        return billingDataSource.getSkuDescription(sku);
    }

    /*
    public final LiveData<Integer> getMessages() {
        return allMessages;
    }

     */

    /*
    public final void sendMessage(int resId) {
        gameMessages.postValue(resId);
    }

     */

    public final LiveData<Boolean> getBillingFlowInProcess() {
        return billingDataSource.getBillingFlowInProcess();
    }

    /*
    public final void debugConsumePremium() {
        billingDataSource.consumeInappPurchase(SKU_PREMIUM);
    }

     */
}
