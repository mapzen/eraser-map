package com.mapzen.privatemaps;

import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.gson.Result;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MainPresenterTest {
    private MainPresenter presenter;
    private TestViewController controller;

    @Before
    public void setUp() throws Exception {
        presenter = new MainPresenterImpl();
        controller = new TestViewController();
        presenter.setViewController(controller);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(presenter).isNotNull();
    }

    @Test
    public void onSearchResultsAvailable_shouldShowSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);
        assertThat(controller.searchResults).isEqualTo(features);
    }

    @Test
    public void restoreViewState_shouldRestorePreviousSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);

        TestViewController newController = new TestViewController();
        presenter.setViewController(newController);
        presenter.restoreViewState();
        assertThat(newController.searchResults).isEqualTo(features);
    }

    @Test
    public void onCollapseSearchView_shouldHideSearchResults() throws Exception {
        Result result = new Result();
        ArrayList<Feature> features = new ArrayList<>();
        result.setFeatures(features);
        presenter.onSearchResultsAvailable(result);
        presenter.onCollapseSearchView();
        assertThat(controller.searchResults).isNull();
    }

    @Test
    public void onQuerySubmit_shouldShowProgress() throws Exception {
        presenter.onQuerySubmit();
        assertThat(controller.isProgressVisible).isTrue();
    }

    @Test
    public void onSearchResultsAvailable_shouldHideProgress() throws Exception {
        controller.showProgress();
        presenter.onSearchResultsAvailable(new Result());
        assertThat(controller.isProgressVisible).isFalse();
    }

    @Test
    public void onExpandSearchView_shouldHideOverflowMenu() throws Exception {
        controller.isOverflowVisible = true;
        presenter.onExpandSearchView();
        assertThat(controller.isOverflowVisible).isFalse();
    }

    @Test
    public void onCollapseSearchView_shouldShowOverflowMenu() throws Exception {
        controller.isOverflowVisible = false;
        presenter.onCollapseSearchView();
        assertThat(controller.isOverflowVisible).isTrue();
    }

    @Test
    public void onSearchResultsAvailable_shouldShowActionViewAll() throws Exception {
        controller.isViewAllVisible = false;
        presenter.onSearchResultsAvailable(new Result());
        assertThat(controller.isViewAllVisible).isTrue();
    }

    @Test
    public void onCollapseSearchView_shouldHideActionViewAll() throws Exception {
        controller.isViewAllVisible = true;
        presenter.onCollapseSearchView();
        assertThat(controller.isViewAllVisible).isFalse();
    }

    private class TestViewController implements ViewController {
        private List<Feature> searchResults;
        private boolean isProgressVisible;
        private boolean isOverflowVisible;
        private boolean isViewAllVisible;

        @Override public void showSearchResults(@NotNull List<? extends Feature> features) {
            searchResults = (List<Feature>) features;
        }

        @Override public void centerOnCurrentFeature(@NotNull List<? extends Feature> features) {
        }

        @Override public void hideSearchResults() {
            searchResults = null;
        }

        @Override public void showProgress() {
            isProgressVisible = true;
        }

        @Override public void hideProgress() {
            isProgressVisible = false;
        }

        @Override public void showOverflowMenu() {
            isOverflowVisible = true;
        }

        @Override public void hideOverflowMenu() {
            isOverflowVisible = false;
        }

        @Override public void showActionViewAll() {
            isViewAllVisible = true;
        }

        @Override public void hideActionViewAll() {
            isViewAllVisible = false;
        }
    }
}
