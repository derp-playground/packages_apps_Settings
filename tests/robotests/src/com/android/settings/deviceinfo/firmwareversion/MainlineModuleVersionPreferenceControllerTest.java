/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

import static com.android.settings.core.BasePreferenceController.AVAILABLE;
import static com.android.settings.core.BasePreferenceController.UNSUPPORTED_ON_DEVICE;
import static com.android.settings.deviceinfo.firmwareversion.MainlineModuleVersionPreferenceController.MODULE_UPDATE_INTENT;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.FeatureFlagUtils;

import androidx.preference.Preference;

import com.android.settings.core.FeatureFlags;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class MainlineModuleVersionPreferenceControllerTest {

    @Mock
    private PackageManager mPackageManager;

    private Context mContext;
    private Preference mPreference;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mContext = spy(RuntimeEnvironment.application);
        mPreference = new Preference(mContext);
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
    }

    @Test
    public void getAvailabilityStatus_noMainlineModuleProvider_unavailable() {
        when(mContext.getString(
                com.android.internal.R.string.config_defaultModuleMetadataProvider)).thenReturn(
                null);

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        assertThat(controller.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void getAvailabilityStatus_noMainlineModulePackageInfo_unavailable() throws Exception {
        final String provider = "test.provider";
        when(mContext.getString(
                com.android.internal.R.string.config_defaultModuleMetadataProvider))
                .thenReturn(provider);
        when(mPackageManager.getPackageInfo(eq(provider), anyInt()))
                .thenThrow(new PackageManager.NameNotFoundException());

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        assertThat(controller.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void getAvailabilityStatus_hasMainlineModulePackageInfo_available() throws Exception {
        setupModulePackage();

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        assertThat(controller.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void updateStates_canHandleIntent_setIntentToPreference() throws Exception {
        setupModulePackage();
        when(mPackageManager.resolveActivity(MODULE_UPDATE_INTENT, 0))
                .thenReturn(new ResolveInfo());

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        controller.updateState(mPreference);

        assertThat(mPreference.getIntent()).isEqualTo(MODULE_UPDATE_INTENT);
    }

    @Test
    public void updateStates_cannotHandleIntent_setNullToPreference() throws Exception {
        setupModulePackage();
        when(mPackageManager.resolveActivity(MODULE_UPDATE_INTENT, 0))
                .thenReturn(null);

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        controller.updateState(mPreference);

        assertThat(mPreference.getIntent()).isNull();
    }

    private void setupModulePackage() throws Exception {
        final String provider = "test.provider";
        final String version = "test version 123";
        final PackageInfo info = new PackageInfo();
        info.versionName = version;
        when(mContext.getString(
                com.android.internal.R.string.config_defaultModuleMetadataProvider))
                .thenReturn(provider);
        when(mPackageManager.getPackageInfo(eq(provider), anyInt())).thenReturn(info);
    }
}
