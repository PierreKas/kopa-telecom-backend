package com.kopa.customeridentity.profile.service;

import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.profile.dto.ProfileResponse;
import com.kopa.customeridentity.profile.dto.UpdateProfileRequest;

public interface ProfileService {

    /** FR-12 - view own profile. Local data only (architecture-reference.md 5.1 notes an
     *  optional HP00005/HPOP502 cross-check, not implemented - the local record is already the
     *  source of truth for what KTRN was given at registration). */
    ProfileResponse getProfile(CallerContext caller);

    ProfileResponse updateProfile(CallerContext caller, UpdateProfileRequest request);
}
