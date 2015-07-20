package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.valhalla.Route;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import static com.mapzen.erasermap.dummy.TestHelper.getFixture;
import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RouteModeViewTest {
    InstructionAdapter adapter;
    RouteModeView routeModeView;
    private static MainActivity startActivity = Robolectric.setupActivity(MainActivity.class);
    private ViewGroup viewGroup;

    @Before
    public void setUp() throws Exception {
        startActivity.setReverse(false);
        startActivity.showRoutePreview(getTestFeature());
        startActivity.success(new Route(getFixture("valhalla_route")));
        startActivity.showRoutingMode(getTestFeature());
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

    @Test
    public void firstInstruction_shouldHaveFirstInstruction() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        TextView instructionText = (TextView) view.findViewById(R.id.instruction_text);
        TextView distance = (TextView) view.findViewById(R.id.distance);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        assertThat(instructionText.getText().toString()).isEqualTo("Go north on Adalbertstraße.");
        assertThat(distance.getText().toString()).isEqualTo("0.2 mi");
        assertThat(icon.getDrawable()).isEqualTo(startActivity.getDrawable(R.drawable.ic_route_1));
    }

    @Test
    public void paging_ShouldSwitchInstruction() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 1);
        TextView instructionText = (TextView) view.findViewById(R.id.instruction_text);
        TextView distance = (TextView) view.findViewById(R.id.distance);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        assertThat(instructionText.getText().toString()).isEqualTo("Turn left onto Engeldamm.");
        assertThat(distance.getText().toString()).isEqualTo("0.1 mi");
        assertThat(icon.getDrawable()).isEqualTo(startActivity.getDrawable(R.drawable.ic_route_15));
    }

    @Test
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
    public void onInstructionComplete_shouldAdvanceViewPager() throws Exception {
        routeModeView.getRouteListener().onInstructionComplete(0);
        assertThat(routeModeView.getPager().getCurrentItem()).isEqualTo(1);
    }

    @Test
    public void onUpdateDistance_shouldUpdateDistanceToNextInstruction() throws Exception {
        adapter.instantiateItem(routeModeView.getPager(), 0);
        routeModeView.getRouteListener().onUpdateDistance(100, 500);
        DistanceView distanceView =
                (DistanceView) routeModeView.findViewByIndex(0).findViewById(R.id.distance);
        assertThat(distanceView.getDistanceInMeters()).isEqualTo(100);
    }

    @Test
    public void onUpdateDistance_shouldUpdateDistanceToDestination() throws Exception {
        adapter.instantiateItem(routeModeView.getPager(), 0);
        routeModeView.getRouteListener().onUpdateDistance(100, 500);
        DistanceView distanceView =
                (DistanceView) routeModeView.findViewById(R.id.destination_distance);
        assertThat(distanceView.getDistanceInMeters()).isEqualTo(500);
    }

    @Test
    public void shouldInjectRouteEngine() throws Exception {
        assertThat(routeModeView.getRouteEngine()).isNotNull();
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
