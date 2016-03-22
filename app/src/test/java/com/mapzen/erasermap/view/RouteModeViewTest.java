package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.erasermap.controller.MainActivity;
import com.mapzen.erasermap.dummy.TestHelper;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.shadows.ShadowMapController;
import com.mapzen.erasermap.shadows.ShadowMapData;
import com.mapzen.erasermap.util.NotificationCreator;
import com.mapzen.valhalla.Route;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.mapzen.erasermap.dummy.TestHelper.getFixture;
import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static com.mapzen.erasermap.dummy.TestHelper.getTestLocation;
import static com.mapzen.erasermap.view.RouteModeView.MAP_DATA_NAME_ROUTE_ICON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RouteModeViewTest {
    private RouteModeView routeModeView;
    private InstructionAdapter adapter;
    private MainActivity startActivity;
    private ViewGroup viewGroup;

    @Before
    public void setUp() throws Exception {
        startActivity = Robolectric.setupActivity(MainActivity.class);
        startActivity.showRoutePreview(getTestLocation(), getTestFeature());
        startActivity.success(new Route(getFixture("valhalla_route")));
        startActivity.startRoutingMode(getTestFeature());
        routeModeView = (RouteModeView) startActivity.findViewById(R.id.route_mode);
        adapter = (InstructionAdapter) ((ViewPager) startActivity
                .findViewById(R.id.instruction_pager)).getAdapter();
        viewGroup = new TestViewGroup(application);
    }

    @Test
    public void routeModeView_shouldNotBeNull() throws Exception {
        assertThat(routeModeView).isNotNull();
    }

    @Test
    public void routeFooter_shouldNotBeNull() throws Exception {
        assertThat(routeModeView.findViewById(R.id.footer)).isNotNull();
    }

    @Test
    public void adapter_ShouldNotBeNull() throws Exception {
        assertThat(adapter).isNotNull();
    }

    @Test
    public void onRestoreViewState_shouldRestoreRoutingView() {
        startActivity.getPresenter().onRestoreViewState();
        assertThat(routeModeView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void defaultInstruction_shouldHaveGrayBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.light_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void activeInstruction_shouldWhiteBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        adapter.setBackgroundColorActive(view);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(android.R.color.white);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void inactiveInstruction_shouldHaveGrayBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        adapter.setBackgroundColorActive(view);
        adapter.setBackgroundColorInactive(view);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.light_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void arrivedInstruction_shouldHaveDestinationBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        adapter.setBackgroundColorArrived(view);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.light_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Test
    public void firstInstruction_shouldHaveFirstStreetName() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        TextView instructionText = (TextView) view.findViewById(R.id.instruction_text);
        TextView distance = (TextView) view.findViewById(R.id.distance);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        assertThat(instructionText.getText().toString()).isEqualTo("Adalbertstraße");
        assertThat(distance.getText().toString()).isEqualTo("0.2 mi");
        assertThat(icon.getDrawable()).isEqualTo(startActivity.getDrawable(R.drawable.ic_route_1));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Test
    public void paging_ShouldSwitchInstruction() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 1);
        TextView instructionText = (TextView) view.findViewById(R.id.instruction_text);
        TextView distance = (TextView) view.findViewById(R.id.distance);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        assertThat(instructionText.getText().toString()).isEqualTo("Engeldamm");
        assertThat(distance.getText().toString()).isEqualTo("0.1 mi");
        assertThat(icon.getDrawable()).isEqualTo(startActivity.getDrawable(R.drawable.ic_route_15));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Test
    public void lastInstruction_shouldHaveFirstInstruction() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup,
                routeModeView.getInstructionPager().getAdapter().getCount() - 1);
        TextView instructionText = (TextView) view.findViewById(R.id.instruction_text);
        TextView distance = (TextView) view.findViewById(R.id.distance);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        assertThat(instructionText.getText().toString())
                .isEqualTo("You have arrived at your destination.");
        assertThat(distance.getText().toString()).isEqualTo("");
        assertThat(icon.getDrawable()).isEqualTo(startActivity.getDrawable(R.drawable.ic_route_4));
    }

    @Test
    public void footer_shouldHaveCorrectDistanceAndDestination() throws Exception {
        TextView distance = (TextView) startActivity.findViewById(R.id.destination_distance);
        TextView destinationText = (TextView) startActivity.findViewById(R.id.destination_name);
        assertThat(distance.getText().toString()).isEqualTo("1.2 mi");
        assertThat(destinationText.getText()).isEqualTo(TestHelper.TEST_NAME);
    }

    @Test
    public void playPostInstructionAlert_shouldUpdateIcon() throws Exception {
        routeModeView.playPostInstructionAlert(1);
        ImageView icon = (ImageView) routeModeView.findViewByIndex(1).findViewById(R.id.icon);
        assertThat(Shadows.shadowOf(icon).getImageResourceId()).isEqualTo(application.getResources()
                .getIdentifier("ic_route_8", "drawable", application.getPackageName()));
    }

    @Test
    public void setCurrentInstruction_shouldAdvanceViewPager() throws Exception {
        routeModeView.setCurrentInstruction(1);
        assertThat(routeModeView.getInstructionPager().getCurrentItem()).isEqualTo(1);
    }

    @Test
    public void updateDistanceToNextInstruction_shouldUpdateDistance() throws Exception {
        adapter.instantiateItem(routeModeView.getInstructionPager(), 0);
        routeModeView.updateDistanceToNextInstruction(100);
        DistanceView distanceView =
                (DistanceView) routeModeView.findViewByIndex(0).findViewById(R.id.distance);
        assertThat(distanceView.getDistanceInMeters()).isEqualTo(100);
    }

    @Test
    public void updateDistanceToDestination_shouldUpdateDistance() throws Exception {
        adapter.instantiateItem(routeModeView.getInstructionPager(), 0);
        routeModeView.updateDistanceToDestination(500);
        DistanceView distanceView =
                (DistanceView) routeModeView.findViewById(R.id.destination_distance);
        assertThat(distanceView.getDistanceInMeters()).isEqualTo(500);
    }

    @Test
    public void showRouteComplete_shouldHideResumeButton() throws Exception {
        routeModeView.setRouteComplete();
        assertThat(routeModeView.findViewById(R.id.resume).getVisibility())
                .isEqualTo(View.GONE);
    }

    @Test
    public void showRouteComplete_shouldSetCurrentInstructionToLast() throws Exception {
        routeModeView.setCurrentInstruction(0);
        routeModeView.setRouteComplete();

        final int current = routeModeView.getInstructionPager().getCurrentItem();
        final int size = routeModeView.getInstructionPager().getAdapter().getCount();
        assertThat(current).isEqualTo(size -1);
    }

    @Test
    public void showRouteComplete_shouldCenterMapOnFinalLocation() throws Exception {
        final ShadowMapController shadowMapController = (ShadowMapController)
                ShadowExtractor.extract(routeModeView.getMapController());

        shadowMapController.getEventQueue().clear();
        routeModeView.setRouteComplete();
        assertThat(shadowMapController.getEventQueue()).isNotEmpty();
    }

    @Test
    public void showRouteComplete_shouldSetRouteIconPosition() throws Exception {
        final ShadowMapData shadowMapData = (ShadowMapData)
                ShadowExtractor.extract(ShadowMapData.getDataByName(MAP_DATA_NAME_ROUTE_ICON));
        final List<Location> geometry = routeModeView.getRoute().getGeometry();

        shadowMapData.getPoints().clear();
        routeModeView.setRouteComplete();
        assertThat(shadowMapData.getPoints().get(0).latitude)
                .isEqualTo(geometry.get(geometry.size() - 1).getLatitude());
        assertThat(shadowMapData.getPoints().get(0).longitude)
                .isEqualTo(geometry.get(geometry.size() - 1).getLongitude());
    }

    @Test
    public void showReroute_shouldNotifyPresenter() throws Exception {
        MainPresenter presenter = Mockito.mock(MainPresenterImpl.class);
        Location location = getTestLocation();
        routeModeView.setMainPresenter(presenter);
        routeModeView.showReroute(location);
        Mockito.verify(presenter, Mockito.times(1)).onReroute(location);
    }

    @Test
    public void shouldInjectRoutePresenter() throws Exception {
        assertThat(routeModeView.getRoutePresenter()).isNotNull();
    }

    @Test @SuppressLint("NewApi")
    public void shouldGenerateNotificationOnFirstInstruction() throws Exception {
        ShadowNotificationManager sManager = getRoutingNotificationManager();
        ShadowNotification sNotification = Shadows.shadowOf(sManager.getAllNotifications().get(0));
        assertThat(sNotification.getContentTitle()).isEqualTo("Name");
        assertThat(sNotification.getContentText()).isEqualTo("Go north on Adalbertstraße.");
        assertThat(sManager.getAllNotifications().get(0).actions[0].title)
                .isEqualTo("Exit Navigation");
    }

    @Test @SuppressLint("NewApi")
    public void shouldGenerateNotificationOnPageSelected() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 1);
        ShadowNotificationManager sManager = getRoutingNotificationManager();
        ShadowNotification sNotification = Shadows.shadowOf(sManager.getAllNotifications().get(0));
        assertThat(sNotification.getContentTitle()).isEqualTo("Name");
        assertThat(sNotification.getContentText()).isEqualTo("Go north on Adalbertstraße.");
        assertThat(sManager.getAllNotifications().get(0).actions[0].title)
                .isEqualTo("Exit Navigation");
    }

    @Test @SuppressLint("NewApi")
    public void shouldKillNotificationOnExitNavigation() throws Exception {
        ShadowNotificationManager sManager = getRoutingNotificationManager();
        sManager.getAllNotifications().get(0).actions[0].actionIntent.send();

        ShadowApplication application = Shadows.shadowOf(startActivity.getApplication());
        Intent broadcastIntent = application.getBroadcastIntents().get(0);
        String broadcastClassName = broadcastIntent.getComponent().getClassName();
        boolean shouldExit = broadcastIntent.getExtras()
                .getBoolean(NotificationCreator.EXIT_NAVIGATION);
        assertThat(shouldExit).isTrue();
        assertThat(broadcastClassName)
                .isEqualTo("com.mapzen.erasermap.util.NotificationBroadcastReceiver");
    }

    private ShadowNotificationManager getRoutingNotificationManager() {
        NotificationManager manager = (NotificationManager)
                startActivity.getSystemService(NOTIFICATION_SERVICE);
        return Shadows.shadowOf(manager);
    }

    class TestViewGroup extends ViewGroup {
        public TestViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }
    }
}
