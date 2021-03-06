package com.james.footballsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.james.footballsim.FootballSim;
import org.robovm.apple.uikit.UIEdgeInsets;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIView;

import java.lang.reflect.InvocationTargetException;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        double scale = UIScreen.getMainScreen().getNativeScale();
        System.out.println( "Scale:"+scale);
        IOSApplication  app = new IOSApplication(new FootballSim(scale), config);
        return app;
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }

    public static Vector2 getSafeAreaInsets() {
        if (Foundation.getMajorSystemVersion() >= 11) {
            UIView view = UIApplication.getSharedApplication().getKeyWindow().getRootViewController().getView();
            UIEdgeInsets edgeInsets = view.getSafeAreaInsets();

            double top = edgeInsets.getTop() * view.getContentScaleFactor();
            double bottom = edgeInsets.getBottom() * view.getContentScaleFactor();
            return new Vector2((float) top, (float) bottom);
        }
        return new Vector2();
    }

}