package com.mapzen.erasermap.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.valhalla.Route;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static com.mapzen.erasermap.dummy.TestHelper.getFixture;
import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static com.mapzen.erasermap.dummy.TestHelper.getTestLocation;
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
        startActivity.setReverse(false);
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
    public void defaultInstruction_shouldHaveTransparentGrayBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_light_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void activeInstruction_shouldHaveTransparentWhiteBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        adapter.setBackgroundColorActive(view);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_white);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void inactiveInstruction_shouldHaveTransparentGrayBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        adapter.setBackgroundColorActive(view);
        adapter.setBackgroundColorInactive(view);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_light_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void arrivedInstruction_shouldHaveDestinationBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        adapter.setBackgroundColorArrived(view);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.pager_item_instruction)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.you_have_arrived);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void firstPagerView_shouldNotHaveLeftArrow() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        ImageButton leftArrow = (ImageButton) view.findViewById(R.id.left_arrow);
        ImageButton rightArrow = (ImageButton) view.findViewById(R.id.right_arrow);
        assertThat(leftArrow.getVisibility()).isNotEqualTo(View.VISIBLE);
        assertThat(rightArrow.getVisibility()).isEqualTo(View.VISIBLE);
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
                routeModeView.getPager().getAdapter().getCount() - 1);
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
        assertThat(destinationText.getText()).isEqualTo("Text, Local Admin, Admin1 Abbr");
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
        assertThat(routeModeView.getPager().getCurrentItem()).isEqualTo(1);
    }

    @Test
    public void updateDistanceToNextInstruction_shouldUpdateDistance() throws Exception {
        adapter.instantiateItem(routeModeView.getPager(), 0);
        routeModeView.updateDistanceToNextInstruction(100);
        DistanceView distanceView =
                (DistanceView) routeModeView.findViewByIndex(0).findViewById(R.id.distance);
        assertThat(distanceView.getDistanceInMeters()).isEqualTo(100);
    }

    @Test
    public void updateDistanceToDestination_shouldUpdateDistance() throws Exception {
        adapter.instantiateItem(routeModeView.getPager(), 0);
        routeModeView.updateDistanceToDestination(500);
        DistanceView distanceView =
                (DistanceView) routeModeView.findViewById(R.id.destination_distance);
        assertThat(distanceView.getDistanceInMeters()).isEqualTo(500);
    }

    @Test
    public void showRouteComplete_shouldHideRouteFooter() throws Exception {
        routeModeView.showRouteComplete();
        assertThat(routeModeView.findViewById(R.id.footer_wrapper).getVisibility())
                .isEqualTo(View.GONE);
    }

    @Test
    public void showRouteComplete_shouldHideResumeButton() throws Exception {
        routeModeView.showRouteComplete();
        assertThat(routeModeView.findViewById(R.id.resume).getVisibility())
                .isEqualTo(View.GONE);
    }

    @Test
    public void showRouteComplete_shouldHideInstructionList() throws Exception {
        routeModeView.showRouteComplete();
        assertThat(routeModeView.findViewById(R.id.instruction_list).getVisibility())
                .isEqualTo(View.GONE);
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

    @Test
    public void shouldSetSlideLayoutTouchListener() throws Exception {
        assertThat(Shadows.shadowOf(routeModeView.findViewById(R.id.drag_area))
                .getOnTouchListener()).isNotNull();
    }

    @Test
    public void shouldInstructionHeaderTouchListener() throws Exception {
        assertThat(Shadows.shadowOf(routeModeView.findViewById(R.id.instruction_route_header))
                .getOnTouchListener()).isNotNull();
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
