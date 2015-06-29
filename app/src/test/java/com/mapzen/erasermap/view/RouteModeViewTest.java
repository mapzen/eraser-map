package com.mapzen.erasermap.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import com.mapzen.erasermap.R;
import com.mapzen.valhalla.Route;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;

import static com.mapzen.erasermap.dummy.TestHelper.getFixture;
import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

public class RouteModeViewTest {
    InstructionAdapter adapter;
    RouteModeView routeModeView;
    private static MainActivity startActivity = Robolectric.setupActivity(MainActivity.class);
    private ViewGroup viewGroup;

    @Before
    public void setUp() throws Exception {
        startActivity.showRoutePreview(getTestFeature());
        startActivity.success(new Route(getFixture("valhalla_route")));
        startActivity.findViewById(R.id.routing_circle).performClick();
        routeModeView = (RouteModeView) startActivity.findViewById(R.id.route_mode);
        adapter = (InstructionAdapter) routeModeView.getPager().getAdapter();
        viewGroup = new TestViewGroup(application);

    }

    @Test
    public void routeModeView_shouldNotBeNull() throws Exception {
        assertThat(routeModeView).isNotNull();
    }

    @Test
    public void adapter_ShouldNotBeNull() throws Exception {
        assertThat(adapter).isNotNull();
    }


    @Test
    public void defaultInstruction_shouldHaveTransparentGrayBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.turn_container)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_light_gray);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    @Test
    public void activeInstruction_shouldHaveTransparentWhiteBackground() throws Exception {
        View view = (View) adapter.instantiateItem(viewGroup, 0);
        adapter.setBackgroundColorActive(view);
        ColorDrawable background = (ColorDrawable) view.findViewById(R.id.turn_container)
                .getBackground();
        int expectedColor = application.getResources().getColor(R.color.transparent_white);
        assertThat(background.getColor()).isEqualTo(expectedColor);
    }

    class TestViewGroup extends ViewGroup {
        public TestViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }
    }
//    @Test
//    public void firstPagerItemShouldBeFirstInstruction() throws Exception {
//        routeModeView.getPager()
//    }
}
