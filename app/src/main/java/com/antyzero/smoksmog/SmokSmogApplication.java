package com.antyzero.smoksmog;

import android.app.Application;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SmokSmogApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with( this, new CrashlyticsCore.Builder()
                .disabled( BuildConfig.DEBUG )
                .build() );

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule( new ApplicationModule( this ) )
                .build();

        CalligraphyConfig.initDefault( new CalligraphyConfig.Builder()
                //.setDefaultFontPath( "fonts/Roboto-Regular.ttf" )
                .build() );
    }

    public ApplicationComponent getAppComponent() {
        return applicationComponent;
    }

    /**
     * Kept to inject mocked components.
     *
     * @param applicationComponent for replace
     */
    @VisibleForTesting
    public void setAppComponent( ApplicationComponent applicationComponent ) {
        this.applicationComponent = applicationComponent;
    }

    /**
     * Get access to application instance
     *
     * @param context
     * @return
     */
    public static final SmokSmogApplication get( Context context ) {
        return (SmokSmogApplication) context.getApplicationContext();
    }
}
