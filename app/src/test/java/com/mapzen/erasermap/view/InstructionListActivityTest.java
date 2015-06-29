package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.pelias.SimpleFeature;
import com.mapzen.valhalla.Route;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

import static com.mapzen.erasermap.dummy.TestHelper.getFixture;
import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class InstructionListActivityTest {
    private static MainActivity startActivity = Robolectric.setupActivity(MainActivity.class);
    private InstructionListActivity activity;

    @Before
    public void setUp() throws Exception {
        startActivity.setReverse(false);
        startActivity.showRoutePreview(getTestFeature());
        startActivity.success(new Route(getFixture("valhalla_route")));
        startActivity.findViewById(R.id.routing_circle).performClick();
        ShadowActivity shadowActivity = shadowOf(startActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        activity = Robolectric.buildActivity(InstructionListActivity.class)
                .withIntent(startedIntent).create().get();
    }

    public void setActivityToReverse() throws IOException, JSONException {
        startActivity.setReverse(true);
        startActivity.showRoutePreview(getTestFeature());
        try {
            startActivity.success(new Route(getFixture("valhalla_route")));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startActivity.findViewById(R.id.routing_circle).performClick();
        ShadowActivity shadowActivity = shadowOf(startActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        activity = Robolectric.buildActivity(InstructionListActivity.class)
                .withIntent(startedIntent).create().get();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldHaveListView() throws Exception {
        assertThat(activity.findViewById(R.id.instruction_list_view)).isNotNull();
    }

    @Test
    public void onDirectionListOpen_shouldHaveOriginSet() throws Exception {
        assertThat(((TextView) activity.findViewById(R.id.destination)).getText())
                .isEqualTo(SimpleFeature.fromFeature(startActivity.getDestination()).toString());
        assertThat(((TextView) activity.findViewById(R.id.starting_point)).getText())
                .isEqualTo(activity.getString(R.string.current_location));
    }

    @Test
    public void onDirectionListOpenReversed_shouldHaveOriginSet() throws Exception {
        setActivityToReverse();
        assertThat(((TextView) activity.findViewById(R.id.starting_point)).getText())
                .isEqualTo(SimpleFeature.fromFeature(startActivity.getDestination()).toString());
        assertThat(((TextView) activity.findViewById(R.id.destination)).getText())
                .isEqualTo(activity.getString(R.string.current_location));
    }

    @Test
    public void onDirectionListOpen_shouldHaveCurrentLocationFirst() throws Exception {
        View view = ((ListView) activity.findViewById(R.id.instruction_list_view)).getAdapter()
                .getView(0, activity.findViewById(R.id.instruction_list_view),
                        getGenericViewGroup());

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        TextView distance = (TextView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(activity.getDrawable(R.drawable.ic_locate));
        assertThat(instruction.getText()).isEqualTo("Current Location");
        assertThat(distance.getText()).isEqualTo("");
    }

    @Test
    public void onDirectionListOpen_shouldHaveFirstInstructionFirst() throws Exception {
          View view = ((ListView) activity.findViewById(R.id.instruction_list_view)).getAdapter()
                  .getView(1, activity.findViewById(R.id.instruction_list_view),
                          getGenericViewGroup());

          ImageView icon = (ImageView) view.findViewById(R.id.icon);
          TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
          TextView distance = (TextView) view.findViewById(R.id.distance);

          assertThat(icon.getDrawable()).isEqualTo(activity.getDrawable(R.drawable.ic_route_1));
          assertThat(instruction.getText()).contains("Go north on Adalbertstraße.");
          assertThat(distance.getText()).isEqualTo("0.2 mi");
    }

    @Test
    public void onDirectionListOpen_shouldHaveLastInstructionLast() throws Exception {
        int pos = ((ListView) activity.findViewById(R.id.instruction_list_view)).getAdapter()
                .getCount() - 1;
        View view = ((ListView) activity.findViewById(R.id.instruction_list_view)).getAdapter()
                .getView(pos, activity.findViewById(R.id.instruction_list_view),
                        getGenericViewGroup());

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView instruction = (TextView) view.findViewById(R.id.simple_instruction);
        TextView distance = (TextView) view.findViewById(R.id.distance);

        assertThat(icon.getDrawable()).isEqualTo(activity.getDrawable(R.drawable.ic_route_4));
        assertThat(instruction.getText()).contains("You have arrived at your destination.");
        assertThat(distance.getText()).isEqualTo("");
    }

    @NotNull
    private ViewGroup getGenericViewGroup() {
        return new ViewGroup(activity.getApplicationContext()) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {

            }
        };
    }
}
